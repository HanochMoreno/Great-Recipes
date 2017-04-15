package com.hanoch.greatrecipes.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.ObjectDrawerItem;

/**
 * Custom adapter for the 'Filters Drawer' list, on free-trial mode
 */
public class DrawerItemAdapterLimited extends ArrayAdapter<ObjectDrawerItem> {

    private Context mContext;
    private ObjectDrawerItem data[] = null;

    public DrawerItemAdapterLimited(Context mContext, ObjectDrawerItem[] data) {
        super(mContext, R.layout.listheader_drawer_layout, data);

        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //View listItem = convertView;

        View listItem;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

        if (isEnabled(position)) {
            // not a title or limited (free-trial version)

            listItem = inflater.inflate(R.layout.listitem_drawer, parent, false);
            ImageView imageView_icon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
            TextView textView_name = (TextView) listItem.findViewById(R.id.textViewName);

            ObjectDrawerItem folder = data[position];

            imageView_icon.setImageResource(folder.icon);
            textView_name.setText(folder.name);

            if (!(position == 4 || position == 11 || position == 12)) {
                textView_name.setTextColor(ActivityCompat.getColor(mContext, R.color.colorFreeTrialDisabledDrawerList));
            }

        } else {
            if (position == 0) {
                // The list header
                listItem = inflater.inflate(R.layout.listheader_drawer_layout, parent, false);
            } else if (position == 1){
                // Diet  title
                listItem = inflater.inflate(R.layout.listitem_drawer_diet_title, parent, false);

            } else if (position == 5) {
                // Allergens title
                listItem = inflater.inflate(R.layout.listitem_drawer_allergens_title, parent, false);

            } else {
                // Unavailable (for free-trial) Allergen or Diet item
                listItem = inflater.inflate(R.layout.listitem_drawer_limited, parent, false);
                ImageView imageView_icon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
                TextView textView_name = (TextView) listItem.findViewById(R.id.textViewName);

                ObjectDrawerItem folder = data[position];

                imageView_icon.setImageResource(folder.icon);
                textView_name.setText(folder.name);
            }
        }

        return listItem;
    }

    /*@Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 4 || position == 11 || position == 12) {
            return true;

        } else {
            // Unavailable search-filter for free-trial mode
            return false;
        }
    }*/

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 0 || position == 1 || position == 5) {
            return false;

        } else {
            // Unavailable search-filter for free-trial mode
            return true;
        }
    }

}