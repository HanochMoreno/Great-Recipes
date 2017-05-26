package com.hanoch.greatrecipes.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.hanoch.greatrecipes.google.IabHelperNonStatic;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.MyIllegalStateException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQ_CODE_PREFERENCES = 1;

    //    private Dialog loginDialog;
    private Dialog googleErrorDialog;
    private Dialog premiumDialog;
    private ProgressDialog progressDialog;
    private boolean alreadyGotGoogleAnswer;
    private String googleErrorDialogTitle;
    private String errorMessage;

    // compute your public key and store it in base64EncodedPublicKey
    private IabHelperNonStatic mIabHelper;
    private boolean iabHelperWasAlreadySetUpSuccessfully;
    private GreatRecipesDbManager dbManager;
    private AppStateManager appStateManager;
    private MyBus bus;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbManager = GreatRecipesDbManager.getInstance();
        appStateManager = AppStateManager.getInstance();
        bus = MyBus.getInstance();
        bus.register(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.please_wait));

        if (getResources().getBoolean(R.bool.isTablet)) {
            if (getResources().getBoolean(R.bool.isSmallTablet))
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (!appStateManager.user.isPremium) {
            if (savedInstanceState != null) {
                alreadyGotGoogleAnswer = savedInstanceState.getBoolean("alreadyGotGoogleAnswer");
            }

            if (alreadyGotGoogleAnswer) {
                initDisplay();
                return;
            }

            getUserPremiumStatusFromGoogle();
        }
    }

