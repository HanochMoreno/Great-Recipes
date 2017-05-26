package com.hanoch.greatrecipes.bus;


public class OnUpdateUserPreferencesEvent {
    public boolean isSuccess;
    public Throwable t;

    public OnUpdateUserPreferencesEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
