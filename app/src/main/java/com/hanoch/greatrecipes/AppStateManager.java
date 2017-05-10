package com.hanoch.greatrecipes;


import com.hanoch.greatrecipes.api.great_recipes_api.User;

public class AppStateManager {
    private static AppStateManager instance;
    public User user;

    private AppStateManager(){}

    public static AppStateManager getInstance() {

        if (instance == null) {
            instance = new AppStateManager();
        }

        return instance;
    }

    public boolean isRecipeFavourite(String recipeId) {
        return user.favouriteRecipesIds.contains(recipeId);
    }

    public boolean isTestingUser() {
        return user == null;
    }

}
