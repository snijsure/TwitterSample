package com.snijsure.twittersample;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

/**
 * Created by subodhnijsure on 3/15/16.
 */
class TwitterApp extends Application {
    private static final String TAG = "TwitterApp";
    private TwitterFeedManager mStreamLoader = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onCofigurationChanged");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

    }

    public TwitterFeedManager getTask() {
        if (mStreamLoader == null) {
            mStreamLoader = new TwitterFeedManager(this);
        }
        return mStreamLoader;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
    }
}
