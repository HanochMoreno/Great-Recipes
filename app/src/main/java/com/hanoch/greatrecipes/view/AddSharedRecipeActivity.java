package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnAppDataEvent;
import com.hanoch.greatrecipes.bus.OnForgotPasswordEvent;
import com.hanoch.greatrecipes.bus.OnGetTokenEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.bus.OnMailWasSentEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.bus.OnUserRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnYummlyRecipeDownloadedEvent;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Preferences;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddSharedRecipeActivity extends AppCompatActivity implements
        RecipeReviewFragment2.FragmentRecipeReviewListener,
        ToolbarMenuSetting {

    private String mRecipeId;

    /**
     * Optional action for this activity:
     * REVIEW_SHARED_USER_RECIPE.
     * REVIEW_SHARED_YUMMLY_RECIPE.
     */
    private int action;

    private int toolbarColor;
    private String toolbarTitle;

    private Toolbar toolbar;

    private MenuItem toolbar_addToList;
    private MenuItem toolbar_closeWebView;

    private ArrayList<ObjectAnimator> fadingInAnimationsList;
    private Bundle savedInstanceState;

    private ApisManager apisManager;
    private MyBus bus;
    private ProgressDialog progressDialog;
    private AppStateManager appStateManager;
    private View mainView;
    private Dialog loginDialog;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        appStateManager = AppStateManager.getInstance();
        apisManager = ApisManager.getInstance();

        bus = MyBus.getInstance();
        bus.register(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String activityToolbarTitle = getString(R.string.recipe_details);
        toolbar.setTitle(activityToolbarTitle);

        toolbarColor = AppConsts.ToolbarColor.PRIMARY;
        toolbarTitle = activityToolbarTitle;

        progressDialog.show();

        if (appStateManager.appData == null) {
            progressDialog.show();
            apisManager.getAppData();
        } else {
            onEvent(new OnAppDataEvent(true, null));
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mainView = findViewById(android.R.id.content);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        action = savedInstanceState.getInt("action");
        mRecipeId = savedInstanceState.getString("mRecipeId");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("action", action);
        outState.putString("mRecipeId", mRecipeId);

        outState.putInt("toolbarColor", toolbarColor);
        outState.putString("toolbarTitle", toolbarTitle);

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);

        ArrayList<Integer> displayedButtons = new ArrayList<>();

        MenuItem toolBarButton;
        for (Integer button : buttons) {

            toolBarButton = toolbar.getMenu().findItem(button);
            if (toolBarButton != null && toolBarButton.isVisible()) {
                displayedButtons.add(button);
            }
        }

        outState.putIntegerArrayList("displayedButtons", displayedButtons);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bus.unregister(this);
    }

//-------------------------------------------------------------------------------------------------

    public void onRecipeWasSaved() {
        // Settings after adding a shared recipe to the user's lists

        int color = ContextCompat.getColor(this, R.color.colorSnackbarGreen);
        if (action == AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {
            AppHelper.showSnackBar(mainView, R.string.added_to_my_own, color);
        } else {
            AppHelper.showSnackBar(mainView, R.string.added_to_online_list, color);
        }

        setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onGetInstructionsClick(String url) {
        // Phone only

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment webViewFragment = WebViewFragment.newInstance(url);

        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_up, R.anim.slide_out_up);

        ft.replace(R.id.layout_container, webViewFragment, AppConsts.Fragments.WEB_VIEW);
        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_add_shared_recipe, menu);

        toolbar_addToList = menu.findItem(R.id.action_addToList).setVisible(false);
        toolbar_closeWebView = menu.findItem(R.id.action_closeWebview).setVisible(false);

        if (savedInstanceState != null) {
            toolbarColor = savedInstanceState.getInt("toolbarColor");
            toolbarTitle = savedInstanceState.getString("toolbarTitle");
            ArrayList<Integer> displayedButtons = savedInstanceState.getIntegerArrayList("displayedButtons");

            toolbar.setBackgroundColor(toolbarColor);
            toolbar.setTitle(toolbarTitle);

            if (displayedButtons != null) {

                for (Integer button : displayedButtons) {

                    MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                    toolbarButton.setVisible(true);
                }
            }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {

            case R.id.action_addToList:
                // Saving a shared recipe

                progressDialog.show();

                if (action == AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {
                    ArrayList<String> userRecipesIds = new ArrayList<>();
                    userRecipesIds.add(appStateManager.sharedUserRecipe._id);

                    apisManager.updateUserRecipes(userRecipesIds, null, BusConsts.ACTION_ADD_SHARED_RECIPE);
                } else if (action == AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
                    ArrayList<String> yummlyRecipesIds = new ArrayList<>();
                    yummlyRecipesIds.add(appStateManager.sharedYummlyRecipe._id);

                    apisManager.updateUserRecipes(null, yummlyRecipesIds, BusConsts.ACTION_ADD_SHARED_RECIPE);
                }
                break;

            case R.id.action_closeWebview:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void setToolbarAttr(ArrayList<Integer> toolbarButtonsList, int color, String title) {

        toolbar.setTitle(title);
        toolbarTitle = title;

        if (color != AppConsts.ToolbarColor.NO_CHANGE) {
            toolbar.setBackgroundColor(color);
            toolbarColor = color;
        }

        ArrayList<Integer> allButtons = getAndHideAllToolbarButtons();

        if (toolbarButtonsList != null) {

            fadingInAnimationsList = new ArrayList<>();

            for (Integer button : allButtons) {
                if (toolbarButtonsList.contains(button)) {
                    MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                    fadingInAnimationsList.add(AnimationHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 600));
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUserRecipeDownloadedEvent event) {
        // After downloading a SHARED user-recipe from Great Recipe API.

        onSharedRecipeDownloadedEvent(event.isSuccess, event.t);
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnYummlyRecipeDownloadedEvent event) {
        // After downloading a SHARED Yummly-recipe from Great Recipe API.

        if (event.action != AppConsts.Actions.DOWNLOAD_SHARED_YUMMLY_RECIPE) {
            return;
        }

        onSharedRecipeDownloadedEvent(event.isSuccess, event.t);
    }

//-------------------------------------------------------------------------------------------------

    private void onSharedRecipeDownloadedEvent(boolean isSuccess, Throwable t) {
        progressDialog.dismiss();
        if (isSuccess) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment fragment = RecipeReviewFragment2.newInstance(action, mRecipeId);
            ft.add(R.id.layout_container, fragment, AppConsts.Fragments.RECIPE_REVIEW);
            ft.commit();

            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
        } else {
            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, null);
            AppHelper.onApiErrorReceived(t, mainView);
        }
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

    @Subscribe
    public void onEvent(OnMailWasSentEvent event) {
        // After an eMail with the password was sent
        if (action == AppConsts.Actions.ACTION_FORGOT_PASSWORD) {
            progressDialog.dismiss();
            showMailWasSentDialog(event.isSuccess, event.email);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showMailWasSentDialog(boolean isSuccessful, String email) {
        progressDialog.dismiss();

        String category = AppConsts.Analytics.CATEGORY_FORGOT_PASSWORD;
        String action = "Restore password mail";
        if (isSuccessful) {
            action += " has been sent";
        } else {
            action += " sending has FAILED";
        }
        AnalyticsHelper.sendEvent(AddSharedRecipeActivity.this, category, action, email);

        Dialog mailWasSentDialog = new Dialog(this);
        mailWasSentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_sent_email, null, false);
        mailWasSentDialog.setContentView(view);

        final TextView tv_dialogTitle = (TextView) mailWasSentDialog.findViewById(R.id.tv_dialogTitle);
        final TextView tv_bodyText = (TextView) mailWasSentDialog.findViewById(R.id.tv_dialogBodyText);

        String title = getString(R.string.restore_password);
        tv_dialogTitle.setText(title);

        tv_bodyText.setText(R.string.restore_password_text);

        if (!isSuccessful) {
            title += getString(R.string.has_failed) + ".\n" + getString(R.string.mail_sending_error);
            tv_bodyText.setText(title);
        }

        View button_ok = mailWasSentDialog.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(v -> {
            mailWasSentDialog.dismiss();
        });

        mailWasSentDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnAppDataEvent event) {

        if (event.isSuccess) {

            if (appStateManager.user == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String email = sp.getString(AppConsts.SharedPrefs.EMAIL, null);
                String password = sp.getString(AppConsts.SharedPrefs.PASSWORD, null);

                if (email == null || password == null) {
                    progressDialog.dismiss();
                    showLoginDialog();
                } else {
                    apisManager.getUserToken(this, email, password);
                }

            } else {
                getSharedRecipeData();
            }
        } else {
            showGetAppDataErrorDialog();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnGetTokenEvent event) {

        if (!event.isSuccess) {
            progressDialog.dismiss();
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnLoginEvent event) {

        if (event.isSuccess) {

            if (loginDialog != null) {
                loginDialog.dismiss();
            }

            Preferences preferences = appStateManager.user.preferences;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(AppConsts.SharedPrefs.EMAIL, preferences.email);
            editor.putString(AppConsts.SharedPrefs.USER_NAME, preferences.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, preferences.password);
            editor.apply();

            String category = AppConsts.Analytics.CATEGORY_LOGIN;
            String action1 = "Login successfully on shared recipe";
            String label = "eMail: " + preferences.email + " | Password: " + preferences.password;
            AnalyticsHelper.sendEvent(AddSharedRecipeActivity.this, category, action1, label);

            getSharedRecipeData();
        } else {
            progressDialog.dismiss();
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {
        // After saving the new shared recipe to the Great Recipes DB

        if (event.action != BusConsts.ACTION_ADD_SHARED_RECIPE) {
            return;
        }

        progressDialog.dismiss();

        if (event.isSuccess) {
            onRecipeWasSaved();
        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void getSharedRecipeData() {
        Intent prevIntent = getIntent();
        mRecipeId = prevIntent.getStringExtra(AppConsts.Extras.RECIPE_ID);
        boolean isUserRecipe = Boolean.parseBoolean(prevIntent.getStringExtra(AppConsts.Extras.IS_USER_RECIPE));
        if (isUserRecipe) {
            action = AppConsts.Actions.REVIEW_SHARED_USER_RECIPE;
            apisManager.getUserRecipeFromGreatRecipesApi(mRecipeId);
        } else {
            action = AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE;
            apisManager.getYummlyRecipeFromGreatRecipesApi(this, mRecipeId, AppConsts.Actions.DOWNLOAD_SHARED_YUMMLY_RECIPE);
        }
    }

//-------------------------------------------------------------------------------------------------

    public ArrayList<Integer> getAndHideAllToolbarButtons() {

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);

        // Resetting all buttons to invisible
        AnimationHelper.cancelAllFadingInAnimations(fadingInAnimationsList);

        MenuItem toolBarButton;
        for (Integer button : buttons) {

            toolBarButton = toolbar.getMenu().findItem(button);
            if (toolBarButton.isVisible()) {
                AnimationHelper.animateToolbarButtonFadingOut(toolBarButton, 500, 0);
            }
        }

        return buttons;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {

        if (toolbar_addToList.isVisible()) {
            showExitWithoutSavingDialog();
        } else if (toolbar_closeWebView.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();

            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
        } else {
            if (isTaskRoot()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

            finish();
        }
    }

//-------------------------------------------------------------------------------------------------

    public void showExitWithoutSavingDialog() {

        AppHelper.vibrate(this);

        final Context context = AddSharedRecipeActivity.this;
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);

        final View view = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(view);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        textView_dialogTitle.setText(R.string.exit_without_saving);
        textView_dialogContent.setText(R.string.are_you_sure);

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(v -> {
            if (isTaskRoot()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showLoginDialog() {

        loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loginDialog.setCancelable(false);
        loginDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_login, null, false);
        loginDialog.setContentView(view);

        final EditText et_email = (EditText) loginDialog.findViewById(R.id.et_email);
        final EditText et_password = (EditText) loginDialog.findViewById(R.id.et_password);
        View til_password = loginDialog.findViewById(R.id.til_password);
        View tv_mailWillBeSent = loginDialog.findViewById(R.id.tv_mailWithPasswordWillBeSent);
        View l_forgotPassword = loginDialog.findViewById(R.id.l_forgotPassword);
        View tv_forgotPasswordClick = l_forgotPassword.findViewById(R.id.tv_forgotPasswordClickHere);
        Button button_login = (Button) loginDialog.findViewById(R.id.button_login);
        Button button_cancel = (Button) loginDialog.findViewById(R.id.button_cancel);

        if (BuildConfig.DEBUG) {
            et_email.setText("han031@hanoch.test");
            et_password.setText("123456");
        }

        button_login.setOnClickListener(v -> {
            AppHelper.hideTheKeyboard(this);

            String eMail = et_email.getText().toString().trim();

            if (et_password.getVisibility() == View.VISIBLE) {
                String password = et_password.getText().toString().trim();
                if (areFieldsValidated(eMail, password)) {
                    progressDialog.show();
                    apisManager.getUserToken(AddSharedRecipeActivity.this, eMail, password);
                }
            } else {
                if (isEmailValidated(eMail)) {
                    progressDialog.show();
                    apisManager.forgotPassword(eMail);
                }
            }
        });

        button_cancel.setOnClickListener(v -> {
            if (et_password.getVisibility() == View.VISIBLE) {
                finish();
            } else {
                // Back to login
                button_login.setText(R.string.login);
                button_cancel.setText(R.string.cancel);
                til_password.setVisibility(View.INVISIBLE);
                tv_mailWillBeSent.setVisibility(View.GONE);
                l_forgotPassword.setVisibility(View.VISIBLE);
            }
        });

        tv_forgotPasswordClick.setOnClickListener(v -> {
            button_login.setText(R.string.proceed);
            button_cancel.setText(R.string.back);
            til_password.setVisibility(View.INVISIBLE);
            tv_mailWillBeSent.setVisibility(View.VISIBLE);
            l_forgotPassword.setVisibility(View.INVISIBLE);
        });

        loginDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showGetAppDataErrorDialog() {

        progressDialog.dismiss();

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

        tv_dialogBodyText.setText(R.string.error_getting_app_data);

        button_retry.setOnClickListener(v -> {
            apisManager.getAppData();
            errorDialog.dismiss();
        });

        button_leave.setOnClickListener(v -> finish());

        errorDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private boolean areFieldsValidated(String eMail, String password) {

        if (!isEmailValidated(eMail)) {
            return false;
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
}
