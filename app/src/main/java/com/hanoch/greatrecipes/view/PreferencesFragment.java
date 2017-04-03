package com.hanoch.greatrecipes.view;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.google.IabHelperNonStatic;
import com.hanoch.greatrecipes.google.IabResult;
import com.hanoch.greatrecipes.google.Purchase;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.FreeTrialCheckBoxPreference;
import com.hanoch.greatrecipes.model.FreeTrialPreference;
import com.hanoch.greatrecipes.model.MyIllegalStateException;


public class PreferencesFragment extends PreferenceFragment
        implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PreferencesFragment";

    private EditTextPreference userName;

    private ListPreference mainMenuDisplay;
    private ListPreference maxOnlineSearchResults;

    private PreferencesFragmentListener mListener;
    private View view;

    private IabHelperNonStatic mIabHelper;
    private String errorMessage;
    private boolean iabHelperWasAlreadySetUpSuccessfully;
    private ProgressDialog progressDialog;
    private Preference prefUpgradeToPremium;

//-------------------------------------------------------------------------------------------------

    public interface PreferencesFragmentListener {
        void onPremiumAccessPurchased();
    }

//-------------------------------------------------------------------------------------------------

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_preferences, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        if (premium) {

            addPreferencesFromResource(R.xml.preference_screen);

            maxOnlineSearchResults = (ListPreference) findPreference(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS);
            maxOnlineSearchResults.setOnPreferenceChangeListener(this);

            String maxOnlineSearchResultsPref = sp.getString(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS, "10");
            maxOnlineSearchResults.setSummary(maxOnlineSearchResultsPref);

        } else {
            // Trial-version
            addPreferencesFromResource(R.xml.preference_screen_limited);

            prefUpgradeToPremium = findPreference(AppConsts.SharedPrefs.PREMIUM_ACCESS);
            prefUpgradeToPremium.setOnPreferenceClickListener(this);

            FreeTrialPreference maxOnlineSearchResultsFreeTrial = (FreeTrialPreference) findPreference(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS);
            maxOnlineSearchResultsFreeTrial.setOnPreferenceClickListener(this);
            maxOnlineSearchResultsFreeTrial.setSummary("5");

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

        Preference prefContactUs = findPreference(AppConsts.SharedPrefs.CONTACT_US);
        prefContactUs.setOnPreferenceClickListener(this);

        String userNamePref = sp.getString(AppConsts.SharedPrefs.USER_NAME, AppConsts.SharedPrefs.NEW_USER);

        userName = (EditTextPreference) findPreference(AppConsts.SharedPrefs.USER_NAME);
        userName.setOnPreferenceChangeListener(this);
        userName.setSummary(userNamePref);

        String mainMenuDisplayPref = sp.getString(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY, AppConsts.SharedPrefs.COLORFUL);

        mainMenuDisplay = (ListPreference) findPreference(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY);
        mainMenuDisplay.setOnPreferenceChangeListener(this);

        if (mainMenuDisplayPref.equals(AppConsts.SharedPrefs.COLORFUL)) {
            // Translation setting
            mainMenuDisplay.setSummary(getActivity().getString(R.string.colorful));

        } else {
            mainMenuDisplay.setSummary(getActivity().getString(R.string.simple));
        }

        Preference prefDeleteFavouritesList = findPreference(AppConsts.SharedPrefs.DELETE_ALL);
        prefDeleteFavouritesList.setOnPreferenceClickListener(this);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AnalyticsHelper.setScreenName(this);

        try {
            mListener = (PreferencesFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PreferencesFragmentListener");
        }

    }

//-------------------------------------------------------------------------------------------------

    @Override
    // onAttach is never called in PreferenceFragment - Do all stuff in onViewCreated()
    public void onAttach(Context context) {
        super.onAttach(context);

        /*AnalyticsHelper.setScreenName(this);

        try {
            mListener = (PreferencesFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement PreferencesFragmentListener");
        }*/
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onResume() {
        super.onResume();

        AnalyticsHelper.setScreenName(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIabHelper != null) {
            try {
                mIabHelper.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mIabHelper = null;
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String newVal = (String) newValue;

        switch (preference.getKey()) {

            case AppConsts.SharedPrefs.USER_NAME:
                userName.setSummary(newVal);
                AnalyticsHelper.sendEvent(PreferencesFragment.this, AppConsts.Analytics.CATEGORY_LOGIN, "Username was changed successfully", newVal);
                return true;

            case AppConsts.SharedPrefs.MAIN_MENU_DISPLAY:
                if (newVal.equals(AppConsts.SharedPrefs.COLORFUL)) {
                    mainMenuDisplay.setSummary(getActivity().getString(R.string.colorful));

                } else {
                    mainMenuDisplay.setSummary(getActivity().getString(R.string.simple));
                }

                return true;

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

            case AppConsts.SharedPrefs.CONTACT_US:
                Intent contactUsIntent = new Intent(Intent.ACTION_SENDTO);
                contactUsIntent.setData(Uri.parse("mailto:" + "Hanoch001@gmail.com"));
                try {
                    getActivity().startActivity(Intent.createChooser(contactUsIntent, getString(R.string.select_mail_app)));
                } catch (ActivityNotFoundException e) {
                    Snackbar snack = Snackbar.make(view, R.string.no_email_app, Snackbar.LENGTH_LONG);
                    ViewGroup group = (ViewGroup) snack.getView();
                    group.setBackgroundColor(Color.RED);
                    snack.show();

                    e.printStackTrace();
                }
                break;

            case AppConsts.SharedPrefs.DELETE_ALL:

                onDeleteAllListClicked();

                break;

            case AppConsts.SharedPrefs.PREMIUM_ACCESS:

                AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "Purchase Premium button clicked");

                preference.setEnabled(false);

                if (BuildConfig.DEBUG) {
                    mListener.onPremiumAccessPurchased();

                    return true;
                }

                if (mIabHelper!= null && iabHelperWasAlreadySetUpSuccessfully) {

                    purchasePremiumAccess();

                } else {

                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setTitle(getString(R.string.connecting_google_servers));
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();

                    mIabHelper = new IabHelperNonStatic(getActivity());

                    try {
                        mIabHelper.startSetup(new IabHelperNonStatic.OnIabSetupFinishedListener() {
                            public void onIabSetupFinished(IabResult result) {

                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                preference.setEnabled(true);

                                if (result.isSuccess()) {
                                    // Hooray, IAB is fully set up!

                                    Log.d(TAG, "Iab was set up successfully");
                                    iabHelperWasAlreadySetUpSuccessfully = true;
                                    purchasePremiumAccess();

                                } else {
                                    // Oh noes, there was a problem.
                                    if (result.toString().contains("unavailable on device")) {
                                        errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + getString(R.string.Billing_unavailable_for_device);
                                    } else {

                                        errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + result;
                                    }
                                    showGoogleErrorDialog(errorMessage);

                                    Log.e(TAG, "Iab set up was FAILED: " + errorMessage);
                                }
                            }
                        });

                    } catch (MyIllegalStateException e) {
                        errorMessage = getString(R.string.problem_starting_purchase_progress);
                        showGoogleErrorDialog(errorMessage);
                        Log.e(TAG, errorMessage);
                        e.printStackTrace();
                    }
                }

                break;

            default:

                if (preference instanceof FreeTrialCheckBoxPreference) {
                    ((FreeTrialCheckBoxPreference) preference).onPreferenceClick(preference);
                } else if (preference instanceof FreeTrialPreference){
                    ((FreeTrialPreference) preference).onPreferenceClick(preference);
                }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    private void purchasePremiumAccess(){

        final IabHelperNonStatic.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = new IabHelperNonStatic.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

                if (result.isFailure()) {

                    if (prefUpgradeToPremium != null) {
                        prefUpgradeToPremium.setEnabled(true);
                    }

                    errorMessage = result.toString();
                    if (errorMessage.contains("Already Owned")) {
                        errorMessage = getString(R.string.error_purchasing) + ": " + getString(R.string.you_are_already_premium);
                    } else {
                        errorMessage = getString(R.string.error_purchasing) + ": " + result;
                    }

                    showGoogleErrorDialog(errorMessage);
                    Log.e(TAG, errorMessage);

                } else if (purchase.getSku().equals(AppConsts.SKU_PREMIUM)) {
                    // give user access to premium content

                    mListener.onPremiumAccessPurchased();
                }
            }
        };

        try {
            mIabHelper.launchPurchaseFlow(getActivity(), AppConsts.SKU_PREMIUM, AppConsts.REQ_CODE_PURCHASE, mPurchaseFinishedListener);

        } catch (MyIllegalStateException e) {
            errorMessage = getString(R.string.problem_starting_purchase_progress);
            showGoogleErrorDialog(errorMessage);
            e.printStackTrace();
        }
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

//-------------------------------------------------------------------------------------------------

    private void showGoogleErrorDialog(String errorMessage) {

        AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "Google Error Dialog Was Showing", errorMessage);

        final Dialog googleErrorDialog = new Dialog(getActivity());

        // hide to default title for Dialog
        googleErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_warning, null, false);
        googleErrorDialog.setCanceledOnTouchOutside(false);
        googleErrorDialog.setCancelable(false);
        googleErrorDialog.setContentView(view);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        //int height = metrics.heightPixels;

        if (getResources().getBoolean(R.bool.isTablet)
                && !(getResources().getBoolean(R.bool.isSmallTablet))
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 10 inches tablet in landscape mode

            googleErrorDialog.getWindow().setLayout((3 * width) / 5, ActionBar.LayoutParams.WRAP_CONTENT);

        } else {
            googleErrorDialog.getWindow().setLayout((6 * width) / 7, ActionBar.LayoutParams.WRAP_CONTENT);
        }

        String dialogTitle = getString(R.string.purchase_has_failed);

        TextView textView_dialogTitle = (TextView) googleErrorDialog.findViewById(R.id.textView_dialogTitle);
        textView_dialogTitle.setText(dialogTitle);

        TextView textView_dialogContent = (TextView) googleErrorDialog.findViewById(R.id.textView_dialogContent);
        textView_dialogContent.setText(errorMessage);

        Button btnOk = (Button) googleErrorDialog.findViewById(R.id.button_yes);
        btnOk.setText(getString(R.string.ok));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                finish();

                googleErrorDialog.dismiss();
            }
        });

        (googleErrorDialog.findViewById(R.id.button_cancel)).setVisibility(View.GONE);

        googleErrorDialog.show();
    }

}
