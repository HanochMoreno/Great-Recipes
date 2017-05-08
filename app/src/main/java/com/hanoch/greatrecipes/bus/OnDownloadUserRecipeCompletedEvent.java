package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.api.YummlyRecipe;

/**
 * Created by Hanoc_000 on 30/04/2017.
 */

public class OnDownloadYummlyRecipeCompletedEvent {
    public boolean isSuccess;
    public YummlyRecipe yummlyRecipe;
    public Throwable t;

    public OnDownloadYummlyRecipeCompletedEvent(boolean isSuccess, YummlyRecipe yummlyRecipe, Throwable t) {
        this.isSuccess = isSuccess;
        this.yummlyRecipe = yummlyRecipe;
        this.t = t;
    }
}
