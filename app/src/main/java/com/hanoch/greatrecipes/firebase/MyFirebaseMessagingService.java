package com.hanoch.greatrecipes.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.ApisManager;

import java.util.ArrayList;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "FBMessagingService";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }
    /**
     * This method is called ONLY when the app is in the foreground!!
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // This method is called ONLY when the app is in the foreground!

        Map<String, String> data = remoteMessage.getData();
        if (data != null && data.size() > 0) {
            Intent intent = new Intent(AppConsts.NOTIFICATION_RECEIVER_FILTER);
            intent.putExtra(AppConsts.Extras.RECIPE_ID, data.get(AppConsts.Extras.RECIPE_ID));
            intent.putExtra(AppConsts.Extras.IS_USER_RECIPE, data.get(AppConsts.Extras.IS_USER_RECIPE));
            broadcaster.sendBroadcast(intent);
        }
    }

}
