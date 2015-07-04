package com.andreapivetta.blu.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;

import java.util.ArrayList;

import twitter4j.DirectMessage;

public class DirectMessagesDatabaseManager {
    private static final String DB_NAME = "messages_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SetsMetaData.TABLE_NAME + " ("
            + SetsMetaData.MESSAGE_ID + " INTEGER NOT NULL PRIMARY KEY, "
            + SetsMetaData.SENDER_ID + " INTEGER NOT NULL, "
            + SetsMetaData.RECIPIENT_ID + " INTEGER NOT NULL, "
            + SetsMetaData.MESSAGE_TEXT + " PM_TEXT NOT NULL,"
            + SetsMetaData.OTHER_NAME + " PM_TEXT,"
            + SetsMetaData.PROFILE_PIC_URL + " PM_TEXT,"
            + SetsMetaData.FLAG_READ + " BOOLEAN NOT NULL,"
            + SetsMetaData.TIMESTAMP + " INTEGER NOT NULL);";
    private SQLiteDatabase myDB;
    private long loggedUserID;

    private static DirectMessagesDatabaseManager directMessagesDatabaseManager;

    public static DirectMessagesDatabaseManager getInstance(Context context) {
        DirectMessagesDatabaseManager r = directMessagesDatabaseManager;
        if (r == null) {
            synchronized (DirectMessagesDatabaseManager.class) {
                r = directMessagesDatabaseManager;
                if (r == null) {
                    r = new DirectMessagesDatabaseManager(context.getApplicationContext());
                    directMessagesDatabaseManager = r;
                }
            }
        }
        return r;
    }

    private DirectMessagesDatabaseManager(Context context) {
        DatabaseHelper myDBHelper = new DatabaseHelper(context, DB_NAME, DB_VERSION, TABLE_CREATE);
        this.loggedUserID = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_key_logged_user), 0L);
        this.myDB = myDBHelper.getWritableDatabase();
    }

    public void clearDatabase() {
        myDB.execSQL("DROP TABLE IF EXISTS " + SetsMetaData.TABLE_NAME);
        myDB.execSQL(TABLE_CREATE);
    }

    public void insertMessage(long messageID, long senderID, long recipientID, String message, long timestamp,
                              String otherUserName, String otherUserProfilePic, boolean read) {
        ContentValues cv = new ContentValues();
        cv.put(SetsMetaData.MESSAGE_ID, messageID);
        cv.put(SetsMetaData.SENDER_ID, senderID);
        cv.put(SetsMetaData.RECIPIENT_ID, recipientID);
        cv.put(SetsMetaData.MESSAGE_TEXT, message);
        cv.put(SetsMetaData.OTHER_NAME, otherUserName);
        cv.put(SetsMetaData.PROFILE_PIC_URL, otherUserProfilePic);
        cv.put(SetsMetaData.TIMESTAMP, timestamp);
        cv.put(SetsMetaData.FLAG_READ, read);
        myDB.insert(SetsMetaData.TABLE_NAME, null, cv);
    }

    private void deleteMessage(long messageID) {
        myDB.execSQL("DELETE FROM " + SetsMetaData.TABLE_NAME + " WHERE " + SetsMetaData.MESSAGE_ID +
                " = " + messageID);
    }

    public ArrayList<Long> getInterlocutors() {
        ArrayList<Long> interlocutors = new ArrayList<>();
        String query = "SELECT DISTINCT " + SetsMetaData.RECIPIENT_ID + ", " + SetsMetaData.SENDER_ID +
                " FROM " + SetsMetaData.TABLE_NAME;
        Cursor cursor = myDB.rawQuery(query, null);

        while (cursor.moveToNext()) {
            if (cursor.getLong(0) == loggedUserID) {
                if (!interlocutors.contains(cursor.getLong(1)))
                    interlocutors.add(cursor.getLong(1));
            } else {
                if (!interlocutors.contains(cursor.getLong(0)))
                    interlocutors.add(cursor.getLong(0));
            }
        }

        cursor.close();
        return interlocutors;
    }

    public Message getLastMessageForGivenUser(long otherUser) {
        String query = "SELECT MAX(" + SetsMetaData.TIMESTAMP + ")," +
                SetsMetaData.MESSAGE_TEXT + "," + SetsMetaData.MESSAGE_ID + "," + SetsMetaData.SENDER_ID + "," +
                SetsMetaData.RECIPIENT_ID + "," + SetsMetaData.OTHER_NAME + "," + SetsMetaData.PROFILE_PIC_URL +
                "," + SetsMetaData.FLAG_READ + " FROM " + SetsMetaData.TABLE_NAME +
                " WHERE " + SetsMetaData.SENDER_ID + " = " + otherUser +
                " OR " + SetsMetaData.RECIPIENT_ID + " = " + otherUser;
        Cursor c = myDB.rawQuery(query, null);
        c.moveToNext();
        Message message =
                new Message(c.getLong(2), c.getLong(3), c.getLong(4), c.getString(1), c.getLong(0),
                        c.getString(5), c.getString(6), c.getInt(7) == 1);
        c.close();
        return message;
    }

    public ArrayList<Message> getConversation(long otherUser) {
        ArrayList<Message> conversation = new ArrayList<>();
        String query = "SELECT * FROM " + SetsMetaData.TABLE_NAME + " WHERE " + SetsMetaData.SENDER_ID + " = " + otherUser +
                " OR " + SetsMetaData.RECIPIENT_ID + " = " + otherUser + " ORDER BY " + SetsMetaData.TIMESTAMP;

        Cursor c = myDB.rawQuery(query, null);
        while (c.moveToNext())
            conversation.add(
                    new Message(c.getLong(0), c.getLong(1), c.getLong(2), c.getString(3), c.getLong(7),
                            c.getString(4), c.getString(5), c.getInt(6) == 1));
        c.close();
        return conversation;
    }

    private ArrayList<Long> getAllReceivedMessages() {
        ArrayList<Long> receivedMessages = new ArrayList<>();
        Cursor cursor = myDB.rawQuery("SELECT " + SetsMetaData.MESSAGE_ID + " FROM " + SetsMetaData.TABLE_NAME +
                " WHERE " + SetsMetaData.SENDER_ID + " != " + loggedUserID, null);

        while (cursor.moveToNext())
            receivedMessages.add(cursor.getLong(0));

        cursor.close();
        return receivedMessages;
    }

    private ArrayList<Long> getAllSentMessages() {
        ArrayList<Long> sentMessages = new ArrayList<>();
        Cursor cursor = myDB.rawQuery("SELECT " + SetsMetaData.MESSAGE_ID + " FROM " + SetsMetaData.TABLE_NAME +
                " WHERE " + SetsMetaData.SENDER_ID + " = " + loggedUserID, null);

        while (cursor.moveToNext())
            sentMessages.add(cursor.getLong(0));

        cursor.close();
        return sentMessages;
    }

    public ArrayList<DirectMessage> checkReceived(ArrayList<DirectMessage> messages) {
        ArrayList<DirectMessage> newMessages = new ArrayList<>();
        ArrayList<Long> existingMessages = getAllReceivedMessages();

        for (DirectMessage dm : messages) {
            if (!existingMessages.contains(dm.getId())) {
                insertMessage(dm.getId(), dm.getSenderId(), dm.getRecipientId(), dm.getText(), dm.getCreatedAt().getTime(),
                        dm.getSenderScreenName(), dm.getSender().getBiggerProfileImageURL(), false);
                newMessages.add(dm);
            }
        }

        return newMessages;
    }

    public void checkSent(ArrayList<DirectMessage> messages) {
        ArrayList<Long> existingMessages = getAllSentMessages();
        for (DirectMessage dm : messages) {
            if (existingMessages.contains(dm.getId())) {
                existingMessages.remove(dm.getId());
            } else {
                insertMessage(dm.getId(), dm.getSenderId(), dm.getRecipientId(), dm.getText(), dm.getCreatedAt().getTime(),
                        dm.getRecipientScreenName(), dm.getRecipient().getBiggerProfileImageURL(), true);
            }
        }

        for (Long message : existingMessages)
            deleteMessage(message);
    }

    public int getCountUnreadMessages() {
        return (int) DatabaseUtils.queryNumEntries(myDB, SetsMetaData.TABLE_NAME, "NOT " + SetsMetaData.FLAG_READ);
    }

    public void markAllAsRead() {
        ContentValues cv = new ContentValues();
        cv.put(SetsMetaData.FLAG_READ, true);

        myDB.update(SetsMetaData.TABLE_NAME, cv, SetsMetaData.MESSAGE_ID + " IN ( " +
                "SELECT " + SetsMetaData.MESSAGE_ID +
                " FROM " + SetsMetaData.TABLE_NAME +
                " WHERE NOT " + SetsMetaData.FLAG_READ +
                ")", null);
    }

    public void markConversationAsRead(String otherName) {
        ContentValues cv = new ContentValues();
        cv.put(SetsMetaData.FLAG_READ, true);

        myDB.update(SetsMetaData.TABLE_NAME, cv, SetsMetaData.MESSAGE_ID + " IN ( " +
                "SELECT " + SetsMetaData.MESSAGE_ID +
                " FROM " + SetsMetaData.TABLE_NAME +
                " WHERE " + SetsMetaData.OTHER_NAME + "=? AND NOT " + SetsMetaData.FLAG_READ +
                ")", new String[]{otherName});

    }

    static final class SetsMetaData {
        static final String TABLE_NAME = "dms_table";
        static final String MESSAGE_ID = "message_id";
        static final String SENDER_ID = "sender_id";
        static final String RECIPIENT_ID = "recipient_id";
        static final String MESSAGE_TEXT = "current_message";
        static final String OTHER_NAME = "other_name";
        static final String PROFILE_PIC_URL = "other_pic_url";
        static final String FLAG_READ = "read";
        static final String TIMESTAMP = "timestamp";
    }
}
