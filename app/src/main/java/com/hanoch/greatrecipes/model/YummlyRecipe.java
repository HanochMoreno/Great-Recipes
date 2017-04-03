package com.hanoch.greatrecipes.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 Represents one result of a recipe online search.
 */

public class YummlyRecipe implements Serializable {

    @SerializedName("name")
    public String title;

    @SerializedName("numberOfServings")
    public int yield;

    @SerializedName("totalTimeInSeconds")
    public int time;

    @SerializedName("ingredientLines")
    public ArrayList<String> ingredients;

    @SerializedName("nutritionEstimates")
    public LinkedTreeMap[] nutritions;

    public LinkedTreeMap attributes;
    public LinkedTreeMap source;
}
