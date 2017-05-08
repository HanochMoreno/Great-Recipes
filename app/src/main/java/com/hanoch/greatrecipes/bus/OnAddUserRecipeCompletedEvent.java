package com.hanoch.greatrecipes.bus;


import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;

public class onUpdateUserRecipeCompletedEvent {
    public boolean isSuccess;
    public UserRecipe recipe;
    public Throwable t;

    public onUpdateUserRecipeCompletedEvent(boolean isSuccess, UserRecipe recipe, Throwable t) {
        this.isSuccess = isSuccess;
        this.recipe = recipe;
        this.t = t;
    }
}
