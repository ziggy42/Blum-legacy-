package com.andreapivetta.blu.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamListener;

public class StreamNotificationService extends Service {

    public final static String NEW_TWEETS_INTENT = "com.andreapivetta.blu.NEW_TWEETS_INTENT";
    public final static String NEW_NOTIFICATION_INTENT = "com.andreapivetta.blu.NEW_NOTIFICATION_INTENT";
    private final UserStreamListener listener = new UserStreamListener() {
        @Override
        public void onDeletionNotice(long l, long l2) {

        }

        @Override
        public void onFriendList(long[] longs) {

        }

        @Override
        public void onFavorite(User user, User user2, Status status) {
            try {
                if (user2.getScreenName().equals(twitter.getScreenName()))
                    Common.pushNotification(status.getId(), user.getName(), Notification.TYPE_FAVOURITE,
                            status.getText(), user.getBiggerProfileImageURL(), user.getId(), getApplicationContext());
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUnfavorite(User user, User user2, Status status) {

        }

        @Override
        public void onFollow(User user, User user2) {

            try {
                if (user2.getScreenName().equals(twitter.getScreenName())) {
                    Common.pushNotification((long) -1, user.getName(), Notification.TYPE_FOLLOW,
                            "", user.getBiggerProfileImageURL(), user.getId(), getApplicationContext());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
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
            ArrayList<String> names = new ArrayList<>();
            for (UserMentionEntity e : status.getUserMentionEntities())
                names.add(e.getScreenName());

            try {
                if (names.contains(twitter.getScreenName())) {
                    if (status.isRetweet()) {
                        if (status.getRetweetedStatus().getUser().getScreenName().equals(twitter.getScreenName()))
                            Common.pushNotification(status.getId(), status.getUser().getName(), Notification.TYPE_RETWEET,
                                    status.getRetweetedStatus().getText(),
                                    status.getUser().getBiggerProfileImageURL(), status.getUser().getId(),
                                    getApplicationContext());
                        else
                            Common.pushNotification(status.getId(), status.getUser().getName(),
                                    Notification.TYPE_RETWEET_MENTIONED, status.getRetweetedStatus().getText(),
                                    status.getUser().getBiggerProfileImageURL(), status.getUser().getId(),
                                    getApplicationContext());
                    } else {
                        Common.pushNotification(status.getId(), status.getUser().getName(), Notification.TYPE_MENTION,
                                status.getText(), status.getUser().getBiggerProfileImageURL(), status.getUser().getId(),
                                getApplicationContext());
                    }
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            Intent i = new Intent();
            i.setAction(NEW_TWEETS_INTENT)
                    .putExtra("PARCEL_STATUS", status);
            sendBroadcast(i);
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
    private TwitterStream twitterStream;
    private Twitter twitter;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        twitter = TwitterUtils.getTwitter(getApplicationContext());

        twitterStream = TwitterUtils.getTwitterStream(getApplicationContext());
        twitterStream.addListener(listener);
        twitterStream.user();
    }

    @Override
    public void onDestroy() {
        twitterStream.cleanUp();
        twitterStream.shutdown();

        super.onDestroy();
    }
}
