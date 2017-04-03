/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hanoch.greatrecipes;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.retrofit.YummlyService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class GreatRecipesApplication extends Application {
    private Tracker mTracker;
    private RequestQueue requestQueue;
    private DbManager dbManager;
    private YummlyService yummlyService;
    private Retrofit retrofit;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public RequestQueue getVolleyRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit =  new Retrofit.Builder()
                    .baseUrl(AppConsts.ApiAccess.YUMMLY_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public YummlyService getYummlyService() {
        if (yummlyService == null) {
            yummlyService = getRetrofit().create(YummlyService.class);
        }
        return yummlyService;
    }

    public DbManager getDbManager() {

        if (dbManager == null) {
            dbManager = new DbManager(this);
        }

        return dbManager;
    }
}
