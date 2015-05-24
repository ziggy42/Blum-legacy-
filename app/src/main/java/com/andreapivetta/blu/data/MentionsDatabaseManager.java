package com.andreapivetta.blu.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class MentionsDatabaseManager {

    private static final String DB_NAME = "mentions_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SetsMetaData.TABLE_NAME + " ("
            + SetsMetaData.TWEET_ID + " INTEGER NOT NULL, "
            + SetsMetaData.USER_ID + " INTEGER NOT NULL, "
            + SetsMetaData.TIMESTAMP + " INTEGER NOT NULL);";
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase myDB;

    public MentionsDatabaseManager(Context context) {
        this.myDBHelper = new com.andreapivetta.blu.data.DatabaseHelper(context, DB_NAME, DB_VERSION, TABLE_CREATE);
    }

    public void open() {
        this.myDB = myDBHelper.getWritableDatabase();
    }

    public void close() {
        this.myDB.close();
    }

    public void clearDatabase() {
        myDB.execSQL("DROP TABLE IF EXISTS " + SetsMetaData.TABLE_NAME);
        myDB.execSQL(TABLE_CREATE);
    }

    public void insertTriple(long tweetID, long userID, long timestamp) {
        ContentValues cv = new ContentValues();
        cv.put(SetsMetaData.USER_ID, userID);
        cv.put(SetsMetaData.TWEET_ID, tweetID);
        cv.put(SetsMetaData.TIMESTAMP, timestamp);
        myDB.insert(SetsMetaData.TABLE_NAME, null, cv);
    }

    private void deleteTriple(long tweetID, long userID, long timestamp) {
        myDB.execSQL("DELETE FROM " + SetsMetaData.TABLE_NAME + " WHERE " + SetsMetaData.USER_ID +
                " = " + userID + " AND " + SetsMetaData.TWEET_ID + " = " + tweetID + " AND " +
                SetsMetaData.TIMESTAMP + " = " + timestamp);
    }

    private ArrayList<ArrayList<Long>> getTriples() {
        ArrayList<ArrayList<Long>> list = new ArrayList<>();
        String query = "SELECT " + SetsMetaData.TWEET_ID + ", " + SetsMetaData.USER_ID +
                ", " + SetsMetaData.TIMESTAMP + " FROM " + SetsMetaData.TABLE_NAME;
        Cursor cursor = myDB.rawQuery(query, null);

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

    public ArrayList<ArrayList<Long>> check(ArrayList<ArrayList<Long>> triples) {
        ArrayList<ArrayList<Long>> newMentions = new ArrayList<>();
        ArrayList<ArrayList<Long>> existingMentions = getTriples();

        for (ArrayList<Long> triple : triples) {
            if (!existingMentions.contains(triple)) {
                newMentions.add(triple);
                insertTriple(triple.get(0), triple.get(1), triple.get(2));
            }
        }

        /*for (ArrayList<Long> triple : triples) {
            if (existingMentions.contains(triple)) {
                existingMentions.remove(triple);
            } else {
                newMentions.add(triple);
                insertTriple(triple.get(0), triple.get(1), triple.get(2));
            }
        }

        for (ArrayList<Long> triple : existingMentions)
            deleteTriple(triple.get(0), triple.get(1), triple.get(2));*/

        return newMentions;
    }

    static final class SetsMetaData {
        static final String TABLE_NAME = "mentions_table";
        static final String TWEET_ID = "tweetid";
        static final String USER_ID = "userid";
        static final String TIMESTAMP = "timestamp";
    }
}
