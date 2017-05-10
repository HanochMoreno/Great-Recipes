package com.hanoch.greatrecipes.api.great_recipes_api;

import com.hanoch.greatrecipes.api.YummlyRecipe;


import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;


public interface GreatRecipesApi {
    @GET("http://10.0.0.3:3000/recipes/get-yummly-recipe-by-yummly-id/")
    Single<YummlyRecipe> getYummlyRecipeByYummlyId(@Query("yummlyId") String yummlyId);

    @GET("http://10.0.0.3:3000/recipes/get-yummly-recipe/")
    Single<YummlyRecipe> getYummlyRecipe(@Query("recipeId") String yummlyId);

    @GET("http://10.0.0.3:3000/recipes/get-user-recipe/")
    Single<UserRecipe> getUserRecipe(@Query("recipeId") String recipeId);

    @POST("http://10.0.0.3:3000/recipes/add-user-recipe")
    Single<UserRecipe> addUserRecipe(@Body UserRecipe userRecipe);

    @POST("http://10.0.0.3:3000/recipes/add-yummly-recipe")
    Single<YummlyRecipe> addYummlyRecipe(@Body YummlyRecipe yummlyRecipe);

    @PUT("http://10.0.0.3:3000/recipes/update-user-recipe")
    Single<UserRecipe> updateUserRecipe(@Body UserRecipe userRecipe);

    @POST("http://10.0.0.3:3000/user/register")
    Single<User> register(@Body String email, String userName, String password);

    @POST("http://10.0.0.3:3000/user/login")
    Single<User> login(@Body String userName, String password);

    @PUT("http://10.0.0.3:3000/user/update-user")
    Single<User> updateUser(@Body User user);
}
