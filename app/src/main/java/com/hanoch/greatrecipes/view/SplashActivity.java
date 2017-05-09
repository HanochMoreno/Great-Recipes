package com.hanoch.greatrecipes.view;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.crashlytics.android.Crashlytics;
import com.hanoch.greatrecipes.utilities.MyFonts;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    private int progressStatus;
    private Thread splash;
    private boolean continueRunning;
    private Handler handler;
    private ProgressBar progressBar;


//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());

//        if (BuildConfig.DEBUG) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }

        setContentView(R.layout.activity_splash);

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
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        });

        splash.start();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();

        continueRunning = false;
        splash.interrupt();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
    }

}
