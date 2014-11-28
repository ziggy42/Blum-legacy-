package com.andreapivetta.blu.twitter;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.andreapivetta.blu.R;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class UpdateTwitterStatus extends AsyncTask<String, String, Boolean> {

    private static SharedPreferences mSharedPreferences;
    private Twitter twitter;
    private File file;
    private Context context;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;

    public UpdateTwitterStatus(Context context, Twitter twitter) {
        this.twitter = twitter;
        this.context = context;
        mSharedPreferences = context.getSharedPreferences("MyPref", 0);
    }

    public UpdateTwitterStatus(Context context, Twitter twitter, File file) {
        this.twitter = twitter;
        this.context = context;
        this.file = file;
        mSharedPreferences = context.getSharedPreferences("MyPref", 0);
    }

    protected Boolean doInBackground(String... args) {
        try {
            StatusUpdate status = new StatusUpdate(args[0]);

            if (file != null)
                status.setMedia(file);

            pushNotification();
            twitter.updateStatus(status);
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    void pushNotification() {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(context.getString(R.string.sending_tweet_title))
                .setContentText(context.getString(R.string.sending_tweet_content))
                .setSmallIcon(R.drawable.ic_stat_notification_sync)
                .setProgress(0, 0, true);

        mNotifyManager.notify(mSharedPreferences.getInt("LAST", 1), mBuilder.build());
    }

    protected void onPostExecute(Boolean status) {
        if (status) {
            int i = mSharedPreferences.getInt("LAST", 1);
            mBuilder.setContentText("Tweet sent!")
                    .setProgress(0, 0, false)
                    .setSmallIcon(R.drawable.ic_stat_navigation_check);
            mNotifyManager.notify(i, mBuilder.build());
            mSharedPreferences.edit().putInt("LAST", i + 1).apply();
        } else {
            Log.i("UPDATE STATUS", "Tweet not sent");
        }
    }

}
