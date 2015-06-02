package com.andreapivetta.blu.services;


import android.app.IntentService;
import android.content.Intent;

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
        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(getApplicationContext());

        try {
            List<DirectMessage> receivedDirectMessages = twitter.getDirectMessages(new Paging(1, 200));
            List<DirectMessage> sentDirectMessages = twitter.getSentDirectMessages(new Paging(1, 200));
            ArrayList<DirectMessage> messages = new ArrayList<>();

            dbm.open();
            if (receivedDirectMessages != null && receivedDirectMessages.size() > 0) {
                for (DirectMessage message : receivedDirectMessages)
                    messages.add(message);

                ArrayList<DirectMessage> newMessages = dbm.checkReceived(messages);
                for (DirectMessage message : newMessages)
                    Message.pushMessage(message, getApplicationContext());
            }

            messages.clear();

            if (sentDirectMessages != null && sentDirectMessages.size() > 0) {
                for (DirectMessage message : sentDirectMessages)
                    messages.add(message);

                dbm.checkSent(messages);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        dbm.close();
    }
}