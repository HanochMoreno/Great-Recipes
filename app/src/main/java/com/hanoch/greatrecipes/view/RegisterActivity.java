package com.hanoch.greatrecipes.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnEmailVerificationEvent;
import com.hanoch.greatrecipes.bus.OnForgotPasswordEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.bus.OnMailWasSentEvent;
import com.hanoch.greatrecipes.bus.OnUpdateDeviceEvent;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Preferences;
import com.hanoch.greatrecipes.model.UserEmailVerification;
import com.squareup.otto.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.HttpException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_email;
    private EditText et_username;
    private EditText et_password;
    private EditText et_retypePassword;
    private TextView tv_alreadyHaveAnAccountOrRegister;
    private TextView tv_forgotYourPassword;
    private View tv_forgotYourPasswordClickHere;
    private View tv_mailWithPasswordWillBeSent;
    private View tv_haveAccountOrRegisterClickHere;
    private View tv_testAppClickHere;
    private View btn_proceed;
    private int action;
    private ApisManager apisManager;

    private String email;
    private String username;
    private String password;
    private String retypePassword;
    private MyBus bus;
    private View til_username;
    private View til_password;
    private View til_retypePassword;
    private ProgressDialog progressDialog;
    private AppStateManager appStateManager;
    private Dialog mailWasSentDialog;
    private View mainView;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_register);

        apisManager = ApisManager.getInstance();
        appStateManager = AppStateManager.getInstance();
        bus = MyBus.getInstance();
        bus.register(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        action = AppConsts.Actions.ACTION_REGISTER;

        til_username = findViewById(R.id.til_username);
        til_password = findViewById(R.id.til_password);
        til_retypePassword = findViewById(R.id.til_retypePassword);

        et_email = (EditText) findViewById(R.id.et_email);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_retypePassword = (EditText) findViewById(R.id.et_retypePassword);

        if (BuildConfig.DEBUG) {
            et_email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String[] split = editable.toString().split("@");
                    if (split.length > 0) {
                        et_username.setText(split[0]);
                    }
                }
            });

            et_email.setText("han001@hanoch.test");
            et_password.setText("123456");
            et_retypePassword.setText("123456");
        }

        tv_forgotYourPassword = (TextView) findViewById(R.id.tv_forgotYourPassword);
        tv_alreadyHaveAnAccountOrRegister = (TextView) findViewById(R.id.tv_alreadyHaveAnAccountOrRegister);

        tv_mailWithPasswordWillBeSent = findViewById(R.id.tv_mailWithPasswordWillBeSent);
        tv_forgotYourPasswordClickHere = findViewById(R.id.tv_forgotPasswordClickHere);
        tv_haveAccountOrRegisterClickHere = findViewById(R.id.tv_haveAccountOrRegisterClickHere);
        tv_testAppClickHere = findViewById(R.id.tv_testAppClickHere);

        btn_proceed = findViewById(R.id.btn_proceed);

        tv_forgotYourPasswordClickHere.setOnClickListener(this);
        tv_haveAccountOrRegisterClickHere.setOnClickListener(this);
        tv_testAppClickHere.setOnClickListener(this);
        btn_proceed.setOnClickListener(this);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && data.toString().equals("http://www.example.com/")) {
            // Get here via DeepLink click
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if (sp.contains(AppConsts.SharedPrefs.EMAIL)) {
                finish();
            } else {
                showLoginScreen();
            }
        } else {
            showRegisterScreen();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        mainView = findViewById(android.R.id.content);
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

        if (mailWasSentDialog != null && mailWasSentDialog.isShowing()) {
            outState.putBoolean("mailWasSentDialogIsShowing", true);

            EditText editText_userInput = (EditText) mailWasSentDialog.findViewById(R.id.editText_userInput);
            String currentUserInput = editText_userInput.getText().toString();
            outState.putString("currentUserInput", currentUserInput);
        }

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
//        boolean loginDialogIsShowing = savedInstanceState.getBoolean("loginDialogIsShowing");
//        if (loginDialogIsShowing) {
//            String currentUserInput = savedInstanceState.getString("currentUserInput");
//            showLoginDialog(currentUserInput);
//        }
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

            case R.id.tv_forgotPasswordClickHere:
                showForgotPasswordScreen();
                break;

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

            case R.id.btn_proceed:
                onProceedButtonClicked();
                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showForgotPasswordScreen() {
        if (BuildConfig.DEBUG) {
            et_email.setText("Hanoch001@gmail.com");
        }
        tv_mailWithPasswordWillBeSent.setVisibility(View.VISIBLE);
        tv_forgotYourPassword.setVisibility(View.GONE);
        tv_forgotYourPasswordClickHere.setVisibility(View.GONE);
        til_username.setVisibility(View.GONE);
        til_password.setVisibility(View.GONE);
        til_retypePassword.setVisibility(View.GONE);
        tv_alreadyHaveAnAccountOrRegister.setText(R.string.open_account_for_free);
        action = AppConsts.Actions.ACTION_FORGOT_PASSWORD;

        if (appStateManager.userEmailVerification != null) {
            et_email.setText(appStateManager.userEmailVerification.email);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showLoginScreen() {
        tv_forgotYourPassword.setVisibility(View.VISIBLE);
        tv_forgotYourPasswordClickHere.setVisibility(View.VISIBLE);
        tv_mailWithPasswordWillBeSent.setVisibility(View.GONE);
        til_username.setVisibility(View.GONE);
        til_password.setVisibility(View.VISIBLE);
        til_retypePassword.setVisibility(View.GONE);
        tv_alreadyHaveAnAccountOrRegister.setText(R.string.open_account_for_free);
        action = AppConsts.Actions.ACTION_LOGIN;

        if (appStateManager.userEmailVerification != null) {
            et_email.setText(appStateManager.userEmailVerification.email);
            et_username.setText(appStateManager.userEmailVerification.username);
            et_username.setSelection(et_username.length());
            et_password.setText(appStateManager.userEmailVerification.password);
            et_password.setSelection(et_password.length());
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showRegisterScreen() {
        tv_forgotYourPassword.setVisibility(View.VISIBLE);
        tv_forgotYourPasswordClickHere.setVisibility(View.VISIBLE);
        tv_mailWithPasswordWillBeSent.setVisibility(View.GONE);
        til_username.setVisibility(View.VISIBLE);
        til_password.setVisibility(View.VISIBLE);
        til_retypePassword.setVisibility(View.VISIBLE);
        tv_alreadyHaveAnAccountOrRegister.setText(R.string.already_have_an_account);
        action = AppConsts.Actions.ACTION_REGISTER;
    }

//-------------------------------------------------------------------------------------------------

    private void onProceedButtonClicked() {
        progressDialog.show();

        switch (action) {

            case AppConsts.Actions.ACTION_FORGOT_PASSWORD:
                if (isEmailValidated(email)) {
                    apisManager.forgotPassword(email);
                }
                break;

            case AppConsts.Actions.ACTION_REGISTER:
                if (areFieldsValidated()) {
                    apisManager.verifyEmail(email, username, password);
                }
                break;

            case AppConsts.Actions.ACTION_LOGIN:
                if (areFieldsValidated()) {
                    apisManager.login(this, email, password);
                }
                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    private boolean areFieldsValidated() {

        email = et_email.getText().toString();
        password = et_password.getText().toString();

        if (!isEmailValidated(email)) {
            return false;
        }

        if (action == AppConsts.Actions.ACTION_REGISTER) {
            username = et_username.getText().toString();

            int trimmedLength = username.trim().length();
            if (trimmedLength < 6 || trimmedLength > 20) {
                AppHelper.showSnackBar(mainView, R.string.username_should_contain_6_to_20_chars, Color.RED);
                return false;
            }
            char[] chars = username.toCharArray();
            for (char aChar : chars) {
                String sChar = String.valueOf(aChar);
                if (!AppConsts.Regex.USERNAME_PATTERN.contains(sChar)) {
                    AppHelper.showSnackBar(mainView, R.string.invalid_username, Color.RED);
                    return false;
                }
            }
        }

        char[] chars = password.toCharArray();
        for (char aChar : chars) {
            String sChar = String.valueOf(aChar);
            if (!AppConsts.Regex.PASSWORD_PATTERN.contains(sChar)) {
                AppHelper.showSnackBar(mainView, R.string.invalid_password, Color.RED);
                return false;
            }
        }
        int trimmedLength = password.trim().length();
        if (trimmedLength < 6 || trimmedLength > 10) {
            AppHelper.showSnackBar(mainView, R.string.password_should_contain_6_to_10_chars, Color.RED);
            return false;
        }

        if (action == AppConsts.Actions.ACTION_REGISTER) {
            retypePassword = et_retypePassword.getText().toString();

            if (!password.equals(retypePassword)) {
                AppHelper.showSnackBar(mainView, R.string.passwords_are_not_identical, Color.RED);
                return false;
            }

            username = username.trim();
        }

        email = email.trim();
        password = password.trim();
        return true;
    }

//-------------------------------------------------------------------------------------------------

    private boolean isEmailValidated(String eMail) {

        Pattern emailPattern = Pattern.compile(AppConsts.Regex.EMAIL_PATTERN);
        Matcher emailMatcher = emailPattern.matcher(eMail);
        if (!emailMatcher.matches()) {
            AppHelper.showSnackBar(mainView, R.string.invalid_email, Color.RED);
            return false;
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnEmailVerificationEvent event) {
        // The user registration details (eMail,username & password) have been saved in the
        // Great Recipes DB.
        // Email has not been verified by the user yet.
        // Now should send a "verify email" link to the user email
        if (event.isSuccess) {
            UserEmailVerification verification = appStateManager.userEmailVerification;
            AppHelper.performMailSending(verification.email, verification._id, AppConsts.Actions.ACTION_REGISTER);
        } else {
            progressDialog.dismiss();

            if (event.t instanceof HttpException) {
                int errorCode = ((HttpException) event.t).code();
                if (errorCode == 440) {
                    AppHelper.showSnackBar(mainView, R.string.email_already_exists, Color.RED);
                    return;
                } else if (BuildConfig.DEBUG && errorCode == 450) {
                    AppHelper.showSnackBar(mainView, "DEBUG: user registered successfully", Color.GREEN);
                    showLoginScreen();
                    return;
                }
            }

            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnMailWasSentEvent event) {
        progressDialog.dismiss();
        showMailWasSentDialog(event.isSuccess, event.action);
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnLoginEvent event) {

        if (event.isSuccess) {

            Preferences preferences = appStateManager.user.preferences;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(AppConsts.SharedPrefs.EMAIL, preferences.email);
            editor.putString(AppConsts.SharedPrefs.USER_NAME, preferences.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, preferences.password);
            editor.apply();
        } else {
            if (event.t instanceof HttpException && ((HttpException) event.t).code() == 430) {
                AppHelper.showSnackBar(mainView, R.string.wrong_email_or_password, Color.RED);
            } else {
                AppHelper.onApiErrorReceived(event.t, mainView);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateDeviceEvent event) {
        // After logging in.

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnForgotPasswordEvent event) {
        if (event.isSuccess) {

            int action = AppConsts.Actions.ACTION_FORGOT_PASSWORD;
            AppHelper.performMailSending(event.email, event.password, action);

        } else {
            progressDialog.dismiss();

            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showMailWasSentDialog(boolean isSuccessful, int action) {

        mailWasSentDialog = new Dialog(this);
        mailWasSentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_sent_email, null, false);
        mailWasSentDialog.setContentView(view);

        final TextView tv_dialogTitle = (TextView) mailWasSentDialog.findViewById(R.id.tv_dialogTitle);
        final TextView tv_body = (TextView) mailWasSentDialog.findViewById(R.id.tv_dialogBodyText);

        String title;
        if (action == AppConsts.Actions.ACTION_REGISTER) {
            title = getString(R.string.verify_your_email);
            tv_body.setText(R.string.verify_email_text);
        } else {
            title = getString(R.string.restore_password);
            tv_body.setText(R.string.restore_password_text);
        }

        tv_dialogTitle.setText(title);

        if (isSuccessful) {
            showLoginScreen();
        } else {
            title += getString(R.string.has_failed) + ".\n" + getString(R.string.mail_sending_error);
            tv_body.setText(title);
        }

        View button_ok = mailWasSentDialog.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(v -> {
            String category;
            String text;
            if (action == AppConsts.Actions.ACTION_REGISTER) {
                category = AppConsts.Analytics.CATEGORY_REGISTER;
                text = "Verification mail";
            } else {
                category = AppConsts.Analytics.CATEGORY_FORGOT_PASSWORD;
                text = "Restore password mail";
            }
            if (isSuccessful) {
                text += " has been sent";
            } else {
                text += " sending has FAILED";
            }
            AnalyticsHelper.sendEvent(RegisterActivity.this, category, text, email);
            mailWasSentDialog.dismiss();
        });

        mailWasSentDialog.show();
    }

}