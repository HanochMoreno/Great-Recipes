package com.hanoch.greatrecipes.api;


import java.util.ArrayList;
import java.util.Comparator;

/**
 * A recipe downloaded from Yummly, as received from GreatRecipesApi or generated from YummlyRecipeResponse2.
 */

public class GGGRecipe2 {

    public String _id;
    public String recipeTitle;
    public String author;
    public int yield;
    public String thumbnailByteArrayAsString;
    public String imageByteArrayAsString;
    public int cookingTime;
    public ArrayList<String> ingredientsList;
    public ArrayList<String> categoriesList;

    public static Comparator<GGGRecipe2> TITLE_COMPARATOR =
            (recipe, recipe2) -> recipe.recipeTitle.compareToIgnoreCase(recipe2.recipeTitle);
}
