package com.andreapivetta.blu.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private String TABLE_CREATE;

    public DatabaseHelper(Context context, String DB_NAME, int DB_VERSION, String TABLE_CREATE) {
        super(context, DB_NAME, null, DB_VERSION);
        this.TABLE_CREATE = TABLE_CREATE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
