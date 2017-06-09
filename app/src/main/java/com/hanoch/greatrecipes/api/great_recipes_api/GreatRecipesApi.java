package com.hanoch.greatrecipes.api.great_recipes_api;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.model.Device;
import com.hanoch.greatrecipes.model.Preferences;
import com.hanoch.greatrecipes.model.RecipesResponse;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.UserEmailVerification;


import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;


public interface GreatRecipesApi {

    @GET(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/get-yummly-recipe/")
    Single<YummlyRecipe> getYummlyRecipe(@Query("yummlyId") String yummlyId,
                                         @HeaderMap HashMap<String, String> headers);

    @GET(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/get-user-recipe/")
    Single<UserRecipe> getUserRecipe(@Query("_id") String recipeId,
                                     @HeaderMap HashMap<String, String> headers);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/add-user-recipe")
    Single<UserRecipe> addUserRecipe(@Body HashMap<String, Object> body,
                                     @HeaderMap HashMap<String, String> headers);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/add-yummly-recipe")
    Single<YummlyRecipe> addYummlyRecipe(@Body HashMap<String, Object> body,
                                         @HeaderMap HashMap<String, String> headers);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/recipes/update-user-recipe")
    Single<UserRecipe> updateUserRecipe(@Body HashMap<String, Object> body,
                                        @HeaderMap HashMap<String, String> headers);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/email-verification")
    Single<UserEmailVerification> verifyEmail(@Body HashMap<String, String> body);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/login")
    Single<UserResponse> login(@Body HashMap<String, String> body,
                               @HeaderMap HashMap<String, String> headers);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/forgot-password")
    Single<String> forgotPassword(@Body HashMap<String, String> body);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/update-user-recipes")
    Single<RecipesResponse> updateUserRecipes(@Body HashMap<String, Object> body,
                                              @HeaderMap HashMap<String, String> headers);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/update-user-property")
    Single<Object> updateUserProperty(@Body HashMap<String, Object> body,
                                      @HeaderMap HashMap<String, String> headers);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/update-user-preferences")
    Single<Preferences> updateUserPreferences(@Body HashMap<String, Object> body,
                                              @HeaderMap HashMap<String, String> headers);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/update-user-servings")
    Single<ArrayList<Serving>> updateUserServings(@Body HashMap<String, Object> body,
                                                  @HeaderMap HashMap<String, String> headers);

    @PUT(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/update-user-devices")
    Single<ArrayList<Device>> updateUserDevices(@Body HashMap<String, Object> body,
                                                @HeaderMap HashMap<String, String> headers);

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/push-notification")
    Single<Boolean> shareRecipe(@Body HashMap<String, Object> body,
                                @HeaderMap HashMap<String, String> headers);

    @GET(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/get-app-data")
    Single<AppData> getAppData();

    @POST(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL + "/user/get-token")
    Single<String> getToken(@Body HashMap<String, String> body);
}