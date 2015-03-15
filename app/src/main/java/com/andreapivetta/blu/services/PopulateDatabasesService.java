package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;

import com.andreapivetta.blu.data.FavoritesDatabaseManager;
import com.andreapivetta.blu.data.FollowersDatabaseManager;
import com.andreapivetta.blu.data.MentionsDatabaseManager;
import com.andreapivetta.blu.data.RetweetsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class PopulateDatabasesService extends IntentService {

    public PopulateDatabasesService() {
        super("PopulateDatabasesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        FavoritesDatabaseManager favoritesDatabaseManager = new FavoritesDatabaseManager(getApplicationContext());
        RetweetsDatabaseManager retweetsDatabaseManager = new RetweetsDatabaseManager(getApplicationContext());
        MentionsDatabaseManager mentionsDatabaseManager = new MentionsDatabaseManager(getApplicationContext());
        FollowersDatabaseManager followersDatabaseManager = new FollowersDatabaseManager(getApplicationContext());

        try {
            favoritesDatabaseManager.open();
            retweetsDatabaseManager.open();
            for (Status tmp : twitter.getUserTimeline(new Paging(1, 200))) {
                for (long userID : Common.getFavoriters(tmp.getId()))
                    favoritesDatabaseManager.insertCouple(userID, tmp.getId());

                for (long userID : Common.getRetweeters(tmp.getId()))
                    retweetsDatabaseManager.insertCouple(userID, tmp.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        favoritesDatabaseManager.close();
        retweetsDatabaseManager.close();

        try {
            mentionsDatabaseManager.open();
            for (Status tmp : twitter.getMentionsTimeline(new Paging(1, 200)))
                mentionsDatabaseManager
                        .insertTriple(tmp.getId(), tmp.getUser().getId(), tmp.getCreatedAt().getTime());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        mentionsDatabaseManager.close();

        try {
            followersDatabaseManager.open();
            IDs ids = twitter.getFollowersIDs(-1);
            do {
                for (long userID : ids.getIDs())
                    followersDatabaseManager.insertFollower(userID);
            } while (ids.hasNext());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        followersDatabaseManager.close();
    }

}
