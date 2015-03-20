package com.example.iwa.pollutiontracking;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;

import com.example.iwa.pollutiontracking.data.PollutionTrackingContract;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BummingDataService extends IntentService {
    private static final String FIND_PLACES_URL = "http://94.23.215.145:27018/find_places/";
    private static final String LOG_TAG = "BummingDataService";

    // parameters
    private static final String EXTRA_PATH = "com.example.iwa.pollutiontracking.extra.PATH";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void StartDownloadVenues(Context context, ArrayList<Integer> pathIDs) {
        Intent intent = new Intent(context, BummingDataService.class);
        intent.putIntegerArrayListExtra(EXTRA_PATH, pathIDs);
        context.startService(intent);
    }

    public static void StartDownloadVenues(Context context) {
        Intent intent = new Intent(context, BummingDataService.class);
        intent.putIntegerArrayListExtra(EXTRA_PATH, new ArrayList<Integer>());
        context.startService(intent);
    }

    public BummingDataService() {
        super("BummingDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ArrayList<Integer> pathIDs = intent.getIntegerArrayListExtra(EXTRA_PATH);
            final List<PointF> path = getPathFromIds(pathIDs);
            handlePoints(path);
        }
    }

    private List<PointF> getPathFromIds(List<Integer> pathIds) {
        List<PointF> path = new ArrayList<>(pathIds.size());

        if (pathIds.size() == 0) {

            Cursor cursor = getContentResolver().query(
                    PollutionTrackingContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            PollutionTrackingContract.LocationEntry.COLUMN_COORD_LAT,
                            PollutionTrackingContract.LocationEntry.COLUMN_COORD_LONG,
                    },
                    null,
                    null,
                    PollutionTrackingContract.LocationEntry.COLUMN_TIMESTAMP
            );


            while(cursor.moveToNext()) {
                float lat, lon;
                lat = cursor.getFloat(0);
                lon = cursor.getFloat(1);
                path.add(new PointF(lat, lon));
            }

            cursor.close();

        } else for (int id: pathIds) {
            float lat, lon;

            Cursor cursor = getContentResolver().query(
                    PollutionTrackingContract.LocationEntry.buildLocationUri(id),
                    new String[]{
                            PollutionTrackingContract.LocationEntry._ID,
                            PollutionTrackingContract.LocationEntry.COLUMN_COORD_LAT,
                            PollutionTrackingContract.LocationEntry.COLUMN_COORD_LONG,
                    },
                    null,
                    null,
                    PollutionTrackingContract.LocationEntry.COLUMN_TIMESTAMP
            );

            if (BuildConfig.DEBUG && cursor.getCount() != 1)
                throw new AssertionError("cursor count (" + cursor.getCount() + ") cannot be different than 1");


            cursor.moveToNext();
            lat = cursor.getFloat(1);
            lon = cursor.getFloat(2);
            cursor.close();

            path.add(new PointF(lat, lon));
        }
        return path;
    }


    private void deleteOldVenues() {
        getContentResolver().delete(PollutionTrackingContract.VenueEntry.CONTENT_URI,
                null,
                null
        );
    }

    /**
     * Handle the path download in the provided background thread.
     * @param path
     */
    private void handlePoints(List<PointF> path) {

        // delete all old venues
        deleteOldVenues();

        // get new data
        for (PointF point: path) {
            List<String> result = downloadVenues(point);
            if (result != null)
                for (String line : result)
                    Log.i(LOG_TAG, line);
        }
    }

    private JSONObject createPayload(List<PointF> path) throws JSONException {

        JSONArray jPath = new JSONArray();
        for (PointF point: path) {
            JSONObject jpoint = new JSONObject();
            jpoint.put("lat", point.x);
            jpoint.put("lon", point.y);
            jPath.put(jpoint);
        }

        JSONObject payload = new JSONObject();
        payload.put("path", jPath);
        return payload;
    }

    private List<String> downloadVenues(PointF point) {
        ArrayList<PointF> monoList = new ArrayList<>();
        monoList.add(point);
        return downloadVenues(monoList);
    }

    private List<String> downloadVenues(List<PointF> path) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (path.size() == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String venuesJsonStr = null;

        try {

            HttpPost httpPost = new HttpPost(FIND_PLACES_URL);
            InputStream inputStream;
            try {
                JSONObject json = createPayload(path);
                if (json != null){
                    // Set json to StringEntity
                    StringEntity se = new StringEntity(json.toString());

                    // Set httpPost Entity
                    httpPost.setEntity(se);
                }
                // Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // Execute POST request to the given URL
                HttpResponse httpResponse = (new DefaultHttpClient()).execute(httpPost);

                Header[] headers = httpResponse.getHeaders("Set-Cookie");
                inputStream = httpResponse.getEntity().getContent();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // Read the input stream into a String
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            venuesJsonStr = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getVenuesDataFromJson(venuesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private List<String> getVenuesDataFromJson(String venuesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.

        final String ADDRESS_FIELD = "address";
        final String LONGITUDE_FIELD = "long";
        final String LATITUDE_FIELD = "lat";
        final String NAME_FIELD = "name";
        final String ID_FIELD = "id";

        Vector<ContentValues> cVVector = new Vector<>();
        List<String> resultStrs = new Vector<>();

        JSONObject venuesJson = new JSONObject(venuesJsonStr);
        Iterator<String> iter = venuesJson.keys();
        for(int i=0; iter.hasNext(); i++) {
            String id = iter.next();

            JSONObject venueJson = venuesJson.getJSONObject(id);

            String name, address;
            double lat, lon;

            name = venueJson.getString(NAME_FIELD);
            address = venueJson.getString(ADDRESS_FIELD);
            lat = venueJson.getDouble(LATITUDE_FIELD);
            lon = venueJson.getDouble(LONGITUDE_FIELD);


            ContentValues venueValues = new ContentValues();

            venueValues.put(PollutionTrackingContract.VenueEntry.COLUMN_URI, id);
            venueValues.put(PollutionTrackingContract.VenueEntry.COLUMN_NAME, name);
            venueValues.put(PollutionTrackingContract.VenueEntry.COLUMN_ADDRESS, address);
            venueValues.put(PollutionTrackingContract.VenueEntry.COLUMN_COORD_LAT, lat);
            venueValues.put(PollutionTrackingContract.VenueEntry.COLUMN_COORD_LONG, lon);

            cVVector.add(venueValues);

            resultStrs.add(id + " - " + name + " - " + lat + ":" + lon);
        }

        if ( cVVector.size() > 0) {
            ContentValues[] contentValues = new ContentValues[cVVector.size()];
            cVVector.toArray(contentValues);
            getContentResolver().bulkInsert(PollutionTrackingContract.VenueEntry.CONTENT_URI, contentValues);
        }

        Log.d(LOG_TAG, "FetchVenueTask Complete. " + cVVector.size() + " Inserted");

        return resultStrs;
    }

}
