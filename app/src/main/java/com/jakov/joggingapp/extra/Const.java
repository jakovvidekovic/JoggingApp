package com.jakov.joggingapp.extra;


import com.parse.ParseGeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class Const {
    public static final SimpleDateFormat sdfTime= new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat sdfDate= new SimpleDateFormat("dd.MM.yyyy.");

    public static final String SHARED_PREFS_KEY="com.jakov.joggingapp";
    public static final String PREFS_UPDATE_KEY="com.jakov.joggingapp.update_key";
    public static final String PREFS_STARTED="com.jakov.joggingapp.started_tracking";
    public static final String PREFS_CURRENT_RUN="com.jakov.joggingapp.current_tracking_run";

    public static final long UPDATE_INTERVAL=1000*9;
    public static final long FAST_UPDATE_INTERVAL=1000*7;

    public static final int NOTIFICATION_ID=5478;


    public static final Date getTime(List<Coordinate> coordinates){
        Date dateTime= new Date(0);
        if(coordinates.size()>1){
            Date startTime=coordinates.get(0).getTime();
            Date lastTime=coordinates.get(coordinates.size()-1).getTime();
            dateTime= new Date(lastTime.getTime()-startTime.getTime());
        }
        return dateTime;
    }
    public static final double getDistance(List<Coordinate> coordinates) {
        double distance = 0;
        if (coordinates.size() > 1)
            for (int i=1;i<coordinates.size();i++) {
                ParseGeoPoint point1= coordinates.get(i-1).getGeoPoint();
                ParseGeoPoint point2= coordinates.get(i).getGeoPoint();
                distance+=point1.distanceInKilometersTo(point2);
            }
        return distance;
    }

    public static final double getAverageSpeed(double distance,Date time){
        double hours = (double) time.getTime() / (1000 * 60 * 60);
        return distance / hours;
    }

}
