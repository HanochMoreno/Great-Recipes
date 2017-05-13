package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.GenericRecipe;

/**
 * A recipe created by a user, as received from GreatRecipesApi.
 */

public class UserRecipe extends GenericRecipe {

    public String instructions;
    public String notes;
    public String userId;

    public UserRecipe() {}

    public UserRecipe(UserRecipe recipe) {
        // CTOR for making a copy

        this._id = recipe._id;
        this.recipeTitle = recipe.recipeTitle;
        this.author = recipe.author;
        this.userId = recipe.userId;
        this.yield = recipe.yield;
        this.imageByteArrayAsString = recipe.imageByteArrayAsString;
//        this.thumbnailByteArrayAsString = recipe.thumbnailByteArrayAsString;
        this.instructions = recipe.instructions;
        this.notes = recipe.notes;
        this.cookingTime = recipe.cookingTime;
        this.ingredientsList = recipe.ingredientsList;
        this.categoriesList = recipe.categoriesList;
    }
}
