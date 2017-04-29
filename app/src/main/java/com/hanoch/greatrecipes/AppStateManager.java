package com.hanoch.greatrecipes;


import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipesResponse;

public class AppStateManager {
    private static AppStateManager instance;
    public User user;
    public UserRecipesResponse userRecipes;

    private AppStateManager(){}

    public static AppStateManager getInstance() {

        if (instance == null) {
            instance = new AppStateManager();
        }

        return instance;
    }
}
