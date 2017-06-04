package com.hanoch.greatrecipes.bus;


public class OnUserRecipeDownloadedEvent {
    public boolean isSuccess;
    public Throwable t;

    public OnUserRecipeDownloadedEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
