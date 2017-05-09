package com.hanoch.greatrecipes.database;


import android.content.Context;
import android.graphics.Bitmap;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.yummly_api.YummlyRecipeResponse2;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnDownloadUserRecipeCompletedEvent;
import com.hanoch.greatrecipes.bus.OnYummlyRecipeDownloadedEvent;
import com.hanoch.greatrecipes.bus.OnLoginEvent;
import com.hanoch.greatrecipes.bus.OnRegisterEvent;
import com.hanoch.greatrecipes.bus.OnUpdateServingsListCompletedEvent;
import com.hanoch.greatrecipes.bus.OnUpdateUserRecipesEvent;
import com.hanoch.greatrecipes.model.ApiProvider;
import com.hanoch.greatrecipes.model.Serving2;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
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
    public void register(String email, String userName, String password) {
        MyBus bus = MyBus.getInstance();

        Subscriber<User> subscriber = new Subscriber<User>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnRegisterEvent(false, null, t));
            }

            @Override
            public void onNext(User user) {
                AppStateManager.getInstance().user = user;
                bus.post(new OnRegisterEvent(true, user, null));
            }
        };

        Single<User> register =
                ApiProvider.getGreatRecipesApi().register(email, userName, password);

        register
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * login a user
     */
    public void login(String userName, String password) {
        MyBus bus = MyBus.getInstance();

        Subscriber<User> subscriber = new Subscriber<User>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnLoginEvent(false, null, t));
            }

            @Override
            public void onNext(User user) {
                AppStateManager.getInstance().user = user;
                bus.post(new OnLoginEvent(true, user, null));
            }
        };

        Single<User> login =
                ApiProvider.getGreatRecipesApi().login(userName, password);

        login
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update the user servingsList to database
     */
    public void updateUserServingsMap(ArrayList<Serving2> servingsList, int action) {
        MyBus bus = MyBus.getInstance();
        User currentUser = AppStateManager.getInstance().user;

        Subscriber<User> subscriber = new Subscriber<User>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateServingsListCompletedEvent(action, false, null, t));
            }

            @Override
            public void onNext(User user) {
                AppStateManager.getInstance().user = user;
                bus.post(new OnUpdateServingsListCompletedEvent(action, true, user.servings, null));
            }
        };

        HashMap<String, Serving2> currentServings = new HashMap<>(currentUser.servings);
        switch (action) {
            case BusConsts.ACTION_ADD_NEW:
                for (Serving2 serving : servingsList) {
                    currentServings.put(serving.servingId, serving);
                }
                break;

            case BusConsts.ACTION_DELETE:
                for (Serving2 serving : servingsList) {
                    currentServings.remove(serving.servingId);
                }
                break;
        }

        User user = new User();
        user._id = currentUser._id;
        user.servings = currentServings;

        Single<User> updateUser =
                ApiProvider.getGreatRecipesApi().updateUser(user);

        updateUser
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Add a new user recipe to database
     *
     * @param userRecipe
     */
    public void addUserRecipe(UserRecipe userRecipe) {
        MyBus bus = MyBus.getInstance();

        Subscriber<UserRecipe> subscriber = new Subscriber<UserRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(BusConsts.ACTION_ADD_NEW, false, null, t));
            }

            @Override
            public void onNext(UserRecipe recipe) {
                ArrayList<UserRecipe> userRecipes = new ArrayList<>();
                userRecipes.add(recipe);

                updateUserRecipes(userRecipes, null, BusConsts.ACTION_ADD_NEW);
            }
        };

        Single<UserRecipe> addUserRecipe =
                ApiProvider.getGreatRecipesApi().addUserRecipe(userRecipe);

        addUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update exist user recipe to database
     *
     * @param userRecipe
     */
    public void updateUserRecipe(UserRecipe userRecipe) {
        MyBus bus = MyBus.getInstance();

        Subscriber<UserRecipe> subscriber = new Subscriber<UserRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(BusConsts.ACTION_EDIT, false, null, t));
            }

            @Override
            public void onNext(UserRecipe recipe) {
                ArrayList<UserRecipe> userRecipes = new ArrayList<>();
                userRecipes.add(recipe);

                updateUserRecipes(userRecipes, null, BusConsts.ACTION_EDIT);
            }
        };


        Single<UserRecipe> updateUserRecipe =
                ApiProvider.getGreatRecipesApi().updateUserRecipe(userRecipe);

        updateUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * http://api.yummly.com/v1/api/recipe/French-Onion-Soup-1292648?_app_id=418b707a&_app_key=249ec501a990bd7d5fa5dd5218bf7e14
     *
     * @param resultYummlyId
     */
    public void downloadRecipeFromYummlyApi(Context context, String resultYummlyId) {
        MyBus bus = MyBus.getInstance();

        String appId = AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL;
        String appKey = AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL;
        String requiredPictureValue = "true";

        try {
            appId = URLEncoder.encode(AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL, "utf-8");
            appKey = URLEncoder.encode(AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL, "utf-8");
            requiredPictureValue = URLEncoder.encode("true", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HashMap<String, String> query = new HashMap<>();
        query.put("_app_id", appId);
        query.put("_app_key", appKey);
        query.put("requirePictures", requiredPictureValue);

        Subscriber<YummlyRecipe> subscriber = new Subscriber<YummlyRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnYummlyRecipeDownloadedEvent(false, null, t));
            }

            @Override
            public void onNext(YummlyRecipe recipe) {
                AppStateManager.getInstance().user.yummlyRecipes.put(recipe._id, recipe);
                bus.post(new OnYummlyRecipeDownloadedEvent(true, recipe, null));
            }
        };

        Single<YummlyRecipeResponse2> getYummlyRecipe =
                ApiProvider.getYummlyApi().getYummlyRecipe(resultYummlyId, query);

        Single.defer((Func0<Single<YummlyRecipeResponse2>>) () -> getYummlyRecipe)
                .subscribeOn(Schedulers.io())
                .map(recipeResponse -> generateRecipeResult(context, recipeResponse))
                .flatMap(yummlyRecipe -> ApiProvider.getGreatRecipesApi().addYummlyRecipe(yummlyRecipe))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    private YummlyRecipe generateRecipeResult(Context context, YummlyRecipeResponse2 yummlyRecipe) {
        YummlyRecipe searchResult = new YummlyRecipe();

        searchResult.yummlyId = yummlyRecipe.yummlyId;

        searchResult.recipeTitle = yummlyRecipe.title;
        searchResult.yield = yummlyRecipe.yield;
        searchResult.cookingTime = yummlyRecipe.time;

        searchResult.author = AppConsts.Category.NO_INFO;
        searchResult.url = AppConsts.Category.NO_INFO;

        if (yummlyRecipe.source != null) {
            if (yummlyRecipe.source.containsKey("sourceDisplayName")) {
                searchResult.author = yummlyRecipe.source.get("sourceDisplayName");
            }

            if (yummlyRecipe.source.containsKey("sourceRecipeUrl")) {
                searchResult.url = yummlyRecipe.source.get("sourceRecipeUrl");
            }
        }

        searchResult.ingredientsList = yummlyRecipe.ingredients;

        ArrayList<String> categories = new ArrayList<>();
        if (yummlyRecipe.attributes != null) {
            categories = yummlyRecipe.attributes.get("course");
        }
        searchResult.categoriesList = categories;

        searchResult.energy = -1;
        if (yummlyRecipe.nutritions != null) {
            for (HashMap<String, Object> nutrition : yummlyRecipe.nutritions) {
                if (nutrition.containsKey("attribute") && nutrition.get("attribute").equals("ENERC_KCAL")) {
                    double calories = (double) nutrition.get("value");
                    searchResult.energy = (int) calories;
                    break;
                }
            }
        }

        String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + yummlyRecipe.yummlyId;
        Bitmap recipeImage = ImageStorage.getImageBitmapByName(context, imageName);

        if (recipeImage == null) {
            searchResult.imageByteArrayAsString = "";
            searchResult.thumbnailByteArrayAsString = "";
        } else {
            searchResult.imageByteArrayAsString = ImageStorage.convertBitmapToByteArrayAsString(recipeImage);
            File imageFile = ImageStorage.getImageFileByName(context, imageName);
            if (imageFile != null) {
                Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(imageFile.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
                searchResult.thumbnailByteArrayAsString = ImageStorage.convertBitmapToByteArrayAsString(thumbnail);
                thumbnail.recycle();
            }
            recipeImage.recycle();
        }

        return searchResult;
    }

//-------------------------------------------------------------------------------------------------

    public void getYummlyRecipeFromGreatRecipesApi(Context context, String resultYummlyId, boolean isYummlyId) {
        MyBus bus = MyBus.getInstance();

        String tempEncoded = resultYummlyId;
        try {
            tempEncoded = URLEncoder.encode(resultYummlyId, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        final String encoded = tempEncoded;

        Subscriber<YummlyRecipe> subscriber = new Subscriber<YummlyRecipe>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnYummlyRecipeDownloadedEvent(false, null, t));
            }

            @Override
            public void onNext(YummlyRecipe recipe) {
                if (recipe == null) {
                    if (isYummlyId) {
                        downloadRecipeFromYummlyApi(context, encoded);
                    }
                } else {
                    AppStateManager.getInstance().user.yummlyRecipes.put(recipe._id, recipe);
                    bus.post(new OnYummlyRecipeDownloadedEvent(true, recipe, null));
                }
            }
        };

        Single<YummlyRecipe> getYummlyRecipe;
        if (isYummlyId) {
            getYummlyRecipe = ApiProvider.getGreatRecipesApi().getYummlyRecipeByYummlyId(resultYummlyId);
        } else {
            getYummlyRecipe = ApiProvider.getGreatRecipesApi().getYummlyRecipe(resultYummlyId);
        }

        getYummlyRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

//    /**
//     * Update the user yummlyRecipesIds after adding a new Yummly recipe
//     *
//     * @param recipesList: The Yummly recipe list (in the Great Recipes DB)
//     */
//    public void updateUserYummlyRecipes(ArrayList<YummlyRecipe> recipesList, int action) {
//        MyBus bus = MyBus.getInstance();
//        User currentUser = AppStateManager.getInstance().user;
//
//        User user = new User();
//        user._id = currentUser._id;
//        user.yummlyRecipes = new HashMap<>(currentUser.yummlyRecipes);
//
//        Subscriber<User> subscriber = new Subscriber<User>() {
//            @Override
//            public void onCompleted() {
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                bus.post(new OnUpdateUserYummlyRecipesEvent(action, false, null, t));
//            }
//
//            @Override
//            public void onNext(User user) {
//                AppStateManager.getInstance().user = user;
//                bus.post(new OnUpdateUserYummlyRecipesEvent(action, true, user.yummlyRecipes, null));
//            }
//        };
//
//        HashMap<String, YummlyRecipe> currentRecipes = new HashMap<>(currentUser.yummlyRecipes);
//        switch (action) {
//            case BusConsts.ACTION_ADD_NEW:
//                for (YummlyRecipe recipe : recipesList) {
//                    currentRecipes.put(recipe._id, recipe);
//                }
//                break;
//
//            case BusConsts.ACTION_DELETE:
//                for (YummlyRecipe recipe : recipesList) {
//                    currentRecipes.remove(recipe._id);
//                }
//                break;
//        }
//
//        Single<User> updateUser =
//                ApiProvider.getGreatRecipesApi().updateUser(user);
//
//        updateUser
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscriber);
//    }

