package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "movies.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + Contract.MovieEntry.TABLE_NAME
                + " (" +
                Contract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                Contract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                Contract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                Contract.MovieEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                Contract.MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                Contract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE + " TEXT NOT NULL, " +
                Contract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                Contract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override //in the future if new column is added an APPEND statement will be needed...
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Contract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}


