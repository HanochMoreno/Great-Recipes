package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.bus.OnRegisterEvent;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.squareup.otto.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_email;
    private EditText et_username;
    private EditText et_password;
    private EditText et_retypePassword;
    private TextView tv_alreadyHaveAnAccountOrRegister;
    private View tv_haveAccountOrRegisterClickHere;
    private View tv_testAppClickHere;
    private View btn_loginOrRegister;
    private int action;
    private GreatRecipesDbManager dbManager;

    private String email;
    private String username;
    private String password;
    private String retypePassword;
    private MyBus bus;
    private View til_email;
    private View til_retypePassword;
    private ProgressDialog progressDialog;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_register);

        dbManager = GreatRecipesDbManager.getInstance();
        bus = MyBus.getInstance();
        bus.register(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        action = AppConsts.Actions.ACTION_REGISTER;

        til_email = findViewById(R.id.til_email);
        til_retypePassword = findViewById(R.id.til_retypePassword);

        et_email = (EditText) findViewById(R.id.et_email);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_retypePassword = (EditText) findViewById(R.id.et_retypePassword);

        if (BuildConfig.DEBUG) {
            et_email.setText("han001@test.com");
            et_username.setText("han001");
            et_password.setText("123456");
            et_retypePassword.setText("123456");
        }

        tv_alreadyHaveAnAccountOrRegister = (TextView) findViewById(R.id.tv_alreadyHaveAnAccountOrRegister);

        tv_haveAccountOrRegisterClickHere = findViewById(R.id.tv_haveAccountOrRegisterClickHere);
        tv_testAppClickHere = findViewById(R.id.tv_testAppClickHere);
        btn_loginOrRegister = findViewById(R.id.btn_loginOrRegister);

        tv_haveAccountOrRegisterClickHere.setOnClickListener(this);
        tv_testAppClickHere.setOnClickListener(this);
        btn_loginOrRegister.setOnClickListener(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bus.unregister(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putInt("action", action);

        outState.putString("email", email);
        outState.putString("username", username);
        outState.putString("password", password);
        outState.putString("retypePassword", retypePassword);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        action = savedInstanceState.getInt("action");

        email = savedInstanceState.getString("email");
        username = savedInstanceState.getString("username");
        password = savedInstanceState.getString("password");
        retypePassword = savedInstanceState.getString("retypePassword");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_haveAccountOrRegisterClickHere:
                if (action == AppConsts.Actions.ACTION_REGISTER) {
                    showLoginScreen();
                } else {
                    showRegisterScreen();
                }
                break;

            case R.id.tv_testAppClickHere:
                // TODO: implementation
                break;

            case R.id.btn_loginOrRegister:
                onContinueButtonClicked();
                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showLoginScreen() {
        til_email.setVisibility(View.GONE);
        til_retypePassword.setVisibility(View.GONE);
        tv_alreadyHaveAnAccountOrRegister.setText(R.string.open_account_for_free);
        action = AppConsts.Actions.ACTION_LOGIN;
    }

//-------------------------------------------------------------------------------------------------

    private void showRegisterScreen() {
        til_email.setVisibility(View.VISIBLE);
        til_retypePassword.setVisibility(View.VISIBLE);
        tv_alreadyHaveAnAccountOrRegister.setText(R.string.already_have_an_account);
        action = AppConsts.Actions.ACTION_REGISTER;
    }

//-------------------------------------------------------------------------------------------------

    private void onContinueButtonClicked() {
        if (areFieldsValidated()) {
            progressDialog.show();

            switch (action) {

                case AppConsts.Actions.ACTION_REGISTER:
                    dbManager.register(email, username, password);
                    break;

                case AppConsts.Actions.ACTION_LOGIN:
                    dbManager.login(username, password);
                    break;
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    private boolean areFieldsValidated() {

        View mainView = findViewById(android.R.id.content);

        username = et_username.getText().toString();
        password = et_password.getText().toString();

        if (action == AppConsts.Actions.ACTION_REGISTER) {
            email = et_email.getText().toString();

            Pattern emailPattern = Pattern.compile(AppConsts.Regex.EMAIL_PATTERN);
            Matcher emailMatcher = emailPattern.matcher(email);
            if (!emailMatcher.matches()) {
                AppHelper.showSnackBar(mainView, R.string.invalid_email, Color.RED);
                return false;
            }
        }

        char[] chars = username.toCharArray();
        for (char aChar : chars) {
            String sChar = String.valueOf(aChar);
            if (!AppConsts.Regex.USERNAME_PATTERN.contains(sChar)) {
                AppHelper.showSnackBar(mainView, R.string.invalid_username, Color.RED);
                return false;
            }
        }

        chars = password.toCharArray();
        for (char aChar : chars) {
            String sChar = String.valueOf(aChar);
            if (!AppConsts.Regex.PASSWORD_PATTERN.contains(sChar)) {
                AppHelper.showSnackBar(mainView, R.string.invalid_password, Color.RED);
                return false;
            }
        }

        if (action == AppConsts.Actions.ACTION_REGISTER) {
            retypePassword = et_retypePassword.getText().toString();

            if (!password.equals(retypePassword)) {
                AppHelper.showSnackBar(mainView, R.string.passwords_are_not_identical, Color.RED);
                return false;
            }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnLoginEvent event) {
        onLoginOrRegisterResponse(event.isSuccess, event.t);
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnRegisterEvent event) {
        onLoginOrRegisterResponse(event.isSuccess, event.t);
    }

//-------------------------------------------------------------------------------------------------

    private void onLoginOrRegisterResponse(boolean isSuccess, Throwable t) {
        progressDialog.dismiss();

        if (isSuccess) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            User user = AppStateManager.getInstance().user;

            editor.putString(AppConsts.SharedPrefs.USER_NAME, user.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, user.password);
            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            View mainView = findViewById(android.R.id.content);
            AppHelper.onApiErrorReceived(t, mainView);
        }
    }

}
