package com.example.iwa.pollutiontracking.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by matteo on 11/03/15.
 */
public class PollutionTrackingProvider extends ContentProvider {
    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;
    private static final int VENUE = 200;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    private PollitionTrackingDBHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found. The code passed into the constructor represents the code to return for the ro..
        // URI. It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PollutionTrackingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PollutionTrackingContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, PollutionTrackingContract.PATH_LOCATION + "/#", LOCATION_ID);

        matcher.addURI(authority, PollutionTrackingContract.PATH_VENUE, VENUE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PollitionTrackingDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "location"
            case LOCATION:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PollutionTrackingContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case LOCATION_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PollutionTrackingContract.LocationEntry.TABLE_NAME,
                        projection,
                        PollutionTrackingContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;

            case VENUE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PollutionTrackingContract.VenueEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (retCursor != null)
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATION:
                return PollutionTrackingContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return PollutionTrackingContract.LocationEntry.CONTENT_ITEM_TYPE;
            case VENUE:
                return PollutionTrackingContract.VenueEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case LOCATION: {
                long _id = db.insert(PollutionTrackingContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PollutionTrackingContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VENUE: {
                long _id = db.insert(PollutionTrackingContract.VenueEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PollutionTrackingContract.VenueEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int deletedRows;

        switch (match) {

            case LOCATION:
                deletedRows = db.delete(PollutionTrackingContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VENUE:
                deletedRows = db.delete(PollutionTrackingContract.VenueEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (selection == null || deletedRows != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int affectedRows;

        switch (match) {

            case LOCATION:
                affectedRows = db.update(PollutionTrackingContract.LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case VENUE:
                affectedRows = db.update(PollutionTrackingContract.VenueEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (affectedRows != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return affectedRows;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }
}
