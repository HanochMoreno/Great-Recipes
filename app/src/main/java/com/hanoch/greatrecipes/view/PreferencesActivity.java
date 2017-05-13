package com.hanoch.greatrecipes.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.google.AnalyticsHelper;


public class PreferencesActivity extends AppCompatActivity
        implements PreferencesFragment.PreferencesFragmentListener {

    private static final String TAG = "PreferencesActivity";

    //region Class variables
    private boolean vibrationPref;

    private String userNamePref;
    private String mainMenuDisplayPref;
    private String maxOnlineSearchResultsPref;

    private boolean veganPref;
    private boolean vegetarianPref;
    private boolean paleoPref;
    private boolean dairyFreePref;
    private boolean eggFreePref;
    private boolean glutenFreePref;
    private boolean peanutFreePref;
    private boolean seafoodFreePref;
    private boolean sesameFreePref;
    private boolean soyFreePref;
    private boolean treeNutFreePref;
    private boolean wheatFreePref;
    //endregion

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activity for phones and small tablets only

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
            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically

            PreferencesFragment preferencesFragment = PreferencesFragment.newInstance();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.add(R.id.layout_container, preferencesFragment, AppConsts.Fragments.PREFERENCES);

            ft.commit();
        }

        // Getting the previous user preference in case he'll click "cancel"
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        userNamePref = sp.getString(AppConsts.SharedPrefs.USER_NAME, AppConsts.SharedPrefs.NEW_USER);
        mainMenuDisplayPref = sp.getString(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY, AppConsts.SharedPrefs.COLORFUL);
        maxOnlineSearchResultsPref = sp.getString(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS, "10");
        vibrationPref = sp.getBoolean(AppConsts.SharedPrefs.VIBRATION, true);

        veganPref = sp.getBoolean(AppConsts.SharedPrefs.VEGAN, false);
        vegetarianPref = sp.getBoolean(AppConsts.SharedPrefs.VEGETARIAN, false);
        paleoPref = sp.getBoolean(AppConsts.SharedPrefs.PALEO, false);
        dairyFreePref = sp.getBoolean(AppConsts.SharedPrefs.DAIRY_FREE, false);
        eggFreePref = sp.getBoolean(AppConsts.SharedPrefs.EGG_FREE, false);
        glutenFreePref = sp.getBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, false);
        peanutFreePref = sp.getBoolean(AppConsts.SharedPrefs.PEANUT_FREE, false);
        seafoodFreePref = sp.getBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, false);
        sesameFreePref = sp.getBoolean(AppConsts.SharedPrefs.SESAME_FREE, false);
        soyFreePref = sp.getBoolean(AppConsts.SharedPrefs.SOY_FREE, false);
        treeNutFreePref = sp.getBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, false);
        wheatFreePref = sp.getBoolean(AppConsts.SharedPrefs.WHEAT_FREE, false);
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
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;

            case R.id.action_cancel:
                onBackPressed();
                break;
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onPremiumAccessPurchased() {

        Log.d(TAG, "Purchased was completed successfully");

        AnalyticsHelper.sendEvent(PreferencesActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "User purchased premium access");

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, true);
        editor.apply();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        PreferencesFragment preferencesFragment = PreferencesFragment.newInstance();
        ft.replace(R.id.layout_container, preferencesFragment, AppConsts.Fragments.PREFERENCES);
        ft.commit();

        View rootView = findViewById(android.R.id.content);
        AppHelper.showSnackBar(rootView, R.string.purchase_complete_successfully, ActivityCompat.getColor(this, R.color.colorSnackbarGreen));
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        // Forcing the same functionality as clicking the "Cancel" button

        // Restoring the previous user preference

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        editor.putBoolean(AppConsts.SharedPrefs.VIBRATION, vibrationPref);

        editor.putString(AppConsts.SharedPrefs.USER_NAME, userNamePref);
        editor.putString(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY, mainMenuDisplayPref);
        editor.putString(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS, maxOnlineSearchResultsPref);

        editor.putBoolean(AppConsts.SharedPrefs.VEGAN, veganPref);
        editor.putBoolean(AppConsts.SharedPrefs.VEGETARIAN, vegetarianPref);
        editor.putBoolean(AppConsts.SharedPrefs.PALEO, paleoPref);
        editor.putBoolean(AppConsts.SharedPrefs.DAIRY_FREE, dairyFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.EGG_FREE, eggFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, glutenFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.PEANUT_FREE, peanutFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, seafoodFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.SESAME_FREE, sesameFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.SOY_FREE, soyFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, treeNutFreePref);
        editor.putBoolean(AppConsts.SharedPrefs.WHEAT_FREE, wheatFreePref);

        editor.commit();

        finish();
    }

}
