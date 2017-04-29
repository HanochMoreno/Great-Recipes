package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.YummlyRecipe;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * All the user's recipes as received from GreatRecipesApi.
 */

public class UserRecipesResponse {

    public HashMap<String, YummlyRecipe> yummlyRecipesMap;
    public HashMap<String, UserRecipe> userRecipesMap;
    public ArrayList<String> favouriteRecipesIds;
}
