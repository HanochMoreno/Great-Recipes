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
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.google.IabHelper;
import com.hanoch.greatrecipes.model.MyIllegalStateException;
import com.hanoch.greatrecipes.model.Preferences;


public class PreferencesGeneralFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PrefsGeneralFragment";

    private EditTextPreference userName;
    private EditTextPreference password;
    private ListPreference mainMenuDisplay;

    private IabHelper mIabHelper;

    private View view;
    private Preference prefUpgradeToPremium;
    private boolean iabHelperWasAlreadySetUpSuccessfully;
    private ProgressDialog progressDialog;
    private String errorMessage;
    private AppStateManager appStateManager;
    private ApisManager apisManager;
    private Preferences newPreferences;

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apisManager = ApisManager.getInstance();
        appStateManager = AppStateManager.getInstance();
        addPreferencesFromResource(R.xml.prefs_general);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean isPremium = appStateManager.user.isPremium;

        prefUpgradeToPremium = findPreference(AppConsts.SharedPrefs.PREMIUM_ACCESS);
        prefUpgradeToPremium.setOnPreferenceClickListener(this);

        if (isPremium) {

            PreferenceCategory categoryGeneral = (PreferenceCategory) findPreference("categoryGeneral");
            categoryGeneral.removePreference(prefUpgradeToPremium);
        }

        String userNamePref = sp.getString(AppConsts.SharedPrefs.USER_NAME, null);
        String passwordPref = sp.getString(AppConsts.SharedPrefs.PASSWORD, null);

        userName = (EditTextPreference) findPreference(AppConsts.SharedPrefs.USER_NAME);
        userName.setOnPreferenceChangeListener(this);
        userName.setSummary(userNamePref);

        password = (EditTextPreference) findPreference(AppConsts.SharedPrefs.PASSWORD);
        password.setOnPreferenceChangeListener(this);
        password.setSummary(passwordPref);

        String mainMenuDisplayPref = sp.getString(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY, AppConsts.SharedPrefs.COLORFUL);

        mainMenuDisplay = (ListPreference) findPreference(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY);
        mainMenuDisplay.setOnPreferenceChangeListener(this);

        if (mainMenuDisplayPref.equals(AppConsts.SharedPrefs.COLORFUL)) {
            // Translation setting
            mainMenuDisplay.setSummary(getActivity().getString(R.string.colorful));

        } else {
            mainMenuDisplay.setSummary(getActivity().getString(R.string.simple));
        }

        Preference prefContactUs = findPreference(AppConsts.SharedPrefs.CONTACT_US);
        prefContactUs.setOnPreferenceClickListener(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onResume() {
        super.onResume();

        newPreferences = ((PreferencesActivityForLargeTablet) getActivity()).newPreferences;
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
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String newVal = (String) newValue;

        switch (preference.getKey()) {

            case AppConsts.SharedPrefs.USER_NAME:
                userName.setSummary(newVal);
                newPreferences.username = newVal;
                return true;

            case AppConsts.SharedPrefs.PASSWORD:
                password.setSummary(newVal);
                newPreferences.password = newVal;
                return true;

            case AppConsts.SharedPrefs.MAIN_MENU_DISPLAY:
                if (newVal.equals(AppConsts.SharedPrefs.COLORFUL)) {
                    mainMenuDisplay.setSummary(getActivity().getString(R.string.colorful));
                    newPreferences.colorfulMenu = true;
                } else {
                    mainMenuDisplay.setSummary(getActivity().getString(R.string.simple));
                    newPreferences.colorfulMenu = false;
                }

                return true;
        }

        return false;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onPreferenceClick(final Preference preference) {

        String key = preference.getKey();

        switch (key) {

            case AppConsts.SharedPrefs.PREMIUM_ACCESS:

                preference.setEnabled(false);

                if (mIabHelper != null && iabHelperWasAlreadySetUpSuccessfully) {

                    purchasePremiumAccess();

                } else {

                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setTitle(getString(R.string.connecting_google_servers));
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();

                    mIabHelper = new IabHelper(getActivity());

                    try {
                        mIabHelper.startSetup(result -> {

                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            if (result.isSuccess()) {
                                // Hooray, IAB is fully set up!

                                iabHelperWasAlreadySetUpSuccessfully = true;
                                purchasePremiumAccess();

                            } else {
                                // Oh noes, there was a problem.
                                if (result.toString().contains("unavailable on device") || result.toString().contains("Billing Unavailable")) {
                                    errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + getString(R.string.Billing_unavailable_for_device);
                                } else {

                                    errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + result;
                                }
                                showGoogleErrorDialog(errorMessage);
                            }
                        });

                    } catch (MyIllegalStateException e) {
                        errorMessage = getString(R.string.problem_starting_purchase_progress);
                        showGoogleErrorDialog(errorMessage);
                        Log.d(TAG, errorMessage);
                        e.printStackTrace();
                    }
                }

                break;


            case AppConsts.SharedPrefs.CONTACT_US:
                Intent contactUsIntent = new Intent(Intent.ACTION_SENDTO);
                contactUsIntent.setData(Uri.parse("mailto:" + "Hanoch001@gmail.com"));
                try {
                    getActivity().startActivity(Intent.createChooser(contactUsIntent, getString(R.string.select_mail_app)));
                } catch (ActivityNotFoundException e) {

                    AppHelper.showSnackBar(view, R.string.no_email_app, Color.RED);
                    e.printStackTrace();
                }
                break;
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    private void purchasePremiumAccess() {

        final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = (result, purchase) -> {
            if (result.isFailure()) {

                if (prefUpgradeToPremium != null) {
                    prefUpgradeToPremium.setEnabled(true);
                }

                errorMessage = result.toString();
                if (errorMessage.contains("Already Owned")) {
                    // The user has already a Premium status
                    apisManager.updatePremiumStatus();
                } else {
                    errorMessage = getString(R.string.error_purchasing) + ": " + result;
                }

                showGoogleErrorDialog(errorMessage);
                Log.d(TAG, errorMessage);

            } else if (purchase.getSku().equals(appStateManager.appData.skuPremium)) {
                // give user access to premium content

                AnalyticsHelper.sendEvent(PreferencesGeneralFragment.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "User purchased premium access");

                apisManager.updatePremiumStatus();

                View rootView = getActivity().findViewById(android.R.id.content);
                AppHelper.showSnackBar(rootView, R.string.purchase_complete_successfully, ActivityCompat.getColor(getActivity(), R.color.colorSnackbarGreen));
            }
        };

        try {
            mIabHelper.launchPurchaseFlow(getActivity(), appStateManager.appData.skuPremium, AppConsts.REQ_CODE_PURCHASE, mPurchaseFinishedListener);

        } catch (MyIllegalStateException e) {
            errorMessage = getString(R.string.problem_starting_purchase_progress);
            showGoogleErrorDialog(errorMessage);
            e.printStackTrace();
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showGoogleErrorDialog(String errorMessage) {

        AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "Google Error Dialog Was Showing", errorMessage);

        final Dialog googleErrorDialog = new Dialog(getActivity());

        googleErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_warning, null, false);
        googleErrorDialog.setCanceledOnTouchOutside(false);
        googleErrorDialog.setCancelable(false);
        googleErrorDialog.setContentView(view);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

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
        btnOk.setOnClickListener(v -> googleErrorDialog.dismiss());
        (googleErrorDialog.findViewById(R.id.button_cancel)).setVisibility(View.GONE);
        googleErrorDialog.show();
    }

}
