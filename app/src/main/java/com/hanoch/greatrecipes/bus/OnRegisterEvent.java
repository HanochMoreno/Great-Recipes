package com.hanoch.greatrecipes.bus;


public class OnRegisterEvent {

    public boolean isSuccess;
    public Throwable t;

    public OnRegisterEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
