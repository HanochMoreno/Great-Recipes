package com.hanoch.greatrecipes.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.R;
import com.crashlytics.android.Crashlytics;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.squareup.otto.Subscribe;

import io.fabric.sdk.android.Fabric;


public class SplashActivity extends AppCompatActivity {

    private int progressStatus;
    private Thread splash;
    private boolean continueRunning;
    private boolean isGotLoginResponse;
    private boolean isUserLoggedIn;
    private Handler handler;
    private ProgressBar progressBar;
    private MyBus bus;


//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        GreatRecipesDbManager dbManager = GreatRecipesDbManager.getInstance();
        bus = MyBus.getInstance();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sp.getString(AppConsts.SharedPrefs.USER_NAME, null);
        String password = sp.getString(AppConsts.SharedPrefs.PASSWORD, null);

        if (username == null || password == null) {
            isGotLoginResponse = true;
            isUserLoggedIn = false;
        } else {
            isGotLoginResponse = false;
            dbManager.login(username, password);
        }
//        if (BuildConfig.DEBUG) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }

        setContentView(R.layout.activity_splash);

        // Preventing the user from rotating the screen:
        if (getResources().getBoolean(R.bool.isTablet)) {

            if (getResources().getBoolean(R.bool.isSmallTablet)) {

                // Small tablet (7")
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            } else {
                // Large tablet (>= 10")
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        } else {
            // Phone
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        TextView textView_greatRecipes = (TextView) findViewById(R.id.textView_greatRecipes);
        textView_greatRecipes.setTypeface(MyFonts.getInstance(this).getMotionPictureFont());

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        handler = new Handler();
        progressStatus = 0;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);

        continueRunning = true;

        splash = new Thread(() -> {

            while (progressStatus < 100) {

                if (!continueRunning) return;

                progressStatus += 1;
                handler.post(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!continueRunning) return;

            if (progressStatus == 100) {
                if (isUserLoggedIn) {
                    goToMainMenuScreen();
                } else if (isGotLoginResponse) {
                    goToRegisterScreen();
                }
            }
        });

        splash.start();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();

        bus.unregister(this);

        continueRunning = false;
        splash.interrupt();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnLoginEvent event) {
        isGotLoginResponse = true;

        if (event.isSuccess) {
            isUserLoggedIn = true;

            if (progressStatus == 100) {
                goToMainMenuScreen();
            }
        } else {
            isUserLoggedIn = false;
        }
    }

//-------------------------------------------------------------------------------------------------

    private void goToMainMenuScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

//-------------------------------------------------------------------------------------------------

    private void goToRegisterScreen() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
