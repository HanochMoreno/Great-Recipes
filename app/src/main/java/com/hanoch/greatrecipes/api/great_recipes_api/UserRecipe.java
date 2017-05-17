package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.GenericRecipe;

import java.util.HashMap;

/**
 * A recipe created by a user, as received from GreatRecipesApi.
 */

public class UserRecipe extends GenericRecipe {

    public String instructions;
    public String notes;
    public String userId;

    public UserRecipe() {}

//-------------------------------------------------------------------------------------------------

    public UserRecipe(UserRecipe recipe) {
        // CTOR for making a copy

        this._id = recipe._id;
        this.recipeTitle = recipe.recipeTitle;
        this.author = recipe.author;
        this.userId = recipe.userId;
        this.yield = recipe.yield;
        this.imageByteArrayAsString = recipe.imageByteArrayAsString;
        this.instructions = recipe.instructions;
        this.notes = recipe.notes;
        this.cookingTime = recipe.cookingTime;
        this.ingredientsList = recipe.ingredientsList;
        this.categoriesList = recipe.categoriesList;
    }

//-------------------------------------------------------------------------------------------------

    public HashMap<String, Object> generateBody() {

        HashMap<String, Object> body = new HashMap<>();
        if (this._id != null) {
            body.put("_id", this._id);
        }
        body.put("recipeTitle", this.recipeTitle);
        body.put("author", this.author);
        body.put("userId", this.userId);
        body.put("yield", this.yield);
        body.put("imageByteArrayAsString", this.imageByteArrayAsString);
        body.put("instructions", this.instructions);
        body.put("notes", this.notes);
        body.put("cookingTime", this.cookingTime);
        body.put("ingredientsList", this.ingredientsList);
        body.put("categoriesList", this.categoriesList);

        return body;
    }
}
