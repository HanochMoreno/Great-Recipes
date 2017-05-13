package com.hanoch.greatrecipes.api.yummly_api;

import com.hanoch.greatrecipes.AppConsts;

import java.util.List;
import java.util.Map;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Single;


public interface YummlyApi {

    @GET(AppConsts.ApiAccess.YUMMLY_KEY_SEARCH)
    Single<Response<YummlySearchResultsResponse>> getSearchResults(
            @QueryMap Map<String, String> queries,
            @Query("allowedDiet[]") List<String> dietItems,
            @Query("allowedAllergy[]") List<String> allergensItems);

    @GET(AppConsts.ApiAccess.YUMMLY_RECIPE_SEARCH + "{recipeId}")
    Single<YummlyRecipeResponse2> getYummlyRecipe(
            @Path("recipeId") String recipeId,
            @QueryMap Map<String, String> queries);
}
