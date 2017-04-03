package com.hanoch.greatrecipes.view;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.FreeTrialPreference;

public class PreferencesListsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PrefsListsFragment";

    private Preference maxOnlineSearchResults;
    private View view;
    private boolean premium;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        if (premium)
            addPreferencesFromResource(R.xml.prefs_lists);
        else
            addPreferencesFromResource(R.xml.prefs_lists_free_trial);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        Preference prefDeleteFavouritesList = findPreference(AppConsts.SharedPrefs.DELETE_ALL);
        prefDeleteFavouritesList.setOnPreferenceClickListener(this);

        maxOnlineSearchResults = findPreference(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS);

        if (premium) {

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
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        switch (preference.getKey()) {

            case AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS:
                maxOnlineSearchResults.setSummary((String) newValue);
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

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // inflate the layout dialog_layout.xml and set it as contentView
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
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GreatRecipesApplication) getActivity().getApplication()).getDbManager().deleteAllRecipes();

                Snackbar snack = Snackbar.make(view, R.string.all_the_recipe_were_deleted, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorSnackbarGreen));
                snack.show();

                // Close the dialog
                dialog.dismiss();
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String toastMessage = getString(R.string.the_operation_was_aborted);
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

                // Close the dialog
                dialog.dismiss();
            }
        });

        // Display the dialog
        dialog.show();
    }
}
