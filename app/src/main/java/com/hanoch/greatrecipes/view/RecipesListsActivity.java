package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.GGGRecipe2;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.OnUpdateUserCompletedEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserFavouriteRecipesIdsCompletedEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipeEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.model.MyListFragment;
import com.hanoch.greatrecipes.model.Recipe;
import com.hanoch.greatrecipes.bus.OnTabChangedEvent;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.squareup.otto.Subscribe;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecipesListsActivity extends AppCompatActivity implements
        ToolbarMenuSetting,
        RecipeReviewFragment2.FragmentRecipeReviewListener,
        EditRecipeFragment2.OnFragmentEditRecipeListener,
        ListFragmentListener {

    private static final int REQ_CODE_RECIPE_DETAILS_ACTIVITY = 10;
    private static final int REQ_CODE_SEARCH_IN_LISTS_ACTIVITY = 20;

    private MyListFragment listFragment;
    private RecipeReviewFragment2 recipeReviewFragment;
    private EditRecipeFragment2 editRecipeFragment;
    private WebViewFragment webViewFragment;
    private CategoriesChooserFragment categoriesChooserFragment;

    private Toolbar toolbar;
    private String extra_serving;

    private int currentlyShownList;

    private MenuItem toolbar_save;
    private MenuItem toolbar_addToList;
    private MenuItem toolbar_edit;
    private MenuItem toolbar_delete;
    private MenuItem toolbar_addToFavourites;
    private MenuItem toolbar_removeFromFavourites;
    private MenuItem toolbar_addServing;
    private MenuItem toolbar_search;
    private MenuItem toolbar_ok;
    private MenuItem toolbar_closeWebView;

    private String mRecipeId;
    private boolean isUserRecipe;

    private ArrayList<String> checkedItemsId;
    private String activityToolbarTitle;
    private ArrayList<ObjectAnimator> fadingInAnimationsList;
    private boolean tabChanged;
    private Bundle savedInstanceState;
    private int toolbarColor;
    private String toolbarTitle;

    private LinearLayout layout_logo;
    private MyBus bus;
    private Dialog rateUsDialog;

    private GreatRecipesDbManager dbManager;
    private AppStateManager appStateManager;

    private ProgressDialog progressDialog;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        bus = MyBus.getInstance(); // Singleton bus instance
        bus.register(this);

        dbManager = GreatRecipesDbManager.getInstance();
        appStateManager = AppStateManager.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));

        checkedItemsId = new ArrayList<>();

        activityToolbarTitle = getString(R.string.recipes_lists);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(activityToolbarTitle);

        toolbarTitle = activityToolbarTitle;
        toolbarColor = AppConsts.ToolbarColor.PRIMARY;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(0);
        }

        setSupportActionBar(toolbar);

        AppHelper.hideTheKeyboard(this);

        Intent prevIntent = getIntent();
        extra_serving = prevIntent.getStringExtra(AppConsts.Extras.EXTRA_SERVING);
        int containerResId;

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet

            layout_logo = (LinearLayout) findViewById(R.id.layout_logo);

            TextView textView_greatRecipes = (TextView) findViewById(R.id.textView_greatRecipes);
            textView_greatRecipes.setTypeface(MyFonts.getInstance(this).getMotionPictureFont());

            containerResId = R.id.layout_listContainer;

        } else {
            // Phone
            containerResId = R.id.layout_container;
        }

        if (savedInstanceState == null) {
            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically

            Fragment tabsFragment = TabsFragment.newInstance(extra_serving);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(containerResId, tabsFragment, AppConsts.Fragments.TABS);
            ft.commit();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        if (getResources().getBoolean(R.bool.isTablet)) {
            if (getResources().getBoolean(R.bool.isSmallTablet))
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        AnalyticsHelper.setScreenName(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();

        if (rateUsDialog == null) {
            // Getting the user "Show Rate Us Dialog" preference
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            int rateUsDialogCounterPref = sp.getInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, 1);
            boolean showRateUsDialogPref = (rateUsDialogCounterPref != AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN
                    && rateUsDialogCounterPref % AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER_REPEAT == 0);

            if (showRateUsDialogPref) {
                showRateUsDialog();
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (rateUsDialog != null && rateUsDialog.isShowing()) {
            outState.putBoolean("rateUsDialogIsShowing", true);

            CheckBox checkBox = (CheckBox) rateUsDialog.findViewById(R.id.checkBox_dontShowAgain);
            outState.putBoolean("currentCheckboxState", checkBox.isChecked());
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            int layout_logoVisibility = layout_logo.getVisibility();
            outState.putInt("layout_logoVisibility", layout_logoVisibility);
        }

        outState.putStringArrayList("checkedItemsId", checkedItemsId);

        outState.putString("mRecipeId", mRecipeId);
//        outState.putLong("mRecipeId", mRecipeId);
        outState.putString("extra_serving", extra_serving);
        outState.putInt("toolbarColor", toolbarColor);
        outState.putString("toolbarTitle", toolbarTitle);
        outState.putInt("currentlyShownList", currentlyShownList);

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.SAVE);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.EDIT);
        buttons.add(AppConsts.ToolbarButtons.DELETE);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.SEARCH);
        buttons.add(AppConsts.ToolbarButtons.OK);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);

        ArrayList<Integer> displayedButtons = new ArrayList<>();

        MenuItem toolBarButton;
        for (Integer button : buttons) {

            toolBarButton = toolbar.getMenu().findItem(button);
            if (toolBarButton != null && toolBarButton.isVisible()) {
                displayedButtons.add(button);
            }
        }

        if (!displayedButtons.isEmpty()) {
            outState.putIntegerArrayList("displayedButtons", displayedButtons);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        boolean rateUsDialogIsShowing = savedInstanceState.getBoolean("rateUsDialogIsShowing");
        if (rateUsDialogIsShowing) {
            boolean currentCheckboxState = savedInstanceState.getBoolean("currentCheckboxState");
            final CheckBox checkBox = (CheckBox) rateUsDialog.findViewById(R.id.checkBox_dontShowAgain);
            checkBox.setChecked(currentCheckboxState);
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            int layout_logoVisibility = savedInstanceState.getInt("layout_logoVisibility");
            layout_logo.setVisibility(layout_logoVisibility);
        }

        checkedItemsId = savedInstanceState.getStringArrayList("checkedItemsId");
        mRecipeId = savedInstanceState.getString("mRecipeId");
        extra_serving = savedInstanceState.getString("extra_serving");
        currentlyShownList = savedInstanceState.getInt("currentlyShownList");

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {

            if (fragment instanceof ListOnlineFragment2 && currentlyShownList == AppConsts.ListType.ONLINE_RECIPES) {
                listFragment = (MyListFragment) fragment;
                break;
            } else if (fragment instanceof ListMyOwnFragment2 && currentlyShownList == AppConsts.ListType.MY_OWN_RECIPES) {
                listFragment = (MyListFragment) fragment;
                break;
            } else if (fragment instanceof ListFavouriteFragment2 && currentlyShownList == AppConsts.ListType.FAVOURITES_RECIPES) {
                listFragment = (MyListFragment) fragment;
                break;
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bus.unregister(this);
    }

//-------------------------------------------------------------------------------------------------

    private void showRateUsDialog() {

        rateUsDialog = new Dialog(this);

        rateUsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Context context = this;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_rate_us, null, false);
        rateUsDialog.setCanceledOnTouchOutside(false);
        rateUsDialog.setCancelable(false);
        rateUsDialog.setContentView(view);

        final CheckBox checkBox = (CheckBox) rateUsDialog.findViewById(R.id.checkBox_dontShowAgain);

        Button button_rateUs = (Button) rateUsDialog.findViewById(R.id.button_rateUs);
        button_rateUs.setOnClickListener(v -> {

            AnalyticsHelper.sendEvent(RecipesListsActivity.this, AppConsts.Analytics.CATEGORY_RATE_US, "Rate us button was clicked");

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN);
            editor.apply();

            Uri myAppUri = Uri.parse("market://details?id=" + context.getPackageName());
            final Intent intent = new Intent(Intent.ACTION_VIEW, myAppUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            rateUsDialog.dismiss();
        });

        Button button_notNow = (Button) rateUsDialog.findViewById(R.id.button_notNow);
        button_notNow.setOnClickListener(v -> {

            AnalyticsHelper.sendEvent(RecipesListsActivity.this, AppConsts.Analytics.CATEGORY_RATE_US, "Not now button was clicked");

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            int rateUsDialogCounterPref;

            if (checkBox.isChecked()) {
                rateUsDialogCounterPref = AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN;

            } else {
                rateUsDialogCounterPref = sp.getInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, 1);
                rateUsDialogCounterPref++;
            }

            editor.putInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, rateUsDialogCounterPref);
            editor.apply();

            rateUsDialog.dismiss();
        });

        rateUsDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onRecipeClick(MyListFragment listFragment, String recipeId) {

        this.listFragment = listFragment;

        currentlyShownList = listFragment.getContainedListType();

        tabChanged = false;

        if (getResources().getBoolean(R.bool.isTablet)) {
            // tablet

            FragmentManager fm = getSupportFragmentManager();

            webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
            if (webViewFragment != null) {
                onBackPressed();
            }

            FragmentTransaction ft = fm.beginTransaction();

            recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (mRecipeId != null && mRecipeId.equals(recipeId) && recipeReviewFragment != null) {
                // The currently displayed recipe was clicked again
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

            } else {

                if (layout_logo.getVisibility() == View.VISIBLE) {
                    AnimationHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                }

                User user = appStateManager.user;

                mRecipeId = recipeId;
                isUserRecipe = user.userRecipes.containsKey(mRecipeId);

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if (extra_serving == null) {

                    if (appStateManager.isRecipeFavourite(recipeId)) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
                    }

                    if (isUserRecipe) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                    }

                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                }

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

                updateRecipeReviewFragment();
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
            }

            ft.commit();

        } else {
            // phone

            mRecipeId = recipeId;

            isUserRecipe = appStateManager.user.userRecipes.containsKey(mRecipeId);

            Intent intent = new Intent(this, RecipeDetailsActivity.class);
            intent.setAction(AppConsts.Actions.ACTION_REVIEW);
            intent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, recipeId);
            intent.putExtra(AppConsts.Extras.EXTRA_IS_USER_RECIPE, isUserRecipe);
            intent.putExtra(AppConsts.Extras.EXTRA_SERVING, extra_serving);
            startActivityForResult(intent, REQ_CODE_RECIPE_DETAILS_ACTIVITY);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onListItemChecked(MyListFragment listFragment, String id, boolean isChecked) {

        this.listFragment = listFragment;

        currentlyShownList = listFragment.getContainedListType();

        if (isChecked) {
            // Unchecked item was checked

            checkedItemsId.add(id);

            toolbarTitle = checkedItemsId.size() + " " + getString(R.string.selected);
            toolbar.setTitle(toolbarTitle);

            ForTablet:
            if (getResources().getBoolean(R.bool.isTablet)) {
                // Tablet only

                if (mRecipeId.equals(id)) {
                    break ForTablet;
                }

                mRecipeId = id;

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                if (toolbar_closeWebView.isVisible()) {
                    fm.popBackStack();
                }

                updateRecipeReviewFragment();
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                ft.commit();
            }

            if (checkedItemsId.size() == 1) {
                // First item was checked

                if (getResources().getBoolean(R.bool.isTablet)) {
                    if (layout_logo.getVisibility() == View.VISIBLE) {
                        AnimationHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                    }
                }

                ArrayList<Integer> toolbarButtons = new ArrayList<>();
                toolbarButtons.add(AppConsts.ToolbarButtons.DELETE);
                setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, toolbarTitle);
            }

        } else {
            // Checked item was unchecked

            checkedItemsId.remove(id);

            if (checkedItemsId.isEmpty()) {
                // The last checked item was unchecked

                if (getResources().getBoolean(R.bool.isTablet)) {

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    if (toolbar_closeWebView.isVisible()) {
                        fm.popBackStack();
                    }

                    recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                    ft.remove(recipeReviewFragment);
                    ft.commit();

                    AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);
                }

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            } else {
                // There are still checked items

                toolbarTitle = checkedItemsId.size() + " " + getString(R.string.selected);
                toolbar.setTitle(toolbarTitle);

                if (getResources().getBoolean(R.bool.isTablet)) {
                    // Tablet only

                    mRecipeId = checkedItemsId.get(checkedItemsId.size() - 1);

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    if (toolbar_closeWebView.isVisible()) {
                        fm.popBackStack();
                    }

                    updateRecipeReviewFragment();
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                    ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                    ft.commit();
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // The Activity recipeDetailsActivity set result after the user
            // decided to add the serving to the meal planning screen:

            switch (requestCode) {

                case REQ_CODE_RECIPE_DETAILS_ACTIVITY:
                case REQ_CODE_SEARCH_IN_LISTS_ACTIVITY:

                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    long recipeId = -1;
                    if (data != null) {
                        recipeId = data.getLongExtra(AppConsts.Extras.EXTRA_RECIPE_ID, -1);
                    }
                    resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, recipeId);
                    finish();
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAddCategoriesClick(ArrayList<String> categoriesList) {
        // Tablet only

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        categoriesChooserFragment = CategoriesChooserFragment.newInstance(categoriesList);

        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);

        ft.replace(R.id.layout_detailsContainer, categoriesChooserFragment, AppConsts.Fragments.CATEGORIES_CHOOSER);

        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.OK);

        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.NO_CHANGE, getString(R.string.choose_categories));
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAddNewRecipeClick(MyListFragment listFragment, int listTypeIndex) {

        mRecipeId = null;

        this.listFragment = listFragment;

        currentlyShownList = listFragment.getContainedListType();

        Intent intent;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        switch (listTypeIndex) {

            case AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH:

                if (!premium) {

                    int downloadedRecipesCount = sp.getInt(AppConsts.SharedPrefs.DOWNLOADED_COUNTER, 0);

                    if (downloadedRecipesCount == 3) {
                        // The free-trial limitation for downloading recipes is exceeded

                        AnalyticsHelper.sendEvent(RecipesListsActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "You exceeded snackbar was shown", "Online");

                        View view = this.findViewById(android.R.id.content);
                        AppHelper.showSnackBar(view, R.string.you_exceeded_the_downloaded_recipes_limit, ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial));
                        break;
                    }
                }

                intent = new Intent(this, OnlineSearchActivity.class);
                startActivity(intent);
                break;

            case AppConsts.RecipeOrigin.ADDED_MANUALLY:

                if (!premium) {

                    int createdRecipesCount = sp.getInt(AppConsts.SharedPrefs.CREATED_COUNTER, 0);

                    if (createdRecipesCount == 3) {
                        // The free-trial limitation for creating recipes is exceeded

                        AnalyticsHelper.sendEvent(RecipesListsActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "You exceeded snackbar was shown", "Online");

                        View view = this.findViewById(android.R.id.content);
                        AppHelper.showSnackBar(view, R.string.you_exceeded_the_recipes_creation_limit, ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial));
                        break;
                    }
                }

                if (getResources().getBoolean(R.bool.isTablet)) {
                    // tablet

                    if (layout_logo.getVisibility() == View.VISIBLE) {
                        AnimationHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                    }

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
                    if (webViewFragment != null) {
                        fm.popBackStack();
                    }

                    recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                    if (recipeReviewFragment != null) {
                        ft.remove(recipeReviewFragment);
                    }

                    editRecipeFragment = EditRecipeFragment2.newInstance(AppConsts.Actions.ACTION_ADD_NEW, null);
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                    ft.replace(R.id.layout_detailsContainer, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);
                    ft.commit();

                    ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);

                    setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

                } else {
                    // phone

                    intent = new Intent(this, RecipeDetailsActivity.class);
                    intent.setAction(AppConsts.Actions.ACTION_ADD_NEW);
                    startActivity(intent);
                }

                break;
        }
    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onRecipeWasSaved(UserRecipe userRecipe) {
//        // Tablet only
//
//        mRecipeId = userRecipe._id;
//
//        AppHelper.hideKeyboardFrom(this, getCurrentFocus());
//
//        FragmentManager fm = getSupportFragmentManager();
//
//        recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
//
//        FragmentTransaction ft = fm.beginTransaction();
//
//        updateRecipeReviewFragment();
//        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_up);
//        ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
//        ft.commit();
//
//        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
//
//        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
//
//        if (appStateManager.user.favouriteRecipesIds.contains(userRecipe._id)) {
//            toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
//        } else {
//            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
//        }
//
//        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
//    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCancelLoginButtonClicked() {
        // in edit recipe fragment

        if (getResources().getBoolean(R.bool.isTablet)) {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
            ft.remove(editRecipeFragment);
            ft.commit();

            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

            listFragment.backToDefaultDisplay(true);

        } else {
            finish();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onGetInstructionsClick(String url) {
        // Tablet only

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        webViewFragment = WebViewFragment.newInstance(url);

        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_up, R.anim.slide_out_up);

        ft.replace(R.id.layout_detailsContainer, webViewFragment, AppConsts.Fragments.WEB_VIEW);
        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void setToolbarAttr(ArrayList<Integer> toolbarButtonsList, int color, String title) {

        if (tabChanged) {

            tabChanged = false;

            if (toolbar_search.isVisible() && toolbar_search.isEnabled()) {
                return;
            }
        }

        toolbarTitle = title;
        toolbar.setTitle(title);

        if (color != AppConsts.ToolbarColor.NO_CHANGE) {
            toolbarColor = color;
            toolbar.setBackgroundColor(color);
        }

        ArrayList<Integer> allButtons = getAndHideAllToolbarButtons();

        fadingInAnimationsList = new ArrayList<>();

        if (toolbarButtonsList == null) {
            fadingInAnimationsList.add(AnimationHelper.animateToolbarButtonFadingIn(toolbar_search, 500, 600));

        } else {

            allButtons.stream().filter(toolbarButtonsList::contains).forEach(button -> {
                MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                fadingInAnimationsList.add(AnimationHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 600));
            });
        }
    }

//-------------------------------------------------------------------------------------------------

    public ArrayList<Integer> getAndHideAllToolbarButtons() {

        if (fadingInAnimationsList != null) {
            // Cancelling all buttons "fading-in" animations, if exists
            fadingInAnimationsList.forEach(ValueAnimator::cancel);
        }

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.SAVE);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.EDIT);
        buttons.add(AppConsts.ToolbarButtons.DELETE);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.SEARCH);
        buttons.add(AppConsts.ToolbarButtons.OK);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);

        // Resetting all buttons to invisible
        MenuItem toolBarButton;
        for (Integer button : buttons) {

            toolBarButton = toolbar.getMenu().findItem(button);
            if (toolBarButton.isVisible()) {
                AnimationHelper.animateToolbarButtonFadingOut(toolBarButton, 500, 0);
            }
        }

        return buttons;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_recipes_lists, menu);

        toolbar_search = menu.findItem(R.id.action_search).setVisible(false);

        toolbar_save = menu.findItem(R.id.action_save).setVisible(false);
        toolbar_addToList = menu.findItem(R.id.action_addToList).setVisible(false);
        toolbar_edit = menu.findItem(R.id.action_edit).setVisible(false);
        toolbar_delete = menu.findItem(R.id.action_delete).setVisible(false);
        toolbar_addToFavourites = menu.findItem(R.id.action_addToFavourites).setVisible(false);
        toolbar_removeFromFavourites = menu.findItem(R.id.action_removeFromFavourites).setVisible(false);
        toolbar_addServing = menu.findItem(R.id.action_addServing).setVisible(false);
        toolbar_ok = menu.findItem(R.id.action_ok).setVisible(false);
        toolbar_closeWebView = menu.findItem(R.id.action_closeWebview).setVisible(false);

        if (savedInstanceState == null) {

            toolbar_search.setVisible(true);

        } else {
            toolbarColor = savedInstanceState.getInt("toolbarColor");
            toolbarTitle = savedInstanceState.getString("toolbarTitle");
            ArrayList<Integer> displayedButtons = savedInstanceState.getIntegerArrayList("displayedButtons");

            toolbar.setBackgroundColor(toolbarColor);
            toolbar.setTitle(toolbarTitle);

            if (displayedButtons != null) {

                for (Integer button : displayedButtons) {

                    MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                    toolbarButton.setVisible(true);
                }
            } else {
                // Specific case: the user uses phone and get in the activity in landscape mode

                toolbar_search.setVisible(true);
            }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        Intent intent;

        ArrayList<Integer> toolbarButtonsList;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
        recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
        editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);

        switch (itemId) {

            case R.id.action_search:

                intent = new Intent(this, SearchInListsActivity.class);
                intent.putExtra(AppConsts.Extras.EXTRA_SERVING, extra_serving);
                startActivityForResult(intent, REQ_CODE_SEARCH_IN_LISTS_ACTIVITY);

                break;

            case R.id.action_edit:
                // Tablet only - "my Own" recipe only

                ListView listView = (ListView) listFragment.getView().findViewById(R.id.listView_recipesList);
                listView.setEnabled(false);

                editRecipeFragment = EditRecipeFragment2.newInstance(AppConsts.Actions.ACTION_EDIT, mRecipeId);
                ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_up);
                ft.replace(R.id.layout_detailsContainer, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);
                ft.commit();

                toolbarButtonsList = new ArrayList<>();

                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

                break;

            case R.id.action_save:
                // Saving a recipe after finishing editing a recipe

                progressDialog.show();

                UserRecipe userRecipeToSave = editRecipeFragment.onSaveUserRecipeClicked();
                dbManager.updateUserRecipe(userRecipeToSave);

                break;

            case R.id.action_addToList:
                // Saving after adding a new recipe to "my own" list

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

                if (!premium) {

                    int createdRecipesCount = sp.getInt(AppConsts.SharedPrefs.CREATED_COUNTER, 0);
                    createdRecipesCount++;
                    SharedPreferences.Editor editor = sp.edit();

                    editor.putInt(AppConsts.SharedPrefs.CREATED_COUNTER, createdRecipesCount);
                    editor.apply();
                }

                progressDialog.show();

                UserRecipe userRecipeToAdd = editRecipeFragment.onSaveUserRecipeClicked();
                dbManager.addUserRecipe(userRecipeToAdd);

                break;

            case R.id.action_delete:

                onDeleteItemsClicked();

                break;

            case R.id.action_addServing:
                // Tablet only, while reviewing a recipe

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, mRecipeId);
                resultIntent.putExtra(AppConsts.Extras.EXTRA_IS_USER_RECIPE, isUserRecipe);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToFavourites:
            case R.id.action_removeFromFavourites:
                // Tablet only, while reviewing a recipe

                progressDialog.show();
                dbManager.updateUserFavouriteRecipesIds(mRecipeId, itemId == R.id.action_addToFavourites);
                break;

            case R.id.action_ok:
                // The user finished choosing categories in categoriesChooserFragment

                categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag(AppConsts.Fragments.CATEGORIES_CHOOSER);
                ArrayList<String> categoriesList = categoriesChooserFragment.getSelectedCategoriesNamesList();

                editRecipeFragment.updateCategoriesList(categoriesList);

                onBackPressed();

                break;

            case R.id.action_closeWebview:
                // Tablet only

                onBackPressed();

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public void onDeleteItemsClicked() {

        AppHelper.vibrate(this);

        final Context context = RecipesListsActivity.this;

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);

        final View view = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(view);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        String dialogTitle;

        if (checkedItemsId.size() > 1) {
            dialogTitle = getString(R.string.deleting) + " " + checkedItemsId.size() + " " + getString(R.string.recipes);

        } else {
            dialogTitle = getString(R.string.deleting) + " " + getString(R.string.a_recipe);
        }

        textView_dialogTitle.setText(dialogTitle);
        textView_dialogContent.setText(getString(R.string.are_you_sure)
                + "\n"
                + getString(R.string.the_operation_is_irreversible));

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(v -> {

            ArrayList<UserRecipe> userRecipes = new ArrayList<>();
            ArrayList<YummlyRecipe> yummlyRecipes = new ArrayList<>();

            for (UserRecipe userRecipe : appStateManager.user.userRecipes.values()) {
                if (checkedItemsId.contains(userRecipe._id)) {
                    userRecipes.add(userRecipe);
                }
            }
            for (YummlyRecipe yummlyRecipe : appStateManager.user.yummlyRecipes.values()) {
                if (checkedItemsId.contains(yummlyRecipe._id)) {
                    yummlyRecipes.add(yummlyRecipe);
                }
            }

            dbManager.updateUserRecipes(userRecipes, yummlyRecipes ,BusConsts.ACTION_DELETE);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (toolbar_closeWebView.isVisible()) {
                // Tablet only

                fm.popBackStack();
            }

            if (recipeReviewFragment != null) {
                // Tablet only

                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.remove(recipeReviewFragment);
                ft.commit();

                AnimationHelper.animateViewFadingIn(context, layout_logo, 500, 500);
            }

            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            listFragment.backToDefaultDisplay(false);

            String toastMessage;

            if (checkedItemsId.size() == 1) {
                toastMessage = getString(R.string.the_recipe_was_deleted);

            } else {
                toastMessage = checkedItemsId.size() + " " + getString(R.string.recipes_were_deleted);
            }

            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();

            checkedItemsId.clear();

            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {

        if (toolbar_search.isVisible()) {
            // Default screen
            finish();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        if (toolbar_delete.isVisible()) {
            // The user renounced selecting recipes to delete

            if (getResources().getBoolean(R.bool.isTablet)) {
                // Tablet only

                AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();
            }

            checkedItemsId.clear();
            listFragment.backToDefaultDisplay(true);
            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            mRecipeId = null;

            return;
        }

        // From now on it's for Tablet only

        if (toolbar_addToFavourites.isVisible()
                || toolbar_removeFromFavourites.isVisible()
                || toolbar_addServing.isVisible()) {
            // The user renounced reviewing a recipe, or adding the recipe to meal planner

            mRecipeId = null;

            AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

            recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
            ft.remove(recipeReviewFragment);
            ft.commit();

            listFragment.backToDefaultDisplay(true);

            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);
            return;
        }

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        if (toolbar_save.isVisible()) {
            // The user renounced editing an exiting recipe

            showExitWithoutSavingDialog(AppConsts.Actions.ACTION_EDIT);

            return;
        }

        if (toolbar_addToList.isVisible()) {
            // The user renounced adding a new recipe to "My own" list

            showExitWithoutSavingDialog(AppConsts.Actions.ACTION_ADD_NEW);

            return;
        }

        if (toolbar_closeWebView.isVisible()) {
            // The user was watching the recipe URL

            fm.popBackStack();

            YummlyRecipe yummlyRecipe = appStateManager.user.yummlyRecipes.get(mRecipeId);

            if (extra_serving == null) {

                if (checkedItemsId.isEmpty()) {

                    if (appStateManager.isRecipeFavourite(yummlyRecipe._id)) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                    }

                } else {
                    // The user was watching the recipe URL while choosing recipes to delete
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.DELETE);
                }

            } else {
                // The user was watching the recipe URL while choosing a recipe to meal planner
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

            return;
        }

        if (toolbar_ok.isVisible()) {
            // The user renounced choosing categories

            fm.popBackStack();

            if (mRecipeId == null) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void updateRecipeReviewFragment() {
        boolean isUserRecipe = false;
        User user = appStateManager.user;

        if (user.yummlyRecipes.containsKey(mRecipeId)) {
            isUserRecipe = false;
        } else if (user.userRecipes.containsKey(mRecipeId)) {
            isUserRecipe = true;
        }

        recipeReviewFragment = RecipeReviewFragment2.newInstance(mRecipeId, isUserRecipe, extra_serving);
    }
//-------------------------------------------------------------------------------------------------

    public void showExitWithoutSavingDialog(final String action) {

        AppHelper.vibrate(this);

        final Dialog dialog = new Dialog(this);

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        final Context context = RecipesListsActivity.this;
        final View view = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(view);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        textView_dialogTitle.setText(R.string.exit_without_saving);
        textView_dialogContent.setText(R.string.are_you_sure);

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(v -> {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            if (action.equals(AppConsts.Actions.ACTION_ADD_NEW)) {

                editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.remove(editRecipeFragment);
                ft.commit();

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                AnimationHelper.animateViewFadingIn(context, layout_logo, 500, 500);

                listFragment.backToDefaultDisplay(true);

            } else {
                // action = ACTION_EDIT

                ListView listView = (ListView) listFragment.getView().findViewById(R.id.listView_recipesList);
                listView.setEnabled(true);

                updateRecipeReviewFragment();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_up);

                if (getResources().getBoolean(R.bool.isTablet)) {
                    ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                } else {
                    ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                }

                ft.commit();

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if (appStateManager.isRecipeFavourite(mRecipeId)) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
                }

                toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
            }

            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {
        // After adding/editing/deleting Recipes or choosing a new serving

        progressDialog.dismiss();

        if (event.isSuccess) {

            FragmentManager fm = getSupportFragmentManager();
            fm.getFragments().stream()
                    .filter(fragment -> fragment instanceof MyListFragment)
                    .forEach(fragment -> ((MyListFragment) fragment).refreshAdapter());

            switch (event.action) {

                case BusConsts.ACTION_DELETE:

                    recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                    if (toolbar_closeWebView.isVisible()) {
                        // Tablet only

                        fm.popBackStack();
                    }

                    if (recipeReviewFragment != null) {
                        // Tablet only
                        FragmentTransaction ft = fm.beginTransaction();

                        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                        ft.remove(recipeReviewFragment);
                        ft.commit();

                        AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);
                    }

                    setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                    listFragment.backToDefaultDisplay(false);

                    String toastMessage;

                    if (checkedItemsId.size() == 1) {
                        toastMessage = getString(R.string.the_recipe_was_deleted);
                    } else {
                        toastMessage = checkedItemsId.size() + " " + getString(R.string.recipes_were_deleted);
                    }

                    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

                    checkedItemsId.clear();
                    break;

                case BusConsts.ACTION_ADD_NEW:
                case BusConsts.ACTION_EDIT:

                    if (extra_serving == null) {
                        // The user added a new recipe or edited a user-recipe
                        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

                        if (getResources().getBoolean(R.bool.isTablet)) {
                            // Tablet

                            AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                            FragmentTransaction ft = fm.beginTransaction();
                            Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                            ft.remove(recipeReviewFragment);
                            ft.commit();

                        } else {
                            onBackPressed();
                        }

                        View rootView = findViewById(android.R.id.content);
                        if (event.action == BusConsts.ACTION_ADD_NEW) {
                            AppHelper.showSnackBar(rootView, R.string.added_to_my_own, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
                        } else if (event.action == BusConsts.ACTION_EDIT) {
                            AppHelper.showSnackBar(rootView, R.string.saved_successfully, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
                        }
                        setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                    } else {
                        // The user selected a serving from the lists
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, mRecipeId);
                        resultIntent.putExtra(AppConsts.Extras.EXTRA_IS_USER_RECIPE, true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                    break;
            }
        } else {
            View mainView = findViewById(android.R.id.content);
            if (event.t instanceof UnknownHostException || event.t instanceof ConnectException) {
                AppHelper.showSnackBar(mainView, R.string.internet_error, Color.RED);
            } else {
                AppHelper.showSnackBar(mainView, R.string.unexpected_error, Color.RED);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserFavouriteRecipesIdsCompletedEvent event) {
        // After toggling favourite button ON TABLET ONLY

        progressDialog.dismiss();
        if (event.isSuccess) {

            FragmentManager fm = getSupportFragmentManager();
            recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (listFragment instanceof ListFavouriteFragment2) {

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.remove(recipeReviewFragment);
                ft.commit();

                AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            } else {
                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                boolean isFavourite = AppStateManager.getInstance().isRecipeFavourite(mRecipeId);
                recipeReviewFragment.setFavouriteImage(isFavourite);

                if (isFavourite) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
                }

                if (isUserRecipe) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                }

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
            }

        } else {
            View mainView = findViewById(android.R.id.content);
            if (event.t instanceof UnknownHostException || event.t instanceof ConnectException) {
                AppHelper.showSnackBar(mainView, R.string.internet_error, Color.RED);
            } else {
                AppHelper.showSnackBar(mainView, R.string.unexpected_error, Color.RED);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    private void onEvent(OnUpdateUserRecipeEvent event) {
        progressDialog.dismiss();

        if (event.isSuccess) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment recipeReviewFragment = RecipeReviewFragment2.newInstance(event.recipe._id, true, extra_serving);

            if (getResources().getBoolean(R.bool.isTablet)) {
                // tablet

                if (layout_logo.getVisibility() == View.VISIBLE) {
                    AnimationHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                }

                boolean recipeDetailsIsShawn = (toolbar_addServing.isVisible() || toolbar_addToList.isVisible() || toolbar_closeWebView.isVisible());
                if (mRecipeId.equals(event.recipe._id) && recipeDetailsIsShawn) {
                    return;
                }

                mRecipeId = event.recipe._id;

                Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
                if (webViewFragment != null) {
                    fm.popBackStack();
                }

                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if (extra_serving != null && extra_serving.equals(AppConsts.Extras.ADD_SERVING)) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
                }

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

            } else {
                // phone

                mRecipeId = event.recipe._id;

                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                ft.addToBackStack(null);
            }

            ft.commit();
        } else {
            int errorResId;
            if (event.t instanceof UnknownHostException || event.t instanceof ConnectException) {
                errorResId = R.string.internet_error;
            } else {
                errorResId = R.string.unexpected_error;
            }

            View rootView = findViewById(android.R.id.content);
            AppHelper.showSnackBar(rootView, errorResId, Color.RED);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onTabChanged(OnTabChangedEvent event) {
        // Bus event

        tabChanged = true;

        if (listFragment != null) {
            listFragment.backToDefaultDisplay(true);
        }

        if (!checkedItemsId.isEmpty()) {
            // The user was selecting items(s) to delete

            checkedItemsId.clear();
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet only

            if (layout_logo.getVisibility() != View.VISIBLE) {
                AnimationHelper.animateViewFadingIn(RecipesListsActivity.this, layout_logo, 500, 500);
            }

            FragmentManager fm = getSupportFragmentManager();

            editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
            webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
            categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag(AppConsts.Fragments.CATEGORIES_CHOOSER);
            recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            FragmentTransaction ft = fm.beginTransaction();

            if (editRecipeFragment != null) {
                // The user was editing an existing recipe, or adding a new  recipe to "My Own" list

                if (categoriesChooserFragment != null) {
                    // The user was choosing categories

                    fm.popBackStack();

                } else {
                    // The user is editing the recipe details

                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                    ft.remove(editRecipeFragment);

                    ft.commit();
                }

            } else {
                // The user was reviewing a recipe, or no recipe was displayed

                if (recipeReviewFragment != null) {
                    // The user was reviewing a recipe

                    if (webViewFragment != null) {
                        // The user was watching the recipe URL

                        fm.popBackStack();
                    }

                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                    ft.remove(recipeReviewFragment);
                }

                ft.commit();
            }
        }

        setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);
    }

}
