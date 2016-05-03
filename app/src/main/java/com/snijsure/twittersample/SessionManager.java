package com.snijsure.twittersample;

/**
 * Created by subodhnijsure on 5/3/16.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Context
    Context mContext;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "TwitterAppPreference";

    // All Shared Preferences Keys
    private static final String USER_LOGGED_IN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_PHONE_NUMBER = "phone";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Constructor
    public SessionManager(Context context) {
        this.mContext = context;
        pref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    /**
     * Create login session
     */
    public void createLoginSession(String phone, String email) {
        Editor editor;
        // Storing login value as TRUE
        editor = pref.edit();
        editor.putBoolean(USER_LOGGED_IN, true);

        // Storing name in pref
        editor.putString(KEY_PHONE_NUMBER, phone);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.apply();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(mContext, AppAuthActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Staring Login Activity
            mContext.startActivity(i);
        }

    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        Editor editor;
        editor = pref.edit();

        // Clearing all data from Shared Preferences
        editor.clear();
        editor.apply();

        // After logout redirect user to Login Activity
        Intent i = new Intent(mContext, AppAuthActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        mContext.startActivity(i);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(USER_LOGGED_IN, false);
    }
}