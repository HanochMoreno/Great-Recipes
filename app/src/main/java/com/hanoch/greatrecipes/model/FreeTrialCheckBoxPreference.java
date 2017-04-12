package com.hanoch.greatrecipes.model;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppHelper;
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
        AppHelper.showSnackBar(root, R.string.this_item_is_not_available_in_free_trial, ActivityCompat.getColor(getContext(), R.color.colorSnackbarFreeTrial));
        setChecked(false);
        return true;
    }

}
