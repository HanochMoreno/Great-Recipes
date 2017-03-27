package com.hanoch.greatrecipes.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.FreeTrialCheckBoxPreference;


public class PreferencesFiltersFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener {

    private static final String TAG = "PrefsFiltersFragment";

    private boolean premium;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        if (premium)
            addPreferencesFromResource(R.xml.prefs_filters);
        else
            addPreferencesFromResource(R.xml.prefs_filters_free_trial);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!premium) {
            // Trial-version

            Log.d(TAG, "Setting-up free-trial filters");

            FreeTrialCheckBoxPreference checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.VEGAN);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.VEGETARIAN);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.DAIRY_FREE);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.EGG_FREE);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.GLUTEN_FREE);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.PEANUT_FREE);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.SEAFOOD_FREE);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.TREE_NUT_FREE);
            checkBox.setOnPreferenceClickListener(this);

            checkBox = (FreeTrialCheckBoxPreference) findPreference(AppConsts.SharedPrefs.WHEAT_FREE);
            checkBox.setOnPreferenceClickListener(this);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceClick(Preference preference) {

        // The user clicked on an unavailable filter in free-trial version
        ((FreeTrialCheckBoxPreference) preference).onPreferenceClick(preference);

        return true;
    }
}
