package com.hanoch.greatrecipes.model;


public class AllergensAndDietPrefItem {

    private int positionInDrawer;
    private String displayedName;
    private String searchKeyName;
    private Boolean isChosen;

    public AllergensAndDietPrefItem(int positionInDrawer, String displayedName,
                                    String searchKeyName, Boolean isChosen) {

        this.positionInDrawer = positionInDrawer;
        this.displayedName = displayedName;
        this.searchKeyName = searchKeyName;
        this.isChosen = isChosen;
    }

    public int getPositionInDrawer() {
        return positionInDrawer;
    }

    public void setPositionInDrawer(int positionInDrawer) {
        this.positionInDrawer = positionInDrawer;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public String getSearchKeyName() {
        return searchKeyName;
    }

    public void setSearchKeyName(String searchKeyName) {
        this.searchKeyName = searchKeyName;
    }

    public Boolean isChosen() {
        return isChosen;
    }

    public void setChosen(Boolean isChosen) {
        this.isChosen = isChosen;
    }
}
