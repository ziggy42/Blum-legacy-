package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;

import com.andreapivetta.blu.data.FavoritesDatabaseManager;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.RetweetsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.Twitter;


public class CheckInteractionsService extends IntentService {


    public CheckInteractionsService() {
        super("CheckInteractionsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        FavoritesDatabaseManager fdbm = new FavoritesDatabaseManager(getApplicationContext());
        RetweetsDatabaseManager rdbm = new RetweetsDatabaseManager(getApplicationContext());

        fdbm.open();
        rdbm.open();
        try {
            for (twitter4j.Status tmp : twitter.getUserTimeline(new Paging(1, 200))) {
                ArrayList<Long> newUsersIDs = fdbm.check(Common.getFavoriters(tmp.getId()), tmp.getId());
                for (long userID : newUsersIDs)
                    Common.pushNotification(tmp.getId(), userID, Notification.TYPE_FAVOURITE, getApplicationContext());

                newUsersIDs = rdbm.check(Common.getRetweeters(tmp.getId()), tmp.getId());
                for (long userID : newUsersIDs)
                    Common.pushNotification(tmp.getId(), userID, Notification.TYPE_RETWEET, getApplicationContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        rdbm.close();
        fdbm.close();
    }

}
