package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
//import com.hanoch.greatrecipes.bus.OnUpdateOnlineDownloadedCountEvent;
import com.hanoch.greatrecipes.bus.OnGotYummlySearchResultsEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserDietAndAllergensEvent;
import com.hanoch.greatrecipes.bus.OnYummlyRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.database.SqLiteDbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.ObjectDrawerItem;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.hanoch.greatrecipes.view.adapters.DrawerItemAdapter;
import com.hanoch.greatrecipes.view.adapters.DrawerItemAdapterLimited;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class OnlineSearchActivity extends AppCompatActivity implements
        OnlineSearchResultsFragment.OnFragmentOnlineSearchListener,
        RecipeReviewFragment2.FragmentRecipeReviewListener,
        ToolbarMenuSetting {

    private LinearLayout layout_logo;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private String activityToolbarTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    public SearchView searchView;
    private CharSequence searchQuery;

    private MenuItem toolbar_search;

    private MenuItem toolbar_addToList;
    private MenuItem toolbar_addServing;
    private MenuItem toolbar_closeWebView;
    private MenuItem toolbar_refresh;

    private ArrayList<ObjectAnimator> fadingInAnimationsList;

    private boolean isAddServingAction;
    private String mRecipeId;
    private String mResultYummlyId;

    /**
     * Variables for restoring toolbar view when onRestoreInstanceState() is called
     */
    private int toolbarColor;
    private String toolbarTitle;
    private Bundle savedInstanceState;
    private boolean isPremium;

    private boolean afterRestoreState;
    private ProgressDialog progressDialog;
    private ApisManager apisManager;
    private MyBus bus;
    private AppStateManager appStateManager;
    private View mainView;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_search);

        appStateManager = AppStateManager.getInstance();
        bus = MyBus.getInstance();
        bus.register(this);

        apisManager = ApisManager.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        isPremium = appStateManager.user.isPremium;

        mResultYummlyId = "";

        Intent prevIntent = getIntent();
        isAddServingAction = prevIntent.getAction() != null;

        activityToolbarTitle = getString(R.string.online_search);
        toolbarTitle = activityToolbarTitle;

        mDrawerTitle = getString(R.string.filters);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarColor = AppConsts.ToolbarColor.PRIMARY;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                toolbar.setTitle(activityToolbarTitle);
                toolbarTitle = activityToolbarTitle;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toolbar.setTitle(mDrawerTitle);
                toolbarTitle = (String) mDrawerTitle;
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        ObjectDrawerItem[] drawerItems = new ObjectDrawerItem[15];

        drawerItems[0] = new ObjectDrawerItem("Header");
        drawerItems[1] = new ObjectDrawerItem("Diet Prefs");
        drawerItems[2] = new ObjectDrawerItem(R.mipmap.ic_filter_vegan, getString(R.string.vegan));
        drawerItems[3] = new ObjectDrawerItem(R.mipmap.ic_filter_vegetarian, getString(R.string.vegetarian));
        drawerItems[4] = new ObjectDrawerItem(R.mipmap.ic_filter_paleo, getString(R.string.paleo));
        drawerItems[5] = new ObjectDrawerItem("Allergens Prefs");
        drawerItems[6] = new ObjectDrawerItem(R.mipmap.ic_filter_dairy_free, getString(R.string.dairy_free));
        drawerItems[7] = new ObjectDrawerItem(R.mipmap.ic_filter_egg_free, getString(R.string.egg_free));
        drawerItems[8] = new ObjectDrawerItem(R.mipmap.ic_filter_gluten_free, getString(R.string.gluten_free));
        drawerItems[9] = new ObjectDrawerItem(R.mipmap.ic_filter_peanut_free, getString(R.string.peanut_free));
        drawerItems[10] = new ObjectDrawerItem(R.mipmap.ic_filter_seafood_free, getString(R.string.seafood_free));
        drawerItems[11] = new ObjectDrawerItem(R.mipmap.ic_filter_sesame_free, getString(R.string.sesame_free));
        drawerItems[12] = new ObjectDrawerItem(R.mipmap.ic_filter_soy_free, getString(R.string.soy_free));
        drawerItems[13] = new ObjectDrawerItem(R.mipmap.ic_filter_tree_nut_free, getString(R.string.treenut_free));
        drawerItems[14] = new ObjectDrawerItem(R.mipmap.ic_filter_wheat_free, getString(R.string.wheat_free));

        if (isPremium) {
            DrawerItemAdapter adapter = new DrawerItemAdapter(this, drawerItems);
            mDrawerList.setAdapter(adapter);

        } else {
            DrawerItemAdapterLimited adapter = new DrawerItemAdapterLimited(this, drawerItems);
            mDrawerList.setAdapter(adapter);
            ColorDrawable color = new ColorDrawable(ActivityCompat.getColor(this, android.R.color.transparent));
            mDrawerList.setDivider(color);
        }

        for (String sPosition : appStateManager.user.preferences.dietAndAllergensList) {
            mDrawerList.setItemChecked(Integer.parseInt(sPosition), true);
        }

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        int containerResId;

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet:

            // When touching the details layout - hide the keyboard if it's showing
            View layout_detailsContainer = findViewById(R.id.layout_detailsContainer);
            layout_detailsContainer.setOnClickListener(v -> {
                if (searchView != null) {
                    searchView.clearFocus();
                }
            });

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

            Fragment onlineSearchResultsFragment = OnlineSearchResultsFragment.newInstance();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.add(containerResId, onlineSearchResultsFragment, AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
            ft.commit();
        }

        mainView = findViewById(android.R.id.content);

    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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
    protected void onNewIntent(Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {

            if (afterRestoreState) {
                afterRestoreState = false;

            } else { // The user made a search

                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }

                if (searchView != null) {
                    searchView.clearFocus();
                }

                if (isPremium || BuildConfig.DEBUG) {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    performOnlineSearch(query);
                } else {
                    if (appStateManager.user.onlineSearchesCount >= 3) {
                        // The free-trial limitation for searching recipes is exceeded

                        String category = AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING;
                        String action = "You exceeded snackbar was shown";
                        String label = "Online Searches";
                        AnalyticsHelper.sendEvent(this, category, action, label);

                        int strResId = R.string.you_exceeded_the_downloaded_recipes_limit;
                        int color = ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial);
                        AppHelper.showSnackBar(mainView, strResId, color);

                    } else {
                        appStateManager.user.onlineSearchesCount++;
                        apisManager.updateOnlineSearchesCount(appStateManager.user.onlineSearchesCount);
                    }
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        afterRestoreState = true;

        mResultYummlyId = savedInstanceState.getString("mResultYummlyId");
        mRecipeId = savedInstanceState.getString("mRecipeId");
        isAddServingAction = savedInstanceState.getBoolean("isAddServingAction");

        if (getResources().getBoolean(R.bool.isTablet)) {
            int layout_logoVisibility = savedInstanceState.getInt("layout_logoVisibility");
            layout_logo.setVisibility(layout_logoVisibility);
        }

        searchQuery = savedInstanceState.getCharSequence("searchQuery");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("mResultYummlyId", mResultYummlyId);
        outState.putString("mRecipeId", mRecipeId);
        outState.putBoolean("isAddServingAction", isAddServingAction);

        if (getResources().getBoolean(R.bool.isTablet)) {

            int layout_logoVisibility = layout_logo.getVisibility();
            outState.putInt("layout_logoVisibility", layout_logoVisibility);
        }

        if (searchView != null) {
            searchQuery = searchView.getQuery();
            outState.putCharSequence("searchQuery", searchQuery);
            boolean searchViewHasFocus = searchView.hasFocus();
            outState.putBoolean("searchViewHasFocus", searchViewHasFocus);
        }

        outState.putInt("toolbarColor", toolbarColor);
        outState.putString("toolbarTitle", toolbarTitle);

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.SEARCH);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        buttons.add(AppConsts.ToolbarButtons.REFRESH);

        ArrayList<Integer> displayedButtons = new ArrayList<>();

        MenuItem toolBarButton;
        for (Integer button : buttons) {

            toolBarButton = toolbar.getMenu().findItem(button);
            if (toolBarButton != null && toolBarButton.isVisible()) {
                displayedButtons.add(button);
            }
        }

        outState.putIntegerArrayList("displayedButtons", displayedButtons);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bus.unregister(this);

        if (!BuildConfig.DEBUG && !appStateManager.user.isPremium) {
            SqLiteDbManager sqliteManager = new SqLiteDbManager(this);
            sqliteManager.deleteAllSearchResults();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void setTitle(CharSequence title) {
        activityToolbarTitle = (String) title;
        toolbar.setTitle(activityToolbarTitle);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_online_search, menu);

        toolbar_search = menu.findItem(R.id.action_search).setVisible(false);

        toolbar_addToList = menu.findItem(R.id.action_addToList).setVisible(false);
        toolbar_addServing = menu.findItem(R.id.action_addServing).setVisible(false);
        toolbar_closeWebView = menu.findItem(R.id.action_closeWebview).setVisible(false);
        toolbar_refresh = menu.findItem(R.id.action_refresh).setVisible(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (toolbar_search != null) {
            searchView = (SearchView) toolbar_search.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    String englishLetters = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                    char[] chars = newText.toCharArray();
                    int errorCharPosition = -1;
                    for (int i = 0; i < chars.length; i++) {
                        char mChar = chars[i];
                        if (!englishLetters.contains(String.valueOf(mChar))) {
                            errorCharPosition = i;

                            AppHelper.hideKeyboardFrom(OnlineSearchActivity.this, getCurrentFocus());
                        }
                    }
                    String oldQuery = "";
                    if (errorCharPosition != -1) {
                        for (int i = 0; i < chars.length; i++) {
                            if (i != errorCharPosition) {
                                oldQuery += chars[i];
                            }
                        }

                        searchView.setQuery(oldQuery, false);

                        // Relocating the selection in the right place
                        EditText editText = (EditText) searchView.findViewById(R.id.search_src_text);
                        editText.setSelection(errorCharPosition);

                        AppHelper.showSnackBar(mainView, R.string.english_letters_only, Color.RED);
                    }

                    return true;
                }
            });
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.onActionViewExpanded();
            View searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlate.setBackgroundColor(Color.GRAY);
        }

        if (savedInstanceState == null) {

            if (toolbar_search != null) {
                toolbar_search.setVisible(true);
            }

        } else {

            if (searchView != null) {
                searchView.setQuery(searchQuery, true);

                boolean searchViewHasFocus = savedInstanceState.getBoolean("searchViewHasFocus");

                if (!searchViewHasFocus) {
                    searchView.clearFocus();
                }
            }

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
                // Specific case: the user uses phone and got in the activity in landscape mode

                toolbar_search.setVisible(true);
            }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            // The user opened the filters drawer

            AppHelper.hideKeyboardFrom(this, getCurrentFocus());
            AppHelper.hideTheKeyboard(this);

            return true;
        }

        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }

        switch (item.getItemId()) {

            case R.id.action_addServing:

                Intent resultIntent = new Intent();

                Bundle extras = new Bundle();
                extras.putString(AppConsts.Extras.RECIPE_ID, appStateManager.yummlySearchResult._id);
                extras.putBoolean(AppConsts.Extras.EXTRA_IS_USER_RECIPE, false);
                resultIntent.putExtras(extras);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToList:

                if (!isPremium && !BuildConfig.DEBUG) {

                    int downloadedRecipesCount = appStateManager.user.onlineDownloadsCount;

                    if (downloadedRecipesCount >= 3) {
                        // The free-trial limitation for downloading recipes is exceeded

                        AnalyticsHelper.sendEvent(OnlineSearchActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "You exceeded snackbar was shown", "Online");

                        AppHelper.showSnackBar(mainView, R.string.you_exceeded_the_downloaded_recipes_limit, ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial));
                        break;

                    } else {
                        downloadedRecipesCount++;
                        apisManager.updateOnlineDownloadsCount(downloadedRecipesCount);
                    }
                }

                progressDialog.show();

                ArrayList<String> yummlyRecipesIds = new ArrayList<>();
                yummlyRecipesIds.add(appStateManager.yummlySearchResult._id);

                apisManager.updateUserRecipes(null, yummlyRecipesIds, BusConsts.ACTION_ADD_NEW);
                break;

            case R.id.action_closeWebview:
                onBackPressed();
                break;

            case R.id.action_refresh:
                // After a failed attempt to save a Yummly recipe

                progressDialog.show();

                FragmentManager fm = getSupportFragmentManager();
                OnlineSearchResultsFragment onlineSearchResultsFragment = (OnlineSearchResultsFragment) fm.findFragmentByTag(AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
                onlineSearchResultsFragment.refreshAdapter();

                apisManager.getYummlyRecipeFromGreatRecipesApi(this, mResultYummlyId, AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void setToolbarAttr(ArrayList<Integer> toolbarButtonsList, int color, String title) {

        toolbar.setTitle(title);

        toolbarTitle = title;

        if (color != AppConsts.ToolbarColor.NO_CHANGE) {
            toolbarColor = color;
            toolbar.setBackgroundColor(color);
        }

        ArrayList<Integer> allButtons = getAndHideAllToolbarButtons();

        if (toolbarButtonsList != null) {

            fadingInAnimationsList = new ArrayList<>();

            for (Integer button : allButtons) {
                if (toolbarButtonsList.contains(button)) {
                    MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                    fadingInAnimationsList.add(AnimationHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 600));
                }
            }

        } else {
            if (!toolbar_search.isVisible()) {
                AnimationHelper.animateToolbarButtonFadingIn(toolbar_search, 500, 600);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    public void performOnlineSearch(String keyToSearch) {

        if (searchKeyTooShort(keyToSearch)) {
            // checking if 'recipe's title search' field contains at least 2 chars
            // will return 'true' also in case of input contains only spaces

            AppHelper.showSnackBar(mainView, R.string.at_least_2_chars_required, Color.RED);
            return;
        }

        progressDialog.show();
        apisManager.performSearchRecipesFromYummlyApi(this, keyToSearch);
    }

//-------------------------------------------------------------------------------------------------

    private boolean searchKeyTooShort(String keyToSearch) {
        // checks if an editText has less than 2 letters excluding spaces.
        int trimmedLength = keyToSearch.trim().length();

        return (trimmedLength < 2);
    }

//-------------------------------------------------------------------------------------------------

    public ArrayList<Integer> getAndHideAllToolbarButtons() {

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        buttons.add(AppConsts.ToolbarButtons.REFRESH);

        if (toolbar_search.isVisible()) {
            toolbar_search.setVisible(false);
        }

        // Resetting all buttons to invisible
        AnimationHelper.cancelAllFadingInAnimations(fadingInAnimationsList);

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (!isPremium && (position != 4 && position != 11 && position != 12)) {
                Activity mainActivity = OnlineSearchActivity.this;
                AppHelper.showSnackBar(mainView, R.string.this_item_is_not_available_in_free_trial, ActivityCompat.getColor(mainActivity, R.color.colorSnackbarFreeTrial));

                mDrawerList.setItemChecked(position, false);
                return;
            }

            apisManager.updateUserDietOrAllergenPreference(position);

//            if (drawerCheckedItemsPositionList.contains(position + "")) {
//                mDrawerList.setItemChecked(position, false);
//                drawerCheckedItemsPositionList.remove(position + "");
//                selectDrawerItem(position, false);
//            } else {
//                drawerCheckedItemsPositionList.add(position + "");
//                mDrawerList.setItemChecked(position, true);
//                selectDrawerItem(position, true);
//            }
        }
    }

//-------------------------------------------------------------------------------------------------

//    private void selectDrawerItem(int position, Boolean isChosen) {
//
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = sp.edit();
//
//        switch (position) {
//
//            // 0 = header
//
//            // 1 = Diet Title
//
//            case 2:
//                // Vegan filter
//                editor.putBoolean(AppConsts.SharedPrefs.VEGAN, isChosen);
//                break;
//
//            case 3:
//                // Vegetarian filter
//                editor.putBoolean(AppConsts.SharedPrefs.VEGETARIAN, isChosen);
//                break;
//
//            case 4:
//                // Paleo filter
//                editor.putBoolean(AppConsts.SharedPrefs.PALEO, isChosen);
//                break;
//
//            // 5 Allergies Title
//
//            case 6:
//                // Dairy filter
//                editor.putBoolean(AppConsts.SharedPrefs.DAIRY_FREE, isChosen);
//                break;
//
//            case 7:
//                // Egg filter
//                editor.putBoolean(AppConsts.SharedPrefs.EGG_FREE, isChosen);
//                break;
//
//            case 8:
//                // Gluten filter
//                editor.putBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, isChosen);
//                break;
//
//            case 9:
//                // Peanut filter
//                editor.putBoolean(AppConsts.SharedPrefs.PEANUT_FREE, isChosen);
//                break;
//
//            case 10:
//                // Seafood filter
//                editor.putBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, isChosen);
//                break;
//
//            case 11:
//                // Sesame filter
//                editor.putBoolean(AppConsts.SharedPrefs.SESAME_FREE, isChosen);
//                break;
//
//            case 12:
//                // Soy filter
//                editor.putBoolean(AppConsts.SharedPrefs.SOY_FREE, isChosen);
//                break;
//
//            case 13:
//                // Tree nut filter
//                editor.putBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, isChosen);
//                break;
//
//            case 14:
//                // Pescetarian filter
//                editor.putBoolean(AppConsts.SharedPrefs.WHEAT_FREE, isChosen);
//                break;
//
//            default:
//                break;
//        }
//
//        editor.apply();
//    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnYummlyRecipeDownloadedEvent event) {
        if (event.action != AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE) {
            return;
        }

        progressDialog.dismiss();

        if (event.isSuccess) {
            YummlyRecipe searchResult = appStateManager.yummlySearchResult;

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment recipeReviewFragment;
            int action = isAddServingAction ? AppConsts.Actions.ADD_SERVING_FROM_YUMMLY : AppConsts.Actions.REVIEW_YUMMLY_ONLINE;

            recipeReviewFragment = RecipeReviewFragment2.newInstance(AppConsts.Actions.REVIEW_YUMMLY_ONLINE, searchResult._id);

            if (getResources().getBoolean(R.bool.isTablet)) {
                // tablet

                if (layout_logo.getVisibility() == View.VISIBLE) {
                    AnimationHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                }

                Boolean recipeDetailsIsShawn = (toolbar_addServing.isVisible() || toolbar_addToList.isVisible() || toolbar_closeWebView.isVisible());
                if (mResultYummlyId.equals(searchResult.yummlyId) && recipeDetailsIsShawn) {
                    return;
                }

                mResultYummlyId = searchResult.yummlyId;
                mRecipeId = searchResult._id;

                Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
                if (webViewFragment != null) {
                    fm.popBackStack();
                }

                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if (isAddServingAction) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
                }

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

            } else {
                // phone

                mResultYummlyId = searchResult.yummlyId;
                mRecipeId = searchResult._id;

                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                ft.addToBackStack(null);

                // Remove the Hamburger icon
                mDrawerToggle.setDrawerIndicatorEnabled(false);

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if (action == AppConsts.Actions.ADD_SERVING_FROM_YUMMLY) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
                }

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
            }

            ft.commit();

        } else {
            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
            toolbarButtonsList.add(AppConsts.ToolbarButtons.REFRESH);
            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.PRIMARY, getString(R.string.unexpected_error));

            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnGotYummlySearchResultsEvent event) {
        progressDialog.dismiss();

        if (event.isSuccess) {

            if (event.results == null || event.results.isEmpty()) {
                // No results
                AppHelper.showSnackBar(mainView, R.string.no_results, Color.RED);
                if (appStateManager.user.onlineSearchesCount != 0) {
                    appStateManager.user.onlineSearchesCount--;
                    apisManager.updateOnlineSearchesCount(appStateManager.user.onlineSearchesCount);
                }
                return;
            }
            OnlineSearchResultsFragment onlineSearchResultsFragment = (OnlineSearchResultsFragment) getSupportFragmentManager().findFragmentByTag(AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
            onlineSearchResultsFragment.OnGotYummlySearchResultsEvent();

        } else {
            appStateManager.user.onlineSearchesCount--;
            apisManager.updateOnlineSearchesCount(appStateManager.user.onlineSearchesCount);
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }
//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserDietAndAllergensEvent event) {
        // After toggling a diet or allergen pref

        if (event.isSuccess) {
            mDrawerList.setItemChecked(event.position, event.action == BusConsts.ACTION_ADD_NEW);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {
        // After updating the user's yummlyRecipes in the database
        progressDialog.dismiss();

        if (event.isSuccess) {

            switch (event.action) {

                case BusConsts.ACTION_ADD_NEW:
                    if (isAddServingAction) {
                        // The user chose a yummly recipe as a serving
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(AppConsts.Extras.RECIPE_ID, mRecipeId);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        // The user saved a yummly recipe to his online list

                        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

                        if (getResources().getBoolean(R.bool.isTablet)) {
                            // Tablet

                            AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                            FragmentManager fm = getSupportFragmentManager();
                            FragmentTransaction ft = fm.beginTransaction();

                            Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                            ft.remove(recipeReviewFragment);
                            ft.commit();

                        } else {
                            mDrawerToggle.setDrawerIndicatorEnabled(true);
                            onBackPressed();
                        }

                        AppHelper.showSnackBar(mainView, R.string.added_to_online_list, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
                        setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);
                    }

                    break;
            }

        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSearchResultClick(long resultId, String resultYummlyId) {

        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet

            AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (recipeReviewFragment != null) {
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();
            }
        }

        setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

        mResultYummlyId = resultYummlyId;

        progressDialog.show();
        apisManager.getYummlyRecipeFromGreatRecipesApi(this, resultYummlyId, AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onGetInstructionsClick(String url) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment webViewFragment = WebViewFragment.newInstance(url);
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_up, R.anim.slide_out_up);

        if (getResources().getBoolean(R.bool.isTablet)) {
            // tablet
            ft.replace(R.id.layout_detailsContainer, webViewFragment, AppConsts.Fragments.WEB_VIEW);

        } else {
            // phone
            ft.replace(R.id.layout_container, webViewFragment, AppConsts.Fragments.WEB_VIEW);

            // Remove the Hamburger icon
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }

        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {

        mDrawerToggle.setDrawerIndicatorEnabled(true);

        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        Fragment recipeReviewFragment;
        if (toolbar_addServing.isVisible() || toolbar_addToList.isVisible()) {
            // The user is reviewing a recipe

            if (getResources().getBoolean(R.bool.isTablet)) {
                AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();
                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);
                return;

            } else {
                fm.popBackStack();
            }
        }

        Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);

        if (webViewFragment != null) {
            // The user is watching the recipe url while reviewing a recipe from the API

            fm.popBackStack();

            if (!getResources().getBoolean(R.bool.isTablet)) {

                // Hide the hamburger icon in phones
                mDrawerToggle.setDrawerIndicatorEnabled(false);
            }

            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

            if (isAddServingAction) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

            return;
        }

        recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

        if (recipeReviewFragment != null) {
            // Internet error - a "blank" recipe is displayed ("No info." in all fields)

            if (getResources().getBoolean(R.bool.isTablet)) {

                AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();
            } else {
                fm.popBackStack();
            }

            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

        } else {
            // No recipe is displayed right now
            finish();
        }
    }

}
