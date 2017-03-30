package com.hanoch.greatrecipes.model;

public class Category {

    private String name;
    private int imageResourceId;
    private int positionInList;
    private boolean selected;

    public Category(String name, int imageResourceId, int positionInList, boolean selected) {
        this.name = name;
        this.imageResourceId = imageResourceId;
        this.positionInList = positionInList;
        this.selected = selected;
    }
    public boolean equals(Category otherCategory) {

        return (this.name.equals(otherCategory.name));
    }

}


