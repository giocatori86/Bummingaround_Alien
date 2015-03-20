package com.example.iwa.pollutiontracking;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.iwa.pollutiontracking.data.PollutionTrackingContract;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Vector;

public class LocationHistoryActivity extends FragmentActivity {

    private static final String TAG = "LocationHistoryActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Vector<LatLng> coordinateVector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_history);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private Vector<LatLng> addPointsOnMap() {
        Log.v(TAG, "loading all position from the DB into the map");
        Cursor cursor = getContentResolver().query(
                PollutionTrackingContract.LocationEntry.CONTENT_URI,
                new String[]{
                        PollutionTrackingContract.LocationEntry._ID,
                        PollutionTrackingContract.LocationEntry.COLUMN_COORD_LAT,
                        PollutionTrackingContract.LocationEntry.COLUMN_COORD_LONG,
                        PollutionTrackingContract.LocationEntry.COLUMN_PRECISION,
                        PollutionTrackingContract.LocationEntry.COLUMN_TIMESTAMP,
                },
                null,
                null,
                PollutionTrackingContract.LocationEntry.COLUMN_TIMESTAMP
        );

        Vector<LatLng> coordinateVector = new Vector<>();

        while (cursor.moveToNext()) {
            /*Log.v(TAG, "position id(" + cursor.getInt(0) + "), " +
                    "lat(" + cursor.getFloat(1) + "), " +
                    "long(" + cursor.getFloat(2) + "), " +
                    "precision(" + cursor.getFloat(3) + "), " +
                    "timestamp(" + cursor.getLong(4) + ")" );*/

            Location location = new Location(PollutionTrackingContract.LocationEntry.CONTENT_URI.toString());
            location.setLatitude(cursor.getFloat(1));
            location.setLongitude(cursor.getFloat(2));
            location.setAccuracy(cursor.getInt(3));
            location.setTime(cursor.getLong(4));

            LatLng markerCenter = new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            );
            coordinateVector.add(markerCenter);

            /*mMap.addMarker(new MarkerOptions()
                            .position(markerCenter)
                            .title(String.valueOf(location.getTime()))
            );*/

            /*
            mMap.addCircle(new CircleOptions()
                    .center(markerCenter)
                    .radius(location.getAccuracy())
                    .fillColor(R.color.accent_material_light)
                    .strokeColor(android.R.color.transparent)
            );
            */
        }

        mMap.addPolyline(new PolylineOptions()
                        .addAll(coordinateVector)
                        .color(R.color.accent_material_light)
        );

        cursor.close();

        return coordinateVector;
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        coordinateVector = addPointsOnMap();

        mMap.setMyLocationEnabled(true);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                centerMapOnMarkers();
            }
        });
    }

    private void centerMapOnMarkers() {
        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng latLng : coordinateVector) {
            b.include(latLng);
        }
        LatLngBounds bounds = b.build();
        //Change the padding as per needed
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cu);

    }

}
