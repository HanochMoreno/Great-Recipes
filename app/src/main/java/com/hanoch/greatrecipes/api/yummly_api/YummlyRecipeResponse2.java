package com.hanoch.greatrecipes.api.yummly_api;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 Represents one result of a recipe online search.
 */

public class YummlyRecipeResponse2 implements Serializable {

    @SerializedName("id")
    public String yummlyId;

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

//-------------------------------------------------------------------------------------------------

    public HashMap<String, Object> generateBody(Context context) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("yummlyId", this.yummlyId);
        body.put("recipeTitle", this.title);
        body.put("yield", this.yield);
        body.put("cookingTime", this.time);
        body.put("author", AppConsts.Category.NO_INFO);
        body.put("url", "");
        body.put("imageUrl", ""); // ToDO: HANDLE!!
        body.put("ingredientsList", this.ingredients);
        body.put("categoriesList", new ArrayList<String>());
        body.put("energy", "0");
        body.put("imageByteArrayAsString", "");

        if (this.source != null) {
            if (this.source.containsKey("sourceDisplayName")) {
                body.put("author", this.source.get("sourceDisplayName"));

            }

            if (this.source.containsKey("sourceRecipeUrl")) {
                body.put("url", this.source.get("sourceRecipeUrl"));
            }
        }

        if (this.attributes != null) {
            if (this.attributes.containsKey("course")) {
                body.put("categoriesList", this.attributes.get("course"));
            }
        }

        if (this.nutritions != null) {
            for (HashMap<String, Object> nutrition : this.nutritions) {
                if (nutrition.containsKey("attribute") && nutrition.get("attribute").equals("ENERC_KCAL")) {
                    double calories = (double) nutrition.get("value");
                    body.put("energy", String.valueOf((int) calories));
                    break;
                }
            }
        }

        String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + this.yummlyId;
        Bitmap recipeImage = ImageStorage.getImageBitmapByName(context, imageName);

        if (recipeImage != null) {
            body.put("imageByteArrayAsString", ImageStorage.convertBitmapToByteArrayAsString(recipeImage));
        }

        return body;
    }

}
