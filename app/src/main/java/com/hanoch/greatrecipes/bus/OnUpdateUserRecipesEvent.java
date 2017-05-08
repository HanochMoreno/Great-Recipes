package com.hanoch.greatrecipes.bus;


import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;

import java.util.HashMap;

public class OnUpdateUserUserRecipesEvent {
    public int action;
    public boolean isSuccess;
    public HashMap<String, UserRecipe> recipesMap;
    public Throwable t;

    public OnUpdateUserUserRecipesEvent(int action, boolean isSuccess, HashMap<String, UserRecipe> recipesMap, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.recipesMap = recipesMap;
        this.t = t;
    }
}
