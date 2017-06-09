package com.hanoch.greatrecipes.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.OnUpdateUserPreferencesEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Preferences;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;


public class PreferencesActivityForLargeTablet extends AppCompatPreferenceActivity {

    private static List<String> fragmentsNames = new ArrayList<>();
    public Preferences newPreferences;
    public boolean isToClearAllRecipesListsOnSave;
    private ApisManager apisManager;
    private ProgressDialog progressDialog;
    private AppStateManager appStateManager;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity for Large Tablets only

        apisManager = ApisManager.getInstance();
        appStateManager = AppStateManager.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(getString(R.string.updating_data));
        progressDialog.setMessage(getString(R.string.please_wait));

        LinearLayout headersRoot = (LinearLayout) findViewById(android.R.id.list).getParent();
        headersRoot.setBackgroundResource(R.color.colorListContainerBackground);

        LinearLayout mainRoot = (LinearLayout) headersRoot.getParent().getParent();
        mainRoot.setBackgroundResource(R.color.colorDetailsContainerBackground);
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, mainRoot, false);
        mainRoot.addView(toolbar, 0); // insert at top

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
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preference_headers, target);

        fragmentsNames.clear();
        for (Header header : target) {
            fragmentsNames.add(header.fragment);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentsNames.contains(fragmentName);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_preferences, menu);
        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {

            case R.id.action_save:
                progressDialog.show();
                apisManager.updateUserPreferences(newPreferences);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("newPreferences", new Gson().toJson(newPreferences, Preferences.class));
        outState.putBoolean("isToClearAllRecipesListsOnSave", isToClearAllRecipesListsOnSave);
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserPreferencesEvent event) {
        View mainView = findViewById(android.R.id.content);

        if (event.isSuccess) {
            AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREFERENCES, "Preferences was changed successfully");

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(AppConsts.SharedPrefs.USER_NAME, appStateManager.user.preferences.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, appStateManager.user.preferences.password);

            editor.apply();
            if (isToClearAllRecipesListsOnSave) {
                apisManager.updateUserRecipes(null, null, BusConsts.ACTION_DELETE_ALL_LISTS);
            } else {
                progressDialog.dismiss();

                AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREFERENCES, "Preferences was changed successfully");
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        } else {
            progressDialog.dismiss();
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {

        if (event.action == BusConsts.ACTION_ADD_SHARED_RECIPE) {
            return;
        }

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