//-------------------------------------------------------------------------------------------------

    private void getUserPremiumStatusFromGoogle() {
        final IabHelperNonStatic.QueryInventoryFinishedListener mGotInventoryListener
                = (result, inventory) -> {

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            alreadyGotGoogleAnswer = true;

            if (result.isFailure()) {
                // handle error here
                errorMessage = getString(R.string.problem_getting_premium_status) + ": " + result;
                googleErrorDialogTitle = getString(R.string.premium_status_error);
                onQueryPremiumStatusFinished(false, false);
                Log.d(TAG, "Problem getting user inventory: " + result);

            } else {
                Log.d(TAG, "onQueryPremiumStatusFinished: successfully got user inventory");
                // does the user have the premium upgrade?
                boolean mIsPremium = inventory.hasPurchase(AppConsts.SKU_PREMIUM);
                onQueryPremiumStatusFinished(true, mIsPremium);
            }
        };

        progressDialog.setTitle(getString(R.string.connecting_google_servers));
        progressDialog.show();

        mIabHelper = new IabHelperNonStatic(this);

        try {
            mIabHelper.startSetup(result -> {

                if (result.isSuccess()) {
                    // Hooray, IAB is fully set up!

                    iabHelperWasAlreadySetUpSuccessfully = true;

                    try {
                        mIabHelper.queryInventoryAsync(mGotInventoryListener);
                    } catch (MyIllegalStateException e) {
                        googleErrorDialogTitle = getString(R.string.premium_status_error);
                        errorMessage = getString(R.string.problem_getting_premium_status);
                        onQueryPremiumStatusFinished(false, false);
                        Log.d(TAG, errorMessage);
                        e.printStackTrace();
                    }
                } else {
                    // Oh no, there was a problem.
                    if (result.toString().contains("unavailable on device") || result.toString().contains("Billing Unavailable")) {
                        errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + getString(R.string.Billing_unavailable_for_device);
                    } else {
                        errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + result;
                    }

                    googleErrorDialogTitle = getString(R.string.premium_status_error);
                    onQueryPremiumStatusFinished(false, false);
                    Log.d(TAG, "Problem setting up In-app Billing:\n " + result);
                }
            });

        } catch (MyIllegalStateException e) {
            googleErrorDialogTitle = getString(R.string.premium_status_error);
            errorMessage = getString(R.string.problem_getting_premium_status);
            onQueryPremiumStatusFinished(false, false);
            Log.d(TAG, errorMessage);
            e.printStackTrace();
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showGoogleErrorDialog(String title, String errorMessage) {

        AnalyticsHelper.sendEvent(this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "Google Error Dialog Was Showing", errorMessage);

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        googleErrorDialog = new Dialog(this);

        // hide to default title for Dialog
        googleErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        TextView textView_dialogTitle = (TextView) googleErrorDialog.findViewById(R.id.textView_dialogTitle);
        textView_dialogTitle.setText(title);

        TextView textView_dialogContent = (TextView) googleErrorDialog.findViewById(R.id.textView_dialogContent);
        textView_dialogContent.setText(errorMessage);

        Button btnOk = (Button) googleErrorDialog.findViewById(R.id.button_yes);
        btnOk.setText(getString(R.string.ok));
        btnOk.setOnClickListener(v -> googleErrorDialog.dismiss());

        (googleErrorDialog.findViewById(R.id.button_cancel)).setVisibility(View.GONE);
        googleErrorDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void onQueryPremiumStatusFinished(boolean isSuccessful, boolean isPremium) {

        alreadyGotGoogleAnswer = true;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        initDisplay();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        if (isSuccessful) {

            if (isPremium) {
                progressDialog.show();
                progressDialog.setTitle(getString(R.string.connecting_google_servers));
                dbManager.updatePremiumStatus();
            } else if (!BuildConfig.DEBUG) {
                showPremiumDialog();
            }

            // Getting the user "Show Rate Us Dialog" preference
            int rateUsDialogCounterPref = sp.getInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, 1);

            editor.putBoolean(AppConsts.SharedPrefs.FAVOURITES_DIALOG_WAS_SHOWN, false);

            boolean increaseRateUsDialogCounterPref = (rateUsDialogCounterPref != AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN)
                    && (rateUsDialogCounterPref % AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER_REPEAT != 0);

            if (increaseRateUsDialogCounterPref) {
                rateUsDialogCounterPref++;
                editor.putInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, rateUsDialogCounterPref);
            }

        } else {
            showGoogleErrorDialog(googleErrorDialogTitle, errorMessage);
        }

        editor.apply();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case REQ_CODE_PREFERENCES:
                    setMainMenuDisplay();
                    break;
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    private void initDisplay() {

        if (getResources().getBoolean(R.bool.isTablet)) {
            if (getResources().getBoolean(R.bool.isSmallTablet))
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_main);

        setMainMenuDisplay();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout btn_myRecipesLists = (LinearLayout) findViewById(R.id.layout_myRecipesLists);
        btn_myRecipesLists.setOnClickListener(this);

        LinearLayout btn_mealPlanner = (LinearLayout) findViewById(R.id.layout_mealPlanner);
        btn_mealPlanner.setOnClickListener(this);

        LinearLayout btn_searchRecipesOnline = (LinearLayout) findViewById(R.id.layout_onlineSearch);
        btn_searchRecipesOnline.setOnClickListener(this);

        LinearLayout btn_addRecipeManually = (LinearLayout) findViewById(R.id.layout_addRecipeManually);
        btn_addRecipeManually.setOnClickListener(this);

        LinearLayout btn_settings = (LinearLayout) findViewById(R.id.layout_settings);
        btn_settings.setOnClickListener(this);

        LinearLayout btn_login = (LinearLayout) findViewById(R.id.layout_logout);
        btn_login.setOnClickListener(this);
    }

//-------------------------------------------------------------------------------------------------

    private void setMainMenuDisplay() {

        ImageView imageView_settings = (ImageView) findViewById(R.id.imageView_settings);
        ImageView imageView_addOwnRecipe = (ImageView) findViewById(R.id.imageView_addOwnRecipe);
        ImageView imageView_onlineSearch = (ImageView) findViewById(R.id.imageView_onlineSearch);
        ImageView imageView_recipesLists = (ImageView) findViewById(R.id.imageView_recipesLists);
        ImageView imageView_login = (ImageView) findViewById(R.id.imageView_login);
        ImageView imageView_mealPlanner = (ImageView) findViewById(R.id.imageView_mealPlanner);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String mainMenuDisplayPref = sp.getString(AppConsts.SharedPrefs.MAIN_MENU_DISPLAY, AppConsts.SharedPrefs.COLORFUL);

        if (mainMenuDisplayPref.equals(AppConsts.SharedPrefs.COLORFUL)) {
            imageView_settings.setImageResource(R.drawable.btn_settings);
            imageView_addOwnRecipe.setImageResource(R.drawable.btn_add_manually);
            imageView_onlineSearch.setImageResource(R.drawable.btn_online_search);
            imageView_recipesLists.setImageResource(R.drawable.btn_lists);
            imageView_login.setImageResource(R.drawable.btn_login);
            imageView_mealPlanner.setImageResource(R.drawable.btn_plan_meal);
        } else {
            imageView_settings.setImageResource(R.drawable.btn_simple_settings);
            imageView_addOwnRecipe.setImageResource(R.drawable.btn_simple_add_manually);
            imageView_onlineSearch.setImageResource(R.drawable.btn_simple_online_search);
            imageView_recipesLists.setImageResource(R.drawable.btn_simple_lists);
            imageView_login.setImageResource(R.drawable.btn_simple_login);
            imageView_mealPlanner.setImageResource(R.drawable.btn_simple_plan_meal);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsHelper.setScreenName(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int rateUsDialogCounterPref = savedInstanceState.getInt("rateUsDialogCounterPref", AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN);

        if (rateUsDialogCounterPref != AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, rateUsDialogCounterPref);
            editor.apply();
        }

        boolean googleErrorDialogIsShowing = savedInstanceState.getBoolean("googleErrorDialogIsShowing");
        if (googleErrorDialogIsShowing) {
            googleErrorDialogTitle = savedInstanceState.getString("googleErrorDialogTitle");
            errorMessage = savedInstanceState.getString("errorMessage");
            showGoogleErrorDialog(googleErrorDialogTitle, errorMessage);
        }

//        boolean loginDialogIsShowing = savedInstanceState.getBoolean("loginDialogIsShowing");
//        if (loginDialogIsShowing) {
//            String currentUserInput = savedInstanceState.getString("currentUserInput");
//            showLoginDialog(currentUserInput);
//        }

        boolean premiumDialogIsShowing = savedInstanceState.getBoolean("premiumDialogIsShowing");
        if (premiumDialogIsShowing) {
            showPremiumDialog();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int rateUsDialogCounterPref = sp.getInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, 1);

        if (rateUsDialogCounterPref != AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN) {

            outState.putInt("rateUsDialogCounterPref", rateUsDialogCounterPref);
        }

        outState.putBoolean("alreadyGotGoogleAnswer", alreadyGotGoogleAnswer);

        if (googleErrorDialog != null && googleErrorDialog.isShowing()) {
            outState.putBoolean("googleErrorDialogIsShowing", true);
            outState.putString("errorMessage", errorMessage);
            outState.putString("googleErrorDialogTitle", googleErrorDialogTitle);
        }

//        if (loginDialog != null && loginDialog.isShowing()) {
//            outState.putBoolean("loginDialogIsShowing", true);
//
//            EditText editText_userInput = (EditText) loginDialog.findViewById(R.id.editText_userInput);
//            String currentUserInput = editText_userInput.getText().toString();
//            outState.putString("currentUserInput", currentUserInput);
//
//        } else
        if (premiumDialog != null && premiumDialog.isShowing()) {
            outState.putBoolean("premiumDialogIsShowing", true);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        super.onDestroy();

        bus.unregister(this);

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
    public void onClick(View v) {

        Intent intent;

        switch (v.getId()) {

            case R.id.layout_myRecipesLists:
                intent = new Intent(this, RecipesListsActivity.class);
                intent.setAction(String.valueOf(AppConsts.Actions.NO_ACTION));
                startActivity(intent);
                break;

            case R.id.layout_mealPlanner:
                intent = new Intent(this, MealPlannerActivity.class);
                startActivity(intent);
                break;

            case R.id.layout_onlineSearch:
                intent = new Intent(this, OnlineSearchActivity.class);
                startActivity(intent);
                break;

            case R.id.layout_addRecipeManually:

                intent = new Intent(this, RecipeDetailsActivity.class);
                intent.setAction(String.valueOf(AppConsts.Actions.ADD_NEW_USER_RECIPE));
                startActivity(intent);
                break;

            case R.id.layout_settings:

                if (getResources().getBoolean(R.bool.isTablet) && !getResources().getBoolean(R.bool.isSmallTablet)) {
                    intent = new Intent(this, PreferencesActivityForLargeTablet.class);
                } else {
                    intent = new Intent(this, PreferencesActivity.class);
                }

                startActivityForResult(intent, REQ_CODE_PREFERENCES);
                break;

            case R.id.layout_logout:

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sp.edit();

                editor.clear();
                editor.apply();

                intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    public void showPremiumDialog() {

        premiumDialog = new Dialog(this);

        // hide to default title for Dialog
        premiumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_premium, null, false);
        premiumDialog.setCanceledOnTouchOutside(false);
        premiumDialog.setContentView(view);

        ArrayList<String> limitationsList = new ArrayList<>();
        limitationsList.add(getString(R.string.limitation_search_filters));
        limitationsList.add(getString(R.string.limitation_search_results));
        limitationsList.add(getString(R.string.limitation_downloaded_recipes));
//        limitationsList.add(getString(R.string.limitation_created_recipes));
        limitationsList.add(getString(R.string.limitation_categories));
        limitationsList.add(getString(R.string.limitation_servings_types));

        String dialogTitle = getString(R.string.trial_version_limitation);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        if (getResources().getBoolean(R.bool.isTablet)
                && !(getResources().getBoolean(R.bool.isSmallTablet))
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 10 inches tablet in landscape mode

            premiumDialog.getWindow().setLayout((3 * width) / 5, ActionBar.LayoutParams.WRAP_CONTENT);

        } else {
            premiumDialog.getWindow().setLayout((6 * width) / 7, ActionBar.LayoutParams.WRAP_CONTENT);
        }

        TextView textView_dialogTitle = (TextView) premiumDialog.findViewById(R.id.textView_limitationsTitle);
        textView_dialogTitle.setText(dialogTitle);

        ListView listView = (ListView) premiumDialog.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.listitem_premium_limitation, R.id.textView_listItem, limitationsList);
        listView.setAdapter(adapter);

        Button btnUpgrade = (Button) premiumDialog.findViewById(R.id.button_upgrade);
        btnUpgrade.setOnClickListener(v -> {

            AnalyticsHelper.sendEvent(MainActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "Purchase Premium button clicked");

            if (mIabHelper != null && iabHelperWasAlreadySetUpSuccessfully) {

                purchasePremiumAccess();

            } else {

                progressDialog.setTitle(getString(R.string.connecting_google_servers));
                progressDialog.show();

                mIabHelper = new IabHelperNonStatic(MainActivity.this);

                try {
                    mIabHelper.startSetup(result -> {

                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();

                        if (result.isSuccess()) {
                            // Hooray, IAB is fully set up!

                            iabHelperWasAlreadySetUpSuccessfully = true;
                            purchasePremiumAccess();

                        } else {
                            // Oh no, there was a problem.

                            if (result.toString().contains("unavailable on device") || result.toString().contains("Billing Unavailable")) {
                                errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + getString(R.string.Billing_unavailable_for_device);
                            } else {
                                errorMessage = getString(R.string.problem_starting_purchase_progress) + ":\n" + result;
                            }

                            showGoogleErrorDialog(getString(R.string.purchase_has_failed), errorMessage);
                            Log.d(TAG, errorMessage);
                        }
                    });

                } catch (MyIllegalStateException e) {
                    errorMessage = getString(R.string.problem_starting_purchase_progress);
                    showGoogleErrorDialog(getString(R.string.purchase_has_failed), errorMessage);
                    Log.d(TAG, errorMessage);
                    e.printStackTrace();
                }
            }

            premiumDialog.dismiss();
        });

        Button btnNotNow = (Button) premiumDialog.findViewById(R.id.button_notNow);
        btnNotNow.setOnClickListener(v -> {

            AnalyticsHelper.sendEvent(MainActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "Not Now Button Clicked");

            premiumDialog.dismiss();
        });

        premiumDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void purchasePremiumAccess() {

        final IabHelperNonStatic.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = (result, purchase) -> {

            if (result.isFailure()) {
                errorMessage = result.toString();
                if (errorMessage.contains("Already Owned")) {
                    // The user has already a Premium status
                    dbManager.updatePremiumStatus();
                } else {
                    errorMessage = getString(R.string.error_purchasing) + ": " + result;
                }

                showGoogleErrorDialog(getString(R.string.purchase_has_failed), errorMessage);
                Log.d(TAG, errorMessage);

            } else if (purchase.getSku().equals(AppConsts.SKU_PREMIUM)) {
                // give user access to premium content
                progressDialog.dismiss();

                AnalyticsHelper.sendEvent(MainActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "User purchased premium access");
                View mainView = findViewById(android.R.id.content);
                AppHelper.showSnackBar(mainView, R.string.purchase_complete_successfully, ContextCompat.getColor(this, R.color.colorSnackbarGreen));

                dbManager.updatePremiumStatus();
            }
        };

        try {
            mIabHelper.launchPurchaseFlow(MainActivity.this, AppConsts.SKU_PREMIUM, AppConsts.REQ_CODE_PURCHASE, mPurchaseFinishedListener);

        } catch (MyIllegalStateException e) {
            errorMessage = getString(R.string.problem_starting_purchase_progress);
            showGoogleErrorDialog(getString(R.string.purchase_has_failed), errorMessage);
            e.printStackTrace();
        }
    }

//-------------------------------------------------------------------------------------------------

//    private void showLoginDialog(String currentUserInput) {
//
//        loginDialog = new Dialog(this);
//
//        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View view = inflater.inflate(R.layout.dialog_login, null, false);
//        loginDialog.setCanceledOnTouchOutside(false);
//        loginDialog.setContentView(view);
//
//        final EditText editText_userInput = (EditText) loginDialog.findViewById(R.id.editText_userInput);
//        editText_userInput.setText(currentUserInput);
//
//        // Relocating the selection after the last char of the editText
//        int pos = editText_userInput.getText().length();
//        editText_userInput.setSelection(pos);
//
//        TextView textView_dialogTitleNote = (TextView) loginDialog.findViewById(R.id.textView_titleNote);
//        textView_dialogTitleNote.setText(getString(R.string.the_username_used_as_author) + " " + (getString(R.string.when_creating_own_recipe)));
//
//        Button button_save = (Button) loginDialog.findViewById(R.id.button_save);
//        button_save.setOnClickListener(v -> {
//
//            // Get the input text
//            String userName = editText_userInput.getText().toString();
//
//            if (usernameTooShort(userName)) {
//                // Less than 6 letters
//                AppHelper.showSnackBar(view, R.string.at_least_6_characters_are_required, Color.RED);
//                return;
//            }
//
//            if (usernameTooLong(userName)) {
//                // More than 20 letters
//                AppHelper.showSnackBar(view, R.string.max_20_chars_are_allowed, Color.RED);
//                return;
//            }
//
//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//            SharedPreferences.Editor editor = sp.edit();
//            editor.putString(AppConsts.SharedPrefs.USER_NAME, userName);
//            editor.commit();
//
//            AnalyticsHelper.sendEvent(MainActivity.this, AppConsts.Analytics.CATEGORY_LOGIN, "Login successfully", userName);
//
//            loginDialog.dismiss();
//        });
//
//        Button btnCancel = (Button) loginDialog.findViewById(R.id.button_cancel);
//        btnCancel.setOnClickListener(v -> loginDialog.dismiss());
//        loginDialog.show();
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    private boolean usernameTooShort(String username) {
//        // checks if the username has less than 6 letters excluding spaces.
//        int trimmedLength = username.trim().length();
//        if (trimmedLength >= 6) {
//            return false;
//        } else {
//            return true;
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    private boolean usernameTooLong(String username) {
//        // checks if the username has less than 6 letters excluding spaces.
//        int trimmedLength = username.trim().length();
//        if (trimmedLength > 20) {
//            return true;
//        } else {
//            return false;
//        }
//    }

}
