package com.andreapivetta.blu.twitter;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.andreapivetta.blu.MainActivity;
import com.andreapivetta.blu.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class UpdateTwitterStatus extends AsyncTask<String, String, String> {

    private static SharedPreferences mSharedPreferences;
    private Activity act;
    private String tweet;

    private Context context;

    public UpdateTwitterStatus(Activity act) {
        this.act = act;
        this.context = act.getApplicationContext();

        mSharedPreferences = act.getApplicationContext().getSharedPreferences(
                "MyPref", 0);
    }

    public UpdateTwitterStatus(Context context) {
        this.context = context;

        mSharedPreferences = context.getSharedPreferences("MyPref", 0);
    }


    protected String doInBackground(String... args) {
        tweet = args[0];
        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TwitterKs.TWITTER_CONSUMER_KEY)
                    .setOAuthConsumerSecret(TwitterKs.TWITTER_CONSUMER_SECRET);

            // Access Token
            String access_token = mSharedPreferences.getString(
                    TwitterKs.PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = mSharedPreferences.getString(
                    TwitterKs.PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token,
                    access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build())
                    .getInstance(accessToken);

            twitter.updateStatus(tweet);
        } catch (TwitterException e) {
            Log.d("Twitter Update Error", e.getMessage());
        }
        return null;
    }

    protected void onPostExecute(String file_url) {
        if (mSharedPreferences.getBoolean("Notifications", true)) {
            if (act != null) {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createNotification();
                    }
                });
            } else {
                createNotification();
            }
        }
    }

    void createNotification() {
        // TODO
        /*int myID = mSharedPreferences.getInt("NOT_ID", 0);

        if (myID > 100) {
            mSharedPreferences.edit().putInt("NOT_ID", 0).apply();
        } else {
            myID++;
            mSharedPreferences.edit().putInt("NOT_ID", myID).apply();
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context);
        mBuilder.setSmallIcon(R.drawable.ic_stat_icon_feather_notification);
        mBuilder.setContentTitle(context.getResources().getString(R.string.status_tweeted_successfully));
        mBuilder.setContentText(tweet);

        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(myID, mBuilder.build());*/
    }
}
