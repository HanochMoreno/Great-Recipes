package com.hanoch.greatrecipes.model;

public class ServingType {

    private String name;
    private int imageResourceId;
    //private int positionInList;
    //private boolean selected;

    public ServingType(String name, int imageResourceId /*, int positionInList, boolean selected*/) {
        this.name = name;
        this.imageResourceId = imageResourceId;
        //this.positionInList = positionInList;
        //this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    /*
    public int getPositionInList() {
        return positionInList;
    }

    public void setPositionInList(int positionInList) {
        this.positionInList = positionInList;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
*/
    /*public boolean equals(ServingType otherCategory) {

        return (this.getName().equals(otherCategory.getName()));
    }*/

}


