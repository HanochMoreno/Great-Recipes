package com.hanoch.greatrecipes.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.model.Recipe;
import com.hanoch.greatrecipes.model.RecipeSearchResult;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.utilities.ImageStorage;

public class DbManager {

    private static final String TAG = "DbManager";

    private static DbManager dbManager;
    private Context context;

    public static DbManager getInstance(Context context) {

        if (dbManager == null) {
            dbManager = new DbManager(context);
        }

        return dbManager;
    }

//-------------------------------------------------------------------------------------------------

    private DbManager(Context context) {
        Log.d(TAG, "DbManager was just created");

        this.context = context;
    }

//-------------------------------------------------------------------------------------------------

    public Uri addNewRecipe(Recipe recipe) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        ContentValues values = new ContentValues();

        values.put(RecipesContract.Recipes.TITLE, recipe.getTitle());
        values.put(RecipesContract.Recipes.YIELD, recipe.getYield());
        values.put(RecipesContract.Recipes.SRC_URL, recipe.getUrl());
        values.put(RecipesContract.Recipes.ENERGY, recipe.getEnergy());
        values.put(RecipesContract.Recipes.RATING, recipe.getRating());
        values.put(RecipesContract.Recipes.AUTHOR, recipe.getAuthor());
        values.put(RecipesContract.Recipes.FAVOURITE_INDEX, recipe.getFavouriteIndex());
        values.put(RecipesContract.Recipes.NOTES, recipe.getNotes());
        values.put(RecipesContract.Recipes.TIME, recipe.getTime());
        values.put(RecipesContract.Recipes.CATEGORIES_LIST, recipe.getCategoriesList());
        values.put(RecipesContract.Recipes.INGREDIENTS_LIST, recipe.getIngredientsList());
        values.put(RecipesContract.Recipes.INSTRUCTIONS, recipe.getInstructions());
        values.put(RecipesContract.Recipes.ORIGIN_INDEX, recipe.getOriginIndex());

        return context.getContentResolver().insert(contentUri, values);
    }

//-------------------------------------------------------------------------------------------------

    public Uri addNewResult(RecipeSearchResult result) {

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        ContentValues values = new ContentValues();

        values.put(RecipesContract.SearchResults.YUMMLY_ID, result.getYummlyId());
        values.put(RecipesContract.SearchResults.TITLE, result.getTitle());
        values.put(RecipesContract.SearchResults.IMAGE_URL, result.getImageUrl());

        return context.getContentResolver().insert(contentUri, values);
    }

//-------------------------------------------------------------------------------------------------

    public int updateSearchResult(RecipeSearchResult result) {

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        String selection = RecipesContract.SearchResults._ID + "=?";
        String[] selectionArgs = {result.getRecipeId() + ""};

        ContentValues values = new ContentValues();

        values.put(RecipesContract.SearchResults.TITLE, result.getTitle());
        values.put(RecipesContract.SearchResults.YUMMLY_ID, result.getYummlyId());
        values.put(RecipesContract.SearchResults.YIELD, result.getYield());
        values.put(RecipesContract.SearchResults.SOURCE_URL, result.getSourceUrl());
        values.put(RecipesContract.SearchResults.IMAGE_URL, result.getImageUrl());
        values.put(RecipesContract.SearchResults.ENERGY, result.getEnergy());
        values.put(RecipesContract.SearchResults.AUTHOR, result.getAuthor());
        values.put(RecipesContract.SearchResults.TIME, result.getTime());
        values.put(RecipesContract.SearchResults.CATEGORIES_LIST, result.getCategories());
        values.put(RecipesContract.SearchResults.INGREDIENTS_LIST, result.getIngredients());

        return context.getContentResolver().update(contentUri, values, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public RecipeSearchResult queryResultObjectById(long id) {

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        // get all columns (projection = null)
        String[] projection = null;

        // get the row with id= id
        String selection = RecipesContract.SearchResults._ID + "=?";
        String[] selectionArgs = {id + ""};

        String sortOrder = RecipesContract.SearchResults._ID + " ASC";

        Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, sortOrder);

        cursor.moveToNext();

        String title = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.TITLE));
        String yummlyId = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.YUMMLY_ID));
        int yield = cursor.getInt(cursor.getColumnIndex(RecipesContract.SearchResults.YIELD));
        String sourceUrl = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.SOURCE_URL));
        String imageUrl = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.IMAGE_URL));
        int energy = cursor.getInt(cursor.getColumnIndex(RecipesContract.SearchResults.ENERGY));
        String author = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.AUTHOR));
        int time = cursor.getInt(cursor.getColumnIndex(RecipesContract.SearchResults.TIME));
        String categories = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.CATEGORIES_LIST));
        String ingredients = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.INGREDIENTS_LIST));

        cursor.close();

        return new RecipeSearchResult(id, yummlyId, title,
                author, yield, time, ingredients, energy, categories, sourceUrl, imageUrl);
    }

