package com.hanoch.greatrecipes.model;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanoch.greatrecipes.R;

public class FreeTrialPreference extends Preference implements Preference.OnPreferenceClickListener {

    private ViewGroup root;

    public FreeTrialPreference(Context context) {
        super(context);
    }
    public FreeTrialPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        root = (ViewGroup) super.onCreateView(parent);
        ((TextView)root.findViewById(android.R.id.title)).setTextColor(ActivityCompat.getColor(getContext(), R.color.colorFreeTrialDisabledPreference));
        ((TextView)root.findViewById(android.R.id.summary)).setTextColor(parent.getResources().getColor(R.color.colorFreeTrialDisabledPreference));
        return root;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Snackbar snack = Snackbar.make(root, R.string.this_item_is_not_available_in_free_trial, Snackbar.LENGTH_SHORT);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.colorSnackbarFreeTrial));
        snack.show();
        return true;
    }

}
