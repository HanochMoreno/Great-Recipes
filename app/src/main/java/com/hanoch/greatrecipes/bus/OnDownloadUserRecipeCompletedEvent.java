package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;


public class OnDownloadUserRecipeCompletedEvent {
    public boolean isSuccess;
    public UserRecipe userRecipe;
    public Throwable t;

    public OnDownloadUserRecipeCompletedEvent(boolean isSuccess, UserRecipe userRecipe, Throwable t) {
        this.isSuccess = isSuccess;
        this.userRecipe = userRecipe;
        this.t = t;
    }
}
