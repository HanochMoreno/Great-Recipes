package com.hanoch.greatrecipes.model;

import com.hanoch.greatrecipes.AppConsts;

public class Recipe{

    public long id;
    public String title;
    public String author;
    public int yield;
    public String url;
    public int energy;
    public float rating;

    /**
     * Indicates the recipe favourite index. <br/>
     * <br> possible values: <br/>
     * 0= Not favourite <br/>
     * 1= favourite <br/>
     */
    public int favouriteIndex;

    /**
     * Private notes the user added.
     */
    public String notes;

    public int time;
    public String categoriesList;
    public String ingredientsList;
    public String instructions;

    /**
     * Indicates the origin of the recipe. <br/>
     * <br> possible values: <br/>
     * 0= added from the online search results <br/>
     * 1= added manually
     */
    public int originIndex;

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
}
