package com.hanoch.greatrecipes.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;

/**
 Represents one result of a recipe online search.
 */

public class ThinRecipeSearchResult implements Serializable{

    @SerializedName("id")
    public String yummlyId;

    @SerializedName("recipeName")
    public String title;

    @SerializedName("imageUrlsBySize")
    public LinkedTreeMap<String, String> imageUrl;
}
