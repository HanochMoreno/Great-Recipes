package com.hanoch.greatrecipes.api.great_recipes_api;


import java.util.ArrayList;

/**
 * A recipe created by a user, as received from GreatRecipesApi.
 */

public class UserRecipe {

    public String _id;
    public String recipeTitle;
    public String author;
    public int yield;
    public String thumbnailByteArrayAsString;
    public String imageByteArrayAsString;
    public String instructions;
    public String notes;
    public int cookingTime;
    public ArrayList<String> ingredientsList;
    public ArrayList<String> categoriesList;
}
