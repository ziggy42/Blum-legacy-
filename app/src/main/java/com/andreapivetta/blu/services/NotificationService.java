package com.andreapivetta.blu.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.andreapivetta.blu.twitter.TwitterUtils;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

public class NotificationService extends Service {

    private static final UserStreamListener listener = new UserStreamListener() {
        @Override
        public void onDeletionNotice(long l, long l2) {

        }

        @Override
        public void onFriendList(long[] longs) {

        }

        @Override
        public void onFavorite(User user, User user2, Status status) {

        }

        @Override
        public void onUnfavorite(User user, User user2, Status status) {

        }

        @Override
        public void onFollow(User user, User user2) {

        }

        @Override
        public void onUnfollow(User user, User user2) {

        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {

        }

        @Override
        public void onUserListMemberAddition(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListMemberDeletion(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListSubscription(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListUnsubscription(User user, User user2, UserList userList) {

        }

        @Override
        public void onUserListCreation(User user, UserList userList) {

        }

        @Override
        public void onUserListUpdate(User user, UserList userList) {

        }

        @Override
        public void onUserListDeletion(User user, UserList userList) {

        }

        @Override
        public void onUserProfileUpdate(User user) {

        }

        @Override
        public void onBlock(User user, User user2) {

        }

        @Override
        public void onUnblock(User user, User user2) {

        }

        @Override
        public void onStatus(Status status) {

        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

        }

        @Override
        public void onTrackLimitationNotice(int i) {

        }

        @Override
        public void onScrubGeo(long l, long l2) {

        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {

        }

        @Override
        public void onException(Exception e) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterStream twitterStream = TwitterUtils.getTwitterStream(getApplicationContext());
        twitterStream.addListener(listener);
        twitterStream.user();
    }
}