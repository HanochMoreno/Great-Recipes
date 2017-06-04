package com.hanoch.greatrecipes.api;


import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.api.great_recipes_api.AppData;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.UserResponse;
import com.hanoch.greatrecipes.api.yummly_api.YummlySearchResultsResponse;
import com.hanoch.greatrecipes.api.yummly_api.YummlyRecipeResponse2;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnAppDataEvent;
import com.hanoch.greatrecipes.bus.OnUpdateDeviceEvent;
import com.hanoch.greatrecipes.bus.OnUserRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnEmailVerificationEvent;
import com.hanoch.greatrecipes.bus.OnForgotPasswordEvent;
import com.hanoch.greatrecipes.bus.OnGotYummlySearchResultsEvent;
import com.hanoch.greatrecipes.bus.OnShareRecipeEvent;
import com.hanoch.greatrecipes.bus.OnToggleRecipeFavouriteEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserDietAndAllergensEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserPreferencesEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserPropertyEvent;
import com.hanoch.greatrecipes.bus.OnYummlyRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.bus.OnUpdateServingsListEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.database.SqLiteDbManager;
import com.hanoch.greatrecipes.model.AllergenAndDiet;
import com.hanoch.greatrecipes.model.ApiProvider;
import com.hanoch.greatrecipes.model.Device;
import com.hanoch.greatrecipes.model.Recipes;
import com.hanoch.greatrecipes.model.RecipesResponse;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.Preferences;
import com.hanoch.greatrecipes.model.UserEmailVerification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;
import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ApisManager {
    private static ApisManager instance;
    private MyBus bus;
    private AppStateManager appStateManager;

    private ApisManager() {
        this.bus = MyBus.getInstance();
        this.appStateManager = AppStateManager.getInstance();
    }

//-------------------------------------------------------------------------------------------------

    public static ApisManager getInstance() {
        if (instance == null) {
            instance = new ApisManager();
        }
        return instance;
    }

//-------------------------------------------------------------------------------------------------

    public void getAppData() {

        Subscriber<AppData> subscriber = new Subscriber<AppData>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnAppDataEvent(false, t));
            }

            @Override
            public void onNext(AppData appData) {
                appStateManager.appData = appData;
                bus.post(new OnAppDataEvent(true, null));
            }
        };

        Single<AppData> getAppData = ApiProvider.getGreatRecipesApi().getAppData("1234");

        getAppData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Verify a new user email
     */
    public void verifyEmail(String email, String username, String password) {

        Subscriber<UserEmailVerification> subscriber = new Subscriber<UserEmailVerification>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnEmailVerificationEvent(false, t));
            }

            @Override
            public void onNext(UserEmailVerification userEmailVerification) {
                appStateManager.userEmailVerification = userEmailVerification;
                bus.post(new OnEmailVerificationEvent(true, null));
            }
        };

        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("username", username);
        body.put("password", password);

        Single<UserEmailVerification> verifyEmail = ApiProvider.getGreatRecipesApi().verifyEmail(body);

        verifyEmail
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    public void forgotPassword(String email) {

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnForgotPasswordEvent(false, null, null, t));
            }

            @Override
            public void onNext(String password) {
                bus.post(new OnForgotPasswordEvent(true, email, password, null));
            }
        };

        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);

        Single<String> login = ApiProvider.getGreatRecipesApi().forgotPassword(body);

        login
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * login a user
     */
    public void login(Context context, String email, String password) {

        Subscriber<UserResponse> subscriber = new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnLoginEvent(false, t));
            }

            @Override
            public void onNext(UserResponse userResponse) {
                appStateManager.user = new User(userResponse);

                bus.post(new OnLoginEvent(true, null));

                String firebaseToken = FirebaseInstanceId.getInstance().getToken();
                if (firebaseToken != null) {
                    Device device = new Device(context, firebaseToken);
                    updateUserDevices(device, BusConsts.ACTION_ADD_NEW);
                }
            }
        };

        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Single<UserResponse> login = ApiProvider.getGreatRecipesApi().login(body);

        login
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update the user servingsList to database
     */
    public void updateUserServings(ArrayList<Serving> servingsList, int action) {

        Subscriber<ArrayList<Serving>> subscriber = new Subscriber<ArrayList<Serving>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateServingsListEvent(action, false, t));
            }

            @Override
            public void onNext(ArrayList<Serving> servings) {
                appStateManager.user.servings = new HashMap<>();
                for (Serving serving : servings) {
                    appStateManager.user.servings.put(serving.servingId, serving);
                }

                bus.post(new OnUpdateServingsListEvent(action, true, null));
            }
        };

        User currentUser = appStateManager.user;
        HashMap<String, Serving> currentServings = new HashMap<>(currentUser.servings);

        switch (action) {
            case BusConsts.ACTION_ADD_NEW:
                for (Serving serving : servingsList) {
                    currentServings.put(serving.servingId, serving);
                }
                break;

            case BusConsts.ACTION_DELETE:
                for (Serving serving : servingsList) {
                    currentServings.remove(serving.servingId);
                }
                break;
        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", currentUser._id);
        body.put("servings", currentServings.values());

        Single<ArrayList<Serving>> updateUserServing = ApiProvider.getGreatRecipesApi().updateUserServings(body);

        updateUserServing
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Add a new user-recipe to database
     */
    public void addUserRecipe(UserRecipe recipe) {

        Subscriber<UserRecipe> subscriber = new Subscriber<UserRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(BusConsts.ACTION_ADD_NEW, false, t));
            }

            @Override
            public void onNext(UserRecipe recipe) {
                ArrayList<String> userRecipesId = new ArrayList<>();
                userRecipesId.add(recipe._id);

                updateUserRecipes(userRecipesId, null, BusConsts.ACTION_ADD_NEW);
            }
        };

        HashMap<String, Object> body = recipe.generateBody();

        Single<UserRecipe> addUserRecipe = ApiProvider.getGreatRecipesApi().addUserRecipe(body);

        addUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update exist user recipe to database
     */
    public void updateUserRecipe(UserRecipe recipe) {

        Subscriber<UserRecipe> subscriber = new Subscriber<UserRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(BusConsts.ACTION_EDIT, false, t));
            }

            @Override
            public void onNext(UserRecipe recipe) {
                ArrayList<String> userRecipesIds = new ArrayList<>();
                userRecipesIds.add(recipe._id);

                updateUserRecipes(userRecipesIds, null, BusConsts.ACTION_EDIT);
            }
        };

        HashMap<String, Object> body = recipe.generateBody();

        Single<UserRecipe> updateUserRecipe = ApiProvider.getGreatRecipesApi().updateUserRecipe(body);

        updateUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * http://api.yummly.com/v1/api/recipes?q=chicken&start=0&maxResult=10&_app_id=418b707a&_app_key=249ec501a990bd7d5fa5dd5218bf7e14
     */
    public void performSearchRecipesFromYummlyApi(Context context, String keyToSearch) {

        User user = appStateManager.user;

        ArrayList<String> userDietAndAllergensList = user.preferences.dietAndAllergensList;

        HashMap<String, String> query = new HashMap<>();
        query.put("q", keyToSearch);
        query.put("start", "0");
        query.put("maxResult", String.valueOf(user.preferences.maxOnlineSearchResults));
        query.put("_app_id", appStateManager.appData.yummlyAppId);
        query.put("_app_key", appStateManager.appData.yummlyAppKey);

        List<String> dietList = new ArrayList<>();
        List<String> allergensList = new ArrayList<>();
        for (AllergenAndDiet item : AppConsts.ALLERGENS_AND_DIET_LIST) {
            if (userDietAndAllergensList.contains(String.valueOf(item.positionInDrawer))) {
                if (item.isDiet) {
                    dietList.add(item.searchKeyName);
                } else {
                    allergensList.add(item.searchKeyName);
                }
            }
        }

        Subscriber<Response<YummlySearchResultsResponse>> subscriber = new Subscriber<Response<YummlySearchResultsResponse>>() {
            @Override
            public final void onCompleted() {
            }

            @Override
            public final void onError(Throwable t) {
                bus.post(new OnGotYummlySearchResultsEvent(false, null, t));
            }

            @Override
            public final void onNext(Response<YummlySearchResultsResponse> response) {

                SqLiteDbManager sqliteManager = new SqLiteDbManager(context);

                // Deleting old search results
                sqliteManager.deleteAllSearchResults();

                // Adding the new search results
                sqliteManager.addSearchResults(response.body().matches);

                bus.post(new OnGotYummlySearchResultsEvent(true, response.body().matches, null));
            }
        };

        Single<Response<YummlySearchResultsResponse>> getSearchResults =
                ApiProvider.getYummlyApi().getSearchResults(query, dietList, allergensList);

        getSearchResults
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * http://api.yummly.com/v1/api/recipe/French-Onion-Soup-1292648?_app_id=418b707a&_app_key=249ec501a990bd7d5fa5dd5218bf7e14
     */
    private void downloadRecipeFromYummlyApi(Context context, String resultYummlyId) {

        HashMap<String, String> query = new HashMap<>();
        query.put("_app_id", AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL);
        query.put("_app_key", AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL);
        query.put("requirePictures", "true");

        Subscriber<YummlyRecipeResponse2> subscriber = new Subscriber<YummlyRecipeResponse2>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnYummlyRecipeDownloadedEvent(false, t, AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE));
            }

            @Override
            public void onNext(YummlyRecipeResponse2 recipe) {
                HashMap<String, Object> body = recipe.generateBody(context);
                addYummlyRecipeToGreatRecipeApi(body);
            }
        };

        Single<YummlyRecipeResponse2> getYummlyRecipeFromYummlyApi =
                ApiProvider.getYummlyApi().getYummlyRecipe(resultYummlyId, query);

        getYummlyRecipeFromYummlyApi
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    private void addYummlyRecipeToGreatRecipeApi(HashMap<String, Object> body) {

        int action = AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE;

        Subscriber<YummlyRecipe> subscriber = new Subscriber<YummlyRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnYummlyRecipeDownloadedEvent(false, t, action));
            }

            @Override
            public void onNext(YummlyRecipe recipe) {
                appStateManager.yummlySearchResult = recipe;
                bus.post(new OnYummlyRecipeDownloadedEvent(true, null, action));
            }
        };

        Single<YummlyRecipe> getYummlyRecipe = ApiProvider.getGreatRecipesApi().addYummlyRecipe(body);
        getYummlyRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    public void getYummlyRecipeFromGreatRecipesApi(Context context, String resultYummlyId, int action) {
        if (action == AppConsts.Actions.REVIEW_YUMMLY_ONLINE) {
            appStateManager.yummlySearchResult = null;
        } else if (action == AppConsts.Actions.DOWNLOAD_SHARED_YUMMLY_RECIPE) {
            appStateManager.sharedYummlyRecipe = null;
        }

        Subscriber<YummlyRecipe> subscriber = new Subscriber<YummlyRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                if (action == AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE && t.getMessage().contains("does not exist in the DB")) {
                    // Recipe not found in the GreatRecipes DB
                    downloadRecipeFromYummlyApi(context, resultYummlyId);
                } else {
                    bus.post(new OnYummlyRecipeDownloadedEvent(false, t, action));
                }
            }

            @Override
            public void onNext(YummlyRecipe recipe) {
                // Recipe was found in the GreatRecipes DB
                if (action == AppConsts.Actions.DOWNLOAD_NEW_YUMMLY_RECIPE) {
                    appStateManager.yummlySearchResult = recipe;
                } else {
                    appStateManager.sharedYummlyRecipe = recipe;
                }
                bus.post(new OnYummlyRecipeDownloadedEvent(true, null, action));
            }
        };

        Single<YummlyRecipe> getYummlyRecipe =
                ApiProvider.getGreatRecipesApi().getYummlyRecipe(resultYummlyId);

        getYummlyRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    public void getUserRecipeFromGreatRecipesApi(String recipeId) {
        appStateManager.sharedUserRecipe = null;

        Subscriber<UserRecipe> subscriber = new Subscriber<UserRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUserRecipeDownloadedEvent(false, t));
            }

            @Override
            public void onNext(UserRecipe recipe) {
                appStateManager.sharedUserRecipe = recipe;
                bus.post(new OnUserRecipeDownloadedEvent(true, null));
            }
        };

        Single<UserRecipe> getUserRecipe = ApiProvider.getGreatRecipesApi().getUserRecipe(recipeId);

        getUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update the user's recipes lists after adding/editing/deleting recipes
     */
    public void updateUserRecipes(ArrayList<String> userRecipesIds, ArrayList<String> yummlyRecipesIds, int action) {

        Subscriber<RecipesResponse> subscriber = new Subscriber<RecipesResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(action, false, t));
            }

            @Override
            public void onNext(RecipesResponse recipesResponse) {
                appStateManager.user.recipes = new Recipes(recipesResponse);
                bus.post(new OnUpdateUserRecipesEvent(action, true, null));
            }
        };

        User currentUser = appStateManager.user;

        ArrayList<String> newUserRecipesIds = null;
        ArrayList<String> newYummlyRecipesIds = null;
        ArrayList<String> newFavouriteRecipesIds = null;

        switch (action) {
            case BusConsts.ACTION_ADD_NEW:
                if (yummlyRecipesIds != null) {
                    newYummlyRecipesIds = new ArrayList<>(currentUser.recipes.yummlyRecipes.keySet());

                    for (String recipeId : yummlyRecipesIds) {
                        newYummlyRecipesIds.add(recipeId);
                    }
                }
                /// no break...
            case BusConsts.ACTION_EDIT:
                if (userRecipesIds != null) {
                    newUserRecipesIds = new ArrayList<>(currentUser.recipes.userRecipes.keySet());

                    for (String recipeId : userRecipesIds) {
                        newUserRecipesIds.add(recipeId);
                    }
                }

                break;

            case BusConsts.ACTION_DELETE:
                newFavouriteRecipesIds = new ArrayList<>(currentUser.recipes.favouriteRecipesIds);

                if (userRecipesIds != null) {
                    newUserRecipesIds = new ArrayList<>(currentUser.recipes.userRecipes.keySet());

                    for (String recipeId : userRecipesIds) {
                        newUserRecipesIds.remove(recipeId);
                        if (newFavouriteRecipesIds.contains(recipeId)) {
                            newFavouriteRecipesIds.remove(recipeId);
                        }
                    }
                }

                if (yummlyRecipesIds != null) {
                    newYummlyRecipesIds = new ArrayList<>(currentUser.recipes.yummlyRecipes.keySet());

                    for (String recipeId : yummlyRecipesIds) {

                        newYummlyRecipesIds.remove(recipeId);
                        if (newFavouriteRecipesIds.contains(recipeId)) {
                            newFavouriteRecipesIds.remove(recipeId);
                        }
                    }
                }

                if (newFavouriteRecipesIds.size() == currentUser.recipes.favouriteRecipesIds.size()) {
                    // No change in favourites list
                    newFavouriteRecipesIds = null;
                }

                break;

            case BusConsts.ACTION_DELETE_ALL_LISTS:
                newUserRecipesIds = new ArrayList<>();
                newYummlyRecipesIds = new ArrayList<>();
                newFavouriteRecipesIds = new ArrayList<>();
                break;

        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", currentUser._id);
        if (newUserRecipesIds != null) {
            body.put("userRecipesIds", newUserRecipesIds);
        }
        if (newYummlyRecipesIds != null) {
            body.put("yummlyRecipesIds", newYummlyRecipesIds);
        }
        if (newFavouriteRecipesIds != null) {
            body.put("favouriteRecipesIds", newFavouriteRecipesIds);
        }

        Single<RecipesResponse> updateUserRecipes = ApiProvider.getGreatRecipesApi().updateUserRecipes(body);

        updateUserRecipes
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update the user's diet And allergens lists after adding/editing/deleting recipes
     */
    public void updateUserDietOrAllergenPreference(int positionInDrawer) {
        String sPositionInDrawer = String.valueOf(positionInDrawer);

        User currentUser = appStateManager.user;

        ArrayList<String> dietAndAllergensList = new ArrayList<>(currentUser.preferences.dietAndAllergensList);

        int action;
        if (dietAndAllergensList.contains(sPositionInDrawer)) {
            dietAndAllergensList.remove(sPositionInDrawer);
            action = BusConsts.ACTION_DELETE;
        } else {
            dietAndAllergensList.add(sPositionInDrawer);
            action = BusConsts.ACTION_ADD_NEW;
        }

        Subscriber<Preferences> subscriber = new Subscriber<Preferences>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserDietAndAllergensEvent(action, false, positionInDrawer, t));
            }

            @Override
            public void onNext(Preferences userPreferences) {
                appStateManager.user.preferences.dietAndAllergensList = userPreferences.dietAndAllergensList;
                bus.post(new OnUpdateUserDietAndAllergensEvent(action, true, positionInDrawer, null));
            }
        };

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", currentUser._id);
        body.put("dietAndAllergensList", dietAndAllergensList);

        Single<Preferences> updateUserPreferences = ApiProvider.getGreatRecipesApi().updateUserPreferences(body);

        updateUserPreferences
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Updates the user preferences
     */
    public void updateUserPreferences(Preferences preferences) {

        Subscriber<Preferences> subscriber = new Subscriber<Preferences>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserPreferencesEvent(false, t));
            }

            @Override
            public void onNext(Preferences preferences) {
                appStateManager.user.preferences = preferences;
                bus.post(new OnUpdateUserPreferencesEvent(true, null));
            }
        };

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", appStateManager.user._id);

        body.put("email", preferences.email);
        body.put("username", preferences.username);
        body.put("password", preferences.password);
        body.put("dietAndAllergensList", preferences.dietAndAllergensList);
        body.put("maxOnlineSearchResults", preferences.maxOnlineSearchResults);

        Single<Preferences> updateUserPreferences = ApiProvider
                .getGreatRecipesApi().updateUserPreferences(body);

        updateUserPreferences
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

