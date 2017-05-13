package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.model.Serving;
import java.util.ArrayList;

public class UserResponse {

    public String _id;
    public String username;
    public String email;
    public String password;
    public ArrayList<String> dietAndAllergensList;
    public ArrayList<UserRecipe> userRecipes;
    public ArrayList<YummlyRecipe> yummlyRecipes;
    public ArrayList<String> favouriteRecipesIds;
    public ArrayList<Serving> servings;
    public boolean isPremium;
    public int maxOnlineSearchResults;
    public int onlineSearchesCount;
}
