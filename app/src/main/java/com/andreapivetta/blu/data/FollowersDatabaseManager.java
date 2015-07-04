package com.andreapivetta.blu.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class FollowersDatabaseManager {

    private static final String DB_NAME = "followers_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SetsMetaData.TABLE_NAME + " (" + SetsMetaData.FOLLOWER_ID + " INTEGER NOT NULL);";
    private SQLiteDatabase myDB;

    private static FollowersDatabaseManager followersDatabaseManager;

    public static FollowersDatabaseManager getInstance(Context context) {
        FollowersDatabaseManager r = followersDatabaseManager;
        if (r == null) {
            synchronized (FollowersDatabaseManager.class) {
                r = followersDatabaseManager;
                if (r == null) {
                    r = new FollowersDatabaseManager(context.getApplicationContext());
                    followersDatabaseManager = r;
                }
            }
        }
        return r;
    }

    private FollowersDatabaseManager(Context context) {
        DatabaseHelper myDBHelper = new DatabaseHelper(context, DB_NAME, DB_VERSION, TABLE_CREATE);
        this.myDB = myDBHelper.getWritableDatabase();
    }

    public void clearDatabase() {
        myDB.execSQL("DROP TABLE IF EXISTS " + SetsMetaData.TABLE_NAME);
        myDB.execSQL(TABLE_CREATE);
    }

    public void insertFollower(long followerID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SetsMetaData.FOLLOWER_ID, followerID);
        myDB.insert(SetsMetaData.TABLE_NAME, null, contentValues);
    }

    private ArrayList<Long> getFollowersList() {
        ArrayList<Long> list = new ArrayList<>();
        String query = "SELECT " + SetsMetaData.FOLLOWER_ID + " FROM " + SetsMetaData.TABLE_NAME;

        Cursor cursor = myDB.rawQuery(query, null);
        while (cursor.moveToNext())
            list.add(cursor.getLong(0));

        cursor.close();
        return list;
    }

    public ArrayList<Long> check(ArrayList<Long> userIDs) {
        ArrayList<Long> newUsersIDs = new ArrayList<>();
        ArrayList<Long> existingUsersIDs = getFollowersList();

        for (long userID : userIDs)
            if (!existingUsersIDs.contains(userID)) {
                newUsersIDs.add(userID);
                insertFollower(userID);
            }

        return newUsersIDs;
    }

    static final class SetsMetaData {
        static final String TABLE_NAME = "followers_table";
        static final String FOLLOWER_ID = "userid";
    }
}
