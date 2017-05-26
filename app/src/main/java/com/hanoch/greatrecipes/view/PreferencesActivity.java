package com.hanoch.greatrecipes.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnUpdateUserPreferencesEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Preferences;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;


public class PreferencesActivity extends AppCompatActivity
        implements PreferencesFragment.PreferencesFragmentListener {

    private static final String TAG = "PreferencesActivity";

    //region Class variables
    private MyBus bus;
    private ProgressDialog progressDialog;
    private AppStateManager appStateManager;
    private GreatRecipesDbManager dbManager;
    public Preferences newPreferences;
    public boolean isToClearAllRecipesListsOnSave;
    //endregion

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activity for phones and small tablets only
        dbManager = GreatRecipesDbManager.getInstance();
        appStateManager = AppStateManager.getInstance();
        bus = MyBus.getInstance();
        bus.register(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(getString(R.string.updating_data));
        progressDialog.setMessage(getString(R.string.please_wait));

        if (getResources().getBoolean(R.bool.isTablet)) { // small tablet
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String activityToolbarTitle = getString(R.string.preferences);
        toolbar.setTitle(activityToolbarTitle);

        if (savedInstanceState == null) {
            newPreferences = new Preferences(appStateManager.user.preferences);
        } else {
            newPreferences = new Gson().fromJson(savedInstanceState.getString("newPreferences"), Preferences.class);
            isToClearAllRecipesListsOnSave = savedInstanceState.getBoolean("isToClearAllRecipesListsOnSave");
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        String mainMenuDisplay = newPreferences.colorfulMenu ? AppConsts.SharedPrefs.COLORFUL : AppConsts.SharedPrefs.SIMPLE;
        editor.putString(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY, mainMenuDisplay);

        editor.putString(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS, String.valueOf(newPreferences.maxOnlineSearchResults));
        editor.putBoolean(AppConsts.SharedPrefs.VIBRATION, newPreferences.vibration);

        ArrayList<String> dietAndAllergensList = newPreferences.dietAndAllergensList;
        editor.putBoolean(AppConsts.SharedPrefs.VEGAN, dietAndAllergensList.contains(AppConsts.SharedPrefs.VEGAN));
        editor.putBoolean(AppConsts.SharedPrefs.VEGETARIAN, dietAndAllergensList.contains(AppConsts.SharedPrefs.VEGETARIAN));
        editor.putBoolean(AppConsts.SharedPrefs.PALEO, dietAndAllergensList.contains(AppConsts.SharedPrefs.PALEO));
        editor.putBoolean(AppConsts.SharedPrefs.DAIRY_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.DAIRY_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.EGG_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.EGG_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.GLUTEN_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.PEANUT_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.PEANUT_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.SEAFOOD_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.SESAME_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.SESAME_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.SOY_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.SOY_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.TREE_NUT_FREE));
        editor.putBoolean(AppConsts.SharedPrefs.WHEAT_FREE, dietAndAllergensList.contains(AppConsts.SharedPrefs.WHEAT_FREE));

        editor.apply();

        if (savedInstanceState == null) {
            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically

            PreferencesFragment preferencesFragment = PreferencesFragment.newInstance();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.add(R.id.layout_container, preferencesFragment, AppConsts.Fragments.PREFERENCES);

            ft.commit();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_preferences, menu);
        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("newPreferences", new Gson().toJson(newPreferences, Preferences.class));
        outState.putBoolean("isToClearAllRecipesListsOnSave", isToClearAllRecipesListsOnSave);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bus.unregister(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {

            case R.id.action_save:
                progressDialog.show();
                dbManager.updateUserPreferences(newPreferences);
                break;

            case R.id.action_cancel:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sp.edit();

                editor.putString(AppConsts.SharedPrefs.USER_NAME, appStateManager.user.preferences.username);
                editor.putString(AppConsts.SharedPrefs.PASSWORD, appStateManager.user.preferences.password);
                editor.apply();

                onBackPressed();
                break;
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onPremiumAccessPurchased() {

        Log.d(TAG, "Purchased was completed successfully");

        progressDialog.show();
        dbManager.updatePremiumStatus();

        AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "User purchased premium access");
        progressDialog.dismiss();
        View mainView = findViewById(android.R.id.content);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        PreferencesFragment preferencesFragment = PreferencesFragment.newInstance();
        ft.replace(R.id.layout_container, preferencesFragment, AppConsts.Fragments.PREFERENCES);
        ft.commit();

        AppHelper.showSnackBar(mainView, R.string.purchase_complete_successfully, ActivityCompat.getColor(this, R.color.colorSnackbarGreen));
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserPreferencesEvent event) {
        View mainView = findViewById(android.R.id.content);
        progressDialog.dismiss();

        if (event.isSuccess) {
            AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREFERENCES, "Preferences was changed successfully");

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(AppConsts.SharedPrefs.USER_NAME, appStateManager.user.preferences.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, appStateManager.user.preferences.password);

            editor.apply();

            if (isToClearAllRecipesListsOnSave) {
                dbManager.updateUserRecipes(null, null, BusConsts.ACTION_DELETE_ALL_LISTS);
            } else {
                progressDialog.dismiss();

                AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREFERENCES, "Preferences was changed successfully");
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {
        View mainView = findViewById(android.R.id.content);
        progressDialog.dismiss();

        if (event.isSuccess) {
            if (event.action == BusConsts.ACTION_DELETE_ALL_LISTS) {
                AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREFERENCES, "Preferences was changed successfully");
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }
}
