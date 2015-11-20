package com.andreapivetta.blu.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.UserFollowed;
import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.receivers.FollowingAlarmReceiver;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.Calendar;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;


public class CheckFollowingService extends IntentService {

    public CheckFollowingService() {
        super("CheckFollowingService");
    }

    public static void startService(Context context) {
        int frequency = 86400;
        AlarmManager a = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, frequency);
        a.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                frequency * 1000,
                PendingIntent.getBroadcast(
                        context, 0, new Intent(context, FollowingAlarmReceiver.class), 0));
    }

    public static void stopService(Context context) {
        ((AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE))
                .cancel(PendingIntent.getBroadcast(context, 0,
                        new Intent(context, FollowingAlarmReceiver.class), 0));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if ((new ConnectionDetector(getApplicationContext())).isConnectingToWiFi()) {
            Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());

            try {
                if (twitter.showUser(twitter.getId()).getFriendsCount() < 2800) {
                    DatabaseManager databaseManager = DatabaseManager.getInstance(getApplicationContext());
                    ArrayList<UserFollowed> following = new ArrayList<>();

                    long cursor = -1;
                    PagableResponseList<User> followingList;
                    do {
                        followingList = twitter.getFriendsList(twitter.getId(), cursor, 200);
                        for (User user : followingList)
                            following.add(new UserFollowed(user.getId(), user.getName(), user.getScreenName(),
                                    user.getBiggerProfileImageURL()));
                    } while ((cursor = followingList.getNextCursor()) != 0);

                    databaseManager.checkFollowing(following);

                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                            .putBoolean(getString(R.string.pref_key_following_populated), true).apply();
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }

        }
    }

}
