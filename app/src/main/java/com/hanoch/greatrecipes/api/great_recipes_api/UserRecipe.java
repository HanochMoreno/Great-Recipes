package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.GenericRecipe;

import java.lang.reflect.Field;
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

//-------------------------------------------------------------------------------------------------

    public boolean isEmpty() {
        // For checking if changes has made when creating a new recipe

        if (recipeTitle != null && !recipeTitle.isEmpty()) return false;
        if (yield != 0) return false;
        if (imageByteArrayAsString != null && !imageByteArrayAsString.isEmpty()) return false;
        if (instructions != null && !instructions.isEmpty()) return false;
        if (notes != null && !notes.isEmpty()) return false;
        if (cookingTime != 0) return false;

        if (ingredientsList != null && !ingredientsList.isEmpty()
                && !ingredientsList.contains(AppConsts.Category.NO_INFO)) return false;

        if (categoriesList != null && !categoriesList.isEmpty()
                && !categoriesList.contains(AppConsts.Category.NO_INFO)) return false;

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public boolean equals(UserRecipe recipe) {
        // For checking if changes has made when editing an existing recipe

        if (!_id.equals(recipe._id)) return false;
        if (!recipeTitle.equals(recipe.recipeTitle)) return false;
        if (!author.equals(recipe.author)) return false;
        if (!userId.equals(recipe.userId)) return false;
        if (yield != recipe.yield) return false;
        if (!imageByteArrayAsString.equals(recipe.imageByteArrayAsString)) return false;
        if (!instructions.equals(recipe.instructions)) return false;
        if (!notes.equals(recipe.notes)) return false;
        if (cookingTime != recipe.cookingTime) return false;

        if (recipe.ingredientsList == null || (ingredientsList.size() != recipe.ingredientsList.size())) {
            return false;
        }
        for (String ingredient : ingredientsList) {
            if (!recipe.ingredientsList.contains(ingredient)) return false;
        }

        if (recipe.categoriesList == null || (categoriesList.size() != recipe.categoriesList.size())) {
            return false;
        }
        for (String category : categoriesList) {
            if (!recipe.categoriesList.contains(category)) return false;
        }

        return true;
    }
}
