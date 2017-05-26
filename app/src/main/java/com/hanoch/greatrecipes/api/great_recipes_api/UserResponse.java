package com.hanoch.greatrecipes.api.great_recipes_api;


import com.hanoch.greatrecipes.model.RecipesResponse;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.model.Preferences;

import java.util.ArrayList;

public class UserResponse {

    public String _id;

    public Preferences preferences;
    public RecipesResponse recipes;

    public ArrayList<Serving> servings;

    public boolean isPremium;
    public int onlineSearchesCount;
}
