package com.hanoch.greatrecipes.bus;


public class OnShareRecipeEvent {

    public boolean isSuccess;
    public Throwable t;

    public OnShareRecipeEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
