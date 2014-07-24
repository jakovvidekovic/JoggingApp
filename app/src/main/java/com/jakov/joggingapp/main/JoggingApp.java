package com.jakov.joggingapp.main;


import android.app.Application;

import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Coordinate;
import com.jakov.joggingapp.extra.Run;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

public class JoggingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Run.class);
        ParseObject.registerSubclass(Coordinate.class);
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));
        ParseACL.setDefaultACL(new ParseACL(), true);
        ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));
    }
}
