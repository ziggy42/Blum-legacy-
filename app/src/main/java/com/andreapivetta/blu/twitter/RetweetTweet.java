package com.andreapivetta.blu.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class RetweetTweet extends AsyncTask<Long, Void, Boolean> {

    private Context context;
    private Twitter twitter;

    public RetweetTweet(Context context, Twitter twitter) {
        this.context = context;
        this.twitter = twitter;
    }

    protected Boolean doInBackground(Long... args) {
        try {
            twitter.retweetStatus(args[0]);
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void onPostExecute(Boolean status) {
        if (status) {
            Toast.makeText(context, "Status retweeted", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("RETWEET STATUS", "Action not performed");
        }
    }
}