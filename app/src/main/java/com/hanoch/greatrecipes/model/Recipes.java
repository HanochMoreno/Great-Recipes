package com.hanoch.greatrecipes.model;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;

import java.util.ArrayList;
import java.util.HashMap;

public class Recipes {
    public HashMap<String, UserRecipe> userRecipes;
    public HashMap<String, YummlyRecipe> yummlyRecipes;
    public ArrayList<String> favouriteRecipesIds;

    public Recipes(RecipesResponse response) {
        this.userRecipes = new HashMap<>();
        this.yummlyRecipes = new HashMap<>();
        this.favouriteRecipesIds = new ArrayList<>();

        if (response != null) {
            if (response.userRecipes != null) {
                for (UserRecipe recipe : response.userRecipes) {
                    this.userRecipes.put(recipe._id, recipe);
                }
            }

            if (response.yummlyRecipes != null) {
                for (YummlyRecipe recipe : response.yummlyRecipes) {
                    this.yummlyRecipes.put(recipe._id, recipe);
                }
            }

            this.favouriteRecipesIds = response.favouriteRecipesIds;
        }
    }
}
