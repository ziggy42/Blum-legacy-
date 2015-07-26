package com.andreapivetta.blu.twitter;

import android.content.Context;
import android.os.AsyncTask;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.SnackbarContainer;

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
            ((SnackbarContainer) context).showSnackBar(context.getString(R.string.status_retweeted));
            //Toast.makeText(context, context.getString(R.string.status_retweeted), Toast.LENGTH_SHORT).show();
        } else {
            ((SnackbarContainer) context).showSnackBar(context.getString(R.string.action_not_performed));
            //Toast.makeText(context, context.getString(R.string.action_not_performed), Toast.LENGTH_SHORT).show();
        }
    }
}