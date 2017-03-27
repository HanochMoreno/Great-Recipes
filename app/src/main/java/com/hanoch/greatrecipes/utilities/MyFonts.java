package com.hanoch.greatrecipes.utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

/**
 * Created by Hanoc_000 on 22/03/2016.
 */
public class MyFonts {

    private static final String TAG = "MyFont";

    private Context context;

    private Typeface aliceFont;
    private Typeface motionPictureFont;

    private static MyFonts myFonts;

    public static MyFonts getInstance(Context context) {
        if (myFonts == null) {
            myFonts =  new MyFonts(context);
        }

        return myFonts;
    }

    private MyFonts(Context context) {
        Log.d(TAG, "MyFonts was just created");
        this.context = context;
        this.aliceFont = Typeface.createFromAsset(context.getAssets(), "fonts/Alice.ttf");
        this.motionPictureFont = Typeface.createFromAsset(context.getAssets(), "fonts/MotionPicture.ttf");
    }

    public Typeface getAliceFont() {
        return aliceFont;
    }

    public Typeface getMotionPictureFont() {
        return motionPictureFont;
    }
}
