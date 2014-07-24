package com.jakov.joggingapp.tracking;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.extra.Coordinate;
import com.jakov.joggingapp.extra.Run;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TrackingActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {
    Button btnStartTracking, btnStopTracking;
    TextView tvStatus, tvDistance, tvAvSpeed, tvTime;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        initLocationClient();
        init();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences != null) {
            Log.e("service","onresume");
            started = preferences.getBoolean(Const.PREFS_STARTED, false);
            started(started);
        }
    }

    private void started(boolean started) {
        btnStartTracking.setEnabled(!started);
        btnStopTracking.setEnabled(started);
        if (started) {
            updateTextView();
        }
        if (!started) {
            tvStatus.setText("Service not started");
            tvTime.setVisibility(View.GONE);
            tvDistance.setVisibility(View.GONE);
            tvAvSpeed.setVisibility(View.GONE);
        }
    }

    private void updateTextView() {
        tvStatus.setText("Tracking");
        tvTime.setVisibility(View.VISIBLE);
        tvDistance.setVisibility(View.VISIBLE);
        tvAvSpeed.setVisibility(View.VISIBLE);
        String runId = preferences.getString(Const.PREFS_CURRENT_RUN, "");
        ParseQuery<Run> runQuery= new ParseQuery<Run>(Run.class);
        runQuery.fromLocalDatastore();
        runQuery.getInBackground(runId,new GetCallback<Run>() {
            @Override
            public void done(Run run, ParseException e) {
                if(e==null){
                    ParseQuery<Coordinate> query = new ParseQuery<Coordinate>(Coordinate.class);
                    query.fromLocalDatastore();
                    query.whereEqualTo(Coordinate.C_RUN,run);
                    query.addAscendingOrder(Coordinate.C_TIME);
                    query.findInBackground(new FindCallback<Coordinate>() {
                        @Override
                        public void done(List<Coordinate> coordinates, ParseException e) {
                            if (e == null) {
                                Date time = Const.getTime(coordinates);
                                double distance = Const.getDistance(coordinates);
                                tvTime.setText("Time: " + Const.sdfTime.format(time) + "h");
                                tvDistance.setText("Distance: " + String.format(Locale.getDefault(), "%1$.2f", distance) + "km");
                                tvAvSpeed.setText("Average Speed: " + String.format(Locale.getDefault(), "%1$.2f", Const.getAverageSpeed(distance, time)) + "km/h");
                            }
                        }
                    });
                }
            }
        });

    }

    private void init() {
        preferences = getSharedPreferences(Const.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        editor = preferences.edit();
        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);
        btnStartTracking.setOnClickListener(this);
        btnStopTracking = (Button) findViewById(R.id.btnStopTracking);
        btnStopTracking.setOnClickListener(this);

        tvStatus = (TextView) findViewById(R.id.tvServiceInfo);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvAvSpeed = (TextView) findViewById(R.id.tvAverageSpeed);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartTracking:
                createNewRun();
                break;
            case R.id.btnStopTracking:
                stopPeriodicUpdates();
                saveRun();
                break;
        }
    }

    private void saveRun() {
        String runId = preferences.getString(Const.PREFS_CURRENT_RUN, "");
        ParseQuery<Run> runQuery= new ParseQuery<Run>(Run.class);
        runQuery.fromLocalDatastore();
        runQuery.getInBackground(runId,new GetCallback<Run>() {
            @Override
            public void done(final Run run, ParseException e) {
                if(e==null){
                    ParseQuery<Coordinate> query = new ParseQuery<Coordinate>(Coordinate.class);
                    query.fromLocalDatastore();
                    query.whereEqualTo(Coordinate.C_RUN,run);
                    query.addAscendingOrder(Coordinate.C_TIME);
                    query.findInBackground(new FindCallback<Coordinate>() {
                        @Override
                        public void done(List<Coordinate> coordinates, ParseException e) {
                            if (e == null) {
                                Date time = Const.getTime(coordinates);
                                double distance = Const.getDistance(coordinates);
                                updateNotification(distance,Const.sdfTime.format(time));
                                run.setTime(time);
                                run.setDistance(distance);
                                run.pinInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            Crouton.makeText(TrackingActivity.this,"Tracking saved",Style.CONFIRM).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateNotification(double distance, String time) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentText("Distance: " + String.format(Locale.getDefault(),"%1$.2f",distance) + "km Time:" + time + "h");
        builder.setContentTitle("Jogging Finished");
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

    public void createNewRun() {
        tvStatus.setText("Starting Tracking Service");
        final Run run = Run.create(Run.class);
        run.setDeleted(false);
        run.setTime(new Date(0));
        run.setDate(new Date());
        run.setUpdatedAt(new Date());
        run.setDistance(0);
        run.setUser(ParseUser.getCurrentUser());
        run.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    run.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                editor.putString(Const.PREFS_CURRENT_RUN, run.getObjectId()).commit();
                                startPeriodicUpdates();
                            }
                        }
                    });

                } else {
                    Log.e("starting service", e.getMessage());
                    Crouton.makeText(TrackingActivity.this, "Tracking not started. No internet connection", Style.ALERT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }


    //region Location Client part
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private PendingIntent mPendingIntent;

    private void initLocationClient() {
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(Const.UPDATE_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(Const.FAST_UPDATE_INTERVAL);

        mLocationClient = new LocationClient(this, this, this);
        Intent intent = new Intent(
                this, LocationIntentService.class);
        mPendingIntent =
                PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public void startPeriodicUpdates() {
        editor.putBoolean(Const.PREFS_STARTED, true).commit();
        started(true);
        if (servicesConnected())
            mLocationClient.requestLocationUpdates(mLocationRequest, mPendingIntent);
    }


    public void stopPeriodicUpdates() {
        editor.putBoolean(Const.PREFS_STARTED, false).commit();
        started(false);
        if (mLocationClient.isConnected() && mPendingIntent != null)
            mLocationClient.removeLocationUpdates(mPendingIntent);

    }
    //endregion


    //region Play Services part

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {

                connectionResult.startResolutionForResult(
                        this,
                        9000);

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {

            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {

        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                9000);

        if (errorDialog != null) {

            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            errorFragment.setDialog(errorDialog);

            errorFragment.show(getSupportFragmentManager(), "Jogging app");
        }
    }


    public static class ErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private boolean servicesConnected() {

        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), "Jogging app");
            }
            return false;
        }
    }
    //endregion
}
