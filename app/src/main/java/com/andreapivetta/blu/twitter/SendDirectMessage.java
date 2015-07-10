package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SendDirectMessage extends AsyncTask<String, Void, Boolean> {

    private Twitter twitter;
    private Context context;
    private long userID;

    public SendDirectMessage(Context context, Twitter twitter, long userID) {
        this.twitter = twitter;
        this.context = context;
        this.userID = userID;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            DirectMessage message = twitter.sendDirectMessage(userID, params[0]);

            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_stream_service), false)) {

                DatabaseManager.getInstance(context).insertDirectMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getRecipient().getName(), message.getRecipientId(),
                        message.getRecipient().getBiggerProfileImageURL(), true);
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
