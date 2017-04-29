package com.hanoch.greatrecipes.api;


import java.util.ArrayList;

/**
 * A recipe downloaded from Yummly, as received from GreatRecipesApi or generated from YummlyRecipeResponse2.
 */

public class YummlyRecipe {

    public String _id;
    public String yummlyId;
    public String recipeTitle;
    public String author;
    public int yield;
    public String url;
    public String imageUrl;
    public String thumbnailByteArrayAsString;
    public String imageByteArrayAsString;
    public int energy;
    public int cookingTime;
    public ArrayList<String> ingredientsList;
    public ArrayList<String> categoriesList;
}
