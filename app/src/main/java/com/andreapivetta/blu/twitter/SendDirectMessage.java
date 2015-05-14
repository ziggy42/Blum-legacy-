package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;

import java.util.Calendar;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SendDirectMessage extends AsyncTask<String, Void, Boolean> {

    private Twitter twitter;
    private Context context;
    private long userID;
    private String otherUserName;
    private String otherUserProfilePic;

    public SendDirectMessage(Context context, Twitter twitter, long userID, String otherUserName,
                             String otherUserProfilePic) {
        this.twitter = twitter;
        this.context = context;
        this.userID = userID;
        this.otherUserName = otherUserName;
        this.otherUserProfilePic = otherUserProfilePic;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            DirectMessage message = twitter.sendDirectMessage(userID, params[0]);

            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_stream_service), false)) {
                DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(context);
                dbm.open();
                dbm.insertMessage(message.getId(), twitter.getId(), userID, params[0],
                        Calendar.getInstance().getTime().getTime(), otherUserName, otherUserProfilePic, true);
                dbm.close();
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void onPostExecute(Boolean status) {
        if (!status) {
            Toast.makeText(context, context.getString(R.string.action_not_performed), Toast.LENGTH_SHORT).show();
        }
    }
}
