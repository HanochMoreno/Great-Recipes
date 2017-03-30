package com.hanoch.greatrecipes.model;


public class AllergensAndDietPrefItem {

    public int positionInDrawer;
    public String displayedName;
    public String searchKeyName;
    public Boolean isChosen;

    public AllergensAndDietPrefItem(int positionInDrawer, String displayedName,
                                    String searchKeyName, Boolean isChosen) {

        this.positionInDrawer = positionInDrawer;
        this.displayedName = displayedName;
        this.searchKeyName = searchKeyName;
        this.isChosen = isChosen;
    }
}
