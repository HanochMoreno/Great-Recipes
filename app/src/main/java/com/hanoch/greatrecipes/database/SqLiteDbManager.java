package com.hanoch.greatrecipes.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.model.Recipe;
import com.hanoch.greatrecipes.model.RecipeSearchResult;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.ThinRecipeSearchResult;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.List;

public class SqLiteDbManager {

    private Context context;

//-------------------------------------------------------------------------------------------------

    public SqLiteDbManager(Context appContext) {
        // Should only be called once by the Application
        this.context = appContext;
    }

//-------------------------------------------------------------------------------------------------

    public Uri addNewRecipe(Recipe recipe) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        ContentValues values = new ContentValues();

        values.put(RecipesContract.Recipes.TITLE, recipe.title);
        values.put(RecipesContract.Recipes.YIELD, recipe.yield);
        values.put(RecipesContract.Recipes.SRC_URL, recipe.url);
        values.put(RecipesContract.Recipes.ENERGY, recipe.energy);
        values.put(RecipesContract.Recipes.RATING, recipe.rating);
        values.put(RecipesContract.Recipes.AUTHOR, recipe.author);
        values.put(RecipesContract.Recipes.FAVOURITE_INDEX, recipe.favouriteIndex);
        values.put(RecipesContract.Recipes.NOTES, recipe.notes);
        values.put(RecipesContract.Recipes.TIME, recipe.time);
        values.put(RecipesContract.Recipes.CATEGORIES_LIST, recipe.categoriesList);
        values.put(RecipesContract.Recipes.INGREDIENTS_LIST, recipe.ingredientsList);
        values.put(RecipesContract.Recipes.INSTRUCTIONS, recipe.instructions);
        values.put(RecipesContract.Recipes.ORIGIN_INDEX, recipe.originIndex);

        return context.getContentResolver().insert(contentUri, values);
    }

//-------------------------------------------------------------------------------------------------

    public void addSearchResults(List<ThinRecipeSearchResult> results) {

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        for (ThinRecipeSearchResult result : results) {
            ContentValues values = new ContentValues();

            values.put(RecipesContract.SearchResults.YUMMLY_ID, result.yummlyId);
            values.put(RecipesContract.SearchResults.TITLE, result.title);
            String imageUrl = result.imageUrl.get("90").replace("s90-c", "s360-c");
            values.put(RecipesContract.SearchResults.IMAGE_URL, imageUrl);

            context.getContentResolver().insert(contentUri, values);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void updateFullSearchResultInfo(RecipeSearchResult result) {

        Uri contentUri = RecipesContract.SearchResults.CONTENT_URI;

        String selection = RecipesContract.SearchResults._ID + "=?";
        String[] selectionArgs = {String.valueOf(result.recipeId)};

        ContentValues values = new ContentValues();

        values.put(RecipesContract.SearchResults.YIELD, result.yield);
        values.put(RecipesContract.SearchResults.SOURCE_URL, result.sourceUrl);
        values.put(RecipesContract.SearchResults.ENERGY, result.energy);
        values.put(RecipesContract.SearchResults.AUTHOR, result.author);
        values.put(RecipesContract.SearchResults.TIME, result.time);
        values.put(RecipesContract.SearchResults.CATEGORIES_LIST, result.categories);
        values.put(RecipesContract.SearchResults.INGREDIENTS_LIST, result.ingredients);

        context.getContentResolver().update(contentUri, values, selection, selectionArgs);
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

    public void updateRecipe(Recipe recipe) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        String selection = RecipesContract.Recipes._ID + "=?";
        String[] selectionArgs = {recipe.id + ""};

        ContentValues values = new ContentValues();

        values.put(RecipesContract.Recipes.TITLE, recipe.title);
        values.put(RecipesContract.Recipes.YIELD, recipe.yield);
        values.put(RecipesContract.Recipes.SRC_URL, recipe.url);
        values.put(RecipesContract.Recipes.ENERGY, recipe.energy);
        values.put(RecipesContract.Recipes.RATING, recipe.rating);
        values.put(RecipesContract.Recipes.AUTHOR, recipe.author);
        values.put(RecipesContract.Recipes.FAVOURITE_INDEX, recipe.favouriteIndex);
        values.put(RecipesContract.Recipes.NOTES, recipe.notes);
        values.put(RecipesContract.Recipes.TIME, recipe.time);
        values.put(RecipesContract.Recipes.CATEGORIES_LIST, recipe.categoriesList);
        values.put(RecipesContract.Recipes.INGREDIENTS_LIST, recipe.ingredientsList);
        values.put(RecipesContract.Recipes.INSTRUCTIONS, recipe.instructions);
        values.put(RecipesContract.Recipes.ORIGIN_INDEX, recipe.originIndex);

        context.getContentResolver().update(contentUri, values, selection, selectionArgs);
    }

//-------------------------------------------------------------------------------------------------

    public int deleteRecipeById(Context context, long id) {

        Uri contentUri = RecipesContract.Recipes.CONTENT_URI;

        String selection = RecipesContract.Recipes._ID + "=?";
        String[] selectionArgs = {id + ""};

        // Delete connected serving as well, if exists
        deleteServingByRecipeId(id);

        // Delete connected image and thumbnail as well, if exist
        ImageStorage.deleteImageByImageName(context, AppConsts.Images.RECIPE_IMAGE + id);
        ImageStorage.deleteImageByImageName(context, AppConsts.Images.RECIPE_THUMBNAIL + id);

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

    public void addNewServing(Serving serving) {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        ContentValues values = new ContentValues();

        values.put(RecipesContract.MealPlanning.RECIPE_ID, serving.recipeId);
        values.put(RecipesContract.MealPlanning.SERVING_TYPE, serving.servingType);

        context.getContentResolver().insert(contentUri, values);
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

    public void deleteServingById(long id) {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        String selection = RecipesContract.MealPlanning._ID + "=?";
        String[] selectionArgs = {id + ""};

        context.getContentResolver().delete(contentUri, selection, selectionArgs);
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

    public void deleteAllServings() {

        Uri contentUri = RecipesContract.MealPlanning.CONTENT_URI;

        // delete the row with recipeId= recipeId
        String selection = null;
        String[] selectionArgs = null;

        context.getContentResolver().delete(contentUri, selection, selectionArgs);
    }

}
