package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.model.ThinRecipeSearchResult;

import java.util.ArrayList;

public class OnGotYummlySearchResultsEvent {
    public boolean isSuccess;
    public ArrayList<ThinRecipeSearchResult> results;
    public Throwable t;

    public OnGotYummlySearchResultsEvent(boolean isSuccess, ArrayList<ThinRecipeSearchResult> results, Throwable t) {
        this.isSuccess = isSuccess;
        this.results = results;
        this.t = t;
    }
}
