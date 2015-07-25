package com.andreapivetta.blu.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseManager {

    private static final String DB_NAME = "blumdb";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase sqLiteDatabase;

    private static DatabaseManager databaseManager;

    public static DatabaseManager getInstance(Context context) {
        DatabaseManager r = databaseManager;
        if (r == null) {
            synchronized (DatabaseManager.class) {
                r = databaseManager;
                if (r == null) {
                    r = new DatabaseManager(context.getApplicationContext());
                    databaseManager = r;
                }
            }
        }
        return r;
    }

    private DatabaseManager(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context, DB_NAME, DB_VERSION);
        this.sqLiteDatabase = databaseHelper.getWritableDatabase();
    }

    public void insertDirectMessage(long messageID, long senderID, long recipientID, String message, long timestamp,
                                    String otherUserName, long otherUserID, String otherUserProfilePic, boolean read) {
        ContentValues cv = new ContentValues();
        cv.put(DirectMessage.MESSAGE_ID, messageID);
        cv.put(DirectMessage.SENDER_ID, senderID);
        cv.put(DirectMessage.RECIPIENT_ID, recipientID);
        cv.put(DirectMessage.MESSAGE_TEXT, message);
        cv.put(DirectMessage.OTHER_NAME, otherUserName);
        cv.put(DirectMessage.OTHER_ID, otherUserID);
        cv.put(DirectMessage.PROFILE_PIC_URL, otherUserProfilePic);
        cv.put(DirectMessage.TIMESTAMP, timestamp);
        cv.put(DirectMessage.FLAG_READ, read);
        sqLiteDatabase.insert(DirectMessage.TABLE_NAME, null, cv);
    }

    private void deleteDirectMessages(Object[] messages) {
        sqLiteDatabase.delete(DirectMessage.TABLE_NAME,
                DirectMessage.MESSAGE_ID + " IN " + Arrays.toString(messages).replace("[", "(").replace("]", ")"), null);
    }

    public int getCountUnreadDirectMessages() {
        return (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, DirectMessage.TABLE_NAME, "NOT " + DirectMessage.FLAG_READ);
    }

    public ArrayList<Message> getLastMessages() {
        ArrayList<Message> conversation = new ArrayList<>();
        Cursor c = sqLiteDatabase.rawQuery(DirectMessage.GET_LAST_MESSAGES, null);
        while (c.moveToNext())
            conversation.add(
                    new Message(c.getLong(2), c.getLong(3), c.getLong(4), c.getLong(8), c.getString(5),
                            c.getString(1), c.getString(6), c.getInt(7) == 1, c.getLong(0)));
        c.close();
        return conversation;
    }

    public ArrayList<Message> getConversation(long otherUserId) {
        ArrayList<Message> conversation = new ArrayList<>();
        Cursor c = sqLiteDatabase.rawQuery(DirectMessage.GET_CONVERSATION, new String[]{String.valueOf(otherUserId)});
        while (c.moveToNext())
            conversation.add(
                    new Message(c.getLong(0), c.getLong(1), c.getLong(2), c.getLong(3), c.getString(4),
                            c.getString(5), c.getString(6), c.getInt(7) == 1, c.getLong(8)));
        c.close();
        return conversation;
    }

    private ArrayList<Long> getAllReceivedMessages() {
        ArrayList<Long> receivedMessages = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(DirectMessage.GET_RECEIVED_MESSAGES, null);
        while (cursor.moveToNext())
            receivedMessages.add(cursor.getLong(0));
        cursor.close();
        return receivedMessages;
    }

    private ArrayList<Long> getAllSentMessages() {
        ArrayList<Long> sentMessages = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(DirectMessage.GET_SENT_MESSAGES, null);
        while (cursor.moveToNext())
            sentMessages.add(cursor.getLong(0));
        cursor.close();
        return sentMessages;
    }

    public ArrayList<twitter4j.DirectMessage> checkReceivedDirectMessages(ArrayList<twitter4j.DirectMessage> messages) {
        ArrayList<twitter4j.DirectMessage> newMessages = new ArrayList<>();
        ArrayList<Long> existingMessages = getAllReceivedMessages();

        for (twitter4j.DirectMessage dm : messages) {
            if (!existingMessages.contains(dm.getId())) {
                insertDirectMessage(dm.getId(), dm.getSenderId(), dm.getRecipientId(), dm.getText(), dm.getCreatedAt().getTime(),
                        dm.getSender().getName(), dm.getSenderId(), dm.getSender().getBiggerProfileImageURL(), false);
                newMessages.add(dm);
            }
        }

        return newMessages;
    }

    public void checkSentDirectMessages(ArrayList<twitter4j.DirectMessage> messages) {
        ArrayList<Long> existingMessages = getAllSentMessages();
        for (twitter4j.DirectMessage dm : messages) {
            if (existingMessages.contains(dm.getId())) {
                existingMessages.remove(dm.getId());
            } else {
                insertDirectMessage(dm.getId(), dm.getSenderId(), dm.getRecipientId(), dm.getText(), dm.getCreatedAt().getTime(),
                        dm.getRecipient().getName(), dm.getRecipientId(), dm.getRecipient().getBiggerProfileImageURL(), true);
            }
        }

        deleteDirectMessages(existingMessages.toArray());
    }

    public void markConversationAsRead(long otherID) {
        ContentValues cv = new ContentValues();
        cv.put(DirectMessage.FLAG_READ, true);

        sqLiteDatabase.update(DirectMessage.TABLE_NAME, cv, DirectMessage.MESSAGE_ID + " IN ( " +
                "SELECT " + DirectMessage.MESSAGE_ID +
                " FROM " + DirectMessage.TABLE_NAME +
                " WHERE " + DirectMessage.OTHER_ID + "=? AND NOT " + DirectMessage.FLAG_READ +
                ")", new String[]{String.valueOf(otherID)});
    }

    public void markAllDirectMessagesAsRead() {
        ContentValues cv = new ContentValues();
        cv.put(DirectMessage.FLAG_READ, true);

        sqLiteDatabase.update(DirectMessage.TABLE_NAME, cv, DirectMessage.MESSAGE_ID + " IN ( " +
                "SELECT " + DirectMessage.MESSAGE_ID +
                " FROM " + DirectMessage.TABLE_NAME +
                " WHERE NOT " + DirectMessage.FLAG_READ +
                ")", null);
    }

    public void insertFavorite(long userID, long tweetID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Favorite.TWEET_ID, tweetID);
        contentValues.put(Favorite.USER_ID, userID);
        sqLiteDatabase.insert(Favorite.TABLE_NAME, null, contentValues);
    }

    private ArrayList<Long> getUsersWhoFavoritedTweet(long tweetID) {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(Favorite.GET_USERS_BY_TWEET, new String[]{String.valueOf(tweetID)});
        while (cursor.moveToNext())
            list.add(cursor.getLong(0));
        cursor.close();
        return list;
    }

    public ArrayList<Long> checkFavorites(ArrayList<Long> userIDs, long tweetID) {
        ArrayList<Long> newUsersIDs = new ArrayList<>();
        ArrayList<Long> existingUsersIDs = getUsersWhoFavoritedTweet(tweetID);

        for (long userID : userIDs) {
            if (!existingUsersIDs.contains(userID)) {
                newUsersIDs.add(userID);
                insertFavorite(userID, tweetID);
            }
        }

        return newUsersIDs;
    }

    public void insertRetweet(long userID, long tweetID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Retweet.TWEET_ID, tweetID);
        contentValues.put(Retweet.USER_ID, userID);
        sqLiteDatabase.insert(Retweet.TABLE_NAME, null, contentValues);
    }

    private ArrayList<Long> getUsersWhoRetweetedTweet(long tweetID) {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(Retweet.GET_USERS_BY_TWEET, new String[]{String.valueOf(tweetID)});
        while (cursor.moveToNext())
            list.add(cursor.getLong(0));
        cursor.close();
        return list;
    }

    public ArrayList<Long> checkRetweets(ArrayList<Long> userIDs, long tweetID) {
        ArrayList<Long> newUsersIDs = new ArrayList<>();
        ArrayList<Long> existingUsersIDs = getUsersWhoRetweetedTweet(tweetID);

        for (long userID : userIDs) {
            if (!existingUsersIDs.contains(userID)) {
                newUsersIDs.add(userID);
                insertRetweet(userID, tweetID);
            }
        }

        return newUsersIDs;
    }

    public void insertFollower(long followerID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Follower.FOLLOWER_ID, followerID);
        sqLiteDatabase.insert(Follower.TABLE_NAME, null, contentValues);
    }

    private ArrayList<Long> getFollowersList() {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(Follower.GET_FOLLOWERS, null);
        while (cursor.moveToNext())
            list.add(cursor.getLong(0));
        cursor.close();
        return list;
    }

    public ArrayList<Long> checkFollowers(ArrayList<Long> userIDs) {
        ArrayList<Long> newUsersIDs = new ArrayList<>();
        ArrayList<Long> existingUsersIDs = getFollowersList();

        for (long userID : userIDs)
            if (!existingUsersIDs.contains(userID)) {
                newUsersIDs.add(userID);
                insertFollower(userID);
            }

        return newUsersIDs;
    }

    public void insertMention(long tweetID, long userID, long timestamp) {
        ContentValues cv = new ContentValues();
        cv.put(Mention.USER_ID, userID);
        cv.put(Mention.TWEET_ID, tweetID);
        cv.put(Mention.TIMESTAMP, timestamp);
        sqLiteDatabase.insert(Mention.TABLE_NAME, null, cv);
    }

    private ArrayList<ArrayList<Long>> getMentions() {
        ArrayList<ArrayList<Long>> list = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery(Mention.GET_ALL, null);
        while (cursor.moveToNext()) {
            ArrayList<Long> tmp = new ArrayList<>();
            tmp.add(cursor.getLong(0));
            tmp.add(cursor.getLong(1));
            tmp.add(cursor.getLong(2));
            list.add(tmp);
        }
        cursor.close();
        return list;
    }

    public ArrayList<ArrayList<Long>> checkMentions(ArrayList<ArrayList<Long>> triples) {
        ArrayList<ArrayList<Long>> newMentions = new ArrayList<>();
        ArrayList<ArrayList<Long>> existingMentions = getMentions();

        for (ArrayList<Long> triple : triples) {
            if (!existingMentions.contains(triple)) {
                newMentions.add(triple);
                insertMention(triple.get(0), triple.get(1), triple.get(2));
            }
        }

        return newMentions;
    }

    public long insertNotification(com.andreapivetta.blu.data.Notification notification) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notification.TYPE, notification.type);
        contentValues.put(Notification.USERNAME, notification.user);
        contentValues.put(Notification.FLAG_READ, notification.read);
        contentValues.put(Notification.USER_ID, notification.userID);
        contentValues.put(Notification.PICURL, notification.profilePicURL);
        contentValues.put(Notification.TIMESTAMP, notification.timestamp);

        if (notification.tweetID > 0) {
            contentValues.put(Notification.TARGET_TWEET, notification.tweetID);
            contentValues.put(Notification.STATUS, notification.status);
        }

        return sqLiteDatabase.insert(Notification.TABLE_NAME, null, contentValues);
    }

    private ArrayList<com.andreapivetta.blu.data.Notification> getAllNotifications(boolean unread) {
        ArrayList<com.andreapivetta.blu.data.Notification> notifications = new ArrayList<>();
        Cursor c = sqLiteDatabase.rawQuery((unread) ? Notification.GET_UNREAD : Notification.GET_READ, null);
        while (c.moveToNext()) {
            notifications.add(new com.andreapivetta.blu.data.
                    Notification(c.getInt(0), c.getString(1), c.getString(2), c.getLong(3), c.getLong(4),
                    c.getString(5), c.getString(6), c.getInt(7) == 1, c.getLong(8)));
        }
        c.close();
        return notifications;
    }

    public ArrayList<com.andreapivetta.blu.data.Notification> getAllUnreadNotifications() {
        return getAllNotifications(false);
    }

    public ArrayList<com.andreapivetta.blu.data.Notification> getAllReadNotifications() {
        return getAllNotifications(true);
    }

    public void setAllAsRead() {
        ContentValues cv = new ContentValues();
        cv.put(Notification.FLAG_READ, true);
        sqLiteDatabase.update(Notification.TABLE_NAME, cv, "NOT " + Notification.FLAG_READ, null);
    }

    public int getCountUnreadNotifications() {
        return (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, Notification.TABLE_NAME, "NOT " + Notification.FLAG_READ);
    }

    public void deleteAllNotifications() {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Notification.TABLE_NAME);
        sqLiteDatabase.execSQL(Notification.CREATE_TABLE);
    }

    public void clearDatabase() {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DirectMessage.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Favorite.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Retweet.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Follower.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Mention.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Notification.TABLE_NAME);

        sqLiteDatabase.execSQL(DirectMessage.CREATE_TABLE);
        sqLiteDatabase.execSQL(Favorite.CREATE_TABLE);
        sqLiteDatabase.execSQL(Retweet.CREATE_TABLE);
        sqLiteDatabase.execSQL(Follower.CREATE_TABLE);
        sqLiteDatabase.execSQL(Mention.CREATE_TABLE);
        sqLiteDatabase.execSQL(Notification.CREATE_TABLE);
    }

    private interface DirectMessage {
        String TABLE_NAME = "dms";
        String MESSAGE_ID = "dm_id";
        String SENDER_ID = "sender_id";
        String RECIPIENT_ID = "recipient_id";
        String OTHER_ID = "other_id";
        String OTHER_NAME = "other_name";
        String MESSAGE_TEXT = "current_message";
        String PROFILE_PIC_URL = "pic_url";
        String FLAG_READ = "read";
        String TIMESTAMP = "timestamp";

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                MESSAGE_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                SENDER_ID + " INTEGER NOT NULL, " +
                RECIPIENT_ID + " INTEGER NOT NULL, " +
                OTHER_ID + " INTEGER, " +
                OTHER_NAME + " TEXT, " +
                MESSAGE_TEXT + " TEXT NOT NULL, " +
                PROFILE_PIC_URL + " TEXT NOT NULL, " +
                FLAG_READ + " BOOLEAN NOT NULL," +
                TIMESTAMP + " INTEGER NOT NULL)";

        String GET_CONVERSATION = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + OTHER_ID + "=? ORDER BY " + TIMESTAMP;
        String GET_RECEIVED_MESSAGES = "SELECT " + MESSAGE_ID + " FROM " + TABLE_NAME +
                " WHERE " + SENDER_ID + "=" + OTHER_ID;
        String GET_SENT_MESSAGES = "SELECT " + MESSAGE_ID + " FROM " + TABLE_NAME +
                " WHERE " + RECIPIENT_ID + "=" + OTHER_ID;
        String GET_LAST_MESSAGES = "SELECT MAX(" + TIMESTAMP + ")," + MESSAGE_TEXT + "," + MESSAGE_ID + "," + SENDER_ID + "," +
                RECIPIENT_ID + "," + OTHER_NAME + "," + PROFILE_PIC_URL + "," + FLAG_READ + "," + OTHER_ID +
                " FROM " + TABLE_NAME + " GROUP BY " + OTHER_ID + " ORDER BY " + TIMESTAMP + " DESC";
    }

    private interface Favorite {
        String TABLE_NAME = "fav_table";
        String TWEET_ID = "tweetid";
        String USER_ID = "userid";

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                TWEET_ID + " INTEGER NOT NULL, " +
                USER_ID + " INTEGER NOT NULL)";

        String GET_USERS_BY_TWEET = "SELECT " + USER_ID + " FROM " + TABLE_NAME + " WHERE " + TWEET_ID + " =?";
    }

    private interface Retweet {
        String TABLE_NAME = "ret_table";
        String TWEET_ID = "tweetid";
        String USER_ID = "userid";

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                TWEET_ID + " INTEGER NOT NULL, " +
                USER_ID + " INTEGER NOT NULL)";

        String GET_USERS_BY_TWEET = "SELECT " + USER_ID + " FROM " + TABLE_NAME + " WHERE " + TWEET_ID + " =?";
    }

    private interface Follower {
        String TABLE_NAME = "followers_table";
        String FOLLOWER_ID = "userid";

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                FOLLOWER_ID + " INTEGER NOT NULL PRIMARY KEY)";

        String GET_FOLLOWERS = "SELECT " + FOLLOWER_ID + " FROM " + TABLE_NAME;
    }

    private interface Mention {
        String TABLE_NAME = "mentions_table";
        String TWEET_ID = "tweetid";
        String USER_ID = "userid";
        String TIMESTAMP = "timestamp";

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                TWEET_ID + " INTEGER NOT NULL, " +
                USER_ID + " INTEGER NOT NULL, " +
                TIMESTAMP + " INTEGER NOT NULL)";

        String GET_ALL = "SELECT * FROM " + TABLE_NAME;
    }

    private interface Notification {
        String TABLE_NAME = "notifications_table";
        String TYPE = "not_type";
        String USERNAME = "user";
        String USER_ID = "userid";
        String TARGET_TWEET = "tweet";
        String STATUS = "status";
        String PICURL = "picurl";
        String FLAG_READ = "read";
        String TIMESTAMP = "timestamp";

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TYPE + " TEXT NOT NULL, " +
                USERNAME + " TEXT NOT NULL, " +
                USER_ID + " INTEGER NOT NULL, " +
                TARGET_TWEET + " INTEGER, " +
                STATUS + " TEXT, " +
                PICURL + " TEXT NOT NULL, " +
                FLAG_READ + " BOOLEAN NOT NULL, " +
                TIMESTAMP + " INTEGER NOT NULL)";

        String GET_UNREAD = "SELECT * FROM " + TABLE_NAME + " WHERE " + FLAG_READ +
                " ORDER BY " + TIMESTAMP + " DESC";
        String GET_READ = "SELECT * FROM " + TABLE_NAME + " WHERE NOT " + FLAG_READ +
                " ORDER BY " + TIMESTAMP + " DESC";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String DB_NAME, int DB_VERSION) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DirectMessage.CREATE_TABLE);
            db.execSQL(Favorite.CREATE_TABLE);
            db.execSQL(Retweet.CREATE_TABLE);
            db.execSQL(Follower.CREATE_TABLE);
            db.execSQL(Mention.CREATE_TABLE);
            db.execSQL(Notification.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
