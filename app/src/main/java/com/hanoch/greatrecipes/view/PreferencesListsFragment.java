package com.hanoch.greatrecipes.view;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.model.FreeTrialPreference;
import com.hanoch.greatrecipes.model.Preferences;
import com.squareup.otto.Subscribe;

public class PreferencesListsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private Preference maxOnlineSearchResults;
    private View view;
    private boolean isPremium;
    private Preferences newPreferences;

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPremium = AppStateManager.getInstance().user.isPremium;

        if (isPremium) {
            addPreferencesFromResource(R.xml.prefs_lists);
        } else {
            addPreferencesFromResource(R.xml.prefs_lists_free_trial);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        Preference prefDeleteFavouritesList = findPreference(AppConsts.SharedPrefs.DELETE_ALL);
        prefDeleteFavouritesList.setOnPreferenceClickListener(this);

        maxOnlineSearchResults = findPreference(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS);

        if (isPremium) {

            // TODO: getArguments().getString("...")

            maxOnlineSearchResults.setOnPreferenceChangeListener(this);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String maxOnlineSearchResultsPref = sp.getString(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS, "10");
            maxOnlineSearchResults.setSummary(maxOnlineSearchResultsPref);

        } else {
            // Trial-version

            maxOnlineSearchResults.setOnPreferenceClickListener(this);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onResume() {
        super.onResume();

        newPreferences = ((PreferencesActivityForLargeTablet) getActivity()).newPreferences;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String newVal = (String) newValue;

        switch (preference.getKey()) {

            case AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS:
                maxOnlineSearchResults.setSummary(newVal);
                newPreferences.maxOnlineSearchResults = Integer.parseInt(newVal);
                return true;
        }

        return false;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceClick(final Preference preference) {

        String key = preference.getKey();

        switch (key) {

            case AppConsts.SharedPrefs.DELETE_ALL:
                onDeleteAllListClicked();
                break;

            default:

                if (preference instanceof FreeTrialPreference){
                    // The user clicked the "maxOnlineSearchResults" preference in free-trial version
                    ((FreeTrialPreference) preference).onPreferenceClick(preference);
                }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public void onDeleteAllListClicked() {

        AppHelper.vibrate(getActivity());

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View dialogView = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(dialogView);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        textView_dialogTitle.setText(R.string.deleting_all_the_recipes);
        textView_dialogContent.setText(getString(R.string.are_you_sure)
                        + "\n"
                        + getString(R.string.the_operation_is_irreversible)
                        + "\n"
                        + getString(R.string.all_servings_will_be_deleted)
        );

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(v -> {
            ((PreferencesActivity) getActivity()).isToClearAllRecipesListsOnSave = false;
            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> {
            ((PreferencesActivity) getActivity()).isToClearAllRecipesListsOnSave = false;
            dialog.dismiss();
        });

        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {

        if (event.action == BusConsts.ACTION_ADD_SHARED_RECIPE) {
            return;
        }

        if (event.isSuccess) {
            if (event.action == BusConsts.ACTION_DELETE_ALL_LISTS) {
                AppHelper.showSnackBar(view, R.string.all_the_recipe_were_deleted, ContextCompat.getColor(getActivity(), R.color.colorSnackbarGreen));
            }
        } else {
            AppHelper.onApiErrorReceived(event.t, view);
        }
    }
}
