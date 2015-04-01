package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.andreapivetta.blu.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class FavoriteTweet extends AsyncTask<Long, Void, Boolean> {

    private Context context;
    private Twitter twitter;
    private boolean removed = false;

    public FavoriteTweet(Context context, Twitter twitter) {
        this.context = context;
        this.twitter = twitter;
    }

    protected Boolean doInBackground(Long... args) {
        try {
            if (args[1] < 0) {
                removed = true;
                twitter.destroyFavorite(args[0]);
            } else {
                twitter.createFavorite(args[0]);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void onPostExecute(Boolean status) {
        if (status) {
            if (removed)
                Toast.makeText(context, context.getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, context.getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.action_not_performed), Toast.LENGTH_SHORT).show();
        }
    }
}
