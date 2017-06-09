package com.hanoch.greatrecipes;


import android.Manifest;
import android.util.SparseArray;

import com.hanoch.greatrecipes.model.AllergenAndDiet;

public class AppConsts {

    public static final int REQ_CODE_PURCHASE = 1000;
    public static final String NOTIFICATION_RECEIVER_FILTER = "NotificationReceived";

//-------------------------------------------------------------------------------------------------

    public static final AllergenAndDiet[] ALLERGENS_AND_DIET_LIST = {
            // Diet
            new AllergenAndDiet(true, 2, AppConsts.Filters.VEGAN, "390^Vegan"),
            new AllergenAndDiet(true, 3, AppConsts.Filters.VEGETARIAN, "387^Lacto-ovo vegetarian"),
            new AllergenAndDiet(true, 4, AppConsts.Filters.PALEO, "403^Paleo"),

            // Allergens
            new AllergenAndDiet(false, 6, AppConsts.Filters.DAIRY_FREE, "396^Dairy-Free"),
            new AllergenAndDiet(false, 7, AppConsts.Filters.EGG_FREE, "397^Egg-Free"),
            new AllergenAndDiet(false, 8, AppConsts.Filters.GLUTEN_FREE, "393^Gluten-Free"),
            new AllergenAndDiet(false, 9, AppConsts.Filters.PEANUT_FREE, "394^Peanut-Free"),
            new AllergenAndDiet(false, 10, AppConsts.Filters.SEAFOOD_FREE, "398^Seafood-Free"),
            new AllergenAndDiet(false, 11, AppConsts.Filters.SESAME_FREE, "399^Sesame-Free"),
            new AllergenAndDiet(false, 12, AppConsts.Filters.SOY_FREE, "400^Soy-Free"),
            new AllergenAndDiet(false, 13, AppConsts.Filters.TREE_NUT_FREE, "395^Tree Nut-Free"),
            new AllergenAndDiet(false, 14, AppConsts.Filters.WHEAT_FREE, "392^Wheat-Free"),
    };

//-------------------------------------------------------------------------------------------------

    public class Fragments {

        public static final String RECIPE_REVIEW = "recipeReviewFragment";
        public static final String EDIT_RECIPE = "editRecipeFragment";
        public static final String CATEGORIES_CHOOSER = "categoriesChooserFragment";
        public static final String WEB_VIEW = "webViewFragment";
        public static final String TABS = "tabsFragment";
        public static final String ONLINE_SEARCH_RESULTS = "onlineSearchResultsFragment";
        public static final String PREFERENCES = "preferencesFragment";
        public static final String SEARCH_IN_LISTS = "searchInListsFragment";
        public static final String SERVINGS_LIST = "servingsListFragment";
    }

//-------------------------------------------------------------------------------------------------

    public class Actions {

        public static final int NO_ACTION = 0;

        public static final int ACTION_REGISTER = 1;
        public static final int ACTION_LOGIN = 2;
        public static final int ACTION_FORGOT_PASSWORD = 3;

        public static final int ADD_NEW_USER_RECIPE = 4;
        public static final int EDIT_USER_RECIPE = 5;

        public static final int REVIEW_YUMMLY_RECIPE = 6;
        public static final int REVIEW_USER_RECIPE = 7;
        public static final int REVIEW_SHARED_USER_RECIPE = 8;
        public static final int REVIEW_SHARED_YUMMLY_RECIPE = 9;
        public static final int REVIEW_YUMMLY_ONLINE = 10;
        public static final int REVIEW_SERVING = 11;

        public static final int ADD_SERVING_FROM_LISTS = 12;
        public static final int ADD_SERVING_FROM_YUMMLY = 13;

        public static final int DOWNLOAD_SHARED_YUMMLY_RECIPE = 14;
        public static final int DOWNLOAD_NEW_YUMMLY_RECIPE = 15;
    }

//-------------------------------------------------------------------------------------------------

    public class Regex {

        public static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        public static final String USERNAME_PATTERN =
                "abcdefghijklmnopqrstuvwxyz. _ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890";

        public static final String PASSWORD_PATTERN =
                "abcdefghijklmnopqrstuvwxyz.!_ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890";

    }

//-------------------------------------------------------------------------------------------------

    public class Extras {

        public static final String RECIPE_ID = "recipeId";
        public static final String IS_USER_RECIPE = "isUserRecipe";
    }

//-------------------------------------------------------------------------------------------------

    public class ToolbarButtons {

        public static final int SAVE = R.id.action_save;
        public static final int EDIT = R.id.action_edit;
        public static final int DELETE = R.id.action_delete;
        public static final int ADD_TO_FAVOURITES = R.id.action_addToFavourites;
        public static final int REMOVE_FROM_FAVOURITES = R.id.action_removeFromFavourites;
        public static final int SHARE = R.id.action_share;
        public static final int ADD_SERVING = R.id.action_addServing;
        public static final int SEARCH = R.id.action_search;
        public static final int OK = R.id.action_ok;
        public static final int CLEAR_SERVING_LIST = R.id.action_clearServingsList;
        public static final int CLOSE_WEBVIEW = R.id.action_closeWebview;
        public static final int ADD_TO_LIST = R.id.action_addToList;
        public static final int REFRESH = R.id.action_refresh;
    }


//-------------------------------------------------------------------------------------------------

    public class ToolbarColor {

        public static final int NO_CHANGE = -1;
        public static final int ACCENT = -49023; // pink
        public static final int PRIMARY = -12627531; // blue
    }

//-------------------------------------------------------------------------------------------------

    public class ListType {

        public static final int MY_OWN_RECIPES = 1;
        public static final int ONLINE_RECIPES = 2;
        public static final int FAVOURITES_RECIPES = 3;
    }

//-------------------------------------------------------------------------------------------------

