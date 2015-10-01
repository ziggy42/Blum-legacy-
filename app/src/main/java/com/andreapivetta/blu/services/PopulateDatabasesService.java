package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.UserFollowed;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;


public class PopulateDatabasesService extends IntentService {

    public PopulateDatabasesService() {
        super("PopulateDatabasesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        DatabaseManager databaseManager = DatabaseManager.getInstance(getApplicationContext());
        databaseManager.clearDatabase();

        try {
            for (Status tmp : twitter.getUserTimeline(new Paging(1, 200))) {
                try {
                    for (long userID : Common.getFavoriters(tmp.getId()))
                        databaseManager.insertFavorite(userID, tmp.getId());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                try {
                    for (long userID : Common.getRetweeters(tmp.getId()))
                        databaseManager.insertRetweet(userID, tmp.getId());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            for (Status tmp : twitter.getMentionsTimeline(new Paging(1, 200)))
                databaseManager
                        .insertMention(tmp.getId(), tmp.getUser().getId(), tmp.getCreatedAt().getTime());

            IDs ids = twitter.getFollowersIDs(-1);
            do {
                for (long userID : ids.getIDs())
                    databaseManager.insertFollower(userID);
            } while (ids.hasNext());

            for (DirectMessage message : twitter.getDirectMessages(new Paging(1, 200)))
                databaseManager.insertDirectMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getSender().getName(), message.getSenderId(),
                        message.getSender().getBiggerProfileImageURL(), true);

            for (DirectMessage message : twitter.getSentDirectMessages(new Paging(1, 200)))
                databaseManager.insertDirectMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getRecipient().getName(), message.getRecipientId(),
                        message.getRecipient().getBiggerProfileImageURL(), true);

            long cursor = -1;
            PagableResponseList<User> pagableFollowings;
            do {
                pagableFollowings = twitter.getFriendsList(twitter.getId(), cursor, 200);
                for (User user : pagableFollowings)
                    databaseManager.insertFollowed(new UserFollowed(user.getId(), user.getName(), user.getScreenName(),
                            user.getBiggerProfileImageURL()));
            } while ((cursor = pagableFollowings.getNextCursor()) != 0);


            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putBoolean(getString(R.string.pref_key_db_populated), true).apply();

            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putBoolean("Following", true).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
