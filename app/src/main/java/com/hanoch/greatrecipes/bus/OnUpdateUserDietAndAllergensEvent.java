package com.hanoch.greatrecipes.bus;


public class OnUpdateUserDietAndAllergensEvent {
    public int action;
    public boolean isSuccess;
    public int position;
    public Throwable t;

    public OnUpdateUserDietAndAllergensEvent(int action, boolean isSuccess, int position, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.position = position;
        this.t = t;
    }
}
