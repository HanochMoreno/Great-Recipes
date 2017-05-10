package com.hanoch.greatrecipes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanoch.greatrecipes.model.AllergenAndDiet;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;


public abstract class AppHelper {

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

    public static AllergenAndDiet[] getAllDietList(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        AllergenAndDiet[] dietList = new AllergenAndDiet[3];

        AllergenAndDiet diet = new AllergenAndDiet(
                2, AppConsts.Filters.VEGAN, "390^Vegan", sp.getBoolean(AppConsts.SharedPrefs.VEGAN, false));
        dietList[0] = diet;

        diet = new AllergenAndDiet(
                3, AppConsts.Filters.VEGETARIAN, "387^Lacto-ovo vegetarian", sp.getBoolean(AppConsts.SharedPrefs.VEGETARIAN, false));
        dietList[1] = diet;

        diet = new AllergenAndDiet(
                4, AppConsts.Filters.PALEO, "403^Paleo", sp.getBoolean(AppConsts.SharedPrefs.PALEO, false));
        dietList[2] = diet;

        return dietList;
    }

//-------------------------------------------------------------------------------------------------

    public static AllergenAndDiet[] getAllAllergiesList(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        AllergenAndDiet[] allergiesList = new AllergenAndDiet[9];

        AllergenAndDiet allergen = new AllergenAndDiet(
                6, AppConsts.Filters.DAIRY_FREE, "396^Dairy-Free", sp.getBoolean(AppConsts.SharedPrefs.DAIRY_FREE, false));
        allergiesList[0] = allergen;

        allergen = new AllergenAndDiet(
                7, AppConsts.Filters.EGG_FREE, "397^Egg-Free", sp.getBoolean(AppConsts.SharedPrefs.EGG_FREE, false));
        allergiesList[1] = allergen;

        allergen = new AllergenAndDiet(
                8, AppConsts.Filters.GLUTEN_FREE, "393^Gluten-Free", sp.getBoolean(AppConsts.SharedPrefs.GLUTEN_FREE, false));
        allergiesList[2] = allergen;

        allergen = new AllergenAndDiet(
                9, AppConsts.Filters.PEANUT_FREE, "394^Peanut-Free", sp.getBoolean(AppConsts.SharedPrefs.PEANUT_FREE, false));
        allergiesList[3] = allergen;

        allergen = new AllergenAndDiet(
                10, AppConsts.Filters.SEAFOOD_FREE, "398^Seafood-Free", sp.getBoolean(AppConsts.SharedPrefs.SEAFOOD_FREE, false));
        allergiesList[4] = allergen;

        allergen = new AllergenAndDiet(
                11, AppConsts.Filters.SESAME_FREE, "399^Sesame-Free", sp.getBoolean(AppConsts.SharedPrefs.SESAME_FREE, false));
        allergiesList[5] = allergen;

        allergen = new AllergenAndDiet(
                12, AppConsts.Filters.SOY_FREE, "400^Soy-Free", sp.getBoolean(AppConsts.SharedPrefs.SOY_FREE, false));
        allergiesList[6] = allergen;

        allergen = new AllergenAndDiet(
                13, AppConsts.Filters.TREE_NUT_FREE, "395^Tree Nut-Free", sp.getBoolean(AppConsts.SharedPrefs.TREE_NUT_FREE, false));
        allergiesList[7] = allergen;

        allergen = new AllergenAndDiet(
                14, AppConsts.Filters.WHEAT_FREE, "392^Wheat-Free", sp.getBoolean(AppConsts.SharedPrefs.WHEAT_FREE, false));
        allergiesList[8] = allergen;

        return allergiesList;
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<AllergenAndDiet> getUserAllowedDietPrefsList(Context context) {

        ArrayList<AllergenAndDiet> allowedDietList = new ArrayList<>();

        AllergenAndDiet[] dietList = getAllDietList(context);

        for (int i = 0; i < dietList.length; i++) {

            if (dietList[i].isChosen) {
                allowedDietList.add(dietList[i]);
            }
        }

        return allowedDietList;
    }

//-------------------------------------------------------------------------------------------------

    public static ArrayList<AllergenAndDiet> getUserAllowedAllergiesPrefsList(Context context) {

        ArrayList<AllergenAndDiet> allowedAllergiesList = new ArrayList<>();

        AllergenAndDiet[] allergiesList = getAllAllergiesList(context);

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

//-------------------------------------------------------------------------------------------------

    public static void showSnackBar(View view, int stringResId, int color) {

        Snackbar snack = Snackbar.make(view, stringResId, Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(color);
        snack.show();
    }

    public static void onApiErrorReceived(Throwable t, View mainView) {
        Log.e("subscriber", "onError: message: " + t.getMessage() + ", cause: " + t.getCause());
        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            showSnackBar(mainView, R.string.internet_error, Color.RED);
        } else {
            showSnackBar(mainView, R.string.unexpected_error, Color.RED);
        }
    }
}
