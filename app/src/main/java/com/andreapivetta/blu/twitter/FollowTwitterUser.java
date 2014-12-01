package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class FollowTwitterUser  extends AsyncTask<Long, Void, Boolean> {

    private Context context;
    private Twitter twitter;
    private boolean follow;

    public FollowTwitterUser(Context context, Twitter twitter, boolean follow) {
        this.context = context;
        this.twitter = twitter;
        this.follow = follow;
    }

    protected Boolean doInBackground(Long... args) {
        try {
            if (follow)
                twitter.createFriendship(args[0]);
            else
                twitter.destroyFriendship(args[0]);
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void onPostExecute(Boolean status) {
        if (status) {
            if (follow)
                Toast.makeText(context, "You're now following this user", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "You're not following this user", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("FOLLOW STATUS", "Action not performed");
        }
    }
}
