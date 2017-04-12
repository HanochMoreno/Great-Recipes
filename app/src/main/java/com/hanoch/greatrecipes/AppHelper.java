package com.hanoch.greatrecipes;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanoch.greatrecipes.model.AllergensAndDietPrefItem;
import com.hanoch.greatrecipes.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class AppHelper {

    private static final String TAG = "App Helper";

    public static void setSelectedRecipe(View view, Context context) {

        FrameLayout selectedItemFrame = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        selectedItemFrame.setPadding(5, 3, 5, 3);

        TextView textView = (TextView) view.findViewById(R.id.textView_itemTitle);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

//-------------------------------------------------------------------------------------------------

    public static void setUnselectedRecipe(View view) {

        FrameLayout selectedItemFrame = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        selectedItemFrame.setPadding(0, 0, 0, 0);

        TextView textView = (TextView) view.findViewById(R.id.textView_itemTitle);
        textView.setTextColor(Color.WHITE);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateCheckedRecipe(View view, Context context) {

        FrameLayout selectedItemImageFrame = (FrameLayout) view.findViewById(R.id.layout_image);
        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_itemImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        checkedIcon.setVisibility(View.VISIBLE);

        animateViewFadingIn(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, selectedItemImageFrame, 1500, 0);

        animateViewDimming(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUncheckedRecipe(View view, Context context) {

        FrameLayout selectedItemImageFrame = (FrameLayout) view.findViewById(R.id.layout_image);
        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_itemImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        animateViewFadingOut(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, selectedItemImageFrame, 1500, 0);

        animateViewBrightening(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateCheckedCategory(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_categoryImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        checkedIcon.setVisibility(View.VISIBLE);

        animateViewFadingIn(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, checkedIcon, 1500, 0);

        animateViewDimming(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUncheckedCategory(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_categoryImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        animateViewFadingOut(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, checkedIcon, 1500, 0);

        animateViewBrightening(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateCheckedServing(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_servingImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        checkedIcon.setVisibility(View.VISIBLE);

        animateViewFlipping(context, checkedIcon, 1500, 0);
        animateViewFadingIn(context, checkedIcon, 1500, 0);

        animateViewDimming(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUncheckedServing(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_servingImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        animateViewFlipping(context, checkedIcon, 1500, 0);
        animateViewFadingOut(context, checkedIcon, 1500, 0);

        animateViewBrightening(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewFlipping(Context context, View view, int duration, long startDelay) {

        ObjectAnimator flipAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.flipping);
        flipAnim.setTarget(view);
        flipAnim.setStartDelay(startDelay);
        flipAnim.setDuration(duration);
        flipAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewFadingIn(Context context, final View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.fade_in);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
                view.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewFadingOut(Context context, final View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.fade_out);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setEnabled(false);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static ObjectAnimator animateToolbarButtonFadingIn(final MenuItem button, int duration, long startDelay) {

        final Drawable icon = button.getIcon();

        ObjectAnimator alphaAnim = ObjectAnimator.ofPropertyValuesHolder(icon, PropertyValuesHolder.ofInt("alpha", 0, 255));
        alphaAnim.setDuration(duration);
        alphaAnim.setStartDelay(startDelay);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                button.setVisible(true);
                button.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                button.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                icon.setAlpha(0);
                button.setVisible(false);
                button.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();

        return alphaAnim;
    }

//-------------------------------------------------------------------------------------------------

    public static ObjectAnimator animateToolbarButtonFadingOut(final MenuItem button, int duration, long startDelay) {

        final Drawable icon = button.getIcon();

        ObjectAnimator alphaAnim = ObjectAnimator.ofPropertyValuesHolder(icon, PropertyValuesHolder.ofInt("alpha", 255, 0));
        alphaAnim.setDuration(duration);
        alphaAnim.setStartDelay(startDelay);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                button.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //button.setEnabled(false);
                button.setVisible(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                button.setVisible(false);
                icon.setAlpha(0);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();

        return alphaAnim;
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewDimming(Context context, View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.dimming);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);
        alphaAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewBrightening(Context context, View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.brightening);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);
        alphaAnim.start();
    }


//-------------------------------------------------------------------------------------------------

    public static void hideTheKeyboard(Activity activity) {
        // Hiding the soft keyboard after pressing the search button
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

//-------------------------------------------------------------------------------------------------

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

//-------------------------------------------------------------------------------------------------

    public static String convertListToString(ArrayList<String> list) {
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(AppConsts.Category.NO_INFO);
        }
        return new Gson().toJson(list);
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<String> convertStringToList(String listAsString) {
        ArrayList<String> list = new Gson().fromJson(listAsString, new TypeToken<ArrayList<String>>() {
        }.getType());

        if (list == null) {
            list = new ArrayList<>();
        }

        return list;
    }

//-------------------------------------------------------------------------------------------------

    public static String getStringRecipeTotalTime(Context context, int totalTime) {

        int hours = totalTime / 3600;
        int minutes = (totalTime % 3600) / 60;

        String stringTime = context.getString(R.string.no_info); // in case total time is <= 0
        if (hours > 0) {
            stringTime = hours + " " + context.getString(R.string.hr_);
            if (minutes > 0) {
                stringTime = stringTime + " " + minutes + " " + context.getString(R.string.min_);
            }

        } else {
            // only minutes
            if (minutes > 0) {
                stringTime = minutes + " " + context.getString(R.string.minutes);
            }
        }

        return stringTime;
    }

//-------------------------------------------------------------------------------------------------

    public static String getStringRecipeServings(Context context, int servings) {

        String stringServing;

        if (servings == 0) {
            stringServing = context.getString(R.string.no_info);
        } else if (servings == 1) {
            stringServing = servings + " " + context.getString(R.string.serving);
        } else {
            stringServing = servings + " " + context.getString(R.string.servings);
        }

        return stringServing;
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<Category> getDefaultCategoriesList() {

        ArrayList<Category> categories = new ArrayList<>();

        Category category = new Category(AppConsts.Category.BREADS, R.drawable.cat_breads, 0, false);
        categories.add(category);

        category = new Category(AppConsts.Category.DESSERTS, R.drawable.cat_desserts, 1, false);
        categories.add(category);

        category = new Category(AppConsts.Category.SIDE_DISHES, R.drawable.cat_side_dishes, 2, false);
        categories.add(category);

        category = new Category(AppConsts.Category.LUNCH_AND_SNACKS, R.drawable.cat_lunch_and_snacks, 3, false);
        categories.add(category);

        category = new Category(AppConsts.Category.APPETIZERS, R.drawable.cat_appetizers, 4, false);
        categories.add(category);

        category = new Category(AppConsts.Category.SALADS, R.drawable.cat_salads, 5, false);
        categories.add(category);

        category = new Category(AppConsts.Category.MAIN_DISHES, R.drawable.cat_main_dishes, 6, false);
        categories.add(category);

        category = new Category(AppConsts.Category.BREAKFAST_AND_BRUNCH, R.drawable.cat_breakfast_and_brunch, 7, false);
        categories.add(category);

        category = new Category(AppConsts.Category.SOUPS, R.drawable.cat_soups, 8, false);
        categories.add(category);

        category = new Category(AppConsts.Category.BEVERAGES, R.drawable.cat_beverages, 9, false);
        categories.add(category);

        category = new Category(AppConsts.Category.CONDIMENTS_AND_SAUCES, R.drawable.cat_condiments_and_sauces, 10, false);
        categories.add(category);

        category = new Category(AppConsts.Category.COCKTAILS, R.drawable.cat_cocktails, 11, false);
        categories.add(category);

        return categories;
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<String> getTranslatedCategoriesList(Context context, ArrayList<String> categoriesList) {

        ArrayList<String> translatedCategoriesList = new ArrayList<>();

        HashMap<String, String> categoriesTranslationMap = new HashMap<>();

        categoriesTranslationMap.put(AppConsts.Category.NO_INFO, context.getString(R.string.no_info));

        categoriesTranslationMap.put(AppConsts.Category.MAIN_DISHES, context.getString(R.string.main_dishes));
        categoriesTranslationMap.put(AppConsts.Category.DESSERTS, context.getString(R.string.desserts));
        categoriesTranslationMap.put(AppConsts.Category.SIDE_DISHES, context.getString(R.string.side_dishes));
        categoriesTranslationMap.put(AppConsts.Category.LUNCH_AND_SNACKS, context.getString(R.string.lunch_and_snacks));
        categoriesTranslationMap.put(AppConsts.Category.APPETIZERS, context.getString(R.string.appetizers));
        categoriesTranslationMap.put(AppConsts.Category.SALADS, context.getString(R.string.salads));
        categoriesTranslationMap.put(AppConsts.Category.BREADS, context.getString(R.string.breads));
        categoriesTranslationMap.put(AppConsts.Category.BREAKFAST_AND_BRUNCH, context.getString(R.string.breakfast_and_brunch));
        categoriesTranslationMap.put(AppConsts.Category.SOUPS, context.getString(R.string.soups));
        categoriesTranslationMap.put(AppConsts.Category.BEVERAGES, context.getString(R.string.beverages));
        categoriesTranslationMap.put(AppConsts.Category.CONDIMENTS_AND_SAUCES, context.getString(R.string.condiments_and_sauces));
        categoriesTranslationMap.put(AppConsts.Category.COCKTAILS, context.getString(R.string.cocktails));

        for (String category : categoriesList) {
            if (categoriesTranslationMap.containsKey(category)) {
                translatedCategoriesList.add(categoriesTranslationMap.get(category));
            }
        }

        return translatedCategoriesList;
    }

//-------------------------------------------------------------------------------------------------

    public static String getTranslatedCategoryName(Context context, String categoryName) {

        String translatedCategoryName;

        HashMap<String, String> categoriesTranslationMap = new HashMap<>();

        categoriesTranslationMap.put(AppConsts.Category.NO_INFO, context.getString(R.string.no_info));

        categoriesTranslationMap.put(AppConsts.Category.MAIN_DISHES, context.getString(R.string.main_dishes));
        categoriesTranslationMap.put(AppConsts.Category.DESSERTS, context.getString(R.string.desserts));
        categoriesTranslationMap.put(AppConsts.Category.SIDE_DISHES, context.getString(R.string.side_dishes));
        categoriesTranslationMap.put(AppConsts.Category.LUNCH_AND_SNACKS, context.getString(R.string.lunch_and_snacks));
        categoriesTranslationMap.put(AppConsts.Category.APPETIZERS, context.getString(R.string.appetizers));
        categoriesTranslationMap.put(AppConsts.Category.SALADS, context.getString(R.string.salads));
        categoriesTranslationMap.put(AppConsts.Category.BREADS, context.getString(R.string.breads));
        categoriesTranslationMap.put(AppConsts.Category.BREAKFAST_AND_BRUNCH, context.getString(R.string.breakfast_and_brunch));
        categoriesTranslationMap.put(AppConsts.Category.SOUPS, context.getString(R.string.soups));
        categoriesTranslationMap.put(AppConsts.Category.BEVERAGES, context.getString(R.string.beverages));
        categoriesTranslationMap.put(AppConsts.Category.CONDIMENTS_AND_SAUCES, context.getString(R.string.condiments_and_sauces));
        categoriesTranslationMap.put(AppConsts.Category.COCKTAILS, context.getString(R.string.cocktails));

        translatedCategoryName = (categoriesTranslationMap.get(categoryName));

        return translatedCategoryName;
    }

//-------------------------------------------------------------------------------------------------

    public static String getTranslatedServingTypeName(Context context, String servingTypeName) {

        String translatedServingTypeName;

        HashMap<String, String> servingTypesTranslationMap = new HashMap<>();

        servingTypesTranslationMap.put(AppConsts.Serving.MAIN_DISH, context.getString(R.string.main_dish));
        servingTypesTranslationMap.put(AppConsts.Serving.DESSERT, context.getString(R.string.dessert));
        servingTypesTranslationMap.put(AppConsts.Serving.SIDE_DISH, context.getString(R.string.side_dish));
        servingTypesTranslationMap.put(AppConsts.Serving.APPETIZER, context.getString(R.string.appetizer));
        servingTypesTranslationMap.put(AppConsts.Serving.SALAD, context.getString(R.string.salad));
        servingTypesTranslationMap.put(AppConsts.Serving.PASTRY, context.getString(R.string.pastry));
        servingTypesTranslationMap.put(AppConsts.Serving.BEVERAGE, context.getString(R.string.beverage));
        servingTypesTranslationMap.put(AppConsts.Serving.SAUCE, context.getString(R.string.sauce));

        translatedServingTypeName = (servingTypesTranslationMap.get(servingTypeName));

        return translatedServingTypeName;
    }

//-------------------------------------------------------------------------------------------------

    public static AllergensAndDietPrefItem[] getAllDietList(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        AllergensAndDietPrefItem[] dietList = new AllergensAndDietPrefItem[3];

        AllergensAndDietPrefItem diet = new AllergensAndDietPrefItem(
                2, AppConsts.Filters.VEGAN, "390^Vegan", sp.getBoolean(AppConsts.SharedPrefs.VEGAN, false));
        dietList[0] = diet;

        diet = new AllergensAndDietPrefItem(
                3, AppConsts.Filters.VEGETARIAN, "387^Lacto-ovo vegetarian", sp.getBoolean(AppConsts.SharedPrefs.VEGETARIAN, false));
        dietList[1] = diet;

        diet = new AllergensAndDietPrefItem(
                4, AppConsts.Filters.PALEO, "403^Paleo", sp.getBoolean(AppConsts.SharedPrefs.PALEO, false));
        dietList[2] = diet;

        return dietList;
    }

//-------------------------------------------------------------------------------------------------

    public static AllergensAndDietPrefItem[] getAllAllergiesList(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        AllergensAndDietPrefItem[] allergiesList = new AllergensAndDietPrefItem[9];

        AllergensAndDietPrefItem allergen = new AllergensAndDietPrefItem(
                6, AppConsts.Filters.DAIRY_FREE, "396^Dairy-Free", sp.getBoolean(AppConsts.SharedPrefs.DAIRY_FREE, false));
        allergiesList[0] = allergen;

        allergen = new AllergensAndDietPrefItem(
                7, AppConsts.Filters.EGG_FREE, "397^Egg-Free", sp.getBoolean(AppConsts.SharedPrefs.EGG_FREE, false));
        allergiesList[1] = allergen;

        allergen = new AllergensAndDietPrefItem(
                8, AppConsts.Filters.GLUTEN_FREE, "393^Gluten-Free", sp.getBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, false));
        allergiesList[2] = allergen;

        allergen = new AllergensAndDietPrefItem(
                9, AppConsts.Filters.PEANUT_FREE, "394^Peanut-Free", sp.getBoolean(AppConsts.SharedPrefs.PEANUT_FREE, false));
        allergiesList[3] = allergen;

        allergen = new AllergensAndDietPrefItem(
                10, AppConsts.Filters.SEAFOOD_FREE, "398^Seafood-Free", sp.getBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, false));
        allergiesList[4] = allergen;

        allergen = new AllergensAndDietPrefItem(
                11, AppConsts.Filters.SESAME_FREE, "399^Sesame-Free", sp.getBoolean(AppConsts.SharedPrefs.SESAME_FREE, false));
        allergiesList[5] = allergen;

        allergen = new AllergensAndDietPrefItem(
                12, AppConsts.Filters.SOY_FREE, "400^Soy-Free", sp.getBoolean(AppConsts.SharedPrefs.SOY_FREE, false));
        allergiesList[6] = allergen;

        allergen = new AllergensAndDietPrefItem(
                13, AppConsts.Filters.TREE_NUT_FREE, "395^Tree Nut-Free", sp.getBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, false));
        allergiesList[7] = allergen;

        allergen = new AllergensAndDietPrefItem(
                14, AppConsts.Filters.WHEAT_FREE, "392^Wheat-Free", sp.getBoolean(AppConsts.SharedPrefs.WHEAT_FREE, false));
        allergiesList[8] = allergen;

        return allergiesList;
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<AllergensAndDietPrefItem> getUserAllowedDietPrefsList(Context context) {

        ArrayList<AllergensAndDietPrefItem> allowedDietList = new ArrayList<>();

        AllergensAndDietPrefItem[] dietList = getAllDietList(context);

        for (int i = 0; i < dietList.length; i++) {

            if (dietList[i].isChosen) {
                allowedDietList.add(dietList[i]);
            }
        }

        return allowedDietList;
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<AllergensAndDietPrefItem> getUserAllowedAllergiesPrefsList(Context context) {

        ArrayList<AllergensAndDietPrefItem> allowedAllergiesList = new ArrayList<>();

        AllergensAndDietPrefItem[] allergiesList = getAllAllergiesList(context);

        for (int i = 0; i < allergiesList.length; i++) {

            if (allergiesList[i].isChosen) {

                allowedAllergiesList.add(allergiesList[i]);
            }
        }

        return allowedAllergiesList;
    }

//-------------------------------------------------------------------------------------------------

    public static void vibrate(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean vibrationPref = sp.getBoolean(AppConsts.SharedPrefs.VIBRATION, true);

        if (vibrationPref) {
            // Vibration for 500 milliseconds
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

}