//-------------------------------------------------------------------------------------------------

    public long[] getSearchResultsIdsList() {

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        // get only "_id" column
        String[] projection = {RecipesContract.SearchResults._ID};

        // get all the rows
        String selection = null;
        String[] selectionArgs = null;

        String sortOrder = RecipesContract.SearchResults._ID + " ASC";

        Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, sortOrder);

        long[] idsList = new long[0];

        if (cursor != null) {

            int totalResultsCount = cursor.getCount();

            idsList = new long[totalResultsCount];

            int i = 0;
            while (cursor.moveToNext()) {
                idsList[i] = cursor.getLong(cursor.getColumnIndex(RecipesContract.SearchResults._ID));
                i = i + 1;
            }

            cursor.close();
        }

        return idsList;
    }

//-------------------------------------------------------------------------------------------------

    public long getRecipeIdFromUri(Uri uri) {

        String recipeId = uri.getLastPathSegment();
        long id = (long) Float.parseFloat(recipeId);

        return id;
    }

//-------------------------------------------------------------------------------------------------

    public int updateRecipe(Recipe recipe) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        String selection = RecipesContract.Recipes._ID + "=?";
        String[] selectionArgs = {recipe.getId() + ""};

        ContentValues values = new ContentValues();

        values.put(RecipesContract.Recipes.TITLE, recipe.getTitle());
        values.put(RecipesContract.Recipes.YIELD, recipe.getYield());
        values.put(RecipesContract.Recipes.SRC_URL, recipe.getUrl());
        values.put(RecipesContract.Recipes.ENERGY, recipe.getEnergy());
        values.put(RecipesContract.Recipes.RATING, recipe.getRating());
        values.put(RecipesContract.Recipes.AUTHOR, recipe.getAuthor());
        values.put(RecipesContract.Recipes.FAVOURITE_INDEX, recipe.getFavouriteIndex());
        values.put(RecipesContract.Recipes.NOTES, recipe.getNotes());
        values.put(RecipesContract.Recipes.TIME, recipe.getTime());
        values.put(RecipesContract.Recipes.CATEGORIES_LIST, recipe.getCategoriesList());
        values.put(RecipesContract.Recipes.INGREDIENTS_LIST, recipe.getIngredientsList());
        values.put(RecipesContract.Recipes.INSTRUCTIONS, recipe.getInstructions());
        values.put(RecipesContract.Recipes.ORIGIN_INDEX, recipe.getOriginIndex());

        return context.getContentResolver().update(contentUri, values, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public int deleteRecipeById(Context context, long id) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        String selection = RecipesContract.Recipes._ID + "=?";
        String[] selectionArgs = {id + ""};

        // Delete connected serving as well, if exists
        deleteServingByRecipeId(id);

        // Delete connected image and thumbnail as well, if exist
        ImageStorage.deleteImageByImageName(context, AppConsts.Images.RECIPE_IMAGE_PREFIX + id);
        ImageStorage.deleteImageByImageName(context, AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + id);

        return context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public int deleteAllRecipes() {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        String selection = null;
        String[] selectionArgs = null;

        // deleting all the images from the SD card:
        ImageStorage.deleteAllImages(context);

        // Delete connected servings as well, if exist
        deleteAllServings();

        return context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public int deleteAllSearchResults() {

        // Deleting old results images:
        long[] resultsIdsList = getSearchResultsIdsList();

        for (long aResultId : resultsIdsList) {

            ImageStorage.deleteImageByImageName(context, AppConsts.Images.RESULT_IMAGE_PREFIX + aResultId);
        }

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        String selection = null;
        String[] selectionArgs = null;

        return context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public Cursor queryRecipeCursorById(long id) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        // get all columns (projection = null)
        String[] projection = null;

        // get the row with id= id
        String selection = RecipesContract.Recipes._ID + "=?";
        String[] selectionArgs = {id + ""};

        String sortOrder = RecipesContract.Recipes._ID + " ASC";

        return context.getContentResolver().query(contentUri, projection, selection, selectionArgs, sortOrder);
    }

//-------------------------------------------------------------------------------------------------

    public Recipe queryRecipeObjectById(long id) {

        Cursor cursor = queryRecipeCursorById(id);
        cursor.moveToNext();

        String title = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.TITLE));
        int yield = cursor.getInt(cursor.getColumnIndex(RecipesContract.Recipes.YIELD));
        String srcUrl = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.SRC_URL));
        int calories = cursor.getInt(cursor.getColumnIndex(RecipesContract.Recipes.ENERGY));
        float rating = cursor.getFloat(cursor.getColumnIndex(RecipesContract.Recipes.RATING));
        String author = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.AUTHOR));
        int favouriteIndex = cursor.getInt(cursor.getColumnIndex(RecipesContract.Recipes.FAVOURITE_INDEX));
        String notes = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.NOTES));
        int imageSelectionIndex = cursor.getInt(cursor.getColumnIndex(RecipesContract.Recipes.TIME));
        String categoriesList = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.CATEGORIES_LIST));
        String ingredientsList = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.INGREDIENTS_LIST));
        String instructions = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.INSTRUCTIONS));
        int originIndex = cursor.getInt(cursor.getColumnIndex(RecipesContract.Recipes.ORIGIN_INDEX));

        cursor.close();

        return new Recipe(id, title, author, yield, srcUrl, calories, rating, favouriteIndex, notes,
                imageSelectionIndex, categoriesList, ingredientsList, instructions, originIndex);
    }

