package com.example.iwa.pollutiontracking.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by matteo on 11/03/15.
 */
public class PollutionTrackingContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website. A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique to the
    // device
    public static final String CONTENT_AUTHORITY = "com.example.iwa.pollutiontracking";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_LOCATION = "location";

    public static final class LocationEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = "location";

        // The coordinates captured
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        // The precision of the captured coordinates
        public static final String COLUMN_PRECISION = "precision";

        // Timestamp for the location
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
