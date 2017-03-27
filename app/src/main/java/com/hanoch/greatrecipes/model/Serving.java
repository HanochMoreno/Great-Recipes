package com.hanoch.greatrecipes.model;


public class Serving {

    private long id;
    private long recipeId;
    private String servingType;
    private boolean fromUserList;
    private boolean selected;

    public Serving(long id, long recipeId, String servingType, boolean fromUserList, boolean selected) {
        this.id = id;
        this.recipeId = recipeId;
        this.servingType = servingType;
        this.fromUserList = fromUserList;
        this.selected = selected;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getServingType() {
        return servingType;
    }

    public void setServingType(String servingType) {
        this.servingType = servingType;
    }

    public boolean isFromUserList() {
        return fromUserList;
    }

    public void setFromUserList(boolean fromUserList) {
        this.fromUserList = fromUserList;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
