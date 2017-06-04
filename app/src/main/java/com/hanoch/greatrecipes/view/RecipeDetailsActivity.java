package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import com.hanoch.greatrecipes.bus.OnAppDataEvent;
import com.hanoch.greatrecipes.bus.OnForgotPasswordEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.bus.OnMailWasSentEvent;
import com.hanoch.greatrecipes.bus.OnToggleRecipeFavouriteEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.bus.OnUserRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnYummlyRecipeDownloadedEvent;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Preferences;
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
    private Dialog loginDialog;

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

        if (action == AppConsts.Actions.REVIEW_SHARED_RECIPE) {

            if (appStateManager.appData == null) {
                progressDialog.show();
                apisManager.getAppData();
            } else {
                onEvent(new OnAppDataEvent(true, null));
            }

            return;
        }

        if (action != AppConsts.Actions.ADD_NEW_USER_RECIPE) {
            mRecipeId = prevIntent.getStringExtra(AppConsts.Extras.RECIPE_ID);
        }

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

    public void onRecipeWasSaved(Boolean isNewRecipe, UserRecipe recipe) {
        // Settings after editing an existing recipe, or adding a new one to "My Own" list

        if (recipe != null) {

            AppHelper.hideKeyboardFrom(this, getCurrentFocus());

            if (isNewRecipe != null) {
                if (isNewRecipe) {
                    AppHelper.showSnackBar(mainView, R.string.added_to_my_own, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
                } else {
                    AppHelper.showSnackBar(mainView, R.string.saved_successfully, ContextCompat.getColor(this, R.color.colorSnackbarGreen));
                }
            }

            mRecipeId = recipe._id;
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
                // or saving shared recipe

                progressDialog.show();

                if (action == AppConsts.Actions.ADD_NEW_USER_RECIPE) {
                    boolean isPremium = appStateManager.user.isPremium;

                    if (!isPremium) {

                        // TODO:
                        //                    appStateManager.user.onlineDownloadsCount++ jkjhkjh;

                        //                    int createdRecipesCount = sp.getInt(AppConsts.SharedPrefs.CREATED_COUNTER, 0);
                        //                    createdRecipesCount++;
                        //                    SharedPreferences.Editor editor = sp.edit();
                        //
                        //                    editor.putInt(AppConsts.SharedPrefs.CREATED_COUNTER, createdRecipesCount);
                        //                    editor.apply();
                    }


                    userRecipe = editRecipeFragment.onSaveUserRecipeClicked();
                    apisManager.addUserRecipe(userRecipe);

                } else if (action == AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {
                    ArrayList<String> userRecipesIds = new ArrayList<>();
                    userRecipesIds.add(appStateManager.sharedUserRecipe._id);

                    apisManager.updateUserRecipes(userRecipesIds, null, BusConsts.ACTION_ADD_NEW);
                } else if (action == AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
                    ArrayList<String> yummlyRecipesIds = new ArrayList<>();
                    yummlyRecipesIds.add(appStateManager.sharedYummlyRecipe._id);

                    apisManager.updateUserRecipes(null, yummlyRecipesIds, BusConsts.ACTION_ADD_NEW);
                }
                break;

            case R.id.action_save:
                // Saving a recipe after finishing editing a recipe

                progressDialog.show();

                userRecipe = editRecipeFragment.onSaveUserRecipeClicked();
                apisManager.updateUserRecipe(userRecipe);
                break;

            case R.id.action_addServing:

                Intent resultIntent = new Intent();

                Bundle extras = new Bundle();
                extras.putString(AppConsts.Extras.RECIPE_ID, mRecipeId);
                extras.putBoolean(AppConsts.Extras.EXTRA_IS_USER_RECIPE, appStateManager.user.isUserRecipe(mRecipeId));
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
                showShareRecipeDialog();
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
    public void onEvent(OnUserRecipeDownloadedEvent event) {
        // After downloading a SHARED user-recipe from Great Recipe API.

        onSharedRecipeDownloadedEvent(event.isSuccess, event.t);
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnYummlyRecipeDownloadedEvent event) {
        // After downloading a SHARED Yummly-recipe from Great Recipe API.

        if (event.action != AppConsts.Actions.DOWNLOAD_SHARED_YUMMLY_RECIPE) {
            return;
        }

        onSharedRecipeDownloadedEvent(event.isSuccess, event.t);
    }

//-------------------------------------------------------------------------------------------------

    private void onSharedRecipeDownloadedEvent(boolean isSuccess, Throwable t) {
        progressDialog.dismiss();
        if (loginDialog != null) {
            loginDialog.dismiss();
        }

        if (isSuccess) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment fragment = RecipeReviewFragment2.newInstance(action, mRecipeId);
            ft.add(R.id.layout_container, fragment, AppConsts.Fragments.RECIPE_REVIEW);
            ft.commit();

            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
            toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
        } else {
            AppHelper.onApiErrorReceived(t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnForgotPasswordEvent event) {
        if (event.isSuccess) {
            int action = AppConsts.Actions.ACTION_FORGOT_PASSWORD;
            AppHelper.performMailSending(event.email, event.password, action);
        } else {
            progressDialog.dismiss();
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnMailWasSentEvent event) {
        // After an eMail with the password was sent
        if (action == AppConsts.Actions.ACTION_FORGOT_PASSWORD) {
            progressDialog.dismiss();
            showMailWasSentDialog(event.isSuccess, event.email);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnAppDataEvent event) {

        if (event.isSuccess) {

            if (appStateManager.user == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String email = sp.getString(AppConsts.SharedPrefs.EMAIL, null);
                String password = sp.getString(AppConsts.SharedPrefs.PASSWORD, null);

                if (email == null || password == null) {
                    progressDialog.dismiss();
                    showLoginDialog();
                } else {
                    apisManager.login(this, email, password);
                }

            } else {
                onEvent(new OnLoginEvent(true, null));
            }
        } else {
            showGetAppDataErrorDialog();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnLoginEvent event) {

        if (event.isSuccess) {

            Preferences preferences = appStateManager.user.preferences;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(AppConsts.SharedPrefs.EMAIL, preferences.email);
            editor.putString(AppConsts.SharedPrefs.USER_NAME, preferences.username);
            editor.putString(AppConsts.SharedPrefs.PASSWORD, preferences.password);
            editor.apply();

            String category = AppConsts.Analytics.CATEGORY_LOGIN;
            String action1 = "Login successfully on shared recipe";
            String label = "eMail: " + preferences.email + " | Password: " + preferences.password;
            AnalyticsHelper.sendEvent(RecipeDetailsActivity.this, category, action1, label);

            Intent prevIntent = getIntent();
            mRecipeId = prevIntent.getStringExtra("recipeId");
            boolean isUserRecipe = Boolean.parseBoolean(prevIntent.getStringExtra("isUserRecipe"));
            if (isUserRecipe) {
                action = AppConsts.Actions.REVIEW_SHARED_USER_RECIPE;
                apisManager.getUserRecipeFromGreatRecipesApi(mRecipeId);
            } else {
                action = AppConsts.Actions.REVIEW_SHARED_USER_RECIPE;
                apisManager.getYummlyRecipeFromGreatRecipesApi(this, mRecipeId, AppConsts.Actions.DOWNLOAD_SHARED_YUMMLY_RECIPE);
            }
        } else {
            progressDialog.dismiss();
            AppHelper.onApiErrorReceived(event.t, mainView);
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

        progressDialog.dismiss();

        if (event.isSuccess) {
            switch (event.action) {
                case BusConsts.ACTION_ADD_NEW:
                    onRecipeWasSaved(true, appStateManager.user.getLastUserRecipe());
                    break;
                case BusConsts.ACTION_EDIT:
                    onRecipeWasSaved(false, appStateManager.user.recipes.userRecipes.get(mRecipeId));
                    break;
            }
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
                || toolbar_removeFromFavourites.isVisible()
                || action == AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {

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
            } else if (action == AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
            } else {

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

            showExitWithoutSavingDialog(action);

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

//-------------------------------------------------------------------------------------------------

    public void showExitWithoutSavingDialog(final int action) {

        AppHelper.vibrate(this);

        final Context context = RecipeDetailsActivity.this;
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);

        final View view = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(view);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        textView_dialogTitle.setText(R.string.exit_without_saving);
        textView_dialogContent.setText(R.string.are_you_sure);

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(v -> {

            if (action == AppConsts.Actions.EDIT_USER_RECIPE) {
                // Settings are the same as after saving
                onRecipeWasSaved(null, appStateManager.user.recipes.userRecipes.get(mRecipeId));
            } else {
                finish();
            }

            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showLoginDialog() {

        loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loginDialog.setCancelable(false);
        loginDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_login, null, false);
        loginDialog.setContentView(view);

        final EditText et_email = (EditText) loginDialog.findViewById(R.id.et_email);
        final EditText et_password = (EditText) loginDialog.findViewById(R.id.et_password);
        View til_password = loginDialog.findViewById(R.id.til_password);
        View tv_mailWillBeSent = loginDialog.findViewById(R.id.tv_mailWithPasswordWillBeSent);
        View l_forgotPassword = loginDialog.findViewById(R.id.l_forgotPassword);
        View tv_forgotPasswordClick = l_forgotPassword.findViewById(R.id.tv_forgotPasswordClickHere);
        Button button_login = (Button) loginDialog.findViewById(R.id.button_login);
        Button button_cancel = (Button) loginDialog.findViewById(R.id.button_cancel);

        if (BuildConfig.DEBUG) {
            et_email.setText("han031@hanoch.test");
            et_password.setText("123456");
        }

        button_login.setOnClickListener(v -> {
            AppHelper.hideTheKeyboard(this);

            String eMail = et_email.getText().toString().trim();

            if (et_password.getVisibility() == View.VISIBLE) {
                String password = et_password.getText().toString().trim();
                if (areFieldsValidated(eMail, password)) {
                    progressDialog.show();
                    apisManager.login(RecipeDetailsActivity.this, eMail, password);
                }
            } else {
                if (isEmailValidated(eMail)) {
                    progressDialog.show();
                    apisManager.forgotPassword(eMail);
                }
            }
        });

        button_cancel.setOnClickListener(v -> {
            if (et_password.getVisibility() == View.VISIBLE) {
                finish();
            } else {
                // Back to login
                button_login.setText(R.string.login);
                button_cancel.setText(R.string.cancel);
                til_password.setVisibility(View.INVISIBLE);
                tv_mailWillBeSent.setVisibility(View.GONE);
                l_forgotPassword.setVisibility(View.VISIBLE);
            }
        });

        tv_forgotPasswordClick.setOnClickListener(v -> {
            button_login.setText(R.string.proceed);
            button_cancel.setText(R.string.back);
            til_password.setVisibility(View.INVISIBLE);
            tv_mailWillBeSent.setVisibility(View.VISIBLE);
            l_forgotPassword.setVisibility(View.INVISIBLE);
        });

        loginDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showMailWasSentDialog(boolean isSuccessful, String email) {
        progressDialog.dismiss();

        String category = AppConsts.Analytics.CATEGORY_FORGOT_PASSWORD;
        String action = "Restore password mail";
        if (isSuccessful) {
            action += " has been sent";
        } else {
            action += " sending has FAILED";
        }
        AnalyticsHelper.sendEvent(RecipeDetailsActivity.this, category, action, email);

        Dialog mailWasSentDialog = new Dialog(this);
        mailWasSentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_sent_email, null, false);
        mailWasSentDialog.setContentView(view);

        final TextView tv_dialogTitle = (TextView) mailWasSentDialog.findViewById(R.id.tv_dialogTitle);
        final TextView tv_bodyText = (TextView) mailWasSentDialog.findViewById(R.id.tv_dialogBodyText);

        String title = getString(R.string.restore_password);
        tv_dialogTitle.setText(title);

        tv_bodyText.setText(R.string.restore_password_text);

        if (!isSuccessful) {
            title += getString(R.string.has_failed) + ".\n" + getString(R.string.mail_sending_error);
            tv_bodyText.setText(title);
        }

        View button_ok = mailWasSentDialog.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(v -> {
            mailWasSentDialog.dismiss();
        });

        mailWasSentDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showShareRecipeDialog() {

        Dialog shareRecipeDialog = new Dialog(this);
        shareRecipeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_share_recipe, null, false);
        shareRecipeDialog.setContentView(view);

        final EditText et_recipientEmail = (EditText) shareRecipeDialog.findViewById(R.id.et_recipientEmail);
        if (BuildConfig.DEBUG) {
            et_recipientEmail.setText("han031@hanoch.test");
        }
        View button_proceed = shareRecipeDialog.findViewById(R.id.button_proceed);
        button_proceed.setOnClickListener(v -> {
            String recipientEmail = et_recipientEmail.getText().toString().trim();
            if (isEmailValidated(recipientEmail)) {
                apisManager.shareRecipe(recipientEmail, mRecipeId);

                String category = AppConsts.Analytics.CATEGORY_REGISTER;
                String text = "Share recipe";
                String label = "from " + appStateManager.user.preferences.email + " to " + recipientEmail;
                AnalyticsHelper.sendEvent(RecipeDetailsActivity.this, category, text, label);

                shareRecipeDialog.dismiss();
            }
        });

        shareRecipeDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showGetAppDataErrorDialog() {

        progressDialog.dismiss();

        Dialog errorDialog = new Dialog(this);
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        errorDialog.setCancelable(false);
        errorDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_splash_error, null, false);
        errorDialog.setContentView(view);

        TextView tv_dialogBodyText = (TextView) errorDialog.findViewById(R.id.tv_dialogBodyText);
        View button_retry = errorDialog.findViewById(R.id.button_retry);
        Button button_leave = (Button) errorDialog.findViewById(R.id.button_leave);

        tv_dialogBodyText.setText(R.string.error_getting_app_data);

        button_retry.setOnClickListener(v -> {
            apisManager.getAppData();
            errorDialog.dismiss();
        });

        button_leave.setOnClickListener(v -> finish());

        errorDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private boolean areFieldsValidated(String eMail, String password) {

        if (!isEmailValidated(eMail)) {
            return false;
        }

        char[] chars = password.toCharArray();
        for (char aChar : chars) {
            String sChar = String.valueOf(aChar);
            if (!AppConsts.Regex.PASSWORD_PATTERN.contains(sChar)) {
                AppHelper.showSnackBar(mainView, R.string.invalid_password, Color.RED);
                return false;
            }
        }
        int trimmedLength = password.trim().length();
        if (trimmedLength < 6 || trimmedLength > 10) {
            AppHelper.showSnackBar(mainView, R.string.password_should_contain_6_to_10_chars, Color.RED);
            return false;
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    private boolean isEmailValidated(String eMail) {

        Pattern emailPattern = Pattern.compile(AppConsts.Regex.EMAIL_PATTERN);
        Matcher emailMatcher = emailPattern.matcher(eMail);
        if (!emailMatcher.matches()) {
            AppHelper.showSnackBar(mainView, R.string.invalid_email, Color.RED);
            return false;
        }

        return true;
    }
}
