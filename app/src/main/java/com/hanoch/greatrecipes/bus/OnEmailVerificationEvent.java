package com.hanoch.greatrecipes.bus;


public class OnEmailVerificationEvent {

    public boolean isSuccess;
    public Throwable t;

    public OnEmailVerificationEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
