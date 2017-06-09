package com.hanoch.greatrecipes.bus;


public class OnUpdateDeviceEvent {
    public int action;
    public boolean isSuccess;
    public Throwable t;

    public OnUpdateDeviceEvent(boolean isSuccess, Throwable t, int action) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
