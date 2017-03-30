package com.hanoch.greatrecipes.model;

/**
 Represents one result of a recipe online search.
 */

public class RecipeSearchResult {

    public long recipeId;
    public String yummlyId;
    public String title;

    public String author;
    public int yield;
    public int time;
    public String ingredients;
    public int energy;
    public String categories;

    public String sourceUrl;
    public String imageUrl;


//-------------------------------------------------------------------------------------------------

    // CTOR for creating a search result card
    public RecipeSearchResult(long recipeId, String yummlyId, String title, String imageUrl) {
        this.recipeId = recipeId;
        this.yummlyId = yummlyId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

//-------------------------------------------------------------------------------------------------

    public RecipeSearchResult(long recipeId, String yummlyId, String title, String author,
                              int yield, int time, String ingredients, int energy,
                              String categories, String sourceUrl, String imageUrl) {

        this.recipeId = recipeId;
        this.yummlyId = yummlyId;
        this.title = title;
        this.author = author;
        this.yield = yield;
        this.time = time;
        this.ingredients = ingredients;
        this.energy = energy;
        this.categories = categories;
        this.sourceUrl = sourceUrl;
        this.imageUrl = imageUrl;
    }

}