//-------------------------------------------------------------------------------------------------

    /**
     * Updates a user specific property
     */
    private void updateUserProperty(OnUpdateUserPropertyEvent busEvent, String key, Object value) {

        Subscriber<Object> subscriber = new Subscriber<Object>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                if (busEvent != null) {
                    busEvent.setThrowable(t);
                    bus.post(busEvent);
                }
            }

            @Override
            public void onNext(Object response) {
                switch (key) {

                    case "isPremium":
                        appStateManager.user.isPremium = true;
                        break;

                    case "onlineDownloadsCount":
                        appStateManager.user.onlineDownloadsCount = (int) response;
                        break;

                    case "onlineSearchesCount":
                        appStateManager.user.onlineSearchesCount = (int) response;
                        break;

                    case "favouriteRecipesIds":
                        appStateManager.user.recipes.favouriteRecipesIds = (ArrayList<String>) response;
                        break;
                }

                if (busEvent != null) {
                    busEvent.setSuccess(true);
                    bus.post(busEvent);
                }
            }
        };

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", appStateManager.user._id);
        body.put(key, value);

        Single<Object> updateUserPremiumStatus = ApiProvider.getGreatRecipesApi().updateUserProperty(body);

        updateUserPremiumStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Push notification: share Recipe
     */
    public void shareRecipe(String recipientEmail, String recipeId) {

        Subscriber<Boolean> subscriber = new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnShareRecipeEvent(false, t));
            }

            @Override
            public void onNext(Boolean isSuccess) {
                if (isSuccess) {
                    bus.post(new OnShareRecipeEvent(true, null));
                }
            }
        };
        boolean isUserRecipe = appStateManager.user.isUserRecipe(recipeId);
        String recipeTitle;
        if (isUserRecipe) {
            recipeTitle = appStateManager.user.recipes.userRecipes.get(recipeId).recipeTitle;
        } else {
            recipeTitle = appStateManager.user.recipes.yummlyRecipes.get(recipeId).recipeTitle;
        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", appStateManager.user._id);
        body.put("recipientEmail", recipientEmail);
        body.put("recipeId", recipeId);
        body.put("recipeTitle", recipeTitle);
        body.put("isUserRecipe", isUserRecipe);

        Single<Boolean> shareRecipe = ApiProvider.getGreatRecipesApi().shareRecipe(body);

        shareRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


//-------------------------------------------------------------------------------------------------

    /**
     * Add or remove device of a user
     */
    public void updateUserDevices(Device device, int action) {

        Subscriber<ArrayList<Device>> subscriber = new Subscriber<ArrayList<Device>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateDeviceEvent(false, t));
            }

            @Override
            public void onNext(ArrayList<Device> devices) {
                if (appStateManager.user != null) {
                    appStateManager.user.devices = new HashMap<>();
                    for (Device device : devices) {
                        appStateManager.user.devices.put(device.androidId, device);
                    }
                }
                bus.post(new OnUpdateDeviceEvent(true, null));
            }
        };

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", appStateManager.user._id);
        body.put("action", action);
        body.put("androidId", device.androidId);
        body.put("firebaseToken", device.firebaseToken);

        Single<ArrayList<Device>> updateUserPremiumStatus = ApiProvider.getGreatRecipesApi().updateUserDevices(body);

        updateUserPremiumStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Updates the premium status to 'true' after purchased by the user
     */
    public void updatePremiumStatus() {
        // Premium access granted by Google.

        appStateManager.user.isPremium = true;
        updateUserProperty(null, "isPremium", true);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Updates the online recipe downloads counter made by the user
     */
    public void updateOnlineDownloadsCount(int onlineDownloadsCount) {

        updateUserProperty(null, "onlineDownloadsCount", onlineDownloadsCount);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Updates the online searches counter made by the user
     */
    public void updateOnlineSearchesCount(int onlineSearchesCount) {

        updateUserProperty(null, "onlineSearchesCount", onlineSearchesCount);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Toggle a recipe favourite index
     */
    public void toggleRecipeFavourite(String recipeId) {

        ArrayList<String> newFavouriteRecipesIds = new ArrayList<>(appStateManager.user.recipes.favouriteRecipesIds);

        if (newFavouriteRecipesIds.contains(recipeId)) {
            newFavouriteRecipesIds.remove(recipeId);
        } else {
            newFavouriteRecipesIds.add(recipeId);
        }

        updateUserProperty(new OnToggleRecipeFavouriteEvent(), "favouriteRecipesIds", newFavouriteRecipesIds);
    }

}