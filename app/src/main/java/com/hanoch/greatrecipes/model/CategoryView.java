package com.hanoch.greatrecipes.model;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Hanoc_000 on 06/02/2016.
 */
public class CategoryView {

    private String categoryName;
    private TextView fullName;
    private ImageView checkedIcon;
    private ImageView image;

    public CategoryView(String categoryName, TextView fullName, ImageView checkedIcon, ImageView image) {
        this.categoryName = categoryName;
        this.fullName = fullName;
        this.checkedIcon = checkedIcon;
        this.image = image;
    }

    public TextView getFullName() {
        return fullName;
    }

    public ImageView getCheckedIcon() {
        return checkedIcon;
    }

    public ImageView getImage() {
        return image;
    }
}
