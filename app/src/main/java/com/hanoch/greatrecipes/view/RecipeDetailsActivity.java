package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity implements
        RecipeReviewFragment.FragmentRecipeReviewListener,
        ToolbarMenuSetting,
        EditRecipeFragment.OnFragmentEditRecipeListener {

    private long mRecipeId;
    private String action;
    private String extra_serving;

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
    private MenuItem toolbar_refresh;

    private ArrayList<ObjectAnimator> fadingInAnimationsList;
    private Bundle savedInstanceState;

    private DbManager dbManager;

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dbManager = ((GreatRecipesApplication) getApplication()).getDbManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String activityToolbarTitle = getString(R.string.recipe_details);
        toolbar.setTitle(activityToolbarTitle);

        toolbarColor = AppConsts.ToolbarColor.PRIMARY;
        toolbarTitle = activityToolbarTitle;

        Intent prevIntent = getIntent();
        mRecipeId = prevIntent.getLongExtra(AppConsts.Extras.EXTRA_RECIPE_ID, AppConsts.NEW_RECIPE);
        extra_serving = prevIntent.getStringExtra(AppConsts.Extras.EXTRA_SERVING);

        action = prevIntent.getAction();

        if (savedInstanceState == null) {
            // only add fragments if the state is null!
            // if there's a state - they will be re-attached automatically

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment;

            if (action.equals(AppConsts.Actions.ACTION_REVIEW)) {
                // Show details of a recipe came from one of the user lists
                fragment = RecipeReviewFragment.newInstance(mRecipeId, null, extra_serving);
                ft.add(R.id.layout_container, fragment, AppConsts.Fragments.RECIPE_REVIEW);

            } else if (action.equals(AppConsts.Actions.ACTION_EDIT)
                    || action.equals(AppConsts.Actions.ACTION_ADD_NEW)) {

                fragment = EditRecipeFragment.newInstance(mRecipeId);
                ft.add(R.id.layout_container, fragment, AppConsts.Fragments.EDIT_RECIPE);
            }

            ft.commit();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        mRecipeId = savedInstanceState.getLong("mRecipeId");
        extra_serving = savedInstanceState.getString("extra_serving");
        action = savedInstanceState.getString("action");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("mRecipeId", mRecipeId);
        outState.putString("action", action);
        outState.putString("extra_serving", extra_serving);

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

    @Override
    public void onRecipeWasSaved(long recipeId) {

        // Settings after editing an existing recipe, or adding a new one to "My Own" list

        mRecipeId = recipeId;

        AppHelper.hideKeyboardFrom(this, getCurrentFocus());

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        Fragment recipeReviewFragment = RecipeReviewFragment.newInstance(recipeId, null, null);

        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);

        ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
        ft.commit();

        action = AppConsts.Actions.ACTION_REVIEW;

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
        finish();
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
        toolbar_refresh = menu.findItem(R.id.action_refresh).setVisible(false);

        if (savedInstanceState == null) {

            ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

            switch (action) {

                case AppConsts.Actions.ACTION_REVIEW:
                    // Show details of a recipe came from one of the user lists

                    Recipe recipe = dbManager.queryRecipeObjectById(mRecipeId);

                    if (extra_serving != null) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
                        setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
                        return true;
                    }

                    if (recipe.originIndex == AppConsts.RecipeOrigin.ADDED_MANUALLY) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                    }

                    if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                    }

                    break;

                case AppConsts.Actions.ACTION_EDIT:
                    // The user wants to edit an existing recipe
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);

                    break;

                default:
                    // The user wants to add a new recipe manually (his own recipe)
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

        Recipe recipe;

        ArrayList<Integer> toolbarButtonsList;

        FragmentManager fm = getSupportFragmentManager();
        RecipeReviewFragment2 recipeReviewFragment = (RecipeReviewFragment2) fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
        EditRecipeFragment2 editRecipeFragment = (EditRecipeFragment2) fm.findFragmentByTag(AppConsts.Fragments.EDIT_RECIPE);

        switch (itemId) {

            case R.id.action_edit:

                fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                editRecipeFragment = EditRecipeFragment2.newInstance(mRecipeId);
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left);
                ft.replace(R.id.layout_container, editRecipeFragment, AppConsts.Fragments.EDIT_RECIPE);
                ft.commit();

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.SAVE);
                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

                action = AppConsts.Actions.ACTION_EDIT;

                break;

            case R.id.action_save:
                // Saving a recipe after finishing editing a recipe

                editRecipeFragment.onSaveRecipe();

                // After the saving, the editFragment calls the recipeWasSaved(long mRecipeId)
                //  method of this activity, and updates mRecipeId

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
                    editor.commit();
                }

                editRecipeFragment.onSaveRecipe();

                // After the saving, the editFragment calls the recipeWasSaved(long mRecipeId)
                //  method of this activity, and updates mRecipeId

                break;

            case R.id.action_addServing:

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConsts.Extras.EXTRA_RECIPE_ID, mRecipeId);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                break;

            case R.id.action_addToFavourites:

                recipe = dbManager.queryRecipeObjectById(mRecipeId);
                recipe.favouriteIndex = AppConsts.FavouriteIndex.FAVOURITE;
                dbManager.updateRecipe(recipe);

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);

                if (recipe.originIndex != AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.EDIT);
                }

                recipeReviewFragment.setFavouriteImage(AppConsts.FavouriteIndex.FAVOURITE);
                setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
                break;

            case R.id.action_removeFromFavourites:

                recipe = dbManager.queryRecipeObjectById(mRecipeId);
                recipe.favouriteIndex = AppConsts.FavouriteIndex.NOT_FAVOURITE;
                dbManager.updateRecipe(recipe);

                toolbarButtonsList = new ArrayList<>();
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                if (recipe.originIndex != AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH) {
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

            case R.id.action_refresh:
                editRecipeFragment.onSaveUserRecipeClicked();
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

            allButtons.stream().filter(toolbarButtonsList::contains).forEach(button -> {
                MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                fadingInAnimationsList.add(AppHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 600));
            });
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
        buttons.add(AppConsts.ToolbarButtons.REFRESH);

        // Resetting all buttons to invisible

        if (fadingInAnimationsList != null) {
            // Cancelling all buttons "fading-in" animations, if exists
            fadingInAnimationsList.forEach(ValueAnimator::cancel);
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

            if (extra_serving == null) {

                Recipe recipe = dbManager.queryRecipeObjectById(mRecipeId);

                if (recipe.favouriteIndex == AppConsts.FavouriteIndex.NOT_FAVOURITE) {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_FAVOURITES);

                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.REMOVE_FROM_FAVOURITES);
                }

            } else {
                // The user is choosing a serving
                toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);
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

            if (action.equals(AppConsts.Actions.ACTION_ADD_NEW)) {
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

        final Context context = RecipeDetailsActivity.this;

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

        textView_dialogTitle.setText(R.string.exit_without_saving);
        textView_dialogContent.setText(R.string.are_you_sure);

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (action.equals(AppConsts.Actions.ACTION_EDIT)) {
                    onRecipeWasSaved(mRecipeId); // Settings are the same as after saving

                } else {
                    // action = ACTION_ADD_NEW
                    finish();
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

}
