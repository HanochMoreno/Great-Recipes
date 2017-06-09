package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.model.Device;
import com.hanoch.greatrecipes.model.Recipes;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.Preferences;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    public String _id;

    public Preferences preferences;
    public HashMap<String, Device> devices;
    public Recipes recipes;

    public HashMap<String, Serving> servings;
    public boolean isPremium;
    public int onlineDownloadsCount;
    public int onlineSearchesCount;

//-------------------------------------------------------------------------------------------------

    public User(UserResponse response) {
        this._id = response._id;

        this.preferences = response.preferences;

        this.devices = new HashMap<>();
        for (Device device : response.devices) {
            this.devices.put(device.androidId, device);
        }

        this.recipes = new Recipes(response.recipes);

        this.servings = new HashMap<>();
        for (Serving serving : response.servings) {
            this.servings.put(serving.servingId, serving);
        }

        this.isPremium = response.isPremium;
        this.onlineDownloadsCount = response.onlineSearchesCount;
        this.onlineSearchesCount = response.onlineSearchesCount;
    }

//-------------------------------------------------------------------------------------------------

    public Serving getLastServing() {
        ArrayList<Serving> list = new ArrayList<>(servings.values());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

//-------------------------------------------------------------------------------------------------

    public boolean isUserRecipe(String recipeId) {
        return recipes.userRecipes.containsKey(recipeId);
    }

//-------------------------------------------------------------------------------------------------

    public boolean isUserRecipeCreatedByThisUser(String recipeId) {
        return isUserRecipe(recipeId) && recipes.userRecipes.get(recipeId).userId.equals(_id);
    }

}
