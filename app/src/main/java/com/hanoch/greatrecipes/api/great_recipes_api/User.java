package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.model.Recipes;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.Preferences;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    public String _id;

    public Preferences preferences;
    public Recipes recipes;

    public HashMap<String, Serving> servings;
    public boolean isPremium;
    public int onlineDownloadsCount;

    public User(UserResponse response) {
        this._id = response._id;
        this.preferences = response.preferences;

        this.recipes = new Recipes(response.recipes);

        this.servings = new HashMap<>();
        for (Serving serving : response.servings) {
            this.servings.put(serving.servingId, serving);
        }

        this.isPremium = response.isPremium;
        this.onlineDownloadsCount = response.onlineSearchesCount;
    }

    public UserRecipe getLastUserRecipe() {
        ArrayList<UserRecipe> list = new ArrayList<>(recipes.userRecipes.values());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public YummlyRecipe getLastYummlyRecipe() {
        ArrayList<YummlyRecipe> list = new ArrayList<>(recipes.yummlyRecipes.values());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public Serving getLastServing() {
        ArrayList<Serving> list = new ArrayList<>(servings.values());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public boolean isUserRecipe(String recipeId) {
        return recipes.userRecipes.containsKey(recipeId);
    }

    public boolean isUserRecipeCreatedByThisUser(String recipeId) {
        return isUserRecipe(recipeId) && recipes.userRecipes.get(recipeId).userId.equals(_id);
    }

}
