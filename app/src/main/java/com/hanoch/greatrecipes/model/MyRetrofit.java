package com.hanoch.greatrecipes.model;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyRetrofit {

    private static Retrofit instance;

    private MyRetrofit(){}

    public static Retrofit getInstance() {

        if (instance == null) {

            instance = new Retrofit.Builder()
//                    .baseUrl(AppConsts.ApiAccess.YUMMLY_BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return instance;
    }
}
