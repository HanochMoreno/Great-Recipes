package com.hanoch.greatrecipes.bus;


public class OnMailWasSentEvent {
    public String email;
    public boolean isSuccess;
    public int action;

    public OnMailWasSentEvent(boolean isSuccess, int action, String email) {
        this.isSuccess = isSuccess;
        this.action = action;
        this.email = email;
    }
}
