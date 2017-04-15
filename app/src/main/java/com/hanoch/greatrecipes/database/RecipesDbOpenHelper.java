package com.hanoch.greatrecipes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hanoch.greatrecipes.AppHelper;

import java.util.ArrayList;
import java.util.Collections;


public class RecipesDbOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "RecipesDbOpenHelper";
    private static final String DB_NAME = "RecipesList.db";
    private static final int DB_VERSION = 2;

    public RecipesDbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating an SQL database file.
        // Executed only at the first time the application is opened.
        String sql = "CREATE TABLE " + RecipesContract.Recipes.TABLE_NAME + "(" +
                RecipesContract.Recipes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipesContract.Recipes.TITLE + " TEXT," +
                RecipesContract.Recipes.YIELD + " INTEGER," +
                RecipesContract.Recipes.SRC_URL + " TEXT," +
                RecipesContract.Recipes.ENERGY + " TEXT," +
                RecipesContract.Recipes.RATING + " INTEGER," +
                RecipesContract.Recipes.AUTHOR + " TEXT," +
                RecipesContract.Recipes.FAVOURITE_INDEX + " INTEGER," +
                RecipesContract.Recipes.NOTES + " TEXT," +
                RecipesContract.Recipes.TIME + "  INTEGER," +
                RecipesContract.Recipes.CATEGORIES_LIST + " TEXT," +
                RecipesContract.Recipes.INGREDIENTS_LIST + " TEXT," +
                RecipesContract.Recipes.INSTRUCTIONS + " TEXT," +
                RecipesContract.Recipes.ORIGIN_INDEX + " INTEGER" +
                ");";

        db.execSQL(sql);

        sql = "CREATE TABLE " + RecipesContract.SearchResults.TABLE_NAME + "(" +
                RecipesContract.SearchResults._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipesContract.SearchResults.TITLE + " TEXT," +
                RecipesContract.SearchResults.YUMMLY_ID + " TEXT," +
                RecipesContract.SearchResults.AUTHOR + " TEXT," +
                RecipesContract.SearchResults.SOURCE_URL + " TEXT," +
                RecipesContract.SearchResults.YIELD + " INTEGER," +
                RecipesContract.SearchResults.TIME + " INTEGER," +
                RecipesContract.SearchResults.INGREDIENTS_LIST + " TEXT," +
                RecipesContract.SearchResults.ENERGY + " INTEGER," +
                RecipesContract.SearchResults.CATEGORIES_LIST + " TEXT," +
                RecipesContract.SearchResults.IMAGE_URL + " TEXT" +
                ");";

        db.execSQL(sql);

        sql = "CREATE TABLE " + RecipesContract.MealPlanning.TABLE_NAME + "(" +
                RecipesContract.MealPlanning._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipesContract.MealPlanning.RECIPE_ID + "  INTEGER," +
                RecipesContract.MealPlanning.SERVING_TYPE + " TEXT" +
                ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // defining what to do when we want to update the database
        // for example, split the column "name" into 2 columns: "first name" and "last name"

        if (oldVersion == 1 && newVersion == 2) {
            String[] resultsColumns = {RecipesContract._ID, RecipesContract.SearchResults.CATEGORIES_LIST, RecipesContract.SearchResults.INGREDIENTS_LIST};
            updateCategoriesAndIngredientsList(db, RecipesContract.SearchResults.TABLE_NAME, resultsColumns);
            String[] recipesColumns = {RecipesContract._ID, RecipesContract.Recipes.CATEGORIES_LIST, RecipesContract.Recipes.INGREDIENTS_LIST};
            updateCategoriesAndIngredientsList(db, RecipesContract.Recipes.TABLE_NAME, recipesColumns);
        }
    }

    private void updateCategoriesAndIngredientsList(SQLiteDatabase db, String tableName, String[] columns) {
        String sortOrder = RecipesContract._ID + " ASC";

        Cursor cursor = db.query(tableName, columns, null, null, null, null, sortOrder);
        if (cursor != null) {
            final String LIST_DIVIDER = "&&";

            long id;
            String categories;
            String ingredients;
            ArrayList<String> resultList = new ArrayList<>();
            String[] splitString;

            while (cursor.moveToNext()) {
                id = cursor.getLong(cursor.getColumnIndex(columns[0]));
                categories = cursor.getString(cursor.getColumnIndex(columns[1]));
                ingredients = cursor.getString(cursor.getColumnIndex(columns[2]));

                resultList.clear();
                if (categories != null) {
                    splitString = (categories.split(LIST_DIVIDER));
                    Collections.addAll(resultList, splitString);
                }
                categories = AppHelper.convertListToString(resultList);

                resultList.clear();
                if (ingredients != null) {
                    splitString = (ingredients.split(LIST_DIVIDER));
                    Collections.addAll(resultList, splitString);
                }
                ingredients = AppHelper.convertListToString(resultList);

                ContentValues values = new ContentValues();
                values.put(columns[1], categories);
                values.put(columns[2], ingredients);

                db.update(tableName, values, RecipesContract._ID + "=" + id, null);
            }

            cursor.close();
        }
    }
}
