package com.perficient.meetingschedulear;

import android.app.Application;
import android.content.Context;

import org.artoolkit.ar6.base.assets.AssetHelper;

public class BaseApplication extends Application {

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getContext();
        this.initializeInstance();
    }

    // Here we do one-off initialisation which should apply to all activities
    // in the application.
    protected void initializeInstance() {

        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(this, "Data");
        assetHelper.cacheAssetFolder(this, "cparam_cache");
    }
}
