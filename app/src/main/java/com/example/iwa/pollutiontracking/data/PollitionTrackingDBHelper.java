package com.example.iwa.pollutiontracking.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.iwa.pollutiontracking.data.PollutionTrackingContract.LocationEntry;

/**
 * Created by matteo on 11/03/15.
 */
public class PollitionTrackingDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pollutionTracking.db";

    public PollitionTrackingDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        final String SQL_CREATE_LOCATION_TABLE =
                "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                        LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                        LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                        LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +

                        LocationEntry.COLUMN_PRECISION + " REAL NOT NULL, " +
                        LocationEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL " +

                        //"UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE " +
                        ");";



        db.execSQL(SQL_CREATE_LOCATION_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop because we use the database only as a cache
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        onCreate(db);
    }
}
