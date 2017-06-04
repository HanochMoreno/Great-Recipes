package com.hanoch.greatrecipes.bus;


public class OnYummlyRecipeDownloadedEvent {
    public int action;
    public boolean isSuccess;
    public Throwable t;

    public OnYummlyRecipeDownloadedEvent(boolean isSuccess, Throwable t, int action) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
