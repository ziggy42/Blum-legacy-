package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class AlternateNotificationService extends IntentService {

    public AlternateNotificationService() {
        super("AlternateNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("AlternateService", "PING");
        Twitter t = TwitterUtils.getTwitter(getApplicationContext());

        try {
            NotificationsDatabaseManager manager = new NotificationsDatabaseManager(getApplicationContext());
            manager.open();
            long lastRetweet = manager.getLastRetweetID();
            manager.close();
            Paging page = new Paging();
            if (lastRetweet != -1) {
                Log.i("AlternateService", t.showStatus(lastRetweet).getText());
                page.setSinceId(lastRetweet);
            } else {
                Log.i("AlternateService", "No retweets yet");
                Paging p = new Paging(1, 1);
                long lastID = t.getRetweetsOfMe(p).get(0).getId();
                page.setSinceId(t.getRetweetsOfMe(p).get(0).getId());
                Log.i("AlternateService", "Last retweet = " + t.showStatus(lastID).getText());
            }

            List<Status> ret = t.getRetweetsOfMe();//t.getRetweetsOfMe(page);
            if (ret.size() != 0) {
                //Log.i("AlternateService", ret.get(0).getText());

                for (Status tmp : ret) {
                    Log.i("AlternateService", tmp.getText());
                }
            } else {
                Log.i("AlternateService", "No new retweets");
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        }

    }
}
