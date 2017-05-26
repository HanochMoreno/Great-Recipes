package com.hanoch.greatrecipes.model;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;

import java.util.ArrayList;

public class RecipesResponse {
    public ArrayList<UserRecipe> userRecipes;
    public ArrayList<YummlyRecipe> yummlyRecipes;
    public ArrayList<String> favouriteRecipesIds;
}
