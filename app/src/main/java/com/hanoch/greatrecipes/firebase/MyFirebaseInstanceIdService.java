package com.hanoch.greatrecipes.firebase;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.api.ApisManager;
import com.hanoch.greatrecipes.bus.BusConsts;
import com.hanoch.greatrecipes.model.Device;


public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private final String TAG = "MyFirebaseIdService";
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(AppConsts.SharedPrefs.FIREBASE_TOKEN, refreshedToken);
        editor.apply();

        if (AppStateManager.getInstance().user != null) {
            Device device = new Device(this, refreshedToken);
            ApisManager.getInstance().updateUserDevices(device, BusConsts.ACTION_ADD_NEW);
        }
    }
}