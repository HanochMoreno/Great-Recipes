package com.hanoch.greatrecipes.database;


import android.content.Context;
import android.graphics.Bitmap;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.UserResponse;
import com.hanoch.greatrecipes.api.yummly_api.YummlySearchResultsResponse;
import com.hanoch.greatrecipes.api.yummly_api.YummlyRecipeResponse2;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnDownloadUserRecipeCompletedEvent;
import com.hanoch.greatrecipes.bus.OnGotYummlySearchResultsEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserDietAndAllergensEvent;
import com.hanoch.greatrecipes.bus.OnYummlyRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.bus.OnRegisterEvent;
import com.hanoch.greatrecipes.bus.OnUpdateServingsListEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.model.AllergenAndDiet;
import com.hanoch.greatrecipes.model.ApiProvider;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;
import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class GreatRecipesDbManager {
    private static GreatRecipesDbManager instance;

    private GreatRecipesDbManager() {
    }

//-------------------------------------------------------------------------------------------------

    public static GreatRecipesDbManager getInstance() {
        if (instance == null) {
            instance = new GreatRecipesDbManager();
        }
        return instance;
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Register a new user
     */
    public void register(String email, String username, String password) {
        MyBus bus = MyBus.getInstance();

        Subscriber<UserResponse> subscriber = new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnRegisterEvent(false, t));
            }

            @Override
            public void onNext(UserResponse userResponse) {
                AppStateManager.getInstance().user = new User(userResponse);
                bus.post(new OnRegisterEvent(true, null));
            }
        };

        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("username", username);
        body.put("password", password);

        Single<UserResponse> register = ApiProvider.getGreatRecipesApi().register(body);

        register
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * login a user
     */
    public void login(String username, String password) {
        MyBus bus = MyBus.getInstance();

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
                AppStateManager.getInstance().user = new User(userResponse);
                bus.post(new OnLoginEvent(true, null));
            }
        };

        HashMap<String, String> body = new HashMap<>();
        body.put("username", username);
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
    public void updateUserServingsMap(ArrayList<Serving> servingsList, int action) {
        MyBus bus = MyBus.getInstance();

        Subscriber<UserResponse> subscriber = new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateServingsListEvent(action, false, t));
            }

            @Override
            public void onNext(UserResponse userResponse) {
                AppStateManager.getInstance().user = new User(userResponse);
                bus.post(new OnUpdateServingsListEvent(action, true, null));
            }
        };

        User currentUser = AppStateManager.getInstance().user;
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

        Single<UserResponse> updateUser = ApiProvider.getGreatRecipesApi().updateUser(body);

        updateUser
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Add a new user recipe to database
     *
     * @param recipe
     */
    public void addUserRecipe(UserRecipe recipe) {
        MyBus bus = MyBus.getInstance();

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
                ArrayList<UserRecipe> userRecipes = new ArrayList<>();
                userRecipes.add(recipe);

                updateUserRecipes(userRecipes, null, BusConsts.ACTION_ADD_NEW);
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
        MyBus bus = MyBus.getInstance();

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
                ArrayList<UserRecipe> userRecipes = new ArrayList<>();
                userRecipes.add(recipe);

                updateUserRecipes(userRecipes, null, BusConsts.ACTION_EDIT);
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
        MyBus bus = MyBus.getInstance();

        User user = AppStateManager.getInstance().user;

        ArrayList<String> userDietAndAllergensList = user.dietAndAllergensList;

        HashMap<String, String> query = new HashMap<>();
        query.put("q", keyToSearch);
        query.put("start", "0");
        query.put("maxResult", String.valueOf(user.maxOnlineSearchResults));
        query.put("_app_id", AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL);
        query.put("_app_key", AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL);

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
        MyBus bus = MyBus.getInstance();

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
                bus.post(new OnYummlyRecipeDownloadedEvent(false, t));
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
        MyBus bus = MyBus.getInstance();

        Subscriber<YummlyRecipe> subscriber = new Subscriber<YummlyRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnYummlyRecipeDownloadedEvent(false, t));
            }

            @Override
            public void onNext(YummlyRecipe recipe) {
                AppStateManager.getInstance().yummlySearchResult = recipe;
                bus.post(new OnYummlyRecipeDownloadedEvent(true, null));
            }
        };

        Single<YummlyRecipe> getYummlyRecipe = ApiProvider.getGreatRecipesApi().addYummlyRecipe(body);
        getYummlyRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    public void getYummlyRecipeFromGreatRecipesApi(Context context, String resultYummlyId) {
        AppStateManager.getInstance().yummlySearchResult = null;

        MyBus bus = MyBus.getInstance();

        Subscriber<YummlyRecipe> subscriber = new Subscriber<YummlyRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnYummlyRecipeDownloadedEvent(false, t));
            }

            @Override
            public void onNext(YummlyRecipe recipe) {
                if (recipe.yummlyId == null) {
                    // Recipe not found in the GreatRecipes DB
                    downloadRecipeFromYummlyApi(context, resultYummlyId);
                } else {
                    AppStateManager.getInstance().yummlySearchResult = recipe;
                    bus.post(new OnYummlyRecipeDownloadedEvent(true, null));
                }
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
        MyBus bus = MyBus.getInstance();

        Subscriber<UserRecipe> subscriber = new Subscriber<UserRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnDownloadUserRecipeCompletedEvent(false, null, t));
            }

            @Override
            public void onNext(UserRecipe recipe) {
                bus.post(new OnDownloadUserRecipeCompletedEvent(true, recipe, null));
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
     * Update the user's recipes lists after adding/editing/deleting recipes or servings
     */
    public void updateUserRecipes(ArrayList<UserRecipe> userRecipes, ArrayList<YummlyRecipe> yummlyRecipes, int action) {
        MyBus bus = MyBus.getInstance();

        Subscriber<UserResponse> subscriber = new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(action, false, t));
            }

            @Override
            public void onNext(UserResponse userResponse) {
                AppStateManager.getInstance().user = new User(userResponse);
                bus.post(new OnUpdateUserRecipesEvent(action, true, null));
            }
        };

        User currentUser = AppStateManager.getInstance().user;

        HashMap<String, UserRecipe> newUserRecipes = null;
        HashMap<String, YummlyRecipe> newYummlyRecipes = null;
        ArrayList<String> newFavouriteRecipesIds = null;
        HashMap<String, Serving> newServings = null;

        switch (action) {
            case BusConsts.ACTION_EDIT:
            case BusConsts.ACTION_ADD_NEW:
                if (userRecipes != null) {
                    newUserRecipes = new HashMap<>(currentUser.userRecipes);

                    for (UserRecipe recipe : userRecipes) {
                        newUserRecipes.put(recipe._id, recipe);
                    }
                }

                if (yummlyRecipes != null) {
                    newYummlyRecipes = new HashMap<>(currentUser.yummlyRecipes);

                    for (YummlyRecipe recipe : yummlyRecipes) {
                        newYummlyRecipes.put(recipe._id, recipe);
                    }
                }

                break;

            case BusConsts.ACTION_DELETE:
                newFavouriteRecipesIds = new ArrayList<>(currentUser.favouriteRecipesIds);
                newServings = new HashMap<>(currentUser.servings);

                if (userRecipes != null) {
                    newUserRecipes = new HashMap<>(currentUser.userRecipes);

                    for (UserRecipe recipe : userRecipes) {
                        newUserRecipes.remove(recipe._id);
                        if (newFavouriteRecipesIds.contains(recipe._id)) {
                            newFavouriteRecipesIds.remove(recipe._id);
                        }
                        if (newServings.containsKey(recipe._id)) {
                            newServings.remove(recipe._id);
                        }
                    }
                }
                if (yummlyRecipes != null) {
                    for (YummlyRecipe recipe : yummlyRecipes) {
                        newYummlyRecipes = new HashMap<>(currentUser.yummlyRecipes);

                        newYummlyRecipes.remove(recipe._id);
                        if (newFavouriteRecipesIds.contains(recipe._id)) {
                            newFavouriteRecipesIds.remove(recipe._id);
                        }
                        if (newServings.containsKey(recipe._id)) {
                            newServings.remove(recipe._id);
                        }
                    }
                }

                if (newFavouriteRecipesIds.size() == currentUser.favouriteRecipesIds.size()) {
                    // No change in favourites list
                    newFavouriteRecipesIds = null;
                }

                if (newServings.size() == currentUser.servings.size()) {
                    // No change in servings map
                    newServings = null;
                }

                break;

            case BusConsts.ACTION_TOGGLE_FAVOURITE:
                newFavouriteRecipesIds = new ArrayList<>(currentUser.favouriteRecipesIds);

                if (userRecipes != null) {
                    for (UserRecipe recipe : userRecipes) {
                        if (newFavouriteRecipesIds.contains(recipe._id)) {
                            newFavouriteRecipesIds.remove(recipe._id);
                        } else {
                            newFavouriteRecipesIds.add(recipe._id);
                        }
                    }
                }
                if (yummlyRecipes != null) {
                    for (YummlyRecipe recipe : yummlyRecipes) {
                        if (newFavouriteRecipesIds.contains(recipe._id)) {
                            newFavouriteRecipesIds.remove(recipe._id);
                        } else {
                            newFavouriteRecipesIds.add(recipe._id);
                        }
                    }
                }

                break;
        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", currentUser._id);
        if (newUserRecipes != null) {
            body.put("userRecipesIds", newUserRecipes.keySet());
        }
        if (newYummlyRecipes != null) {
            body.put("yummlyRecipesIds", newYummlyRecipes.keySet());
        }
        if (newFavouriteRecipesIds != null) {
            body.put("favouriteRecipesIds", newFavouriteRecipesIds);
        }
        if (newServings != null) {
            body.put("servings", newServings.values());
        }

        Single<UserResponse> updateUser = ApiProvider.getGreatRecipesApi().updateUser(body);

        updateUser
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update the user's diet And allergens lists after adding/editing/deleting recipes
     */
    public void updateUserDietAndAllergensLists(int positionInDrawer) {
        String sPositionInDrawer = String.valueOf(positionInDrawer);

        MyBus bus = MyBus.getInstance();
        User currentUser = AppStateManager.getInstance().user;

        ArrayList<String> dietAndAllergensList = new ArrayList<>(currentUser.dietAndAllergensList);

        int action;
        if (dietAndAllergensList.contains(sPositionInDrawer)) {
            dietAndAllergensList.remove(sPositionInDrawer);
            action = BusConsts.ACTION_DELETE;
        } else {
            dietAndAllergensList.add(sPositionInDrawer);
            action = BusConsts.ACTION_ADD_NEW;
        }

        Subscriber<UserResponse> subscriber = new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserDietAndAllergensEvent(action, false, positionInDrawer, t));
            }

            @Override
            public void onNext(UserResponse userResponse) {
                AppStateManager.getInstance().user = new User(userResponse);
                bus.post(new OnUpdateUserDietAndAllergensEvent(action, true, positionInDrawer, null));
            }
        };

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", currentUser._id);
        body.put("dietAndAllergensList", dietAndAllergensList);

        Single<UserResponse> updateUser = ApiProvider.getGreatRecipesApi().updateUser(body);

        updateUser
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Clear all the user's recipes lists
     */
    public void clearAllLists(int action) {
        MyBus bus = MyBus.getInstance();

        Subscriber<UserResponse> subscriber = new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(action, false, t));
            }

            @Override
            public void onNext(UserResponse userResponse) {
                AppStateManager.getInstance().user = new User(userResponse);
                bus.post(new OnUpdateUserRecipesEvent(action, true, null));
            }
        };

        HashMap<String, Object> body = new HashMap<>();
        body.put("_id", AppStateManager.getInstance().user._id);
        body.put("servings", new HashMap<>());
        body.put("userRecipes", new HashMap<>());
        body.put("yummlyRecipes", new HashMap<>());
        body.put("favouriteRecipesIds", new ArrayList<>());

        Single<UserResponse> updateUser = ApiProvider.getGreatRecipesApi().updateUser(body);

        updateUser
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}