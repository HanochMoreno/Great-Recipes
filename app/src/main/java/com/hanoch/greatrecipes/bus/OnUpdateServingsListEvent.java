package com.hanoch.greatrecipes.bus;


public class OnUpdateServingsListEvent {

    public int action;
    public boolean isSuccess;
    public Throwable t;

    public OnUpdateServingsListEvent(int action, boolean isSuccess, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
