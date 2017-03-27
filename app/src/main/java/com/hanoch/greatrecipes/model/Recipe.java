package com.hanoch.greatrecipes.model;

import com.hanoch.greatrecipes.AppConsts;

public class Recipe{

    private long id;
    private String title;
    private String author;
    private int yield;
    private String url;
    private int energy;
    private float rating;

    /**
     * Indicates the recipe favourite index. <br/>
     * <br> possible values: <br/>
     * 0= Not favourite <br/>
     * 1= favourite <br/>
     */
    private int favouriteIndex;

    /**
     * Private notes the user added.
     */
    private String notes;

    private int time;
    private String categoriesList;
    private String ingredientsList;
    private String instructions;

    /**
     * Indicates the origin of the recipe. <br/>
     * <br> possible values: <br/>
     * 0= added from the online search results <br/>
     * 1= added manually
     */
    private int originIndex;

    //CTOR for recipe came from online search
    public Recipe(String title, String author, int yield, String url, String categoriesList,
                    String ingredientsList, int time, int energy) {

        this.title = title;
        this.author = author;
        this.yield = yield;
        this.url = url;
        this.energy = energy;
        this.rating = AppConsts.NO_RATING;
        this.favouriteIndex = AppConsts.FavouriteIndex.NOT_FAVOURITE;
        this.time = time;
        this.categoriesList = categoriesList;
        this.ingredientsList = ingredientsList;
        this.originIndex = AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH;
    }

    //CTOR for recipe written by the user
    public Recipe(long id, String title, String author, int yield, float rating,
                  int favouriteIndex, String notes, int time,
                  String categoriesList, String ingredientsList, String instructions) {

        this.id = id;
        this.title = title;
        this.author = author;
        this.yield = yield;
        this.url = "";
        this.energy = 0;
        this.rating = rating;
        this.favouriteIndex = favouriteIndex;
        this.notes = notes;
        this.time = time;
        this.categoriesList = categoriesList;
        this.ingredientsList = ingredientsList;
        this.instructions = instructions;
        this.originIndex = AppConsts.RecipeOrigin.ADDED_MANUALLY;
    }

    //CTOR for recipe query by the provider
    public Recipe(long id, String title, String author, int yield, String url, int energy,
                  float rating, int favouriteIndex, String notes, int time,
                  String categoriesList, String ingredientsList, String instructions, int originIndex) {

        this.id = id;
        this.title = title;
        this.author = author;
        this.yield = yield;
        this.url = url;
        this.energy = energy;
        this.rating = rating;
        this.favouriteIndex = favouriteIndex;
        this.notes = notes;
        this.time = time;
        this.categoriesList = categoriesList;
        this.ingredientsList = ingredientsList;
        this.instructions = instructions;
        this.originIndex = originIndex;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getEnergy() {
        return energy;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getFavouriteIndex() {
        return favouriteIndex;
    }

    public void setFavouriteIndex(int favouriteIndex) {
        this.favouriteIndex = favouriteIndex;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(String categoriesList) {
        this.categoriesList = categoriesList;
    }

    public String getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(String ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getOriginIndex() {
        return originIndex;
    }

    public void setOriginIndex(int originIndex) {
        this.originIndex = originIndex;
    }
}
