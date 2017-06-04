package com.hanoch.greatrecipes.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.crashlytics.android.Crashlytics;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnAppDataEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.squareup.otto.Subscribe;

import io.fabric.sdk.android.Fabric;


public class SplashActivity extends AppCompatActivity {

    public static final int NO_ERROR = 0;
    public static final int ERROR_GET_APP_DATA_FAILED = 1;
    public static final int ERROR_LOGIN_FAILED = 2;

    private int progressStatus;
    private Thread splash;
    private boolean continueRunning;
    private boolean isGotLoginResponse;
    private boolean isGotAppDataResponse;
    private Handler handler;
    private ProgressBar progressBar;
    private MyBus bus;
    private AppStateManager appStateManager;
    private ApisManager apisManager;
    private int errorToDisplay;
    private TextView tv_progressBarText;


//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        apisManager = ApisManager.getInstance();
        appStateManager = AppStateManager.getInstance();
        bus = MyBus.getInstance();

        if (appStateManager.appData == null) {
            apisManager.getAppData();
        } else {
            onEvent(new OnAppDataEvent(true, null));
        }

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

        tv_progressBarText = (TextView) findViewById(R.id.tv_progressBarText);

        handler = new Handler();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);

        startProgressBar();
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

    private void startProgressBar () {
        progressStatus = 0;
        continueRunning = true;

        splash = new Thread(() -> {

            while (progressStatus < 100) {

                if (!continueRunning) return;

                progressStatus += 1;
                handler.post(() -> {
                    progressBar.setProgress(progressStatus);

                    if (!isGotAppDataResponse) {
                        tv_progressBarText.setText(R.string.downloading_app_data);
                    } else if (!isGotLoginResponse) {
                        tv_progressBarText.setText(R.string.downloading_login_info);
                    } else {
                        tv_progressBarText.setText(R.string.init_app);
                    }
                });

                try {
                    if (isGotAppDataResponse && isGotLoginResponse) {
                        Thread.sleep(30);
                    } else {
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!continueRunning) return;

            if (progressStatus == 100) {
                if (errorToDisplay != NO_ERROR) {
                    handler.post(() -> showErrorDialog(errorToDisplay));
                } else if (appStateManager.appData != null && appStateManager.user != null) {
                    goToMainMenuScreen();
                } else if (isGotAppDataResponse && isGotLoginResponse) {
                    goToRegisterScreen();
                }
            }
        });

        splash.start();
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnAppDataEvent event) {
        isGotAppDataResponse = true;

        if (event.isSuccess) {
            errorToDisplay = NO_ERROR;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String email = sp.getString(AppConsts.SharedPrefs.EMAIL, null);
            String password = sp.getString(AppConsts.SharedPrefs.PASSWORD, null);

            if (email == null || password == null) {
                isGotLoginResponse = true;
            } else {
                isGotLoginResponse = false;
                apisManager.login(this, email, password);
            }
        } else if (progressStatus == 100) {
            showErrorDialog(ERROR_GET_APP_DATA_FAILED);
        } else {
            isGotLoginResponse = true;
            errorToDisplay = ERROR_GET_APP_DATA_FAILED;
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnLoginEvent event) {
        isGotLoginResponse = true;

        if (event.isSuccess) {
            errorToDisplay = NO_ERROR;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            User user = appStateManager.user;
            editor.putString(AppConsts.SharedPrefs.EMAIL, user.preferences.email);
            editor.putString(AppConsts.SharedPrefs.USER_NAME, user.preferences.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, user.preferences.password);
            editor.apply();

            if (progressStatus == 100) {
                goToMainMenuScreen();
            }
        } else if (progressStatus == 100) {
            showErrorDialog(ERROR_LOGIN_FAILED);
        } else {
            errorToDisplay = ERROR_LOGIN_FAILED;
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showErrorDialog(int action) {

        Dialog errorDialog = new Dialog(this);
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        errorDialog.setCancelable(false);
        errorDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_splash_error, null, false);
        errorDialog.setContentView(view);

        TextView tv_dialogBodyText = (TextView) errorDialog.findViewById(R.id.tv_dialogBodyText);
        View button_retry = errorDialog.findViewById(R.id.button_retry);
        Button button_leave = (Button) errorDialog.findViewById(R.id.button_leave);

        switch (action) {
            case ERROR_GET_APP_DATA_FAILED:
                tv_dialogBodyText.setText(R.string.error_getting_app_data);
                break;

            case ERROR_LOGIN_FAILED:
                tv_dialogBodyText.setText(R.string.error_getting_login_info);
                break;
        }

        button_retry.setOnClickListener(v -> {

            startProgressBar();

            switch (action) {
                case ERROR_GET_APP_DATA_FAILED:
                    apisManager.getAppData();
                    break;

                case ERROR_LOGIN_FAILED:
                    onEvent(new OnAppDataEvent(true, null));
                    break;
            }

            errorDialog.dismiss();
        });

        button_leave.setOnClickListener(v -> {
            switch (action) {
                case ERROR_GET_APP_DATA_FAILED:
                    finish();
                    break;

                case ERROR_LOGIN_FAILED:
                    goToRegisterScreen();
                    break;
            }

            errorDialog.dismiss();
        });

        errorDialog.show();
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
