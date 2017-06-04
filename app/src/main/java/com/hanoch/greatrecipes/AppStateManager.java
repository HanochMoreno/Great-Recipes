package com.hanoch.greatrecipes;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.AppData;
import com.hanoch.greatrecipes.model.UserEmailVerification;

public class AppStateManager {
    private static AppStateManager instance;
    public User user;
    public UserEmailVerification userEmailVerification;
    public YummlyRecipe yummlySearchResult;
    public YummlyRecipe sharedYummlyRecipe;
    public UserRecipe sharedUserRecipe;
    public AppData appData;

    private AppStateManager(){}

    public static AppStateManager getInstance() {

        if (instance == null) {
            instance = new AppStateManager();
        }

        return instance;
    }

    public boolean isRecipeFavourite(String recipeId) {
        return user.recipes.favouriteRecipesIds.contains(recipeId);
    }

    public boolean isTestingUser() {
        return user == null;
    }

}
