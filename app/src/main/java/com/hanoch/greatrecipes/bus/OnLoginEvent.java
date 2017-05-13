package com.hanoch.greatrecipes.bus;


public class OnLoginEvent {

    public boolean isSuccess;
    public Throwable t;

    public OnLoginEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
