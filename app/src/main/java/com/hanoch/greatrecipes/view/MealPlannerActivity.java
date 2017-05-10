package com.hanoch.greatrecipes.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.OnUpdateServingsListEvent;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.ServingType;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.hanoch.greatrecipes.view.adapters.ServingTypesAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class MealPlannerActivity extends AppCompatActivity implements
        ServingsListFragment2.FragmentServingsListListener,
        RecipeReviewFragment2.FragmentRecipeReviewListener,
        ToolbarMenuSetting {

    private static final int REQ_CODE_ONLINE_SEARCH_ACTIVITY = 1;
    private static final int REQ_CODE_LISTS_ACTIVITY = 2;

    private ServingsListFragment2 listFragment;

    private LinearLayout layout_logo;
    private Toolbar toolbar;

    private String activityToolbarTitle;

    private MenuItem toolbar_clearServingsList;
    private MenuItem toolbar_delete;
    private MenuItem toolbar_closeWebView;

    private ArrayList<ObjectAnimator> fadingInAnimationsList;

    private Bundle savedInstanceState;

    private ArrayList<String> selectedItemsId;

    private int listSize;
    private Serving mServing;
    private String servingType;

    private ArrayList<ServingType> servingTypesList = new ArrayList<>();;

    private int toolbarColor;
    private String toolbarTitle;

    private GreatRecipesDbManager dbManager;
    private AppStateManager appStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        dbManager = GreatRecipesDbManager.getInstance();
        appStateManager = AppStateManager.getInstance();

        selectedItemsId = new ArrayList<>();

        activityToolbarTitle = getString(R.string.meal_planner);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(activityToolbarTitle);
        setSupportActionBar(toolbar);

        toolbarColor = AppConsts.ToolbarColor.PRIMARY;
        toolbarTitle = activityToolbarTitle;

        AppHelper.hideTheKeyboard(this);

        int containerResId;
        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet:

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

            Fragment servingsListFragment = ServingsListFragment2.newInstance();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(containerResId, servingsListFragment, AppConsts.Fragments.SERVINGS_LIST);
            ft.commit();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        if (getResources().getBoolean(R.bool.isTablet)) {
            if (getResources().getBoolean(R.bool.isSmallTablet)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        AnalyticsHelper.setScreenName(this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        listFragment = (ServingsListFragment2) getSupportFragmentManager().findFragmentByTag(AppConsts.Fragments.SERVINGS_LIST);

        if (getResources().getBoolean(R.bool.isTablet)) {

            int layout_logoVisibility = savedInstanceState.getInt("layout_logoVisibility");
            layout_logo.setVisibility(layout_logoVisibility);
        }

        selectedItemsId = savedInstanceState.getStringArrayList("selectedItemsId");
        servingType = savedInstanceState.getString("servingType");
        mServing = new Gson().fromJson(savedInstanceState.getString("servingAsJson"), Serving.class);

        listSize = savedInstanceState.getInt("listSize");
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getResources().getBoolean(R.bool.isTablet)) {
            int layout_logoVisibility = layout_logo.getVisibility();
            outState.putInt("layout_logoVisibility", layout_logoVisibility);
        }

        if (mServing != null) {
            outState.putString("servingAsJson", new Gson().toJson(mServing, Serving.class));
        }

        outState.putStringArrayList("selectedItemsId", selectedItemsId);
        outState.putString("servingType", servingType);
        outState.putInt("listSize", listSize);
        outState.putInt("toolbarColor", toolbarColor);
        outState.putString("toolbarTitle", toolbarTitle);

        ArrayList<Integer> buttons = new ArrayList<>();

        buttons.add(AppConsts.ToolbarButtons.DELETE);
        buttons.add(AppConsts.ToolbarButtons.CLEAR_SERVING_LIST);
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
    public void onListSizeChanged(int listSize) {
        this.listSize = listSize;

        if (toolbar_clearServingsList != null) {

            updateClearListButtonVisibility(listSize);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void updateClearListButtonVisibility(int listSize) {

        if (listSize == 0) {
            setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);

        } else {
            ArrayList<Integer> toolbarButtons = new ArrayList<>();
            toolbarButtons.add(AppConsts.ToolbarButtons.CLEAR_SERVING_LIST);
            setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.PRIMARY, activityToolbarTitle);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void showRecipeDetails(Serving serving) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment recipeReviewFragment;

        if (getResources().getBoolean(R.bool.isTablet)) {
            // tablet

            recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (mServing != null && mServing.recipeId.equals(serving.recipeId) && recipeReviewFragment != null) {
                return;
            }

            if (layout_logo.getVisibility() == View.VISIBLE) {
                AnimationHelper.animateViewFadingOut(this, layout_logo, 500, 0);
            }

            mServing = serving;

            Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
            if (webViewFragment != null) {
                onBackPressed();
            }

            recipeReviewFragment = RecipeReviewFragment2.newInstance(serving.recipeId, serving.isUserRecipe, AppConsts.Extras.REVIEW_SERVING);
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
            ft.replace(R.id.layout_detailsContainer, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);

        } else {
            // phone

            recipeReviewFragment = RecipeReviewFragment2.newInstance(serving.recipeId, serving.isUserRecipe, AppConsts.Extras.REVIEW_SERVING);
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
            ft.replace(R.id.layout_container, recipeReviewFragment, AppConsts.Fragments.RECIPE_REVIEW);
            ft.addToBackStack(null);
        }

        ft.commit();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onServingChecked(Serving serving, boolean isChecked) {

        if (isChecked) {
            selectedItemsId.add(serving.servingId);

            if (selectedItemsId.size() == 1) {
                ArrayList<Integer> toolbarButtons = new ArrayList<>();
                toolbarButtons.add(AppConsts.ToolbarButtons.DELETE);
                setToolbarAttr(toolbarButtons, AppConsts.ToolbarColor.ACCENT, null);
            }

        } else {
            selectedItemsId.remove(serving.servingId);

            if (selectedItemsId.isEmpty()) {
                updateClearListButtonVisibility(listSize);
            }
        }

        if (selectedItemsId.size() == 1) {

            if (getResources().getBoolean(R.bool.isTablet)) {

                showRecipeDetails(serving);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAddNewServingClick() {
        // pop-up custom alert-dialog to insert a new ingredient:

        final Activity thisActivity = this;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_add_serving, null, false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);

        // Retrieve views from the inflated dialog layout and update their values
        final TextView dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        dialogTitle.setText(R.string.choose_serving_type);

        final RadioGroup viewGroup_searchSelector = (RadioGroup) view.findViewById(R.id.radioGroup_searchSelector);
        final RadioButton radioButton_fromLists = (RadioButton) view.findViewById(R.id.radioButton_fromLists);
        final GridView gridView_servingTypesList = (GridView) view.findViewById(R.id.gridView_servingTypesList);
        final TextView textView_servingType = (TextView) view.findViewById(R.id.textView_selectedType);
        final LinearLayout layout_buttons = (LinearLayout) view.findViewById(R.id.layout_dialogButtons);
        final LinearLayout layout_servingType = (LinearLayout) view.findViewById(R.id.layout_servingType);

        viewGroup_searchSelector.setVisibility(View.GONE);
        layout_buttons.setVisibility(View.GONE);
        layout_servingType.setVisibility(View.GONE);

        if (servingTypesList.isEmpty()) {
            servingTypesList.add(new ServingType(AppConsts.Serving.APPETIZER, R.drawable.cat_appetizers));
            servingTypesList.add(new ServingType(AppConsts.Serving.SIDE_DISH, R.drawable.cat_side_dishes));
            servingTypesList.add(new ServingType(AppConsts.Serving.MAIN_DISH, R.drawable.cat_main_dishes));
            servingTypesList.add(new ServingType(AppConsts.Serving.SALAD, R.drawable.cat_salads));
            servingTypesList.add(new ServingType(AppConsts.Serving.PASTRY, R.drawable.cat_breads));
            servingTypesList.add(new ServingType(AppConsts.Serving.BEVERAGE, R.drawable.cat_beverages));
            servingTypesList.add(new ServingType(AppConsts.Serving.SAUCE, R.drawable.cat_condiments_and_sauces));
            servingTypesList.add(new ServingType(AppConsts.Serving.DESSERT, R.drawable.cat_desserts));
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);
        final int downloadedRecipesCount = (premium ? 0 : (sp.getInt(AppConsts.SharedPrefs.DOWNLOADED_COUNTER, 0)));

        ServingTypesAdapter adapter = new ServingTypesAdapter(this, servingTypesList, premium);
        gridView_servingTypesList.setAdapter(adapter);

        gridView_servingTypesList.setOnItemClickListener((parent, view1, position, id) -> {
            View textView_premium = view1.findViewById(R.id.textView_premium);

            if (textView_premium.getVisibility() == View.VISIBLE) {

                Animation animation = new ScaleAnimation(1f, 2f, 1f, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(700);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(1);
                textView_premium.startAnimation(animation);

                return;
            }

            servingType = servingTypesList.get(position).name;

            String translatedServingType = AppHelper.getTranslatedServingTypeName(MealPlannerActivity.this, servingType);
            textView_servingType.setText(translatedServingType);

            gridView_servingTypesList.setVisibility(View.GONE);
            viewGroup_searchSelector.setVisibility(View.VISIBLE);
            layout_buttons.setVisibility(View.VISIBLE);
            layout_servingType.setVisibility(View.VISIBLE);
            dialogTitle.setText(R.string.does_recipe_exist);
        });

        ImageView button_chooseServingType = (ImageView) dialog.findViewById(R.id.button_chooseServingType);

        button_chooseServingType.setOnClickListener(v -> {
            gridView_servingTypesList.setVisibility(View.VISIBLE);
            viewGroup_searchSelector.setVisibility(View.GONE);
            layout_buttons.setVisibility(View.GONE);
            layout_servingType.setVisibility(View.GONE);
            dialogTitle.setText(R.string.choose_serving_type);
        });

        Button button_search = (Button) dialog.findViewById(R.id.button_search);
        button_search.setOnClickListener(v -> {

            if (radioButton_fromLists.isChecked()) {
                openListsActivity();

            } else {
                if (!premium && downloadedRecipesCount == 3) {
                    AnalyticsHelper.sendEvent(thisActivity, AppConsts.Analytics.CATEGORY_PREMIUM_HANDLING, "You exceeded snackbar was shown", "Online");
                    AppHelper.showSnackBar(view, R.string.you_exceeded_the_downloaded_recipes_limit, ContextCompat.getColor(thisActivity, R.color.colorSnackbarFreeTrial));
                    return;
                } else {
                    openOnlineSearchActivity();
                }
            }

            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onGetInstructionsClick(String url) {

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_up, R.anim.slide_out_up);

        Fragment webViewFragment = WebViewFragment.newInstance(url);

        if (getResources().getBoolean(R.bool.isTablet)) {

            // phone
            ft.replace(R.id.layout_detailsContainer, webViewFragment, AppConsts.Fragments.WEB_VIEW);

        } else {
            // tablet
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
    public void setToolbarAttr(ArrayList<Integer> toolbarButtonsList, int color, String title) {

        toolbar.setTitle(title);

        toolbarTitle = title;

        if (color != AppConsts.ToolbarColor.NO_CHANGE) {
            toolbar.setBackgroundColor(color);
            toolbarColor = color;
        }

        fadingInAnimationsList = new ArrayList<>();

        ArrayList<Integer> allButtons = getAndHideAllToolbarButtons();

        if (toolbarButtonsList != null) {

            allButtons.stream().filter(toolbarButtonsList::contains).forEach(button -> {
                MenuItem toolbarButton = toolbar.getMenu().findItem(button);
                if (button == AppConsts.ToolbarButtons.CLEAR_SERVING_LIST) {
                    toolbarButton.setVisible(true);
                } else {
                    fadingInAnimationsList.add(AnimationHelper.animateToolbarButtonFadingIn(toolbarButton, 500, 0));
                }
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

        buttons.add(AppConsts.ToolbarButtons.DELETE);
        buttons.add(AppConsts.ToolbarButtons.CLEAR_SERVING_LIST);
        buttons.add(AppConsts.ToolbarButtons.CLOSE_WEBVIEW);

        // Resetting all buttons to invisible
        MenuItem toolBarButton;
        for (Integer button : buttons) {

            toolBarButton = toolbar.getMenu().findItem(button);
            if (toolBarButton.isVisible()) {
                if (button == AppConsts.ToolbarButtons.CLEAR_SERVING_LIST) {
                    toolBarButton.setVisible(false);
                } else {
                    AnimationHelper.animateToolbarButtonFadingOut(toolBarButton, 500, 0);
                }
            }
        }

        return buttons;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_create_meal, menu);

        toolbar_clearServingsList = menu.findItem(R.id.action_clearServingsList).setVisible(false);
        toolbar_delete = menu.findItem(R.id.action_delete).setVisible(false);
        toolbar_closeWebView = menu.findItem(R.id.action_closeWebview).setVisible(false);

        if (savedInstanceState == null) {

            if (listSize > 0) {
                toolbar_clearServingsList.setVisible(true);
            }

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
                // Specific case: the user uses phone and got in the activity in landscape mode

                if (listSize > 0) {
                    toolbar_clearServingsList.setVisible(true);
                }
            }
        }

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {

            case R.id.action_clearServingsList:
                onDeleteItemsClicked(true);
                break;

            case R.id.action_delete:
                onDeleteItemsClicked(false);
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

    private void openOnlineSearchActivity() {

        Intent intent = new Intent(this, OnlineSearchActivity.class);
        intent.putExtra(AppConsts.Extras.EXTRA_SERVING, AppConsts.Extras.ADD_SERVING);
        startActivityForResult(intent, REQ_CODE_ONLINE_SEARCH_ACTIVITY);
    }

//-------------------------------------------------------------------------------------------------

    private void openListsActivity() {

        Intent intent = new Intent(this, RecipesListsActivity.class);
        intent.putExtra(AppConsts.Extras.EXTRA_SERVING, AppConsts.Extras.ADD_SERVING);
        startActivityForResult(intent, REQ_CODE_LISTS_ACTIVITY);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The user finished choosing a recipe
        if (resultCode == Activity.RESULT_OK) {
            // The request code doesn't matter in this case

            String recipeId = data.getStringExtra(AppConsts.Extras.EXTRA_RECIPE_ID);
            boolean isUserRecipe = data.getBooleanExtra(AppConsts.Extras.EXTRA_IS_USER_RECIPE, false);

            Serving serving = new Serving();
            serving.recipeId = recipeId;
            serving.isUserRecipe = isUserRecipe;
            serving.servingId = String.valueOf(System.currentTimeMillis());
            serving.servingType = servingType;

            ArrayList<Serving> servingsList = new ArrayList<>();
            servingsList.add(serving);
            dbManager.updateUserServingsMap(servingsList, BusConsts.ACTION_ADD_NEW);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Subscribe
    public void onEvent(OnUpdateServingsListEvent event) {
        if (event.isSuccess) {
            FragmentManager fm = getSupportFragmentManager();
            ServingsListFragment2 servingsListFragment = (ServingsListFragment2) fm.findFragmentByTag(AppConsts.Fragments.SERVINGS_LIST);
            servingsListFragment.adapter.refreshAdapter();

            if (getResources().getBoolean(R.bool.isTablet)) {
                switch (event.action) {
                    case BusConsts.ACTION_ADD_NEW:
                        onListSizeChanged(event.servingsMap.size());
                        mServing = event.servingsMap.get(mServing.servingId);
                        showRecipeDetails(mServing);
                        break;
                    case BusConsts.ACTION_EDIT:
                        mServing = event.servingsMap.get(mServing.servingId);
                        showRecipeDetails(mServing);
                        break;
                    case BusConsts.ACTION_DELETE:
                        onListSizeChanged(event.servingsMap.size());
                        listFragment.backToDefaultDisplay(false);

                        Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

                        if (recipeReviewFragment != null) {
                            // Tablet only

                            Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);

                            if (webViewFragment != null) {
                                fm.popBackStack();
                            }

                            FragmentTransaction ft = fm.beginTransaction();
                            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                            ft.remove(recipeReviewFragment);
                            ft.commit();

                            AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);
                        }

                        String toastMessage;

                        if (selectedItemsId.size() == 1) {
                            toastMessage = getString(R.string.the_serving_was_deleted);

                        } else {
                            toastMessage = selectedItemsId.size() + " " + getString(R.string.servings_were_deleted);
                        }

                        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

                        selectedItemsId.clear();
                        break;
                }
            }
        } else {
            View mainView = findViewById(android.R.id.content);
            AppHelper.onApiErrorReceived(event.t, mainView);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void onDeleteItemsClicked(boolean isToDeleteAll) {

        AppHelper.vibrate(this);

        final Context context = MealPlannerActivity.this;

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(context);

        final View view = inflater.inflate(R.layout.dialog_warning, null, false);

        dialog.setCancelable(false);

        dialog.setContentView(view);

        TextView textView_dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
        TextView textView_dialogContent = (TextView) dialog.findViewById(R.id.textView_dialogContent);

        String dialogTitle;

        if (isToDeleteAll) {
            dialogTitle = getString(R.string.deleting_all_servings);
        } else if (selectedItemsId.size() > 1) {
            dialogTitle = getString(R.string.deleting) + " " + selectedItemsId.size() + " " + getString(R.string.servings);
        } else {
            dialogTitle = getString(R.string.deleting) + " " + getString(R.string.a_serving);
        }

        textView_dialogTitle.setText(dialogTitle);
        textView_dialogContent.setText(getString(R.string.are_you_sure)
                + "\n"
                + getString(R.string.the_operation_is_irreversible));

        Button button_yes = (Button) dialog.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(v -> {

            ArrayList<Serving> servingsList = new ArrayList<>(appStateManager.user.servings.values());
            servingsList.stream()
                    .filter(serving -> !selectedItemsId.contains(serving.servingId))
                    .forEach(servingsList::remove);

            dbManager.updateUserServingsMap(servingsList, BusConsts.ACTION_DELETE);

//            listFragment.backToDefaultDisplay(false);
//
//            FragmentManager fm = getSupportFragmentManager();
//            Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
//
//            if (recipeReviewFragment != null) {
//                // Tablet only
//
//                Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);
//
//                if (webViewFragment != null) {
//                    fm.popBackStack();
//                }
//
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
//                ft.remove(recipeReviewFragment);
//                ft.commit();
//
//                AnimationHelper.animateViewFadingIn(context, layout_logo, 500, 500);
//            }
//
//            String toastMessage;
//
//            if (selectedItemsId.size() == 1) {
//                toastMessage = getString(R.string.the_serving_was_deleted);
//
//            } else {
//                toastMessage = selectedItemsId.size() + " " + getString(R.string.servings_were_deleted);
//            }
//
//            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
//
//            selectedItemsId.clear();

            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {

        FragmentManager fm = getSupportFragmentManager();

        ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

        if (toolbar_delete.isVisible()) {
            // The user renounced selecting servings to delete

            selectedItemsId.clear();

            Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);

            if (recipeReviewFragment != null) {
                // Tablet only

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                ft.remove(recipeReviewFragment);
                ft.commit();

                AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);
            }

            listFragment.backToDefaultDisplay(true);

            updateClearListButtonVisibility(listSize);

        } else {

            Fragment webViewFragment = fm.findFragmentByTag(AppConsts.Fragments.WEB_VIEW);

            if (webViewFragment != null) {
                // The user was watching the recipe URL

                fm.popBackStack();

                if (selectedItemsId.isEmpty()) {
                    // Always on phone
                    // The user was watching the recipe URL

                    updateClearListButtonVisibility(listSize);

                } else {
                    // Tablet only
                    // The user was watching the recipe URL while selecting mServing(s) to delete

                    toolbarButtonsList.add(AppConsts.ToolbarButtons.DELETE);
                    setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.NO_CHANGE, null);
                }

            } else {
                // The user was just watching a recipe, or no recipe was displayed

                Fragment recipeReviewFragment = fm.findFragmentByTag(AppConsts.Fragments.RECIPE_REVIEW);
                if (recipeReviewFragment == null) {
                    finish();

                } else {
                    // The user was just watching a recipe
                    if (getResources().getBoolean(R.bool.isTablet)) {

                        FragmentTransaction ft = fm.beginTransaction();
                        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                        ft.remove(recipeReviewFragment);
                        ft.commit();

                        AnimationHelper.animateViewFadingIn(this, layout_logo, 500, 500);

                    } else {
                        fm.popBackStack();
                    }
                }
            }
        }
    }

}
