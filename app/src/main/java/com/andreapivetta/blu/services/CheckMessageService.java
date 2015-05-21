package com.andreapivetta.blu.services;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class CheckMessageService extends IntentService {

    public CheckMessageService() {
        super("CheckMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("MessageService", "MessageService START");

        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(getApplicationContext());
        dbm.open();
        try {
            ArrayList<DirectMessage> messages = new ArrayList<>();
            List<DirectMessage> receivedDirectMessages = twitter.getDirectMessages(new Paging(1, 200));
            List<DirectMessage> sentDirectMessages = twitter.getSentDirectMessages(new Paging(1, 200));

            if (receivedDirectMessages.size() > 0 && sentDirectMessages.size() > 0) {
                for (DirectMessage message : receivedDirectMessages)
                    messages.add(message);

                for (DirectMessage message : sentDirectMessages)
                    messages.add(message);

                ArrayList<DirectMessage> newMessages = dbm.check(messages);
                for (DirectMessage message : newMessages)
                    Message.pushMessage(message, getApplicationContext());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        dbm.close();

        Log.i("MessageService", "MessageService STOP");
    }
}