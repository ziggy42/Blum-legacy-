package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.FavoritesDatabaseManager;
import com.andreapivetta.blu.data.FollowersDatabaseManager;
import com.andreapivetta.blu.data.MentionsDatabaseManager;
import com.andreapivetta.blu.data.RetweetsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;


public class PopulateDatabasesService extends IntentService {

    public PopulateDatabasesService() {
        super("PopulateDatabasesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        FavoritesDatabaseManager favoritesDatabaseManager = FavoritesDatabaseManager.getInstance(getApplicationContext());
        RetweetsDatabaseManager retweetsDatabaseManager = RetweetsDatabaseManager.getInstance(getApplicationContext());
        MentionsDatabaseManager mentionsDatabaseManager = MentionsDatabaseManager.getInstance(getApplicationContext());
        FollowersDatabaseManager followersDatabaseManager = FollowersDatabaseManager.getInstance(getApplicationContext());
        DirectMessagesDatabaseManager dbm = DirectMessagesDatabaseManager.getInstance(getApplicationContext());

        try {
            favoritesDatabaseManager.clearDatabase();
            retweetsDatabaseManager.clearDatabase();
            for (Status tmp : twitter.getUserTimeline(new Paging(1, 200))) {
                try {
                    for (long userID : Common.getFavoriters(tmp.getId()))
                        favoritesDatabaseManager.insertCouple(userID, tmp.getId());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                try {
                    for (long userID : Common.getRetweeters(tmp.getId()))
                        retweetsDatabaseManager.insertCouple(userID, tmp.getId());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            mentionsDatabaseManager.clearDatabase();
            for (Status tmp : twitter.getMentionsTimeline(new Paging(1, 200)))
                mentionsDatabaseManager
                        .insertTriple(tmp.getId(), tmp.getUser().getId(), tmp.getCreatedAt().getTime());

            followersDatabaseManager.clearDatabase();
            IDs ids = twitter.getFollowersIDs(-1);
            do {
                for (long userID : ids.getIDs())
                    followersDatabaseManager.insertFollower(userID);
            } while (ids.hasNext());

            dbm.clearDatabase();
            for (DirectMessage message : twitter.getDirectMessages(new Paging(1, 200)))
                dbm.insertMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getSender().getName(),
                        message.getSender().getBiggerProfileImageURL(), true);

            for (DirectMessage message : twitter.getSentDirectMessages(new Paging(1, 200)))
                dbm.insertMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getRecipient().getName(),
                        message.getRecipient().getBiggerProfileImageURL(), true);

            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putBoolean(getString(R.string.pref_key_db_populated), true).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
