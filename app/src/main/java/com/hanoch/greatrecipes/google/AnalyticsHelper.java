package com.hanoch.greatrecipes.google;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hanoch.greatrecipes.BuildConfig;
import com.hanoch.greatrecipes.GreatRecipesApplication;


public class AnalyticsHelper {

    private static boolean ignoreInDebugMode = true;

    public static void setScreenName(Activity activity) {
        setScreenName(activity, activity.getClass().getSimpleName());
    }

    public static void setScreenName(android.app.Fragment fragment) {
        setScreenName(fragment.getActivity(), fragment.getClass().getSimpleName());
    }

    public static void setScreenName(Fragment fragment) {
        setScreenName(fragment.getActivity(), fragment.getClass().getSimpleName());
    }

    public static void setScreenName(Activity activity, String screenName) {

        if (BuildConfig.DEBUG) {
            if (ignoreInDebugMode) return;
            else screenName = "DEBUG " + screenName;
        }

        // Obtain the shared Tracker instance.
        GreatRecipesApplication application = (GreatRecipesApplication) activity.getApplication();
        Tracker mTracker = application.getDefaultTracker();

        Log.i(screenName, "Setting screen name: " + screenName);
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendEvent(android.app.Fragment fragment, String category, String action) {
        sendEvent(fragment.getActivity(), category, action);
    }

    public static void sendEvent(android.app.Fragment fragment, String category, String action, String label) {
        sendEvent(fragment.getActivity(), category, action, label);
    }

//    public static void sendEvent(Fragment fragment, String category, String action) {
//        sendEvent(fragment.getActivity(), category, action);
//    }

    public static void sendEvent(Fragment fragment, String category, String action, String label) {
        sendEvent(fragment.getActivity(), category, action, label);
    }

    public static void sendEvent(Activity activity, String category, String action) {
        sendEvent(activity, category, action, null);
    }

    public static void sendEvent(Activity activity, String category, String action, String label) {

        if (BuildConfig.DEBUG && ignoreInDebugMode) return;

        GreatRecipesApplication application = (GreatRecipesApplication) activity.getApplication();
        Tracker mTracker = application.getDefaultTracker();

        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();

        builder.setCategory(category).setAction(action);
        if (label != null) {
            builder.setLabel(label);
        }

        mTracker.send(builder.build());
    }
}

