package com.hanoch.greatrecipes.api.yummly_api;

import com.hanoch.greatrecipes.model.ThinRecipeSearchResult;

import java.util.List;

/**
 Represents one result of a recipe online search.
 */

public class SearchResultsResponse {

    public List<ThinRecipeSearchResult> matches;
}
