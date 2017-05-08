package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.model.Serving2;

import java.util.HashMap;


public class OnUpdateServingsListCompletedEvent {

    public int action;
    public boolean isSuccess;
    public HashMap<String, Serving2> servingsMap;
    public Throwable t;

    public OnUpdateServingsListCompletedEvent(int action, boolean isSuccess, HashMap<String, Serving2> servingsMap, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.servingsMap = servingsMap;
        this.t = t;
    }
}