    public static class Category {

        public static final String NO_INFO = "No info.";

        public static final String MAIN_DISHES = "Main Dishes";
        public static final String DESSERTS = "Desserts";
        public static final String SIDE_DISHES = "Side Dishes";
        public static final String LUNCH_AND_SNACKS = "Lunch and Snacks";
        public static final String APPETIZERS = "Appetizers";
        public static final String SALADS = "Salads";
        public static final String BREADS = "Breads";
        public static final String BREAKFAST_AND_BRUNCH = "Breakfast and Brunch";
        public static final String SOUPS = "Soups";
        public static final String BEVERAGES = "Beverages";
        public static final String CONDIMENTS_AND_SAUCES = "Condiments and Sauces";
        public static final String COCKTAILS = "Cocktails";
    }

//-------------------------------------------------------------------------------------------------

    public static class Serving {

        public static final String MAIN_DISH = "Main Dish";
        public static final String DESSERT = "Dessert";
        public static final String SIDE_DISH = "Side Dish";
        public static final String APPETIZER = "Appetizer";
        public static final String SALAD = "Salad";
        public static final String PASTRY = "Pastry";
        public static final String BEVERAGE = "Beverage";
        public static final String SAUCE = "Sauce";
    }

//-------------------------------------------------------------------------------------------------

    private static class Filters {

        private static final String VEGAN = "Vegan";
        private static final String VEGETARIAN = "Vegetarian";
        private static final String PALEO = "Paleo";
        private static final String DAIRY_FREE = "Dairy-free";
        private static final String EGG_FREE = "Egg-free";
        private static final String GLUTEN_FREE = "Gluten-free";
        private static final String PEANUT_FREE = "Peanut-free";
        private static final String SEAFOOD_FREE = "Seafood-free";
        private static final String SESAME_FREE = "Sesame-free";
        private static final String SOY_FREE = "Soy-free";
        private static final String TREE_NUT_FREE = "Tree-nut-free";
        private static final String WHEAT_FREE = "Wheat-free";
    }

//-------------------------------------------------------------------------------------------------

    public static class SharedPrefs {

        public static final String VEGAN = "390^Vegan";
        public static final String VEGETARIAN = "387^Lacto-ovo vegetarian";
        public static final String PALEO = "403^Paleo";
        public static final String DAIRY_FREE = "396^Dairy-Free";
        public static final String EGG_FREE = "397^Egg-Free";
        public static final String GLUTEN_FREE = "393^Gluten-Free";
        public static final String PEANUT_FREE = "394^Peanut-Free";
        public static final String SEAFOOD_FREE = "398^Seafood-Free";
        public static final String SESAME_FREE = "399^Sesame-Free";
        public static final String SOY_FREE = "400^Soy-Free";
        public static final String TREE_NUT_FREE = "395^Tree Nut-Free";
        public static final String WHEAT_FREE = "392^Wheat-Free";

        public static final String EMAIL = "email";
        public static final String USER_NAME = "username";
        public static final String PASSWORD = "password";
        public static final String MAIN_MENU_DISPLAY = "mainMenuDisplay";
        public static final String MAX_ONLINE_SEARCH_RESULTS = "maxOnlineSearchResults";
        public static final String DELETE_ALL = "deleteAll";

        public static final String COLORFUL = "Colorful";
        public static final String SIMPLE = "Simple";
        public static final String PREMIUM_ACCESS = "premiumAccess";
        public static final String CONTACT_US = "contactUs";
        public static final String VIBRATION = "vibration";

        public static final String SHOW_FAVOURITES_DIALOG = "showFavouritesDialogAgain";
        public static final String FAVOURITES_DIALOG_WAS_SHOWN = "favouritesDialogWasShown";

        public static final int RATE_US_DIALOG_COUNTER_REPEAT = 6;
        public static final int RATE_US_DIALOG_NEVER_SHOW_AGAIN = -1;
        public static final String RATE_US_DIALOG_COUNTER = "rateUsDialogCounter";
    }

//-------------------------------------------------------------------------------------------------

    public class ApiAccess {

        public static final String YUMMLY_BASE_URL = "http://api.yummly.com/v1/api/";
        public static final String GREAT_RECIPES_BASE_URL = "http://10.0.0.4:3000";
        public static final String YUMMLY_KEY_SEARCH = YUMMLY_BASE_URL + "recipes";
        public static final String YUMMLY_RECIPE_SEARCH = YUMMLY_BASE_URL + "recipe/";
    }

//-------------------------------------------------------------------------------------------------

    public class Images {

        public static final String RESULT_IMAGE_PREFIX = "result_image_";
    }

//-------------------------------------------------------------------------------------------------

    public class Analytics {

        public static final String CATEGORY_PREMIUM_HANDLING = "Premium Handling";
        public static final String CATEGORY_LOGIN = "Login";
        public static final String CATEGORY_REGISTER = "Register";
        public static final String CATEGORY_SHARE_RECIPE = "CATEGORY_SHARE_RECIPE";
        public static final String CATEGORY_FORGOT_PASSWORD = "Forgot Password";
        public static final String CATEGORY_RATE_US = "Rate Us";
        public static final String CATEGORY_PREFERENCES = "Preferences";
    }

//-------------------------------------------------------------------------------------------------

    public class Permissions {

        public static final int PERMISSION_REQ_CODE_WRITE_STORAGE = 101;
        public static final int PERMISSION_REQ_CODE_READ_STORAGE = 102;
        public static final int PERMISSION_INTERNET = 103;
    }

    public static SparseArray<String> permissionsMap = new SparseArray<String>() {
        {
            put(Permissions.PERMISSION_REQ_CODE_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            put(Permissions.PERMISSION_REQ_CODE_READ_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    };

}




