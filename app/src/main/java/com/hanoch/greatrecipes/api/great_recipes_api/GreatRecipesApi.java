package com.hanoch.greatrecipes.api.great_recipes_api;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.YummlyRecipe;


import java.util.HashMap;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;


public interface GreatRecipesApi {

    @GET(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/get-yummly-recipe/")
    Single<YummlyRecipe> getYummlyRecipe(@Query("yummlyId") String yummlyId);

    @GET(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/get-user-recipe/")
    Single<UserRecipe> getUserRecipe(@Query("recipeId") String recipeId);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/add-user-recipe")
    Single<UserRecipe> addUserRecipe(@Body HashMap<String, Object> body);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/add-yummly-recipe")
    Single<YummlyRecipe> addYummlyRecipe(@Body HashMap<String, Object> body);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/update-user-recipe")
    Single<UserRecipe> updateUserRecipe(@Body HashMap<String, Object> body);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/register")
    Single<UserResponse> register(@Body HashMap<String, String> body);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/login")
    Single<UserResponse> login(@Body HashMap<String, String> body);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/update-user")
    Single<UserResponse> updateUser(@Body HashMap<String, Object> body);
}
