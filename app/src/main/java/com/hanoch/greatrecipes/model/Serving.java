package com.hanoch.greatrecipes.model;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;

public class Serving {
    public String servingId;
    public String recipeId;
    public UserRecipe userRecipe;
    public YummlyRecipe yummlyRecipe;
    public String servingType;
    public boolean isUserRecipe;
}
