package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.model.Serving;

import java.util.HashMap;


public class OnUpdateServingsListEvent {

    public int action;
    public boolean isSuccess;
    public HashMap<String, Serving> servingsMap;
    public Throwable t;

    public OnUpdateServingsListEvent(int action, boolean isSuccess, HashMap<String, Serving> servingsMap, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.servingsMap = servingsMap;
        this.t = t;
    }
}
