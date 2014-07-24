package com.jakov.joggingapp.extra;


import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;

@ParseClassName("Run")
public class Run extends ParseObject implements Serializable {
    public static final String C_DISTANCE="distance";
    public static final String C_DATE="date";
    public static final String C_TIME="time";
    public static final String C_USER="user";
    public static final String C_UPDATED_AT="updateAt";
    public static final String C_DELETED="deleted";
    public static final String TABLE_NAME="Run";
    public static final String KEY_ARGS="com.jakov.joggingapp.run";


    public boolean isDeleted(){return getBoolean(C_DELETED);}
    public void setDeleted(boolean deleted){put(C_DELETED,deleted);}
    public Date getUpdatedAt(){return getDate(C_UPDATED_AT);}
    public void setUpdatedAt(Date date){put(C_UPDATED_AT,date);}
    public double getDistance(){
        return getDouble(C_DISTANCE);
    }
    public void setDistance(double distance){
        put(C_DISTANCE,distance);
    }
    public Date getDate(){
        return getDate(C_DATE);
    }
    public void setDate(Date date){
        put(C_DATE,date);
    }
    public void setTime(Date time){
        put(C_TIME,time);
    }
    public Date getTime(){
        return getDate(C_TIME);
    }
    public void setUser(ParseUser user){
        put(C_USER,user);
    }
    public ParseUser getUser(){
        return getParseUser(C_USER);
    }

    public Run(double distance,Date date, Date time,ParseUser user){
        put(C_DISTANCE,distance);
        put(C_DATE,date);
        put(C_TIME,time);
        put(C_USER,user);
    }
    public Run(){}
}
