package com.perficient.meetingschedulear;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;


public class BaseApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context sContext;

    private static Resources sResources;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sResources = getResources();
    }

    public static Context getContextObject() {
        return sContext;
    }

    public static Resources getResourcesObject() {
        return sResources;
    }
}
