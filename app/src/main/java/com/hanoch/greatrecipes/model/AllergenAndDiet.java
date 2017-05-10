package com.hanoch.greatrecipes.model;


public class AllergenAndDiet {

    public int positionInDrawer;
    public String displayName;
    public String searchKeyName;
    public Boolean isChosen;

    public AllergenAndDiet(int positionInDrawer, String displayName,
                           String searchKeyName, Boolean isChosen) {

        this.positionInDrawer = positionInDrawer;
        this.displayName = displayName;
        this.searchKeyName = searchKeyName;
        this.isChosen = isChosen;
    }
}
