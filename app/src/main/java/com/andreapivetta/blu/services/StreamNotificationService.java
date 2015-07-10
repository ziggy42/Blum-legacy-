package com.andreapivetta.blu.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.twitter.TwitterUtils;

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
    private TwitterStream twitterStream;
    private Twitter twitter;
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
                if (user2.getId() == twitter.getId()) {
                    Notification.pushNotification(status.getId(), user.getName(), Notification.TYPE_FAVOURITE,
                            status.getText(), user.getBiggerProfileImageURL(), user.getId(), getApplicationContext());

                    DatabaseManager.getInstance(getApplicationContext()).insertFavorite(user.getId(), status.getId());
                }
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
                if (user2.getId() == twitter.getId()) {
                    Notification.pushNotification((long) -1, user.getName(), Notification.TYPE_FOLLOW,
                            "", user.getBiggerProfileImageURL(), user.getId(), getApplicationContext());

                    DatabaseManager.getInstance(getApplicationContext()).insertFollower(user.getId());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUnfollow(User user, User user2) {

        }

        @Override
        public void onDirectMessage(DirectMessage message) {
            DatabaseManager databaseManager = DatabaseManager.getInstance(getApplicationContext());

            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getLong(getApplicationContext().getString(R.string.pref_key_logged_user), 0L) == message.getSenderId()) {
                databaseManager.insertDirectMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getRecipient().getName(), message.getRecipientId(),
                        message.getRecipient().getBiggerProfileImageURL(), true);
            } else {
                databaseManager.insertDirectMessage(message.getId(), message.getSenderId(), message.getRecipientId(),
                        message.getText(), message.getCreatedAt().getTime(), message.getSender().getName(), message.getSenderId(),
                        message.getSender().getBiggerProfileImageURL(), false);

                Message.pushMessage(message, getApplicationContext());
            }
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
        public void onUserSuspension(long suspendedUser) {

        }

        @Override
        public void onUserDeletion(long deletedUser) {

        }

        @Override
        public void onBlock(User user, User user2) {

        }

        @Override
        public void onUnblock(User user, User user2) {

        }

        @Override
        public void onRetweetedRetweet(User source, User target, Status retweetedStatus) {

        }

        @Override
        public void onFavoritedRetweet(User source, User target, Status favoritedRetweeet) {

        }

        @Override
        public void onQuotedTweet(User source, User target, Status quotingTweet) {

        }

        @Override
        public void onStatus(Status status) {
            ArrayList<Long> names = new ArrayList<>();
            for (UserMentionEntity e : status.getUserMentionEntities())
                names.add(e.getId());

            try {
                if (names.contains(twitter.getId())) {
                    if (status.isRetweet()) {
                        if (status.getRetweetedStatus().getUser().getId() == twitter.getId()) {
                            Notification.pushNotification(status.getId(), status.getUser().getName(), Notification.TYPE_RETWEET,
                                    status.getRetweetedStatus().getText(),
                                    status.getUser().getBiggerProfileImageURL(), status.getUser().getId(),
                                    getApplicationContext());

                            DatabaseManager.getInstance(getApplicationContext()).insertRetweet(status.getUser().getId(), status.getId());
                        } else
                            Notification.pushNotification(status.getId(), status.getUser().getName(),
                                    Notification.TYPE_RETWEET_MENTIONED, status.getRetweetedStatus().getText(),
                                    status.getUser().getBiggerProfileImageURL(), status.getUser().getId(),
                                    getApplicationContext());
                    } else {
                        Notification.pushNotification(status.getId(), status.getUser().getName(), Notification.TYPE_MENTION,
                                status.getText(), status.getUser().getBiggerProfileImageURL(), status.getUser().getId(),
                                getApplicationContext());

                        DatabaseManager.getInstance(getApplicationContext()).insertMention(status.getId(), status.getUser().getId(), status.getCreatedAt().getTime());
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

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                twitterStream.clearListeners();
                twitterStream.cleanUp();
                twitterStream.shutdown();

                return null;
            }
        }.execute();

    }
}
