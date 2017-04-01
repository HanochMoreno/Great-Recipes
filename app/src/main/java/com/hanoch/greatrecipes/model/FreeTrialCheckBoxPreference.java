package com.hanoch.greatrecipes.model;

import android.content.Context;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanoch.greatrecipes.R;

public class FreeTrialCheckBoxPreference extends CheckBoxPreference implements Preference.OnPreferenceClickListener {

    private ViewGroup root;

    public FreeTrialCheckBoxPreference(Context context) {
        super(context);
    }
    public FreeTrialCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public FreeTrialCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        root = (ViewGroup) super.onCreateView(parent);
        ((TextView)root.findViewById(android.R.id.title)).setTextColor(ActivityCompat.getColor(getContext(), R.color.colorFreeTrialDisabledPreference));
        ((TextView)root.findViewById(android.R.id.summary)).setTextColor(ActivityCompat.getColor(getContext(), R.color.colorFreeTrialDisabledPreference));
        return root;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Snackbar snack = Snackbar.make(root, R.string.this_item_is_not_available_in_free_trial, Snackbar.LENGTH_SHORT);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.colorSnackbarFreeTrial));
        snack.show();
        setChecked(false);
        return true;
    }

}
