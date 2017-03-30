package com.hanoch.greatrecipes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class RecipesDbOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "RecipesList.db";
    private static final int DB_VERSION = 1;

    public RecipesDbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating an SQL database file.
        // happens only at the first time the application is opened.
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

        // creating an SQL database file.
        // happens only at the first time the application is opened.
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

        // in our case we will just remove the old db table and create a new one...

        String sql;

        sql = "DROP TABLE IF EXISTS " + RecipesContract.Recipes.TABLE_NAME;
        db.execSQL(sql);

        sql = "DROP TABLE IF EXISTS " + RecipesContract.SearchResults.TABLE_NAME;
        db.execSQL(sql);

        sql = "DROP TABLE IF EXISTS " + RecipesContract.MealPlanning.TABLE_NAME;
        db.execSQL(sql);

        onCreate(db);
    }
}
