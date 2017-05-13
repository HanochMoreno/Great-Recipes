package com.hanoch.greatrecipes.bus;


public class OnUpdateUserRecipesEvent {
    public int action;
    public boolean isSuccess;
    public Throwable t;

    public OnUpdateUserRecipesEvent(int action, boolean isSuccess, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
