package com.hanoch.greatrecipes.api.yummly_api;

import com.hanoch.greatrecipes.model.ThinRecipeSearchResult;

import java.util.ArrayList;

/**
 Represents one result of a Yummly recipes online search.
 */

public class YummlySearchResultsResponse {

    public ArrayList<ThinRecipeSearchResult> matches;
}
