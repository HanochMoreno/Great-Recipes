package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.MyBus;
import com.hanoch.greatrecipes.model.MyFragment;
import com.hanoch.greatrecipes.model.Recipe;
import com.hanoch.greatrecipes.model.TabChangedEvent;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class RecipesListsActivity extends AppCompatActivity implements
        ToolbarMenuSetting,
        RecipeReviewFragment.FragmentRecipeReviewListener,
        EditRecipeFragment.OnFragmentEditRecipeListener,
        ListFragmentListener {

    private static final int REQ_CODE_RECIPE_DETAILS_ACTIVITY = 10;
    private static final int REQ_CODE_SEARCH_IN_LISTS_ACTIVITY = 20;

    private MyFragment listFragment;
    private RecipeReviewFragment recipeReviewFragment;
    private EditRecipeFragment editRecipeFragment;
    private WebViewFragment webViewFragment;
    private CategoriesChooserFragment categoriesChooserFragment;

    private Toolbar toolbar;
    private TabChangedReceiver tabChangedReceiver;
    //private LocalBroadcastManager lbm;
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

    private long mRecipeId;
    private ArrayList<Integer> checkedItemsId;
    private String activityToolbarTitle;
    private ArrayList<ObjectAnimator> fadingInAnimationsList;
    private boolean tabChanged;
    private Bundle savedInstanceState;
    private int toolbarColor;
    private String toolbarTitle;

    private LinearLayout layout_logo;
    private MyBus bus;
    private Dialog rateUsDialog;

    private DbManager dbManager;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        dbManager = DbManager.getInstance(this);

        mRecipeId = AppConsts.NEW_RECIPE;

        checkedItemsId = new ArrayList<>();

        activityToolbarTitle = getString(R.string.recipes_lists);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(activityToolbarTitle);

        toolbarTitle = activityToolbarTitle;
        toolbarColor = AppConsts.ToolbarColor.PRIMARY;

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Do something for Lollipop and above versions
            toolbar.setElevation(0);
        }

        setSupportActionBar(toolbar);

        AppHelper.hideTheKeyboard(this);

        Intent prevIntent = getIntent();
        extra_serving = prevIntent.getStringExtra(AppConsts.Extras.EXTRA_SERVING);
        FragmentManager fm = getSupportFragmentManager();

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet:

            layout_logo = (LinearLayout) findViewById(R.id.layout_logo);

            TextView textView_greatRecipes = (TextView) findViewById(R.id.textView_greatRecipes);
            textView_greatRecipes.setTypeface(MyFonts.getInstance(this).getMotionPictureFont());

            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically
            if (savedInstanceState == null) {

                // create frags: List (no details at start...)
                Fragment tabsFragment = TabsFragment.newInstance(extra_serving);
                FragmentTransaction ft = fm.beginTransaction();

                ft.add(R.id.layout_listContainer, tabsFragment, AppConsts.Fragments.TABS);

                ft.commit();

            }
        } else {
            // Phone

            if (savedInstanceState == null) {
                // only add fragments if the state is null!
                // if there's a state - they will be re-attached automatically

                Fragment tabsFragment = TabsFragment.newInstance(extra_serving);
                FragmentTransaction ft = fm.beginTransaction();

                ft.add(R.id.layout_container, tabsFragment, AppConsts.Fragments.TABS);

                ft.commit();
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        if (getResources().getBoolean(R.bool.isTablet)) {

            /*View v = findViewById(R.id.layout_7inTablet);
            if (v != null) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);*/

            if (getResources().getBoolean(R.bool.isSmallTablet)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        AnalyticsHelper.setScreenName(this);

        /* Registering the TabChangedReceiver
        tabChangedReceiver = new TabChangedReceiver();
        IntentFilter filter = new IntentFilter();

        filter.addAction(AppConsts.Actions.ACTION_TAB_CHANGED);

        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(tabChangedReceiver, filter); */

        bus = MyBus.getInstance(); // Singleton bus instance
        bus.register(this);
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

        outState.putIntegerArrayList("checkedItemsId", checkedItemsId);

        outState.putLong("mRecipeId", mRecipeId);
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
          //  showRateUsDialog(currentCheckboxState);
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            int layout_logoVisibility = savedInstanceState.getInt("layout_logoVisibility");
            layout_logo.setVisibility(layout_logoVisibility);
        }

        checkedItemsId = savedInstanceState.getIntegerArrayList("checkedItemsId");
        mRecipeId = savedInstanceState.getLong("mRecipeId");
        extra_serving = savedInstanceState.getString("extra_serving");
        currentlyShownList = savedInstanceState.getInt("currentlyShownList");

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {

            if (fragment instanceof ListOnlineFragment && currentlyShownList == AppConsts.ListType.ONLINE_RECIPES) {
                listFragment = (MyFragment) fragment;
                break;
            } else if (fragment instanceof ListMyOwnFragment && currentlyShownList == AppConsts.ListType.MY_OWN_RECIPES) {
                listFragment = (MyFragment) fragment;
                break;
            } else if (fragment instanceof ListFavouriteFragment && currentlyShownList == AppConsts.ListType.FAVOURITES_RECIPES) {
                listFragment = (MyFragment) fragment;
                break;
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();

        // Stop listening to TabChanged broadcasts
        // lbm.unregisterReceiver(tabChangedReceiver);

        // Stop listening to TabChanged broadcasts
        bus.unregister(this);
    }

//-------------------------------------------------------------------------------------------------

    private void showRateUsDialog() {

        rateUsDialog = new Dialog(this);

        // hide to default title for Dialog
        rateUsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Context context = this;

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_rate_us, null, false);
        rateUsDialog.setCanceledOnTouchOutside(false);
        rateUsDialog.setCancelable(false);
        rateUsDialog.setContentView(view);

        final CheckBox checkBox = (CheckBox) rateUsDialog.findViewById(R.id.checkBox_dontShowAgain);

        Button button_rateUs = (Button) rateUsDialog.findViewById(R.id.button_rateUs);
        button_rateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AnalyticsHelper.sendEvent(RecipesListsActivity.this, AppConsts.Analytics.CATEGORY_RATE_US, "Rate us button was clicked");

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN);
                editor.apply();

                Uri myAppUri = Uri.parse("market://details?id=" + context.getPackageName());
                final Intent intent = new Intent(Intent.ACTION_VIEW, myAppUri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                // Close the rateUsDialog
                rateUsDialog.dismiss();
            }
        });

        Button button_notNow = (Button) rateUsDialog.findViewById(R.id.button_notNow);
        button_notNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AnalyticsHelper.sendEvent(RecipesListsActivity.this, AppConsts.Analytics.CATEGORY_RATE_US, "Not now button was clicked");

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sp.edit();
                int rateUsDialogCounterPref;

                if (checkBox.isChecked()) {
                    rateUsDialogCounterPref = AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN;

                } else {
                    rateUsDialogCounterPref = sp.getInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, 1);
                    rateUsDialogCounterPref ++;
                }

                editor.putInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, rateUsDialogCounterPref);
                editor.apply();

                // Close the rateUsDialog
                rateUsDialog.dismiss();
            }
        });

        // Display the rateUsDialog
        rateUsDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onRecipeClick(MyFragment listFragment, long recipeId) {

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

            recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (mRecipeId == recipeId && recipeReviewFragment != null) {
                // The currently displayed recipe was clicked again
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

            } else {

                if (layout_logo.getVisibility() == View.VISIBLE) {
                    AppHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                }

                Recipe recipe = dbManager.queryRecipeObjectById(recipeId);

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if (extra_serving == null) {

                    if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                    }

                    if (recipe.originIndex != AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                    }

                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                }

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

                mRecipeId = recipeId;
                recipeReviewFragment = RecipeReviewFragment.newInstance(recipeId, null, extra_serving);

                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
            }

            ft.commit();

        } else {
            // phone

            mRecipeId = recipeId;

            Intent intent = new Intent(this, RecipeDetailsActivity.class);
            intent.setAction(AppConsts.Actions.ACTION_REVIEW);
            intent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, recipeId);
            intent.putExtra(AppConsts.Extras.EXTRA_SERVING, extra_serving);
            startActivityForResult(intent, REQ_CODE_RECIPE_DETAILS_ACTIVITY);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onListItemChecked(MyFragment listFragment, long id, boolean isChecked) {

        this.listFragment = listFragment;

        currentlyShownList = listFragment.getContainedListType();

        if (isChecked) {
            // Unchecked item was checked

            checkedItemsId.add((int) id);

            toolbarTitle = checkedItemsId.size() + " " + getString(R.string.selected);
            toolbar.setTitle(toolbarTitle);

            ForTablet:
            if (getResources().getBoolean(R.bool.isTablet)) {
                // Tablet only

                if (mRecipeId == id) {
                    break ForTablet;
                }

                mRecipeId = id;

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                if (toolbar_closeWebView.isVisible()) {
                    fm.popBackStack();
                }

                recipeReviewFragment = RecipeReviewFragment.newInstance(mRecipeId, null, extra_serving);
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                ft.commit();
            }

            if (checkedItemsId.size() == 1) {
                // First item was checked

                if (getResources().getBoolean(R.bool.isTablet)) {
                    if (layout_logo.getVisibility() == View.VISIBLE) {
                        AppHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                    }
                }

                ArrayList<Integer> toolbarButtons = new ArrayList<>();
                toolbarButtons.add(AppConsts.ToolbarButtons.DELETE);
                setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, toolbarTitle);
            }

        } else {
            // Checked item was unchecked

            checkedItemsId.remove(Integer.valueOf((int) id));

            if (checkedItemsId.isEmpty()) {
                // The last checked item was unchecked

                if (getResources().getBoolean(R.bool.isTablet)) {

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    if (toolbar_closeWebView.isVisible()) {
                        fm.popBackStack();
                    }

                    recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                    ft.remove(recipeReviewFragment);
                    ft.commit();

                    AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);
                }

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            } else {
                // There are still checked items

                toolbarTitle = checkedItemsId.size() + " " + getString(R.string.selected);
                toolbar.setTitle(toolbarTitle);

                if (getResources().getBoolean(R.bool.isTablet)) {
                    // Tablet only

                    mRecipeId = (long) checkedItemsId.get(checkedItemsId.size() - 1);

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    if (toolbar_closeWebView.isVisible()) {
                        fm.popBackStack();
                    }

                    recipeReviewFragment = RecipeReviewFragment.newInstance(mRecipeId, null, null);
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
    public void onAddNewRecipeClick(MyFragment listFragment, int listTypeIndex) {

        mRecipeId = AppConsts.NEW_RECIPE;

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
                        Snackbar snack = Snackbar.make(view, R.string.you_exceeded_the_downloaded_recipes_limit, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial));
                        snack.show();

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
                        Snackbar snack = Snackbar.make(view, R.string.you_exceeded_the_recipes_creation_limit, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial));
                        snack.show();

                        break;
                    }
                }

                if (getResources().getBoolean(R.bool.isTablet)) {
                    // tablet

                    if (layout_logo.getVisibility() == View.VISIBLE) {
                        AppHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                    }

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
                    if (webViewFragment != null) {
                        fm.popBackStack();
                    }

                    recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                    if (recipeReviewFragment != null) {
                        ft.remove(recipeReviewFragment);
                    }

                    editRecipeFragment = EditRecipeFragment.newInstance(AppConsts.NEW_RECIPE);
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

    @Override
    public void onRecipeWasSaved(long recipeId) {
        // Tablet only

        mRecipeId = recipeId;

        AppHelper.hide_keyboard_from(this, getCurrentFocus());

        FragmentManager fm = getSupportFragmentManager();

        recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

        FragmentTransaction ft = fm.beginTransaction();

        recipeReviewFragment = RecipeReviewFragment.newInstance(recipeId, null, extra_serving);

        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_up);
        ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);

        ft.commit();

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        Recipe recipe = dbManager.queryRecipeObjectById(recipeId);

        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);

        if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

        } else {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        }

        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCancelLoginButtonClicked() {
        // in edit recipe fragment

        if (getResources().getBoolean(R.bool.isTablet)) {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
            ft.remove(editRecipeFragment);
            ft.commit();

            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

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
            fadingInAnimationsList.add(AppHelper.animateToolbarButtonFadingIn(toolbar_search, 500, 600));

        } else {

            for (Integer button : allButtons) {

                if (toolbarButtonsList.contains(button)) {
                    MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                    fadingInAnimationsList.add(AppHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 600));
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    public ArrayList<Integer> getAndHideAllToolbarButtons() {

        if (fadingInAnimationsList != null) {
            // Cancelling all buttons "fading-in" animations, if exists
            for (ObjectAnimator animation : fadingInAnimationsList) {
                animation.cancel();
            }
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
                AppHelper.animateToolbarButtonFadingOut(toolBarButton, 500, 0);
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
        recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
        editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);

        Recipe recipe;

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

                editRecipeFragment = EditRecipeFragment.newInstance(mRecipeId);
                ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_up);
                ft.replace(R.id.layout_detailsContainer, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);
                ft.commit();

                toolbarButtonsList = new ArrayList<>();

                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

                break;

            case R.id.action_save:
                // Tablet only - "my Own" recipe only
                // Save a recipe after finishing editing

                listFragment.backToDefaultDisplay(true);

                editRecipeFragment.onSaveRecipe();

                // After the saving, the editFragment calls the recipeWasSaved(long recipeId)
                // method of this activity, and updates mRecipeId

                break;

            case R.id.action_addToList:
                // Tablet only - "my Own" recipe only
                // Save a recipe after finishing adding a new recipe

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

                if (!premium) {

                    int createdRecipesCount = sp.getInt(AppConsts.SharedPrefs.CREATED_COUNTER, 0);
                    createdRecipesCount++;
                    SharedPreferences.Editor editor = sp.edit();

                    editor.putInt(AppConsts.SharedPrefs.CREATED_COUNTER, createdRecipesCount);
                    editor.commit();
                }

                listFragment.backToDefaultDisplay(true);

                editRecipeFragment.onSaveRecipe();

                // After the saving, the editFragment calls the recipeWasSaved(long recipeId)
                // method of this activity, and updates mRecipeId

                break;

            case R.id.action_delete:

                onDeleteItemsClicked();

                break;

            case R.id.action_addServing:
                // Tablet only, while reviewing a recipe

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, mRecipeId);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToFavourites:
                // Tablet only, while reviewing a recipe

                recipe = dbManager.queryRecipeObjectById(mRecipeId);

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);

                if (recipe.originIndex == AppConsts.RecipeOrigin.ADDED_MANUALLY) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                }

                recipeReviewFragment.setFavouriteImage(AppConsts.FavouriteIndex.FAVOURITE);

                recipe.favouriteIndex = AppConsts.FavouriteIndex.FAVOURITE;
                dbManager.updateRecipe(recipe);

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

                break;

            case R.id.action_removeFromFavourites:
                // Tablet only
                // Tablet only, while reviewing a recipe

                recipe = dbManager.queryRecipeObjectById(mRecipeId);

                recipe.favouriteIndex = AppConsts.FavouriteIndex.NOT_FAVOURITE;
                dbManager.updateRecipe(recipe);

                if (listFragment instanceof ListFavouriteFragment) {

                    recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                    ft.remove(recipeReviewFragment);
                    ft.commit();

                    AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                    setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                } else {
                    toolbarButtonsList = new ArrayList<>();
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                    if (recipe.originIndex == AppConsts.RecipeOrigin.ADDED_MANUALLY) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                    }

                    recipeReviewFragment.setFavouriteImage(AppConsts.FavouriteIndex.NOT_FAVOURITE);

                    setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
                }

                break;

            case R.id.action_ok:
                // The user finished choosing categories in categoriesChooserFragment

                categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag(AppConsts.Fragments.CATEGORIES_CHOOSER);
                ArrayList<String> categoriesList = categoriesChooserFragment.getSelectedCategoriesNamesList();
                editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);

                editRecipeFragment.updateCategoriesList(this, categoriesList);

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

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the layout dialog_layout.xml and set it as contentView
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
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                if (toolbar_closeWebView.isVisible()) {
                    // Tablet only

                    fm.popBackStack();
                }

                if (recipeReviewFragment != null) {
                    // Tablet only

                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                    ft.remove(recipeReviewFragment);
                    ft.commit();

                    AppHelper.animateViewFadingIn(context, layout_logo, 500, 500);
                }

                for (final Integer checkedItemId : checkedItemsId) {
                    dbManager.deleteRecipeById(context, checkedItemId);
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

                // Close the dialog
                dialog.dismiss();
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Display the dialog
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

                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();
            }

            checkedItemsId.clear();
            listFragment.backToDefaultDisplay(true);
            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            mRecipeId = AppConsts.NEW_RECIPE;

            return;
        }

        // From now on it's for Tablet only

        if (toolbar_addToFavourites.isVisible()
                || toolbar_removeFromFavourites.isVisible()
                || toolbar_addServing.isVisible()) {
            // The user renounced reviewing a recipe, or adding the recipe to meal planner

            mRecipeId = AppConsts.NEW_RECIPE;

            AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

            recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

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

            Recipe recipe = dbManager.queryRecipeObjectById(mRecipeId);

            if (extra_serving == null) {

                if (checkedItemsId.isEmpty()) {

                    if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
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

            if (mRecipeId == AppConsts.NEW_RECIPE) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void showExitWithoutSavingDialog(final String action) {

        AppHelper.vibrate(this);

        final Dialog dialog = new Dialog(this);

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        final Context context = RecipesListsActivity.this;

        // inflate the layout dialog_layout.xml and set it as contentView
        final View view = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(view);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        textView_dialogTitle.setText(R.string.exit_without_saving);
        textView_dialogContent.setText(R.string.are_you_sure);

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                if (action.equals(AppConsts.Actions.ACTION_ADD_NEW)) {

                    editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                    ft.remove(editRecipeFragment);
                    ft.commit();

                    setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                    AppHelper.animateViewFadingIn(context, layout_logo, 500, 500);

                    listFragment.backToDefaultDisplay(true);

                } else {
                    // action = ACTION_EDIT

                    ListView listView = (ListView) listFragment.getView().findViewById(R.id.listView_recipesList);
                    listView.setEnabled(true);

                    recipeReviewFragment = RecipeReviewFragment.newInstance(mRecipeId, null, null);
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_up);

                    if (getResources().getBoolean(R.bool.isTablet)) {
                        ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                    } else {
                        ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                    }

                    ft.commit();

                    Recipe recipe = dbManager.queryRecipeObjectById(mRecipeId);

                    ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                    if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                    }

                    toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);

                    setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
                }

                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Display the dialog
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onTabChanged(TabChangedEvent event) {

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
                AppHelper.animateViewFadingIn(RecipesListsActivity.this, layout_logo, 500, 500);
            }

            FragmentManager fm = getSupportFragmentManager();

            editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
            webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
            categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag(AppConsts.Fragments.CATEGORIES_CHOOSER);
            recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

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

//-------------------------------------------------------------------------------------------------

    public class TabChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(AppConsts.Actions.ACTION_TAB_CHANGED)) {

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
                        AppHelper.animateViewFadingIn(RecipesListsActivity.this, layout_logo, 500, 500);
                    }

                    FragmentManager fm = getSupportFragmentManager();

                    editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
                    webViewFragment = (WebViewFragment) fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
                    categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag(AppConsts.Fragments.CATEGORIES_CHOOSER);
                    recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

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
    }
}
