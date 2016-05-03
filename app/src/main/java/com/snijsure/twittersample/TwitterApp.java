package com.snijsure.twittersample;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.fabric.sdk.android.Fabric;

/**
 * Created by subodhnijsure on 3/15/16.
 */
class TwitterApp extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TAG = "TwitterApp";
    private TwitterFeedManager mStreamLoader = null;
    private AuthCallback authCallback;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onCofigurationChanged");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SessionManager loginSession = new SessionManager(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                this.getResources().getString(R.string.fabric_twitter_key),
                this.getResources().getString(R.string.fabric_twitter_secret));

        Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig), new Digits());
        Debug.startMethodTracing("startup");
        Log.d(TAG, "onCreate");
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // Do something with the session
                // TODO: associate the session userID with your user model
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Authentication successful for "
                            + phoneNumber, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Authentication successful for " +
                                    session.getPhoneNumber(),
                            Toast.LENGTH_LONG).show();
                }
                loginSession.createLoginSession(session.getPhoneNumber(),session.getEmail().toString());
                Intent i = new Intent(getApplicationContext(), TwitterActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Staring Login Activity
                getApplicationContext().startActivity(i);
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        };
    }

    public AuthCallback getAuthCallback(){
        return authCallback;
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
