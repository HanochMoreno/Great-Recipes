package com.hanoch.greatrecipes.view.adapters;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.ServingType;

import java.util.ArrayList;

/**
 * Custom adapter for the 'Serving Types' list, on free-trial mode
 */

public class ServingTypesAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ServingType> servingTypesList;
    private boolean isPremium;

//----------------------------------------------------------------------------------------------

    public ServingTypesAdapter(Context context, ArrayList<ServingType> servingTypesList, boolean isPremium) {

        this.context = context;
        this.servingTypesList = servingTypesList;
        this.isPremium = isPremium;
    }

//----------------------------------------------------------------------------------------------

    @Override
    public int getCount() {
        return servingTypesList.size();
    }

//----------------------------------------------------------------------------------------------

    @Override
    public ServingType getItem(int position) {
        return servingTypesList.get(position);
    }

//----------------------------------------------------------------------------------------------

    @Override
    public long getItemId(int position) {
        return 0;
    }

//----------------------------------------------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /*View view= convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.listitem_serving_type_chooser, parent, false);
        }*/

        View view = LayoutInflater.from(context).inflate(R.layout.listitem_serving_type_chooser, parent, false);

        ServingType servingType = servingTypesList.get(position);

        TextView textView_servingTypeName = (TextView) view.findViewById(R.id.textView_servingTypeName);
        ImageView imageView_servingTypeImage = (ImageView) view.findViewById(R.id.imageView_servingTypeImage);

        String translatedServingTypeName = AppHelper.getTranslatedServingTypeName(context, servingType.getName());

        textView_servingTypeName.setText(translatedServingTypeName);
        imageView_servingTypeImage.setImageResource(servingType.getImageResourceId());

        if (!isPremium && !(position == 0 || position == 1) /*!isEnabled(position)*/) {
            // Free-trial only

            TextView textView_premium = (TextView) view.findViewById(R.id.textView_premium);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);

            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            imageView_servingTypeImage.setColorFilter(filter);

            textView_premium.setVisibility(View.VISIBLE);
            textView_premium.setRotation(30f);
        }

        return view;
    }

//----------------------------------------------------------------------------------------------

    /*@Override
    public boolean areAllItemsEnabled() {
//        return isPremium;
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 0 || position == 1) {
            return true;

        } else {
            // Unavailable serving for free-trial mode
            return false;
        }
    }*/
}
