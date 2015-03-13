package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.data.FavoritesDatabaseManager;
import com.andreapivetta.blu.data.FollowersDatabaseManager;
import com.andreapivetta.blu.data.MentionsDatabaseManager;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.data.RetweetsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class BasicNotificationService extends IntentService {

    public final static String NEW_NOTIFICATION_INTENT = "com.andreapivetta.blu.NEW_NOTIFICATION_INTENT";
    private Twitter twitter;
    private SharedPreferences mSharedPreferences;
    private NotificationManager mNotifyMgr;

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mSharedPreferences = getSharedPreferences(Common.PREF, 0);
        twitter = TwitterUtils.getTwitter(getApplicationContext());
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        new CheckInteractions().execute(null, null, null);
        new CheckFollowers().execute(null, null, null);
        new CheckMentions().execute(null, null, null);
    }

    void pushNotification(long tweetID, long userID, String type) throws TwitterException {
        User currentUser = twitter.showUser(userID);
        String user = currentUser.getName();
        String status = twitter.showStatus(tweetID).getText();

        NotificationsDatabaseManager dbManager = new NotificationsDatabaseManager(getApplicationContext());
        dbManager.open();
        long id = dbManager.insertNotification(
                new Notification(false, tweetID, user, type, status, currentUser.getProfileImageURL(), userID));
        dbManager.close();

        Intent i = new Intent();
        i.setAction(NEW_NOTIFICATION_INTENT);
        sendBroadcast(i);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        mBuilder.setDefaults(android.app.Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setLargeIcon(Common.getBitmapFromURL(currentUser.getProfileImageURL()))
                .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                .setLights(Color.BLUE, 500, 1000);

        if (mSharedPreferences.getBoolean(Common.PREF_HEADS_UP_NOTIFICATIONS, true))
            mBuilder.setPriority(android.app.Notification.PRIORITY_HIGH);

        switch (type) {
            case Notification.TYPE_FAVOURITE:
                resultIntent = new Intent(getApplicationContext(), TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(getString(R.string.fav_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_star_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_RETWEET:
                resultIntent = new Intent(getApplicationContext(), TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(getString(R.string.retw_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_repeat_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_FOLLOW:
                resultIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                resultIntent.putExtra("ID", userID);
                resultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(getString(R.string.follow_not_title, user))
                        .setContentText(status)
                        .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_MENTION:
                resultIntent = new Intent(getApplicationContext(), TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(getString(R.string.reply_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_reply_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
            case Notification.TYPE_RETWEET_MENTIONED:
                resultIntent = new Intent(getApplicationContext(), TweetActivity.class);
                resultIntent.putExtra("STATUS_ID", tweetID);
                resultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentTitle(getString(R.string.retw_ment_not_title, user))
                        .setContentText(status)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(status))
                        .setSmallIcon(R.drawable.ic_repeat_white_24dp)
                        .setContentIntent(resultPendingIntent);
                break;
        }

        mNotifyMgr.notify((int) id, mBuilder.build());
    }

    private class CheckInteractions extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            FavoritesDatabaseManager fdbm = new FavoritesDatabaseManager(getApplicationContext());
            RetweetsDatabaseManager rdbm = new RetweetsDatabaseManager(getApplicationContext());

            fdbm.open();
            rdbm.open();

            try {
                for (twitter4j.Status tmp : twitter.getUserTimeline(new Paging(1, 200))) {
                    ArrayList<Long> newUsersIDs = fdbm.check(Common.getFavoriters(tmp.getId()), tmp.getId());
                    for (long userID : newUsersIDs)
                        pushNotification(tmp.getId(), userID, Notification.TYPE_FAVOURITE);

                    newUsersIDs = rdbm.check(Common.getRetweeters(tmp.getId()), tmp.getId());
                    for (long userID : newUsersIDs)
                        pushNotification(tmp.getId(), userID, Notification.TYPE_RETWEET);
                }

            } catch (TwitterException e) {
                e.printStackTrace();
            }

            rdbm.close();
            fdbm.close();
            return null;
        }
    }

    private class CheckFollowers extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            FollowersDatabaseManager dbm = new FollowersDatabaseManager(getApplicationContext());
            dbm.open();

            try {
                ArrayList<Long> usersIDs = new ArrayList<>();
                IDs ids = twitter.getFollowersIDs(-1);
                do {
                    for (long userID : ids.getIDs())
                        usersIDs.add(userID);
                } while (ids.hasNext());

                ArrayList<Long> newUsersIDs = dbm.check(usersIDs);
                for (long userID : newUsersIDs)
                    pushNotification(-1L, userID, Notification.TYPE_FOLLOW);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            dbm.close();
            return null;
        }
    }

    private class CheckMentions extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            MentionsDatabaseManager dbm = new MentionsDatabaseManager(getApplicationContext());
            dbm.open();

            try {
                ArrayList<ArrayList<Long>> triples = new ArrayList<>();
                for (twitter4j.Status mention : twitter.getMentionsTimeline(new Paging(1, 200))) {
                    ArrayList<Long> tmp = new ArrayList<>();
                    tmp.add(mention.getId());
                    tmp.add(mention.getUser().getId());
                    tmp.add(mention.getCreatedAt().getTime());
                    triples.add(tmp);
                }

                ArrayList<ArrayList<Long>> newMentions = dbm.check(triples);
                for (ArrayList<Long> triple : newMentions)
                    pushNotification(triple.get(0), triple.get(1), Notification.TYPE_MENTION);

            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            dbm.close();
            return true;
        }
    }

}
