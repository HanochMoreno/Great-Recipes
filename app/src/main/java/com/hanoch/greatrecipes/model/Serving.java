package com.hanoch.greatrecipes.model;


public class Serving {

    public long id;
    public long recipeId;
    public String servingType;
    public boolean fromUserList;
    public boolean selected;

    public Serving(long id, long recipeId, String servingType, boolean fromUserList, boolean selected) {
        this.id = id;
        this.recipeId = recipeId;
        this.servingType = servingType;
        this.fromUserList = fromUserList;
        this.selected = selected;
    }

}
