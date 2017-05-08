package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.model.Serving2;

import java.util.ArrayList;


public class OnUpdateServingsListCompleted {

    public String action;
    public boolean isSuccess;
    public ArrayList<Serving2> servingsList;
    public Throwable t;

    public OnUpdateServingsListCompleted(String action, boolean isSuccess, ArrayList<Serving2> servingsList, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.servingsList = servingsList;
        this.t = t;
    }
}
