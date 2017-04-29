package com.hanoch.greatrecipes.api.yummly_api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 Represents one result of a recipe online search.
 */

public class YummlyRecipeResponse2 implements Serializable {

    @SerializedName("name")
    public String title;

    @SerializedName("numberOfServings")
    public int yield;

    @SerializedName("totalTimeInSeconds")
    public int time;

    @SerializedName("ingredientLines")
    public ArrayList<String> ingredients;

    @SerializedName("nutritionEstimates")
    public HashMap<String, Object>[] nutritions;

    public HashMap<String, ArrayList<String>> attributes;
    public HashMap<String, String> source;
}
