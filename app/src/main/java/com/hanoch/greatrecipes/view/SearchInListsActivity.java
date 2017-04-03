package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.model.MyFragment;
import com.hanoch.greatrecipes.model.Recipe;

import java.util.ArrayList;

public class SearchInListsActivity extends AppCompatActivity implements
        ListFragmentListener,
        RecipeReviewFragment.FragmentRecipeReviewListener,
        EditRecipeFragment.OnFragmentEditRecipeListener,
        ToolbarMenuSetting {

    private LinearLayout layout_logo;

    private Toolbar toolbar;
    private MyFragment listFragment;

    private ArrayList<ObjectAnimator> fadingInAnimationsList;

    private MenuItem toolbar_search;

    private MenuItem toolbar_save;
    private MenuItem toolbar_edit;
    private MenuItem toolbar_delete;
    private MenuItem toolbar_addToFavourites;
    private MenuItem toolbar_removeFromFavourites;
    private MenuItem toolbar_addServing;
    private MenuItem toolbar_ok;
    private MenuItem toolbar_closeWebView;

    public SearchView searchView;

    private ArrayList<Integer> checkedItemsId;
    private String activityToolbarTitle;

    private long mRecipeId;
    private int toolbarColor;
    private String toolbarTitle;
    private String extra_serving;
    private Bundle savedInstanceState;
    private CharSequence searchQuery;
    private boolean afterRestoreState;

    private DbManager dbManager;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        dbManager = ((GreatRecipesApplication) getApplication()).getDbManager();

        mRecipeId = AppConsts.NEW_RECIPE;

        checkedItemsId = new ArrayList<>();

        activityToolbarTitle = getString(R.string.search_in_lists);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(activityToolbarTitle);
        setSupportActionBar(toolbar);

        toolbarColor = AppConsts.ToolbarColor.PRIMARY;
        toolbarTitle = activityToolbarTitle;

        Intent prevIntent = getIntent();
        // The only action exists is "ADD_SERVING"- otherwise action will be null;
        extra_serving = prevIntent.getStringExtra(AppConsts.Extras.EXTRA_SERVING);
        int conteinerResId;

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet:

            layout_logo = (LinearLayout) findViewById(R.id.layout_logo);

            TextView textView_greatRecipes = (TextView) findViewById(R.id.textView_greatRecipes);
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/MotionPicture.ttf");
            textView_greatRecipes.setTypeface(typeFace);

            conteinerResId = R.id.layout_listContainer;

        } else {
            // Phone
            conteinerResId = R.id.layout_container;
        }

        if (savedInstanceState == null) {
            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically

            Fragment searchInListsFragment = SearchInListsFragment.newInstance(extra_serving);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.add(conteinerResId, searchInListsFragment, AppConsts.Fragments.SEARCH_IN_LISTS);
            ft.commit();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        afterRestoreState = true;

        listFragment = (MyFragment) getSupportFragmentManager().findFragmentByTag(AppConsts.Fragments.SEARCH_IN_LISTS);

        if (getResources().getBoolean(R.bool.isTablet)) {

            int layout_logoVisibility = savedInstanceState.getInt("layout_logoVisibility");
            layout_logo.setVisibility(layout_logoVisibility);
        }

        checkedItemsId = savedInstanceState.getIntegerArrayList("checkedItemsId");
        mRecipeId = savedInstanceState.getLong("mRecipeId");
        extra_serving = savedInstanceState.getString("extra_serving");

        searchQuery = savedInstanceState.getCharSequence("searchQuery");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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

        outState.putIntegerArrayList("checkedItemsId", checkedItemsId);
        outState.putLong("mRecipeId", mRecipeId);
        outState.putString("extra_serving", extra_serving);
        outState.putInt("toolbarColor", toolbarColor);
        outState.putString("toolbarTitle", toolbarTitle);

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.SEARCH);
        buttons.add(AppConsts.ToolbarButtons.SAVE);
        buttons.add(AppConsts.ToolbarButtons.EDIT);
        buttons.add(AppConsts.ToolbarButtons.DELETE);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
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
    protected void onResume() {
        super.onResume();

        if (getResources().getBoolean(R.bool.isTablet)) {

            /*View v = findViewById(R.id.layout_7inTablet);
            if (v != null) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);*/
            if (getResources().getBoolean(R.bool.isSmallTablet)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            if (!afterRestoreState) {
                String query = intent.getStringExtra(SearchManager.QUERY);

                SearchInListsFragment searchInListsFragment = (SearchInListsFragment) getSupportFragmentManager().findFragmentByTag("searchInListsFragment");
                searchInListsFragment.performSearch(query, true);

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
    public void onRecipeClick(MyFragment listFragment, long recipeId) {

        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

        this.listFragment = listFragment;

        Recipe recipe = dbManager.queryRecipeObjectById(recipeId);

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        if (extra_serving == null) {

            if (recipe.favouriteIndex == AppConsts.FavouriteIndex.FAVOURITE) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);

            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
            }

            if (recipe.originIndex == AppConsts.RecipeOrigin.ADDED_MANUALLY) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
            }

        } else {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
        }

        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment recipeReviewFragment;

        if (getResources().getBoolean(R.bool.isTablet)) {
            // tablet

            Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
            if (webViewFragment != null) {
                onBackPressed();
            }

            recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (mRecipeId == recipeId && recipeReviewFragment != null) {
                // The currently displayed recipe was clicked again

                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);

                setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                mRecipeId = AppConsts.NEW_RECIPE;

            } else {

                if (layout_logo.getVisibility() == View.VISIBLE) {
                    AppHelper.animateViewFadingOut(this, layout_logo, 500, 0);
                }

                mRecipeId = recipeId;
                recipeReviewFragment = RecipeReviewFragment.newInstance(recipeId, null, extra_serving);

                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
            }

        } else {
            // phone

            mRecipeId = recipeId;

            recipeReviewFragment = RecipeReviewFragment.newInstance(recipeId, null, extra_serving);
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
            ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
            ft.addToBackStack(null);
        }

        ft.commit();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onListItemChecked(MyFragment listFragment, long id, boolean isChecked) {

        this.listFragment = listFragment;

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

                Fragment recipeReviewFragment = RecipeReviewFragment.newInstance(mRecipeId, null, extra_serving);
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

                    Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
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

                    Fragment recipeReviewFragment = RecipeReviewFragment.newInstance(mRecipeId, null, null);
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                    ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
                    ft.commit();
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAddNewRecipeClick(MyFragment listFragment, int listTypeIndex) {
        // No implementation in this activity
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
        }

        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onRecipeWasSaved(long recipeId) {
        // After saving the changes- review the recipe

        mRecipeId = recipeId;

        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment recipeReviewFragment = RecipeReviewFragment.newInstance(recipeId, null, extra_serving);
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_up);

        if (getResources().getBoolean(R.bool.isTablet)) {
            ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
        } else {
            ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
        }

        ft.commit();

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        Recipe recipe = dbManager.queryRecipeObjectById(recipeId);

        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);

        if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

        } else {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        }

        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCancelLoginButtonClicked() {
        // Irrelevant for this activity
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAddCategoriesClick(ArrayList<String> categoriesList) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment categoriesChooserFragment = CategoriesChooserFragment.newInstance(categoriesList);

        if (getResources().getBoolean(R.bool.isTablet)) {
            ft.replace(R.id.layout_detailsContainer, categoriesChooserFragment, AppConsts.Fragments.CATEGORIES_CHOOSER);

        } else {
            ft.replace(R.id.layout_container, categoriesChooserFragment, AppConsts.Fragments.CATEGORIES_CHOOSER);

        }

        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.OK);

        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.NO_CHANGE, getString(R.string.choose_categories));
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void setToolbarAttr(ArrayList<Integer> toolbarButtonsList, int color, String title) {

        if (color != AppConsts.ToolbarColor.NO_CHANGE) {
            toolbar.setBackgroundColor(color);
            toolbarColor = color;
        }

        toolbar.setTitle(title);
        toolbarTitle = title;

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

        if (fadingInAnimationsList != null) {
            // Cancelling all buttons "fading-in" animations, if exists
            for (ObjectAnimator animation : fadingInAnimationsList) {
                animation.cancel();
            }
        }

        ArrayList<Integer> buttons = new ArrayList<>();

        //buttons.add(AppConsts.ToolbarButtons.SEARCH);

        buttons.add(AppConsts.ToolbarButtons.SAVE);
        buttons.add(AppConsts.ToolbarButtons.EDIT);
        buttons.add(AppConsts.ToolbarButtons.DELETE);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.OK);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);

        // Resetting all buttons to invisible

        if (toolbar_search.isVisible()) {
            toolbar_search.setVisible(false);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_search_in_lists, menu);

        toolbar_search = menu.findItem(R.id.action_search).setVisible(false);

        toolbar_save = menu.findItem(R.id.action_save).setVisible(false);
        toolbar_edit = menu.findItem(R.id.action_edit).setVisible(false);
        toolbar_delete = menu.findItem(R.id.action_delete).setVisible(false);
        toolbar_addToFavourites = menu.findItem(R.id.action_addToFavourites).setVisible(false);
        toolbar_removeFromFavourites = menu.findItem(R.id.action_removeFromFavourites).setVisible(false);
        toolbar_addServing = menu.findItem(R.id.action_addServing).setVisible(false);
        toolbar_ok = menu.findItem(R.id.action_ok).setVisible(false);
        toolbar_closeWebView = menu.findItem(R.id.action_closeWebview).setVisible(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (toolbar_search != null) {
            searchView = (SearchView) toolbar_search.getActionView();
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

        int itemId = item.getItemId();

        Recipe recipe;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        EditRecipeFragment editRecipeFragment = (EditRecipeFragment) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
        RecipeReviewFragment recipeReviewFragment = (RecipeReviewFragment) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

        ArrayList<Integer> toolbarButtonsList;

        switch (itemId) {

            case R.id.action_edit:
                // The user clicked "Edit" button to edit a recipe

                ListView listView = (ListView) listFragment.getView().findViewById(R.id.listView_searchResults);
                listView.setEnabled(false);

                editRecipeFragment = EditRecipeFragment.newInstance(mRecipeId);
                //ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left);
                ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_right);

                if (getResources().getBoolean(R.bool.isTablet)) {
                    // tablet

                    ft.replace(R.id.layout_detailsContainer, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);

                } else {
                    // phone

                    ft.replace(R.id.layout_container, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);
                }

                ft.commit();

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

                break;

            case R.id.action_save:
                // Save a recipe after finishing editing an existing recipe

                editRecipeFragment.onSaveRecipe();

                // After the saving, the editFragment calls the recipeWasSaved(long recipeId)
                // method of this activity, and updates mRecipeId

                break;

            case R.id.action_delete:

                onDeleteItemsClicked();

                break;

            case R.id.action_addServing:

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, mRecipeId);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToFavourites:
                // While reviewing a recipe

                recipe = dbManager.queryRecipeObjectById(mRecipeId);
                recipe.favouriteIndex = AppConsts.FavouriteIndex.FAVOURITE;
                dbManager.updateRecipe(recipe);

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);

                if (recipe.originIndex == AppConsts.RecipeOrigin.ADDED_MANUALLY) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                }

                recipeReviewFragment.setFavouriteImage(AppConsts.FavouriteIndex.FAVOURITE);

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

                break;

            case R.id.action_removeFromFavourites:
                // While reviewing a recipe

                recipe = dbManager.queryRecipeObjectById(mRecipeId);
                recipe.favouriteIndex = AppConsts.FavouriteIndex.NOT_FAVOURITE;
                dbManager.updateRecipe(recipe);

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                if (recipe.originIndex == AppConsts.RecipeOrigin.ADDED_MANUALLY) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                }

                recipeReviewFragment.setFavouriteImage(AppConsts.FavouriteIndex.NOT_FAVOURITE);

                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);

                break;

            case R.id.action_ok:
                // The user finished choosing categories in categoriesChooserFragment

                CategoriesChooserFragment categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag("categoriesChooserFragment");
                ArrayList<String> categoriesList = categoriesChooserFragment.getSelectedCategoriesNamesList();
                editRecipeFragment.updateCategoriesList(this, categoriesList);

                onBackPressed();

                break;

            case R.id.action_closeWebview:

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

        final Context context = SearchInListsActivity.this;

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

                Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                if (recipeReviewFragment != null) {
                    // Tablet only

                    Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
                    if (webViewFragment != null) {

                        fm.popBackStack();
                    }

                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

                    ft.remove(recipeReviewFragment);
                    ft.commit();

                    AppHelper.animateViewFadingIn(context, layout_logo, 500, 500);
                }

                for (int i = 0; i < checkedItemsId.size(); i++) {
                    dbManager.deleteRecipeById(context, checkedItemsId.get(i));
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

        if (toolbarColor == AppConsts.ToolbarColor.PRIMARY) {
            finish();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        if (toolbar_delete.isVisible()) {
            // The user renounced selecting recipes to delete

            if (getResources().getBoolean(R.bool.isTablet)) {
                // Tablet only

                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

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

        if (toolbar_addToFavourites.isVisible()
                || toolbar_removeFromFavourites.isVisible()
                || toolbar_addServing.isVisible()) {
            // The user renounced reviewing a recipe, or adding the recipe to meal planner

            mRecipeId = AppConsts.NEW_RECIPE;

            if (getResources().getBoolean(R.bool.isTablet)) {
                // Tablet

                AppHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();

            } else {
                fm.popBackStack();
            }

            listFragment.backToDefaultDisplay(true);
            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

            return;
        }

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        if (toolbar_save.isVisible()) {
            // The user renounced editing an exiting recipe

            showExitWithoutSavingDialog();

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

            toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void showExitWithoutSavingDialog() {

        AppHelper.vibrate(this);

        final Dialog dialog = new Dialog(this);

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        final Context context = SearchInListsActivity.this;

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

                ListView listView = (ListView) listFragment.getView().findViewById(R.id.listView_searchResults);
                listView.setEnabled(true);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment recipeReviewFragment = RecipeReviewFragment.newInstance(mRecipeId, null, null);
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

}

