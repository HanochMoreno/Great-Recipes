package com.hanoch.greatrecipes.view;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.FreeTrialCheckBoxPreference;

import java.util.ArrayList;


public class PreferencesFiltersFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener {

    private static final String TAG = "PrefsFiltersFragment";

    private boolean isPremium;
    private ArrayList<String> dietAndAllergensList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPremium = AppStateManager.getInstance().user.isPremium;
        dietAndAllergensList = ((PreferencesActivityForLargeTablet) getActivity()).newPreferences.dietAndAllergensList;

        if (isPremium)
            addPreferencesFromResource(R.xml.prefs_filters);
        else
            addPreferencesFromResource(R.xml.prefs_filters_free_trial);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preference checkBox = findPreference(AppConsts.SharedPrefs.VEGAN);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.VEGETARIAN);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.PALEO);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.DAIRY_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.EGG_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.GLUTEN_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.PEANUT_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.SEAFOOD_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.SESAME_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.SOY_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.TREE_NUT_FREE);
        checkBox.setOnPreferenceClickListener(this);

        checkBox = findPreference(AppConsts.SharedPrefs.WHEAT_FREE);
        checkBox.setOnPreferenceClickListener(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (isPremium || !(preference instanceof FreeTrialCheckBoxPreference)) {
            if (dietAndAllergensList.contains(preference.getKey())) {
                dietAndAllergensList.remove(preference.getKey());
            } else {
                dietAndAllergensList.add(preference.getKey());
            }
        } else {
            // The user clicked on an unavailable filter in free-trial version
            ((FreeTrialCheckBoxPreference) preference).onPreferenceClick(preference);
        }

        return true;
    }
}
