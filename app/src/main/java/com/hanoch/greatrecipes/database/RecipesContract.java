package com.hanoch.greatrecipes.database;

import android.net.Uri;

public class RecipesContract {

    public static final String AUTHORITY = "com.hanoch.recipesdbprovider.provider.recipes";
    public static final String _ID = "_id";

//-------------------------------------------------------------------------------------------------

    public static class Recipes {

        public static final String TABLE_NAME  = "recipes";
        public static final Uri CONTENT_URI  = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
        public static final String _ID  = RecipesContract._ID;
        public static final String TITLE = "title";
        public static final String YIELD = "yield";
        public static final String SRC_URL = "sourceUrl";
        public static final String ENERGY = "energy";
        public static final String RATING = "rating";
        public static final String AUTHOR = "Author";
        public static final String FAVOURITE_INDEX = "favouriteIndex";
        public static final String NOTES = "notes";
        public static final String TIME = "time";
        public static final String CATEGORIES_LIST = "categoriesList";
        public static final String INGREDIENTS_LIST = "ingredientsList";
        public static final String INSTRUCTIONS = "instructions";
        public static final String ORIGIN_INDEX = "originIndex";
    }

//-------------------------------------------------------------------------------------------------

    public static class SearchResults {

        public static final String TABLE_NAME  = "searchResults";
        public static final Uri CONTENT_URI  = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
        public static final String _ID  = RecipesContract._ID;
        public static final String YUMMLY_ID = "yummlyId";
        public static final String IMAGE_URL = "imageUrl";
        public static final String TITLE = "title";
        public static final String AUTHOR = "author";
        public static final String SOURCE_URL = "sourceUrl";
        public static final String YIELD = "yield";
        public static final String TIME = "time";
        public static final String INGREDIENTS_LIST = "ingredients";
        public static final String ENERGY = "energy";
        public static final String CATEGORIES_LIST = "categories";
    }

//-------------------------------------------------------------------------------------------------

    public static class MealPlanning {

        public static final String TABLE_NAME  = "mealPlanning";
        public static final Uri CONTENT_URI  = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
        public static final String _ID  = RecipesContract._ID;
        public static final String RECIPE_ID  = "recipeId";
        public static final String SERVING_TYPE = "servingType";
    }

}
