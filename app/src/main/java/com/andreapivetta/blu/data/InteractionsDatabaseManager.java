package com.andreapivetta.blu.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;

public abstract class InteractionsDatabaseManager {

    protected DatabaseHelper myDBHelper;
    protected SQLiteDatabase myDB;
    protected Context context;

    public InteractionsDatabaseManager(Context context) {
        this.context = context;
    }

    protected abstract String getTableCreate();

    public void open() {
        this.myDB = myDBHelper.getWritableDatabase();
    }

    public void close() {
        this.myDB.close();
    }

    abstract void insertCouple(long userID, long tweetID);

    abstract void deleteCouple(long userID, long tweetID);

    abstract ArrayList<Long> getCouplesFromTweet(long tweetID);

    public ArrayList<Long> check(ArrayList<Long> userIDs, long tweetID) {
        ArrayList<Long> newUsersIDs = new ArrayList<>();
        ArrayList<Long> existingUsersIDs = getCouplesFromTweet(tweetID);

        for (long userID : userIDs) {
            if (existingUsersIDs.contains(userID)) {
                existingUsersIDs.remove(userID);
            } else {
                newUsersIDs.add(userID);
                insertCouple(userID, tweetID);
            }
        }

        for (long userID : existingUsersIDs)
            deleteCouple(userID, tweetID);

        return newUsersIDs;
    }

    abstract ArrayList<Long> getInteractors(Status status);

    abstract List<Status> getPopulationSource() throws TwitterException;

    public void populateDatabase() {
        new PopulateDatabaseAsyncTask().execute(null, null, null);
    }

    protected class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(getTableCreate());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

    private class PopulateDatabaseAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            open();

            try {
                for (twitter4j.Status tmp : getPopulationSource())
                    for (long userID : getInteractors(tmp))
                        insertCouple(userID, tmp.getId());

            } catch (TwitterException e) {
                e.printStackTrace();
            }

            close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

        }
    }

}
