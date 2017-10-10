package com.perficient.meetingschedulear;

import android.app.Application;
import android.content.Context;


public class BaseApplication extends Application {

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getContext();
    }
}
