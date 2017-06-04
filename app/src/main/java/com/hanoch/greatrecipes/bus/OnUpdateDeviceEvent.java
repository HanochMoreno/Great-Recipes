package com.hanoch.greatrecipes.bus;

/**
 * Created by Hanoc_000 on 03/06/2017.
 */

public class OnUpdateDeviceEvent {
    public boolean isSuccess;
    public Throwable t;

    public OnUpdateDeviceEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
