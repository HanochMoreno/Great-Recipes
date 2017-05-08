package com.hanoch.greatrecipes;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public abstract class AnimationHelper {

    public static void animateSelectedRecipe(View view, Context context) {

        FrameLayout selectedItemFrame = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        selectedItemFrame.setPadding(5, 3, 5, 3);

        TextView textView = (TextView) view.findViewById(R.id.textView_itemTitle);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUnselectedRecipe(View view) {

        FrameLayout selectedItemFrame = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        selectedItemFrame.setPadding(0, 0, 0, 0);

        TextView textView = (TextView) view.findViewById(R.id.textView_itemTitle);
        textView.setTextColor(Color.WHITE);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateCheckedRecipe(View view, Context context) {

        FrameLayout selectedItemImageFrame = (FrameLayout) view.findViewById(R.id.layout_image);
        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_itemImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        checkedIcon.setVisibility(View.VISIBLE);

        animateViewFadingIn(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, selectedItemImageFrame, 1500, 0);

        animateViewDimming(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUncheckedRecipe(View view, Context context) {

        FrameLayout selectedItemImageFrame = (FrameLayout) view.findViewById(R.id.layout_image);
        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_itemImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        animateViewFadingOut(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, selectedItemImageFrame, 1500, 0);

        animateViewBrightening(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateCheckedCategory(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_categoryImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        checkedIcon.setVisibility(View.VISIBLE);

        animateViewFadingIn(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, checkedIcon, 1500, 0);

        animateViewDimming(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUncheckedCategory(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_categoryImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        animateViewFadingOut(context, checkedIcon, 1500, 0);

        animateViewFlipping(context, checkedIcon, 1500, 0);

        animateViewBrightening(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateCheckedServing(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_servingImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        checkedIcon.setVisibility(View.VISIBLE);

        animateViewFlipping(context, checkedIcon, 1500, 0);
        animateViewFadingIn(context, checkedIcon, 1500, 0);

        animateViewDimming(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateUncheckedServing(View view, Context context) {

        ImageView selectedItemImage = (ImageView) view.findViewById(R.id.imageView_servingImage);
        ImageView checkedIcon = (ImageView) view.findViewById(R.id.imageView_checkedIcon);

        animateViewFlipping(context, checkedIcon, 1500, 0);
        animateViewFadingOut(context, checkedIcon, 1500, 0);

        animateViewBrightening(context, selectedItemImage, 1500, 0);
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewFlipping(Context context, View view, int duration, long startDelay) {

        ObjectAnimator flipAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.flipping);
        flipAnim.setTarget(view);
        flipAnim.setStartDelay(startDelay);
        flipAnim.setDuration(duration);
        flipAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewFadingIn(Context context, final View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.fade_in);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
                view.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewFadingOut(Context context, final View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.fade_out);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setEnabled(false);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static ObjectAnimator animateToolbarButtonFadingIn(final MenuItem button, int duration, long startDelay) {

        final Drawable icon = button.getIcon();

        ObjectAnimator alphaAnim = ObjectAnimator.ofPropertyValuesHolder(icon, PropertyValuesHolder.ofInt("alpha", 0, 255));
        alphaAnim.setDuration(duration);
        alphaAnim.setStartDelay(startDelay);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                button.setVisible(true);
                button.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                button.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                icon.setAlpha(0);
                button.setVisible(false);
                button.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();

        return alphaAnim;
    }

//-------------------------------------------------------------------------------------------------

    public static ObjectAnimator animateToolbarButtonFadingOut(final MenuItem button, int duration, long startDelay) {

        final Drawable icon = button.getIcon();

        ObjectAnimator alphaAnim = ObjectAnimator.ofPropertyValuesHolder(icon, PropertyValuesHolder.ofInt("alpha", 255, 0));
        alphaAnim.setDuration(duration);
        alphaAnim.setStartDelay(startDelay);

        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                button.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //button.setEnabled(false);
                button.setVisible(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                button.setVisible(false);
                icon.setAlpha(0);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        alphaAnim.start();

        return alphaAnim;
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewDimming(Context context, View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.dimming);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);
        alphaAnim.start();
    }

//-------------------------------------------------------------------------------------------------

    public static void animateViewBrightening(Context context, View view, int duration, long startDelay) {

        ObjectAnimator alphaAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.brightening);
        alphaAnim.setTarget(view);
        alphaAnim.setStartDelay(startDelay);
        alphaAnim.setDuration(duration);
        alphaAnim.start();
    }
}
