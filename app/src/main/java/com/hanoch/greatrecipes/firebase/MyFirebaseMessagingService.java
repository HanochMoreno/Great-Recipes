package com.hanoch.greatrecipes.firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.ApisManager;

import java.util.ArrayList;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "FBMessagingService";

    /**
     * This method is called ONLY when the app is in the foreground!!
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // This method is called ONLY when the app is in the foreground!

        Map<String, String> data = remoteMessage.getData();
        String recipeId = null;
        boolean isUserRecipe = false;
        if (data != null && data.size() > 0) {
            ArrayList<String> values = new ArrayList<>(data.values());
            Log.d(TAG, "Message data payload: " + data);
            recipeId = values.get(0); // recipeId or recipeYummlyId
            isUserRecipe = Boolean.parseBoolean(values.get(1));
        }

//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

        if (recipeId != null) {
            if (isUserRecipe) {
                ApisManager.getInstance().getUserRecipeFromGreatRecipesApi(recipeId);
            } else {
                int action = AppConsts.Actions.DOWNLOAD_SHARED_YUMMLY_RECIPE;
                ApisManager.getInstance().getYummlyRecipeFromGreatRecipesApi(this, recipeId, action);
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

}
