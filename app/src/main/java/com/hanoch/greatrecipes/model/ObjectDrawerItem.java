package com.hanoch.greatrecipes.model;

public class ObjectDrawerItem {

    public int icon;
    public String name;

    // Constructor for items
    public ObjectDrawerItem(int icon, String name) {

        this.icon = icon;
        this.name = name;
    }

    // Constructor for titles
    public ObjectDrawerItem(String title) {

        this.icon = -1;
        this.name = title;
    }
}
