package com.hanoch.greatrecipes.model;

import android.content.Context;

import com.hanoch.greatrecipes.AppHelper;

public class Device {
    public String androidId;
    public String firebaseToken;

    public Device(Context context, String firebaseToken) {
        this.androidId = AppHelper.getDeviceAndroidId(context);
        this.firebaseToken = firebaseToken;
    }
}
