package com.hanoch.greatrecipes.model;

import com.hanoch.greatrecipes.api.great_recipes_api.GreatRecipesApi;
import com.hanoch.greatrecipes.api.yummly_api.YummlyApi;

public abstract class ApiProvider {
    private static GreatRecipesApi greatRecipesApi;
    private static YummlyApi yummlyApi;

    public static GreatRecipesApi getGreatRecipesApi() {
        if (greatRecipesApi == null) {
            greatRecipesApi = MyRetrofit.getInstance().create(GreatRecipesApi.class);
        }
        return greatRecipesApi;
    }

    public static YummlyApi getYummlyApi() {
        if (yummlyApi == null) {
            yummlyApi = MyRetrofit.getInstance().create(YummlyApi.class);
        }
        return yummlyApi;
    }

}
