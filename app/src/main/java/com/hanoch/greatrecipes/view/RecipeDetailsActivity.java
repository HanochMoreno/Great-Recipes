package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.TextView;

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnShareRecipeEvent;
import com.hanoch.greatrecipes.bus.OnToggleRecipeFavouriteEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeDetailsActivity extends AppCompatActivity implements
        RecipeReviewFragment2.FragmentRecipeReviewListener,
        ToolbarMenuSetting,
        EditRecipeFragment2.OnFragmentEditRecipeListener {

    private String mRecipeId;

    /**
     * Optional actions for this activity:
     * ADD_NEW_USER_RECIPE.
     * EDIT_USER_RECIPE.
     * REVIEW_YUMMLY_RECIPE.
     * REVIEW_USER_RECIPE.
     * ADD_SERVING_FROM_LISTS.
     */
    private int action;

    private int toolbarColor;
    private String toolbarTitle;

    private Toolbar toolbar;

    private MenuItem toolbar_addToList;
    private MenuItem toolbar_save;
    private MenuItem toolbar_edit;
    private MenuItem toolbar_addToFavourites;
    private MenuItem toolbar_removeFromFavourites;
    private MenuItem toolbar_addServing;
    private MenuItem toolbar_ok;
    private MenuItem toolbar_closeWebView;
    private MenuItem toolbar_share;

    private ArrayList<ObjectAnimator> fadingInAnimationsList;
    private Bundle savedInstanceState;

    private ApisManager apisManager;
    private MyBus bus;
    private ProgressDialog progressDialog;
    private AppStateManager appStateManager;
    private View mainView;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        appStateManager = AppStateManager.getInstance();
        apisManager = ApisManager.getInstance();

        bus = MyBus.getInstance();
        bus.register(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String activityToolbarTitle = getString(R.string.recipe_details);
        toolbar.setTitle(activityToolbarTitle);

        toolbarColor = AppConsts.ToolbarColor.PRIMARY;
        toolbarTitle = activityToolbarTitle;

        Intent prevIntent = getIntent();
        action = Integer.parseInt(prevIntent.getAction());

        if (action != AppConsts.Actions.ADD_NEW_USER_RECIPE) {
            mRecipeId = prevIntent.getStringExtra(AppConsts.Extras.RECIPE_ID);
        }

        AppHelper.hideTheKeyboard(this);

        if (savedInstanceState == null) {
            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment;

            int action = this.action;
            if (action == AppConsts.Actions.ADD_SERVING_FROM_LISTS) {
                if (appStateManager.user.isUserRecipe(mRecipeId)) {
                    action = AppConsts.Actions.REVIEW_USER_RECIPE;
                } else {
                    action = AppConsts.Actions.REVIEW_YUMMLY_RECIPE;
                }
            }

            switch (action) {

                case AppConsts.Actions.REVIEW_SHARED_USER_RECIPE:
                case AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE:
                case AppConsts.Actions.REVIEW_YUMMLY_RECIPE:
                case AppConsts.Actions.REVIEW_USER_RECIPE:
                    // Show details of a recipe from the user lists

                    fragment = RecipeReviewFragment2.newInstance(action, mRecipeId);
                    ft.add(R.id.layout_container, fragment, AppConsts.Fragments.RECIPE_REVIEW);
                    break;

                case AppConsts.Actions.EDIT_USER_RECIPE:
                case AppConsts.Actions.ADD_NEW_USER_RECIPE:

                    fragment = EditRecipeFragment2.newInstance(action, mRecipeId);
                    ft.add(R.id.layout_container, fragment, AppConsts.Fragments.EDIT_RECIPE);
                    break;
            }

            ft.commit();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mainView = findViewById(android.R.id.content);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        action = savedInstanceState.getInt("action");
        mRecipeId = savedInstanceState.getString("mRecipeId");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("action", action);
        outState.putString("mRecipeId", mRecipeId);

        outState.putInt("toolbarColor", toolbarColor);
        outState.putString("toolbarTitle", toolbarTitle);

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.SAVE);
        buttons.add(AppConsts.ToolbarButtons.EDIT);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.OK);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        buttons.add(AppConsts.ToolbarButtons.SHARE);

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
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    public void onToggleFavourite() {
        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
        toolbarButtonsList.add(AppConsts.ToolbarButtons.SHARE);

        if (appStateManager.user.isUserRecipeCreatedByThisUser(mRecipeId)) {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
        }

        boolean isFavourite = appStateManager.isRecipeFavourite(mRecipeId);
        if (isFavourite) {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        } else {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        }

        FragmentManager fm = getSupportFragmentManager();
        RecipeReviewFragment2 recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
        recipeReviewFragment.setFavouriteImage(isFavourite);
        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
    }

//-------------------------------------------------------------------------------------------------

    public void onExitWithoutSavingClick(int action) {
        if (action == AppConsts.Actions.EDIT_USER_RECIPE) {
            // Settings are the same as after saving
            onRecipeWasSaved(null);
        } else {
            finish();
        }
    }

//-------------------------------------------------------------------------------------------------

    public void onRecipeWasSaved(Boolean isNewRecipe) {
        // Settings after editing an existing recipe, or adding a new one to "My Own" list

        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

        if (isNewRecipe != null) {
            if (isNewRecipe) {
                AppHelper.showSnackBar(mainView, R.string.added_to_my_own, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
            } else {
                AppHelper.showSnackBar(mainView, R.string.saved_successfully, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
            }
        }

        action = AppConsts.Actions.REVIEW_USER_RECIPE;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment recipeReviewFragment = RecipeReviewFragment2.newInstance(action, mRecipeId);

        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);

        ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
        ft.commit();

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
        toolbarButtonsList.add(AppConsts.ToolbarButtons.SHARE);
        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);

        if (appStateManager.isRecipeFavourite(mRecipeId)) {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        } else {
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        }

        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAddCategoriesClick(ArrayList<String> categoriesList) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment categoriesChooserFragment = CategoriesChooserFragment.newInstance(categoriesList);

        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);

        ft.replace(R.id.layout_container, categoriesChooserFragment, AppConsts.Fragments.CATEGORIES_CHOOSER);

        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.OK);

        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.NO_CHANGE, getString(R.string.choose_categories));
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onGetInstructionsClick(String url) {
        // Phone only

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment webViewFragment = WebViewFragment.newInstance(url);

        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_up, R.anim.slide_out_up);

        ft.replace(R.id.layout_container, webViewFragment, AppConsts.Fragments.WEB_VIEW);
        ft.addToBackStack(null);
        ft.commit();

        ArrayList<Integer> toolbarButtons = new ArrayList<>();
        toolbarButtons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_recipe_details, menu);

        toolbar_addToList = menu.findItem(R.id.action_addToList).setVisible(false);
        toolbar_save = menu.findItem(R.id.action_save).setVisible(false);
        toolbar_edit = menu.findItem(R.id.action_edit).setVisible(false);
        toolbar_addToFavourites = menu.findItem(R.id.action_addToFavourites).setVisible(false);
        toolbar_removeFromFavourites = menu.findItem(R.id.action_removeFromFavourites).setVisible(false);
        toolbar_addServing = menu.findItem(R.id.action_addServing).setVisible(false);
        toolbar_ok = menu.findItem(R.id.action_ok).setVisible(false);
        toolbar_closeWebView = menu.findItem(R.id.action_closeWebview).setVisible(false);
        toolbar_share = menu.findItem(R.id.action_share).setVisible(false);

        if (savedInstanceState == null) {

            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

            switch (action) {
                case AppConsts.Actions.ADD_SERVING_FROM_LISTS:
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                    break;

                case AppConsts.Actions.REVIEW_USER_RECIPE:
                    if (appStateManager.user.isUserRecipeCreatedByThisUser(mRecipeId)) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                    }
                    // no break
                case AppConsts.Actions.REVIEW_YUMMLY_RECIPE:
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.SHARE);

                    if (appStateManager.isRecipeFavourite(mRecipeId)) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
                    }
                    break;

                case AppConsts.Actions.EDIT_USER_RECIPE:
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
                    break;

                case AppConsts.Actions.ADD_NEW_USER_RECIPE:
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
                    break;
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

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
            }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        FragmentManager fm = getSupportFragmentManager();
        EditRecipeFragment2 editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
        UserRecipe userRecipe;

        switch (itemId) {

            case R.id.action_edit:

                fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                editRecipeFragment = EditRecipeFragment2.newInstance(AppConsts.Actions.EDIT_USER_RECIPE, mRecipeId);
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left);
                ft.replace(R.id.layout_container, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);
                ft.commit();

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

                action = AppConsts.Actions.EDIT_USER_RECIPE;

                break;

            case R.id.action_addToList:
                // Saving after adding a new recipe to "my own" list

                UserRecipe userRecipeToAdd = editRecipeFragment.onSaveUserRecipeClicked();
                if (userRecipeToAdd == null) {
                    // No changes made
                    onExitWithoutSavingClick(AppConsts.Actions.EDIT_USER_RECIPE);
                } else if (userRecipeToAdd.recipeTitle.isEmpty()) {
                    // No recipe title
                    AppHelper.showSnackBar(mainView, R.string.recipe_title_is_empty, Color.RED);
                } else {
                    progressDialog.show();
                    apisManager.addUserRecipe(userRecipeToAdd);
                }
                break;

            case R.id.action_save:
                // Saving a recipe after finishing editing a recipe

                UserRecipe userRecipeToSave = editRecipeFragment.onSaveUserRecipeClicked();
                if (userRecipeToSave == null) {
                    // No changes made
                    onExitWithoutSavingClick(AppConsts.Actions.EDIT_USER_RECIPE);
                } else if (userRecipeToSave.recipeTitle.isEmpty()) {
                    // No recipe title
                    AppHelper.showSnackBar(mainView, R.string.recipe_title_is_empty, Color.RED);
                } else {
                    progressDialog.show();
                    apisManager.updateUserRecipe(userRecipeToSave);
                }
                break;

            case R.id.action_addServing:

                Intent resultIntent = new Intent();

                Bundle extras = new Bundle();
                extras.putString(AppConsts.Extras.RECIPE_ID, mRecipeId);
                extras.putBoolean(AppConsts.Extras.IS_USER_RECIPE, appStateManager.user.isUserRecipe(mRecipeId));
                resultIntent.putExtras(extras);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToFavourites:
            case R.id.action_removeFromFavourites:

                progressDialog.show();
                apisManager.toggleRecipeFavourite(mRecipeId);

                break;

            case R.id.action_ok:
                // The user finished choosing categories in categoriesChooserFragment

                CategoriesChooserFragment categoriesChooserFragment = (CategoriesChooserFragment) fm.findFragmentByTag(AppConsts.Fragments.CATEGORIES_CHOOSER);
                ArrayList<String> categoriesList = categoriesChooserFragment.getSelectedCategoriesNamesList();
                editRecipeFragment.updateCategoriesList(categoriesList);

                onBackPressed();
                break;

            case R.id.action_closeWebview:
                onBackPressed();
                break;

            case R.id.action_share:
                AppHelper.showShareRecipeDialog(this, progressDialog, mRecipeId);
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
            toolbar.setBackgroundColor(color);
            toolbarColor = color;
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
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnToggleRecipeFavouriteEvent event) {
        // After toggling recipe favourite index

        progressDialog.dismiss();

        if (event.isSuccess) {
            onToggleFavourite();
        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateUserRecipesEvent event) {
        // After saving a new or edited user-recipe

        if (event.action == BusConsts.ACTION_ADD_SHARED_RECIPE) {
            return;
        }

        progressDialog.dismiss();

        if (event.isSuccess) {
            switch (event.action) {
                case BusConsts.ACTION_ADD_NEW:
                    mRecipeId = appStateManager.lastAddedUserRecipe._id;
                    // No break...
                case BusConsts.ACTION_EDIT:
                    onRecipeWasSaved(action == BusConsts.ACTION_ADD_NEW);
                    break;
            }
        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnShareRecipeEvent event) {

        progressDialog.dismiss();

        if (event.isSuccess) {
            int color = ContextCompat.getColor(this, R.color.colorSnackbarGreen);
            AppHelper.showSnackBar(mainView, R.string.recipe_was_sent, color);
        } else {
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    public ArrayList<Integer> getAndHideAllToolbarButtons() {

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
        buttons.add(AppConsts.ToolbarButtons.SAVE);
        buttons.add(AppConsts.ToolbarButtons.EDIT);
        buttons.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
        buttons.add(AppConsts.ToolbarButtons.ADD_SERVING);
        buttons.add(AppConsts.ToolbarButtons.OK);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);
        buttons.add(AppConsts.ToolbarButtons.SHARE);

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

    @Override
    public void onBackPressed() {

        if (toolbar_addServing.isVisible()
                || toolbar_addToFavourites.isVisible()
                || toolbar_removeFromFavourites.isVisible()) {

            finish();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        if (toolbar_closeWebView.isVisible()) {
            fm.popBackStack();

            if (action == AppConsts.Actions.ADD_SERVING_FROM_LISTS) {
                // The user is choosing a serving
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SHARE);

                if (appStateManager.isRecipeFavourite(mRecipeId)) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);
                }
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

            return;
        }

        if (toolbar_save.isVisible() || toolbar_addToList.isVisible()) {
            // The user renounced editing an existing recipe, or adding a new one to "My Own" recipes


            int action = toolbar_save.isVisible()
                    ? AppConsts.Actions.EDIT_USER_RECIPE
                    : AppConsts.Actions.ADD_NEW_USER_RECIPE;

            EditRecipeFragment2 editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);
            editRecipeFragment.showExitWithoutSavingDialog(action);
//            showExitWithoutSavingDialog(action);

            return;
        }

        if (toolbar_ok.isVisible()) {
            // The user renounced choosing categories

            fm.popBackStack();

            if (action == AppConsts.Actions.ADD_NEW_USER_RECIPE) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            } else {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
            }

            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
        }
    }

}