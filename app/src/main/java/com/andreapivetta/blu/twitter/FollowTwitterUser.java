package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.os.AsyncTask;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.SnackbarContainer;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class FollowTwitterUser extends AsyncTask<Long, Void, Boolean> {

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
                ((SnackbarContainer) context).showSnackBar(context.getString(R.string.following_added));
                //Toast.makeText(context, context.getString(R.string.following_added), Toast.LENGTH_SHORT).show();
            else
                ((SnackbarContainer) context).showSnackBar(context.getString(R.string.following_removed));
                //Toast.makeText(context, context.getString(R.string.following_removed), Toast.LENGTH_SHORT).show();
        } else {
            ((SnackbarContainer) context).showSnackBar(context.getString(R.string.action_not_performed));
            //Toast.makeText(context, context.getString(R.string.action_not_performed), Toast.LENGTH_SHORT).show();
        }
    }
}
