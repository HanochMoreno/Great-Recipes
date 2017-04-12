package com.hanoch.greatrecipes.retrofit;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.model.RecipeSearchResultsResponse;
import com.hanoch.greatrecipes.model.YummlyRecipe;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Single;


public interface YummlyService {
    @GET(AppConsts.ApiAccess.YUMMLY_KEY_SEARCH)
    Single<RecipeSearchResultsResponse> getSearchResults(
            @QueryMap Map<String, String> queries,
            @Query("allowedDiet[]") List<String> dietItems,
            @Query("allowedAllergy[]") List<String> allergensItems);

    @GET(AppConsts.ApiAccess.YUMMLY_RECIPE_SEARCH + "{recipeId}")
    Single<YummlyRecipe> getRecipeInfo(
            @Path("recipeId") String recipeId,
            @QueryMap Map<String, String> queries);
}
