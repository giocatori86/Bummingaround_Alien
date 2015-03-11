package com.example.iwa.pollutiontracking;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.example.iwa.pollutiontracking.data.PollutionTrackingContract;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void startLocationTrackingService() {
        Intent intent = new Intent(this, PositionTrackingService.class);
        startService(intent);
    }

    void stopLocationTrackingService() {
        Intent intent = new Intent(this, PositionTrackingService.class);
        stopService(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ToggleButton locationButton = (ToggleButton) rootView.findViewById(R.id.start_location_listener);
            locationButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (isChecked)
                        mainActivity.startLocationTrackingService();
                    else
                        mainActivity.stopLocationTrackingService();
                }
            });

            Button logPositionsButton = (Button) rootView.findViewById(R.id.log_positions_from_db);
            logPositionsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.v(TAG, "logging all the postions in the DB");
                    Cursor cursor = getActivity().getContentResolver().query(
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
                            null
                    );

                    while (cursor.moveToNext()) {
                        Log.v(TAG, "position id(" + cursor.getInt(0) + "), " +
                                "lat(" + cursor.getFloat(1) + "), " +
                                "long(" + cursor.getFloat(2) + "), " +
                                "precision(" + cursor.getFloat(3) + "), " +
                                "timestamp(" + cursor.getInt(4) + ")" );
                    }

                    cursor.close();
                }
            });

            Button showMapButton = (Button) rootView.findViewById(R.id.show_map);
            showMapButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LocationHistoryActivity.class);
                    startActivity(intent);
                }
            });
            return rootView;
        }
    }
}
