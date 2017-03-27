package com.hanoch.greatrecipes.model;

/**
 Represents one result of a recipe online search.
 */

public class RecipeSearchResult {

    private long recipeId;
    private String yummlyId;
    private String title;

    private String author;
    private int yield;
    private int time;
    private String ingredients;
    private int energy;
    private String categories;

    private String sourceUrl;
    private String imageUrl;


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

//-------------------------------------------------------------------------------------------------

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getYummlyId() {
        return yummlyId;
    }

    public void setYummlyId(String yummlyId) {
        this.yummlyId = yummlyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYield() {
        return yield;
    }

    public void setYield(int yield) {
        this.yield = yield;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
