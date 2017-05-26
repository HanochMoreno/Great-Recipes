package com.hanoch.greatrecipes.bus;


public class OnUpdateUserPropertyEvent {
    public boolean isSuccess;
    public Throwable t;

    public OnUpdateUserPropertyEvent() {
    }

    public OnUpdateUserPropertyEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }


    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public void setThrowable(Throwable t) {
        this.t = t;
    }

}