//-------------------------------------------------------------------------------------------------

    public void getUserRecipeFromGreatRecipesApi(String recipeId) {
        MyBus bus = MyBus.getInstance();

        try {
            recipeId = URLEncoder.encode(recipeId, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }

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

        Single<UserRecipe> getUserRecipe =
                ApiProvider.getGreatRecipesApi().getUserRecipe(recipeId);

        getUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    /**
     * Update the user's recipes lists after adding/editing/deleting recipes
     */
    public void updateUserRecipes(ArrayList<UserRecipe> userRecipes, ArrayList<YummlyRecipe> yummlyRecipes, int action) {
        MyBus bus = MyBus.getInstance();
        User currentUser = AppStateManager.getInstance().user;

        User user = new User();
        user._id = currentUser._id;
        user.userRecipes = new HashMap<>(currentUser.userRecipes);
        user.yummlyRecipes = new HashMap<>(currentUser.yummlyRecipes);
        user.favouriteRecipesIds = new ArrayList<>(currentUser.favouriteRecipesIds);

        Subscriber<User> subscriber = new Subscriber<User>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                bus.post(new OnUpdateUserRecipesEvent(action, false, null, t));
            }

            @Override
            public void onNext(User user) {
                AppStateManager.getInstance().user = user;
                bus.post(new OnUpdateUserRecipesEvent(action, true, user, null));
            }
        };

        switch (action) {
            case BusConsts.ACTION_EDIT:
            case BusConsts.ACTION_ADD_NEW:
                if (userRecipes != null) {
                    for (UserRecipe recipe : userRecipes) {
                        user.userRecipes.put(recipe._id, recipe);
                    }
                }
                if (yummlyRecipes != null) {
                    for (YummlyRecipe recipe : yummlyRecipes) {
                        user.yummlyRecipes.put(recipe._id, recipe);
                    }
                }

                user.favouriteRecipesIds = null;
                break;

            case BusConsts.ACTION_DELETE:
                user.servings = new HashMap<>(currentUser.servings);

                if (userRecipes != null) {
                    for (UserRecipe recipe : userRecipes) {
                        user.userRecipes.remove(recipe._id);
                        if (user.favouriteRecipesIds.contains(recipe._id)) {
                            user.favouriteRecipesIds.remove(recipe._id);
                        }
                        if (user.servings.containsKey(recipe._id)) {
                            user.servings.remove(recipe._id);
                        }
                    }
                }
                if (yummlyRecipes != null) {
                    for (YummlyRecipe recipe : yummlyRecipes) {
                        user.yummlyRecipes.remove(recipe._id);
                        if (user.favouriteRecipesIds.contains(recipe._id)) {
                            user.favouriteRecipesIds.remove(recipe._id);
                        }
                        if (user.servings.containsKey(recipe._id)) {
                            user.servings.remove(recipe._id);
                        }
                    }
                }

                if (user.favouriteRecipesIds.size() == currentUser.favouriteRecipesIds.size()) {
                    // No change in favourites list
                    user.favouriteRecipesIds = null;
                }

                if (user.servings.size() == currentUser.servings.size()) {
                    // No change in servings map
                    user.servings = null;
                }

                break;

            case BusConsts.ACTION_TOGGLE_FAVOURITE:
                if (userRecipes != null) {
                    for (UserRecipe recipe : userRecipes) {
                        if (user.favouriteRecipesIds.contains(recipe._id)) {
                            user.favouriteRecipesIds.remove(recipe._id);
                        } else {
                            user.favouriteRecipesIds.add(recipe._id);
                        }
                    }
                }
                if (yummlyRecipes != null) {
                    for (YummlyRecipe recipe : yummlyRecipes) {
                        if (user.favouriteRecipesIds.contains(recipe._id)) {
                            user.favouriteRecipesIds.remove(recipe._id);
                        } else {
                            user.favouriteRecipesIds.add(recipe._id);
                        }
                    }
                }

                user.userRecipes = null;
                user.yummlyRecipes = null;

                break;
        }

        Single<User> updateUser =
                ApiProvider.getGreatRecipesApi().updateUser(user);

        updateUser
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------

}