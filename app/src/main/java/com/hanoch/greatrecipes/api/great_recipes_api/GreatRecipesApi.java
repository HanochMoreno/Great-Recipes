package com.hanoch.greatrecipes.api.great_recipes_api;

import com.hanoch.greatrecipes.api.YummlyRecipe;


import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;


public interface GreatRecipesApi {
    @GET("http://10.0.0.3:3000/recipes/get-yummly-recipe/")
    Single<YummlyRecipe> getYummlyRecipe(@Query("yummlyId") String yummlyId);

    @GET("http://10.0.0.3:3000/recipes/get-user-recipe/")
    Single<UserRecipe> getUserRecipe(@Query("recipeId") String recipeId);

    @POST("http://10.0.0.3:3000/recipes/add-user-recipe")
    Single<UserRecipe> addUserRecipe(@Body UserRecipe userRecipe);

    @POST("http://10.0.0.3:3000/recipes/add-yummly-recipe")
    Single<YummlyRecipe> addYummlyRecipe(@Body YummlyRecipe yummlyRecipe);

    @PUT("http://10.0.0.3:3000/recipes/update-user-recipe")
    Single<UserRecipe> updateUserRecipe(@Body UserRecipe userRecipe);

    @PUT("http://10.0.0.3:3000/user/update-user")
    Single<User> updateUser(@Body User user);

    @GET("http://10.0.0.3:3000/user/get-recipes/")
    Single<UserRecipesResponse> getUserRecipes(@Query("userId") String userId);
}
