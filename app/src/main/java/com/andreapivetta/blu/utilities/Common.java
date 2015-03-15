package com.andreapivetta.blu.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class Common {

    public static final String PREF = "MyPref";
    public static final String PREF_ANIMATIONS = "Anim";
    public static final String PREF_HEADS_UP_NOTIFICATIONS = "headsup";
    public static final String PREF_STREAM_ON = "twitterstream";
    public static final String PREF_FREQ = "freq";
    public final static String NEW_NOTIFICATION_INTENT = "com.andreapivetta.blu.NEW_NOTIFICATION_INTENT";
    private static final String FAVORITERS_URL = "https://twitter.com/i/activity/favorited_popup?id=";
    private static final String RETWEETERS_URL = "https://twitter.com/i/activity/retweeted_popup?id=";
    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36";

    public static ArrayList<Long> getFavoriters(long tweetID) throws Exception {
        return getUsers(tweetID, FAVORITERS_URL);
    }

    public static ArrayList<Long> getRetweeters(long tweetID) throws Exception {
        return getUsers(tweetID, RETWEETERS_URL);
    }

    private static ArrayList<Long> getUsers(long tweetID, String url) throws Exception {
        ArrayList<Long> usersIDs = new ArrayList<>();
        Document doc = Jsoup.parse(getJson(tweetID, url).getString("htmlUsers"));

        if (doc != null) {
            for (Element e : doc.getElementsByTag("img")) {
                try {
                    usersIDs.add(Long.parseLong(e.attr("data-user-id")));
                } catch (Exception x) {
                    // doesn't have it, could be an emoji or something from the looks of it.
                }
            }
        }
        return usersIDs;
    }

    private static JSONObject getJson(long tweetId, String url) {
        try {
            URL obj = new URL(url + tweetId);

            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("user-agent", USER_AGENT);
            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");

            connection.disconnect();
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void pushNotification(long tweetID, String user, String type, String status,
                                        String profilePicURL, long userID, Context context) {
        NotificationsDatabaseManager databaseManager = new NotificationsDatabaseManager(context);
        databaseManager.open();
        long id = databaseManager.insertNotification(
                new Notification(false, tweetID, user, type, status, profilePicURL, userID));
        databaseManager.close();

        Intent i = new Intent();
        i.setAction(NEW_NOTIFICATION_INTENT);
        context.sendBroadcast(i);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        mBuilder.setDefaults(android.app.Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(Common.getBitmapFromURL(profilePicURL))
                .setLights(Color.BLUE, 500, 1000);

        if (context.getSharedPreferences(PREF, 0).getBoolean(Common.PREF_HEADS_UP_NOTIFICATIONS, true))
            mBuilder.setPriority(android.app.Notification.PRIORITY_HIGH);

        switch (type) {
            case Notification.TYPE_FAVOURITE:
                resultIntent = new Intent(context, TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(context.getString(R.string.fav_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_star_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_RETWEET:
                resultIntent = new Intent(context, TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(context.getString(R.string.retw_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_repeat_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_FOLLOW:
                resultIntent = new Intent(context, UserProfileActivity.class);
                resultIntent.putExtra("ID", userID);
                resultPendingIntent = PendingIntent.getActivity(
                        context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(context.getString(R.string.follow_not_title, user))
                        .setContentText(status)
                        .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_MENTION:
                resultIntent = new Intent(context, TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(context.getString(R.string.reply_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_reply_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_RETWEET_MENTIONED:
                resultIntent = new Intent(context, TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(context.getString(R.string.retw_ment_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_repeat_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
        }

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify((int) id, mBuilder.build());
    }

    public static void pushNotification(long tweetID, long userID,
                                        String type, Context context) throws TwitterException {
        Twitter twitter = TwitterUtils.getTwitter(context);
        User currentUser = twitter.showUser(userID);

        pushNotification(tweetID, currentUser.getName(), type, twitter.showStatus(tweetID).getText(),
                currentUser.getProfileImageURL(), currentUser.getId(), context);
    }
}
