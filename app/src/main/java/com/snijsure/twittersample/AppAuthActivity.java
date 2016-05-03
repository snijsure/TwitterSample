package com.snijsure.twittersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;

/**
 * Created by subodhnijsure on 5/3/16.
 */
public class AppAuthActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        AuthCallback callback = ((TwitterApp) getApplication()).getAuthCallback();
        if ( digitsButton != null ) {
            digitsButton.setCallback(callback);
            digitsButton.setAuthTheme(R.style.CustomDigitsTheme);
        }
    }
}
