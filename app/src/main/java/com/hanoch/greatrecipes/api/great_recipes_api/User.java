package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.model.Serving2;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    public String _id;
    public String author;
    public String email;
    public String password;
    public ArrayList<String> dietList;
    public ArrayList<String> allergensList;
    public HashMap<String, UserRecipe> userRecipes;
    public HashMap<String, YummlyRecipe> yummlyRecipes;
    public ArrayList<String> favouriteRecipesIds;
    public HashMap<String, Serving2> servings;
    public boolean isPremium;
    public int onlineSearchesCount;

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
