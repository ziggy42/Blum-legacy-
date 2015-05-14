package com.andreapivetta.blu.twitter;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.andreapivetta.blu.R;

import java.io.File;
import java.util.ArrayList;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class UpdateTwitterStatus extends AsyncTask<String, String, Boolean> {

    private static SharedPreferences mSharedPreferences;
    private Twitter twitter;
    private ArrayList<File> imageFiles = new ArrayList<>();
    private long inReplyTo;
    private Context context;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;

    public UpdateTwitterStatus(Context context, Twitter twitter, long inReplyTo) {
        this.twitter = twitter;
        this.context = context;
        this.inReplyTo = inReplyTo;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public UpdateTwitterStatus(Context context, Twitter twitter, long inReplyTo, ArrayList<File> imageFiles) {
        this(context, twitter, inReplyTo);
        this.imageFiles = imageFiles;
    }

    protected Boolean doInBackground(String... args) {
        try {
            pushNotification();
            StatusUpdate status = new StatusUpdate(args[0]);

            if (imageFiles.size() > 0) {
                long[] mediaIds = new long[imageFiles.size()];
                for (int i = 0; i < mediaIds.length; i++)
                    mediaIds[i] = twitter.uploadMedia(imageFiles.get(i)).getMediaId();

                status.setMediaIds(mediaIds);
            }

            if (inReplyTo > 0)
                status.setInReplyToStatusId(inReplyTo);

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
            mBuilder.setContentText(context.getString(R.string.tweet_sent_message))
                    .setProgress(0, 0, false)
                    .setSmallIcon(R.drawable.ic_stat_navigation_check);
            mNotifyManager.notify(i, mBuilder.build());
            mSharedPreferences.edit().putInt("LAST", i + 1).apply();
        } else {
            Toast.makeText(context, context.getString(R.string.action_not_performed), Toast.LENGTH_SHORT).show();
        }
    }

}
