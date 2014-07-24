package com.jakov.joggingapp.extra;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("Coordinate")
public class Coordinate extends ParseObject{
    public static final String C_GEO_POINT="geoPoint";
    public static final String C_TIME="time";
    public static final String C_RUN="run";

    public ParseGeoPoint getGeoPoint(){return getParseGeoPoint(C_GEO_POINT);}
    public void setGeoPoint(double lat,double lng){put(C_GEO_POINT,new ParseGeoPoint(lat,lng));}
    public String getRunId(){return getString(C_RUN);}
    public void setRun(Run run){put(C_RUN,run);}
    public String getId(){return getObjectId();}
    public void setTime(Date date){put(C_TIME,date);}
    public Date getTime(){return getDate(C_TIME);}

    public Coordinate(){}
    public Coordinate(double lat,double lng,Run run){
        setGeoPoint(lat,lng);
        setRun(run);
    }
}