//-------------------------------------------------------------------------------------------------

    public Cursor queryRecipesByTitle(String title/*, int listTypeIndex*/) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        // get all columns (projection = null)
        String[] projection = null;

        // get the row with id= id
        /*String selection = RecipesContract.Recipes.TITLE + " LIKE ? AND " + RecipesContract.Recipes.LIST_TYPE_INDEX + "=?";
        String[] selectionArgs = {"%" + title + "%", listTypeIndex+""};*/

        String selection = RecipesContract.Recipes.TITLE + " LIKE ? ";
        String[] selectionArgs = {"%" + title + "%"};

        String sortOrder = RecipesContract.Recipes.TITLE + " COLLATE LOCALIZED ASC";

        return context.getContentResolver().query(contentUri, projection, selection, selectionArgs, sortOrder);
    }

//-------------------------------------------------------------------------------------------------

    public Uri addNewServing(Serving serving) {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        ContentValues values = new ContentValues();

        values.put(RecipesContract.MealPlanning.RECIPE_ID, serving.getRecipeId());
        values.put(RecipesContract.MealPlanning.SERVING_TYPE, serving.getServingType());

        return context.getContentResolver().insert(contentUri, values);
    }

//-------------------------------------------------------------------------------------------------

    public Cursor queryServingCursorById(long id) {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        // get all columns (projection = null)
        String[] projection = null;

        // get the row with id= id
        String selection = RecipesContract.MealPlanning._ID + "=?";
        String[] selectionArgs = {id + ""};

        String sortOrder = RecipesContract.MealPlanning._ID + " ASC";

        return context.getContentResolver().query(contentUri, projection, selection, selectionArgs, sortOrder);
    }

//-------------------------------------------------------------------------------------------------

    public Serving queryServingObjectById(long id) {

        Cursor cursor = queryServingCursorById(id);
        cursor.moveToNext();

        // recipeID is NOT id!!!
        long recipeId = cursor.getLong(cursor.getColumnIndex(RecipesContract.MealPlanning.RECIPE_ID));
        String servingType = cursor.getString(cursor.getColumnIndex(RecipesContract.MealPlanning.SERVING_TYPE));

        cursor.close();

        return new Serving(id, recipeId, servingType, false, false);
    }


//-------------------------------------------------------------------------------------------------

    public int deleteServingById(long id) {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        String selection = RecipesContract.MealPlanning._ID + "=?";
        String[] selectionArgs = {id + ""};

        return context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public int deleteServingByRecipeId(long recipeId) {

        // recipeID is NOT the serving id !!

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        // delete the row with recipeId= recipeId
        String selection = RecipesContract.MealPlanning.RECIPE_ID + "=?";
        String[] selectionArgs = {recipeId + ""};

        return context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public int deleteAllServings() {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        // delete the row with recipeId= recipeId
        String selection = null;
        String[] selectionArgs = null;

        return context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

}
