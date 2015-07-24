package com.andreapivetta.blu.data;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;
import com.andreapivetta.blu.utilities.ThemeUtils;

import java.util.Calendar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class Notification {

    public final static String NEW_NOTIFICATION_INTENT = "com.andreapivetta.blu.NEW_NOTIFICATION_INTENT";

    public final static String TYPE_FAVOURITE = "stellina";
    public final static String TYPE_RETWEET = "retweet";
    public final static String TYPE_MENTION = "mention";
    public final static String TYPE_FOLLOW = "newfollower";
    public final static String TYPE_RETWEET_MENTIONED = "retweetmentioned";

    public boolean read;
    public long tweetID, userID;
    public String user, type, status, profilePicURL;
    public int notificationID;
    public long timestamp;

    public Notification(boolean read, long tweet, String user, String type, String status, String profilePicURL,
                        long userID) {
        this.read = read;
        this.tweetID = tweet;
        this.user = user;
        this.type = type;
        this.status = status;
        this.profilePicURL = profilePicURL;
        this.userID = userID;

        Calendar c = Calendar.getInstance();
        this.timestamp = c.getTimeInMillis();
    }

    public Notification(int notificationID, String type, String userName, long userID, long tweetID, String status,
                        String profilePicURL, boolean read, long timestamp) {
        this.read = read;
        this.tweetID = tweetID;
        this.user = userName;
        this.type = type;
        this.status = status;
        this.profilePicURL = profilePicURL;
        this.timestamp = timestamp;
        this.userID = userID;
        this.notificationID = notificationID;
    }

    public static void pushNotification(long tweetID, String user, String type, String status,
                                        String profilePicURL, long userID, Context context) {

        long id = DatabaseManager.getInstance(context).insertNotification(new Notification(false, tweetID, user, type, status, profilePicURL, userID));
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_mute_notifications), false)) {

            Intent i = new Intent();
            i.setAction(NEW_NOTIFICATION_INTENT);
            context.sendBroadcast(i);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            Intent resultIntent;
            PendingIntent resultPendingIntent;

            mBuilder.setDefaults(android.app.Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setColor(ThemeUtils.getResourceColorPrimary(context))
                    .setLargeIcon(Common.getBitmapFromURL(profilePicURL))
                    .setLights(Color.BLUE, 500, 1000);

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_heads_up_notifications), true)
                    && (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1))
                mBuilder.setPriority(android.app.Notification.PRIORITY_HIGH);

            switch (type) {
                case TYPE_FAVOURITE:
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
                case TYPE_RETWEET:
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
                case TYPE_FOLLOW:
                    resultIntent = new Intent(context, UserProfileActivity.class);
                    resultIntent.putExtra(UserProfileActivity.TAG_ID, userID);
                    resultPendingIntent = PendingIntent.getActivity(
                            context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    mBuilder.setContentTitle(user)
                            .setContentText(context.getString(R.string.follow_not_title, user))
                            .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                            .setContentIntent(resultPendingIntent);
                    break;
                case TYPE_MENTION:
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
                case TYPE_RETWEET_MENTIONED:
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
    }

    public static void pushNotification(long tweetID, long userID,
                                        String type, Context context) throws TwitterException {
        Twitter twitter = TwitterUtils.getTwitter(context);
        User currentUser = twitter.showUser(userID);

        pushNotification(tweetID, currentUser.getName(), type,
                (tweetID > 0) ? twitter.showStatus(tweetID).getText() : "", currentUser.getProfileImageURL(),
                currentUser.getId(), context);
    }
}
