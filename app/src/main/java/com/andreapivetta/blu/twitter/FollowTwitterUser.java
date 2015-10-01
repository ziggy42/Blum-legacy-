package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.os.AsyncTask;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.SnackbarContainer;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.UserFollowed;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class FollowTwitterUser extends AsyncTask<User, Void, Boolean> {

    private Context context;
    private Twitter twitter;
    private boolean follow;

    public FollowTwitterUser(Context context, Twitter twitter, boolean follow) {
        this.context = context;
        this.twitter = twitter;
        this.follow = follow;
    }

    protected Boolean doInBackground(User... args) {
        try {
            if (follow) {
                twitter.createFriendship(args[0].getId());
                DatabaseManager.getInstance(context).insertFollowed(new UserFollowed(args[0].getId(), args[0].getName(),
                        args[0].getScreenName(), args[0].getBiggerProfileImageURL()));
            } else {
                twitter.destroyFriendship(args[0].getId());
                DatabaseManager.getInstance(context).deleteFollowed(new Object[]{args[0].getId()});
            }
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
            else
                ((SnackbarContainer) context).showSnackBar(context.getString(R.string.following_removed));
        } else {
            ((SnackbarContainer) context).showSnackBar(context.getString(R.string.action_not_performed));
        }
    }
}
