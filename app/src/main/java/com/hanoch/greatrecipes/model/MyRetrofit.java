package com.hanoch.greatrecipes.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hanoch.greatrecipes.AppConsts;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyRetrofit {

    private static Retrofit instance;

    private MyRetrofit(){}

    public static Retrofit getInstance() {

        if (instance == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(AppConsts.ApiAccess.GREAT_RECIPES_BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return instance;
    }
}
