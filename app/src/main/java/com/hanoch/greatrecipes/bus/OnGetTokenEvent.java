package com.hanoch.greatrecipes.bus;


public class OnGetTokenEvent {

    public boolean isSuccess;
    public Throwable t;

    public OnGetTokenEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
