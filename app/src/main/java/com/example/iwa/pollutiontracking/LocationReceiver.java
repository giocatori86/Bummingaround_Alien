package com.example.iwa.pollutiontracking;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.example.iwa.pollutiontracking.data.PollutionTrackingContract;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationReceiver";

    public LocationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i(TAG, "location received: " + intent);
        final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        Log.i(TAG, "new location: " + location);

        addLocation(context, location);
    }

    private long addLocation(Context context, Location location) {
        Log.v(TAG, "inserting " + location);

        ContentValues values = new ContentValues();
        values.put(PollutionTrackingContract.LocationEntry.COLUMN_COORD_LAT, location.getLatitude());
        values.put(PollutionTrackingContract.LocationEntry.COLUMN_COORD_LONG, location.getLongitude());
        values.put(PollutionTrackingContract.LocationEntry.COLUMN_PRECISION, location.getAccuracy());
        values.put(PollutionTrackingContract.LocationEntry.COLUMN_TIMESTAMP, location.getTime());

        Uri locationUri = context.getContentResolver().insert(PollutionTrackingContract.LocationEntry.CONTENT_URI, values);
        return ContentUris.parseId(locationUri);
    }
}
