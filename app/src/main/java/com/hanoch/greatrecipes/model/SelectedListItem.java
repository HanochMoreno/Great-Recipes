package com.hanoch.greatrecipes.model;


public class SelectedListItem {

    private long id;
    private boolean checked;

//----------------------------------------------------------------------------------------------

    public SelectedListItem(long id, boolean checked) {
        this.id = id;
        this.checked = checked;
    }

//----------------------------------------------------------------------------------------------

    public long getId() {
        return id;
    }

//----------------------------------------------------------------------------------------------

    public void setId(long id) {
        this.id = id;
    }

//----------------------------------------------------------------------------------------------

    public boolean isChecked() {
        return checked;
    }

//----------------------------------------------------------------------------------------------

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
