package com.hanoch.greatrecipes.api;

/**
 * A recipe downloaded from Yummly, as received from GreatRecipesApi or generated from YummlyRecipeResponse2.
 */
public class YummlyRecipe extends GenericRecipe {

    public String yummlyId;
    public String url;
    public String imageUrl;
    public int energy;
}
