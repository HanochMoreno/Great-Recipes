package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.api.YummlyRecipe;


public class OnYummlyRecipeDownloadedEvent {
    public boolean isSuccess;
    public YummlyRecipe yummlyRecipe;
    public Throwable t;

    public OnYummlyRecipeDownloadedEvent(boolean isSuccess, YummlyRecipe yummlyRecipe, Throwable t) {
        this.isSuccess = isSuccess;
        this.yummlyRecipe = yummlyRecipe;
        this.t = t;
    }
}
