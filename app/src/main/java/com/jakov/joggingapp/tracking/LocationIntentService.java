package com.jakov.joggingapp.tracking;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.LocationClient;
import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.extra.Coordinate;
import com.jakov.joggingapp.extra.Run;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationIntentService extends IntentService {

    public LocationIntentService() {
        super("LocationIntentService");
    }

    SharedPreferences preferences;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(LocationClient.KEY_LOCATION_CHANGED)) {
                Bundle b = intent.getExtras();
                Location loc = (Location) b.get(LocationClient.KEY_LOCATION_CHANGED);
                preferences = getSharedPreferences(Const.SHARED_PREFS_KEY, MODE_PRIVATE);
                saveLocation(loc);
                showNotification();

            }
        }
    }

    private void showNotification() {
        String runId = preferences.getString(Const.PREFS_CURRENT_RUN, "");
        ParseQuery<Run> runQuery= ParseQuery.getQuery(Run.class);
        runQuery.fromLocalDatastore();
        runQuery.getInBackground(runId,new GetCallback<Run>() {
            @Override
            public void done(Run run, ParseException e) {
                if(e==null){
                    ParseQuery<Coordinate> query = new ParseQuery<Coordinate>(Coordinate.class);
                    query.fromLocalDatastore();
                    query.whereEqualTo(Coordinate.C_RUN, run);
                    query.findInBackground(new FindCallback<Coordinate>() {
                        @Override
                        public void done(List<Coordinate> coordinates, ParseException e) {
                            if (e == null) {
                                double distance=Const.getDistance(coordinates);
                                String time=Const.sdfTime.format(Const.getTime(coordinates));
                                buildNotification(distance,time);
                            }else{
                                Log.e("service","didnt find coord "+e.getMessage());
                            }
                        }
                    });
                }
            }
        });



    }

    private void buildNotification(double distance, String time) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setOngoing(true);
        builder.setContentText("Distance: " + String.format(Locale.getDefault(),"%1$.2f",distance) + "km Time:" + time + "h");
        builder.setContentTitle("Jogging");
        builder.setSmallIcon(R.drawable.ic_noftification_icon);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent resultIntent = new Intent(this, TrackingActivity.class);
        stackBuilder.addParentStack(TrackingActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Const.NOTIFICATION_ID, builder.build());


    }


    private void saveLocation(final Location location) {

        String runId = preferences.getString(Const.PREFS_CURRENT_RUN, "");
        if (!runId.isEmpty()) {
            ParseQuery<Run> query = ParseQuery.getQuery(Run.class);
            query.fromLocalDatastore();
            query.getInBackground(runId, new GetCallback<Run>() {
                @Override
                public void done(Run run, ParseException e) {
                    if (e == null) {
                        Coordinate coord = new Coordinate(location.getLatitude(), location.getLongitude(), run);
                        coord.setTime(new Date());
                        coord.pinInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                } else
                                    Log.e("service", "cant add coord: " + e.getMessage());
                            }

                        });
                    } else
                        Log.e("service", "cant find: " + e.getMessage());
                }
            });
        }
    }

}
