package com.hanoch.greatrecipes.bus;


public class OnAppDataEvent {

    public boolean isSuccess;
    public Throwable t;

    public OnAppDataEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
