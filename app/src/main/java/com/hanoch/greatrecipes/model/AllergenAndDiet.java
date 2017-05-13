package com.hanoch.greatrecipes.model;


public class AllergenAndDiet {

    public boolean isDiet;
    public int positionInDrawer;
    public String displayName;
    public String searchKeyName;

    public AllergenAndDiet(boolean isDiet, int positionInDrawer, String displayName, String searchKeyName) {

        this.isDiet = isDiet;
        this.positionInDrawer = positionInDrawer;
        this.displayName = displayName;
        this.searchKeyName = searchKeyName;
    }
}
