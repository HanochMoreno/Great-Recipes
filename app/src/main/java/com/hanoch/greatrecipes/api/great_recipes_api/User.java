package com.hanoch.greatrecipes.api.great_recipes_api;


import java.util.ArrayList;

public class User {

    public String _id;
    public String author;
    public String email;
    public String password;
    public ArrayList<String> dietList;
    public ArrayList<String> allergensList;
    public ArrayList<String> userRecipesIds;
    public ArrayList<String> yummlyRecipesIds;
    public ArrayList<String> favouriteRecipesIds;
    public boolean isPremium;
    public int onlineSearchesCount;
}
