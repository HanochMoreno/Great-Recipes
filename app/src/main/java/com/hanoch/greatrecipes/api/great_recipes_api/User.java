package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.model.Serving;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    public String _id;
    public String email;
    public String username;
    public String password;
    public ArrayList<String> dietAndAllergensList;
    public HashMap<String, UserRecipe> userRecipes;
    public HashMap<String, YummlyRecipe> yummlyRecipes;
    public ArrayList<String> favouriteRecipesIds;
    public HashMap<String, Serving> servings;
    public boolean isPremium;
    public int maxOnlineSearchResults;
    public int onlineSearchesCount;

    public User(UserResponse response) {
        this._id = response._id;
        this.email = response.email;
        this.username = response.username;
        this.password = response.password;
        this.dietAndAllergensList = response.dietAndAllergensList;

        this.userRecipes = new HashMap<>();
        for (UserRecipe recipe : response.userRecipes) {
            this.userRecipes.put(recipe._id, recipe);
        }

        this.yummlyRecipes = new HashMap<>();
        for (YummlyRecipe recipe : response.yummlyRecipes) {
            this.yummlyRecipes.put(recipe._id, recipe);
        }

        this.servings = new HashMap<>();
        for (Serving serving : response.servings) {
            this.servings.put(serving.servingId, serving);
        }

        this.favouriteRecipesIds = response.favouriteRecipesIds;
        this.isPremium = response.isPremium;
        this.maxOnlineSearchResults = response.maxOnlineSearchResults;
        this.onlineSearchesCount = response.onlineSearchesCount;
    }

    public UserRecipe getLastUserRecipe() {
        ArrayList<UserRecipe> list = new ArrayList<>(userRecipes.values());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public YummlyRecipe getLastYummlyRecipe() {
        ArrayList<YummlyRecipe> list = new ArrayList<>(yummlyRecipes.values());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public boolean isUserRecipe(String recipeId) {
        return userRecipes.containsKey(recipeId);
    }

}
