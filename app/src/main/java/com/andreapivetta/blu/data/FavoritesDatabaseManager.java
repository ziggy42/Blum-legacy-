package com.andreapivetta.blu.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class FavoritesDatabaseManager extends InteractionsDatabaseManager {

    private static final String DB_NAME = "favorites_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SetsMetaData.TABLE_NAME + " ("
            + SetsMetaData.TWEET_ID + " INTEGER NOT NULL, "
            + SetsMetaData.USER_ID + " INTEGER NOT NULL);";

    public FavoritesDatabaseManager(Context context) {
        super(context);
        this.myDBHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    protected String getTableCreate() {
        return TABLE_CREATE;
    }

    @Override
    public void insertCouple(long userID, long tweetID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SetsMetaData.TWEET_ID, tweetID);
        contentValues.put(SetsMetaData.USER_ID, userID);
        myDB.insert(SetsMetaData.TABLE_NAME, null, contentValues);
    }

    @Override
    void deleteCouple(long userID, long tweetID) {
        myDB.execSQL("DELETE FROM " + SetsMetaData.TABLE_NAME + " WHERE " + SetsMetaData.USER_ID +
                " = " + userID + " AND " + SetsMetaData.TWEET_ID + " = " + tweetID);
    }

    @Override
    ArrayList<Long> getCouplesFromTweet(long tweetID) {
        ArrayList<Long> list = new ArrayList<>();
        String query = "SELECT " + SetsMetaData.USER_ID + " FROM " + SetsMetaData.TABLE_NAME +
                " WHERE " + SetsMetaData.TWEET_ID + " = " + tweetID;
        Cursor cursor = myDB.rawQuery(query, null);

        while (cursor.moveToNext())
            list.add(cursor.getLong(0));

        cursor.close();
        return list;
    }

    static final class SetsMetaData {
        static final String TABLE_NAME = "favorites_table";
        static final String TWEET_ID = "tweetid";
        static final String USER_ID = "userid";
    }
}
