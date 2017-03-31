package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.AllergensAndDietPrefItem;
import com.hanoch.greatrecipes.model.ObjectDrawerItem;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.hanoch.greatrecipes.view.adapters.DrawerItemAdapter;
import com.hanoch.greatrecipes.view.adapters.DrawerItemAdapterLimited;

import java.util.ArrayList;

public class OnlineSearchActivity extends AppCompatActivity implements
        OnlineSearchResultsFragment.OnFragmentOnlineSearchListener,
        RecipeReviewFragment.FragmentRecipeReviewListener,
        ToolbarMenuSetting {


    private static final String TAG = "OnlineSearchActivity";

    private TextView textView_greatRecipes;
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

    private ArrayList<String> drawerCheckedItemsPositionList;
    private String extra_serving;
    private long mRecipeId;
    private String mResultYummlyId;

    /**
     * Variables for restoring toolbar view when onRestoreInstanceState() is called
     */
    private int toolbarColor;
    private String toolbarTitle;
    private Bundle savedInstanceState;
    private boolean isPremium;

    private boolean afterRestoreState;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_online_search);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        isPremium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        mResultYummlyId = "";

        Intent prevIntent = getIntent();
        extra_serving = prevIntent.getStringExtra(AppConsts.Extras.EXTRA_SERVING);

        drawerCheckedItemsPositionList = new ArrayList<String>();
        ArrayList<AllergensAndDietPrefItem> allowedDietPrefsList = AppHelper.getUserAllowedDietPrefsList(this);
        ArrayList<AllergensAndDietPrefItem> allowedAllergiesPrefsList = AppHelper.getUserAllowedAllergiesPrefsList(this);

        for (int i = 0; i < allowedDietPrefsList.size(); i++) {

            if (allowedDietPrefsList.get(i).isChosen) {
                drawerCheckedItemsPositionList.add(allowedDietPrefsList.get(i).positionInDrawer + "");
            }
        }

        for (int i = 0; i < allowedAllergiesPrefsList.size(); i++) {

            if (allowedAllergiesPrefsList.get(i).isChosen) {
                drawerCheckedItemsPositionList.add(allowedAllergiesPrefsList.get(i).positionInDrawer + "");
            }
        }

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

        mDrawerLayout.setDrawerListener(mDrawerToggle);

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
            ColorDrawable color = new ColorDrawable(getResources().getColor(android.R.color.transparent));
            mDrawerList.setDivider(color);
        }

        for (int i = 0; i < drawerCheckedItemsPositionList.size(); i++) {
            mDrawerList.setItemChecked(Integer.parseInt(drawerCheckedItemsPositionList.get(i)), true);
        }

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet:

            // When touching the details layout - hide the keyboard if it's showing
            View layout_detailsContainer = findViewById(R.id.layout_detailsContainer);
            layout_detailsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (searchView != null) {
                        searchView.clearFocus();
                    }
                }
            });

            layout_logo = (LinearLayout) findViewById(R.id.layout_logo);

            textView_greatRecipes = (TextView) findViewById(R.id.textView_greatRecipes);
            textView_greatRecipes.setTypeface(MyFonts.getInstance(this).getMotionPictureFont());

            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically
            if (savedInstanceState == null) {

                // create frags: List (no details at start...)
                Fragment onlineSearchResultsFragment = OnlineSearchResultsFragment.newInstance();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.layout_listContainer, onlineSearchResultsFragment, AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
                ft.commit();
            }

        } else {
            // Phone

            if (savedInstanceState == null) {
                // only add fragments if the state is null!
                // if there's a state - they will be re-attached automatically

                // Creating OnlineSearchResultsFragment
                Fragment onlineSearchResultsFragment = OnlineSearchResultsFragment.newInstance();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                ft.add(R.id.layout_container, onlineSearchResultsFragment, AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
                ft.commit();
            }
        }
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

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            if (!afterRestoreState) { // The user made a search

                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }

                String query = intent.getStringExtra(SearchManager.QUERY);

                OnlineSearchResultsFragment onlineSearchResultsFragment = (OnlineSearchResultsFragment) getSupportFragmentManager().findFragmentByTag(AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
                onlineSearchResultsFragment.performOnlineSearch(query, true);

                if (searchView != null) {
                    searchView.clearFocus();
                }

            } else {
                afterRestoreState = false;
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
        mRecipeId = savedInstanceState.getLong("mRecipeId");
        extra_serving = savedInstanceState.getString("extra_serving");

        drawerCheckedItemsPositionList = savedInstanceState.getStringArrayList("drawerCheckedItemsPositionList");

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
        outState.putLong("mRecipeId", mRecipeId);
        outState.putString("extra_serving", extra_serving);

        outState.putStringArrayList("drawerCheckedItemsPositionList", drawerCheckedItemsPositionList);

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
                    int errorCharPoition = -1;
                    for (int i = 0; i < chars.length; i++) {
                        char mChar = chars[i];
                        if (!englishLetters.contains(String.valueOf(mChar))) {
                            errorCharPoition = i;

                            AppHelper.hideKeyboardFrom(OnlineSearchActivity.this, getCurrentFocus());
                        }
                    }
                    String oldQuery = "";
                    if (errorCharPoition != -1) {
                        for (int i = 0; i < chars.length; i++) {
                            if (i!=errorCharPoition) {
                                oldQuery += chars[i];
                            }
                        }

                        searchView.setQuery(oldQuery, false);

                        // Relocating the selection in the right place
                        EditText editText = (EditText)searchView.findViewById(R.id.search_src_text);
                        editText.setSelection(errorCharPoition);

                        View rootView = OnlineSearchActivity.this.findViewById(android.R.id.content);
                        Snackbar snack = Snackbar.make(rootView, R.string.english_letters_only, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(Color.RED);
                        snack.show();
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

        FragmentManager fm = getSupportFragmentManager();
        RecipeReviewFragment recipeReviewFragment;

        switch (item.getItemId()) {

            case R.id.action_addServing:

                recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                recipeReviewFragment.onSaveOnlineRecipe();

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, mRecipeId);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToList:

                if (!isPremium) {

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    int downloadedRecipesCount = sp.getInt(AppConsts.SharedPrefs.DOWNLOADED_COUNTER, 0);

                    if (downloadedRecipesCount == 3) {
                        // The free-trial limitation for downloading recipes is exceeded

                        AnalyticsHelper.sendEvent(OnlineSearchActivity.this, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "You exceeded snackbar was shown", "Online");

                        View view = this.findViewById(android.R.id.content);
                        Snackbar snack = Snackbar.make(view, R.string.you_exceeded_the_downloaded_recipes_limit, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSnackbarFreeTrial));
                        snack.show();

                        break;

                    } else {
                        downloadedRecipesCount++;
                        SharedPreferences.Editor editor = sp.edit();

                        editor.putInt(AppConsts.SharedPrefs.DOWNLOADED_COUNTER, downloadedRecipesCount);
                        editor.commit();
                    }
                }

                recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                recipeReviewFragment.onSaveOnlineRecipe();

                // After saving the recipe to the user 'Online List', the recipeReviewFragment
                // will call onRecipeWasSaved() method of this activity

                break;

            case R.id.action_closeWebview:

                onBackPressed();

                break;

            case R.id.action_refresh:

                OnlineSearchResultsFragment onlineSearchResultsFragment = (OnlineSearchResultsFragment) fm.findFragmentByTag(AppConsts.Fragments.ONLINE_SEARCH_RESULTS);
                onlineSearchResultsFragment.refreshAdapter();

                recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                recipeReviewFragment.tryToDownloadRecipeDetails();

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
                    fadingInAnimationsList.add(AppHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 600));
                }
            }
        } else {
            if (!toolbar_search.isVisible()) {
                AppHelper.animateToolbarButtonFadingIn(toolbar_search, 500, 600);
            }
        }
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

        if (fadingInAnimationsList != null) {
            // Cancelling all buttons "fading-in" animations, if exists
            for (ObjectAnimator animation : fadingInAnimationsList) {
                animation.cancel();
            }
        }

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (!isPremium && (position != 4 && position != 11 && position != 12)) {
                Activity mainActivity = OnlineSearchActivity.this;
                View mainView = mainActivity.findViewById(android.R.id.content);
                Snackbar snack = Snackbar.make(mainView, R.string.this_item_is_not_available_in_free_trial, Snackbar.LENGTH_SHORT);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(ActivityCompat.getColor(mainActivity, R.color.colorSnackbarFreeTrial));
                snack.show();
                mDrawerList.setItemChecked(position, false);
                return;
            }

            if (drawerCheckedItemsPositionList.contains(position + "")) {
                mDrawerList.setItemChecked(position, false);
                drawerCheckedItemsPositionList.remove(position + "");
                selectDrawerItem(position, false);
            } else {
                drawerCheckedItemsPositionList.add(position + "");
                mDrawerList.setItemChecked(position, true);
                selectDrawerItem(position, true);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    private void selectDrawerItem(int position, Boolean isChosen) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        switch (position) {

            // 0 = header

            // 1 = Diet Title

            case 2:
                // Vegan filter
                editor.putBoolean(AppConsts.SharedPrefs.VEGAN, isChosen);
                //fragment = new NavBarFragOpt1();
                break;

            case 3:
                // Vegetarian filter
                editor.putBoolean(AppConsts.SharedPrefs.VEGETARIAN, isChosen);
                //fragment = new NavBarFragOpt2();
                break;

            case 4:
                // Paleo filter
                editor.putBoolean(AppConsts.SharedPrefs.PALEO, isChosen);
                //fragment = new NavBarFragOpt3();
                break;

            // 5 Allergies Title

            case 6:
                // Dairy filter
                editor.putBoolean(AppConsts.SharedPrefs.DAIRY_FREE, isChosen);
                break;

            case 7:
                // Egg filter
                editor.putBoolean(AppConsts.SharedPrefs.EGG_FREE, isChosen);
                break;

            case 8:
                // Gluten filter
                editor.putBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, isChosen);
                break;

            case 9:
                // Peanut filter
                editor.putBoolean(AppConsts.SharedPrefs.PEANUT_FREE, isChosen);
                break;

            case 10:
                // Seafood filter
                editor.putBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, isChosen);
                break;

            case 11:
                // Sesame filter
                editor.putBoolean(AppConsts.SharedPrefs.SESAME_FREE, isChosen);
                break;

            case 12:
                // Soy filter
                editor.putBoolean(AppConsts.SharedPrefs.SOY_FREE, isChosen);
                break;

            case 13:
                // Tree nut filter
                editor.putBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, isChosen);
                break;

            case 14:
                // Pescetarian filter
                editor.putBoolean(AppConsts.SharedPrefs.WHEAT_FREE, isChosen);
                break;

            default:
                break;
        }

        editor.commit();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSearchResultClick(long resultId, String resultYummlyId) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment recipeReviewFragment = RecipeReviewFragment.newInstance(resultId, resultYummlyId, extra_serving);

        if (getResources().getBoolean(R.bool.isTablet)) {
            // tablet

            if (layout_logo.getVisibility() == View.VISIBLE) {
                AppHelper.animateViewFadingOut(this, layout_logo, 500, 0);
            }

            Boolean recipeDetailsIsShawn = (toolbar_addServing.isVisible() || toolbar_addToList.isVisible() || toolbar_closeWebView.isVisible());
            if (this.mResultYummlyId.equals(resultYummlyId) && recipeDetailsIsShawn) return;

            this.mResultYummlyId = resultYummlyId;

            Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
            if (webViewFragment != null) {
                fm.popBackStack();
            }

            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
            ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);

        } else {
            // phone

            this.mResultYummlyId = resultYummlyId;

            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
            ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
            ft.addToBackStack(null);

            // Remove the Hamburger icon
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }

        ft.commit();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onRecipeWasSaved(long recipeId) {
        // This method is called by the recipeReviewFragment after finishing saving
        // a recipe to the user's 'Online List'

        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet

            AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

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

        setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);
        mRecipeId = recipeId;
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
                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

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

            if (extra_serving == null) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);

            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

            return;
        }

        recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

        if (recipeReviewFragment != null) {
            // Internet error - a "blank" recipe is displayed ("No info." in all fields)

            if (getResources().getBoolean(R.bool.isTablet)) {

                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

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
