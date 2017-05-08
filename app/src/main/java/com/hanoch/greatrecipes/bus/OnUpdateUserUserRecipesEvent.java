package com.hanoch.greatrecipes.bus;


import com.hanoch.greatrecipes.api.YummlyRecipe;

import java.util.HashMap;

public class OnUpdateUserYummlyRecipesEvent {
    public int action;
    public boolean isSuccess;
    public HashMap<String, YummlyRecipe> recipesMap;
    public Throwable t;

    public OnUpdateUserYummlyRecipesEvent(int action, boolean isSuccess, HashMap<String, YummlyRecipe> recipesMap, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.recipesMap = recipesMap;
        this.t = t;
    }
}
