package com.myapps.documentscanner.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.myapps.documentscanner.data.TextContract.TextEntry;

public class TextDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "texttable.db";
    private static final int DATABASE_VERSION = 1;


    public TextDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database creation SQL statement
        final String SQL_CREATE_TEXT_TABLE = "CREATE TABLE "
                + TextEntry.TABLE_NAME
                + "("
                + TextEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TextEntry.COLUMN_SUMMARY + " TEXT NOT NULL,"
                + TextEntry.COLUMN_DESCRIPTION
                + " TEXT NOT NULL"
                + ");";

        db.execSQL(SQL_CREATE_TEXT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Replace old Table with new One.
        db.execSQL(" DROP TABLE IF EXISTS "+TextEntry.TABLE_NAME);
        onCreate(db);
    }
}
