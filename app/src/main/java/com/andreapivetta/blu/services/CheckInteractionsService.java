package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;

import com.andreapivetta.blu.data.FavoritesDatabaseManager;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.RetweetsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;

public class CheckInteractionsService extends IntentService {


    public CheckInteractionsService() {
        super("CheckInteractionsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.i("InteractionsService", "InteractionsService START");

        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        FavoritesDatabaseManager fdbm = new FavoritesDatabaseManager(getApplicationContext());
        RetweetsDatabaseManager rdbm = new RetweetsDatabaseManager(getApplicationContext());

        try {
            List<Status> userTimeLine = twitter.getUserTimeline(new Paging(1, 200));
            fdbm.open();
            rdbm.open();
            if (userTimeLine.size() > 0) {
                for (Status tmp : userTimeLine) {
                    ArrayList<Long> newUsersIDs;
                    ArrayList<Long> existingUserIDs = Common.getFavoriters(tmp.getId());
                    if (existingUserIDs != null) {
                        newUsersIDs = fdbm.check(existingUserIDs, tmp.getId());
                        for (long userID : newUsersIDs)
                            Notification.pushNotification(tmp.getId(), userID, Notification.TYPE_FAVOURITE, getApplicationContext());
                    }

                    existingUserIDs = Common.getRetweeters(tmp.getId());
                    if (existingUserIDs != null) {
                        newUsersIDs = rdbm.check(existingUserIDs, tmp.getId());
                        for (long userID : newUsersIDs)
                            Notification.pushNotification(tmp.getId(), userID, Notification.TYPE_RETWEET, getApplicationContext());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        rdbm.close();
        fdbm.close();

        //Log.i("InteractionsService", "InteractionsService STOP");
    }

}
