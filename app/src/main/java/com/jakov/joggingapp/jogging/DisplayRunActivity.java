package com.jakov.joggingapp.jogging;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Coordinate;
import com.jakov.joggingapp.extra.Run;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.List;

public class DisplayRunActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_run);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

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


    private void setUpMap(){
        final PolylineOptions polylineOptions= new PolylineOptions();
        String runId=getIntent().getExtras().getString(Run.KEY_ARGS);
        ParseQuery<Run> query= ParseQuery.getQuery(Run.class);
        query.fromLocalDatastore();
        query.getInBackground(runId,new GetCallback<Run>() {
            @Override
            public void done(Run run, ParseException e) {
                if(e==null){
                    ParseQuery<Coordinate> coordinateQuery= ParseQuery.getQuery(Coordinate.class);
                    coordinateQuery.fromLocalDatastore();
                    coordinateQuery.whereEqualTo(Coordinate.C_RUN, run);
                    coordinateQuery.addAscendingOrder(Coordinate.C_TIME);
                    coordinateQuery.findInBackground(new FindCallback<Coordinate>() {
                        @Override
                        public void done(List<Coordinate> coordinates, ParseException e) {
                            if(e==null){
                                for(Coordinate c:coordinates){
                                    ParseGeoPoint gp=c.getGeoPoint();
                                    polylineOptions.add(new LatLng(gp.getLatitude(),gp.getLongitude()));
                                }
                                mMap.addPolyline(polylineOptions);
                                if(coordinates.size()>1){
                                    ParseGeoPoint first=coordinates.get(0).getGeoPoint();
                                    ParseGeoPoint last= coordinates.get(coordinates.size()-1).getGeoPoint();
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(first.getLatitude(),first.getLongitude())).title("Start"));
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(last.getLatitude(), last.getLongitude())).title("Finish"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(first.getLatitude(),first.getLongitude()),14));
                                }

                            }
                        }
                    });
                }
            }
        });

    }
}
