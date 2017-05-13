package com.hanoch.greatrecipes.bus;


public class OnYummlyRecipeDownloadedEvent {
    public boolean isSuccess;
    public Throwable t;

    public OnYummlyRecipeDownloadedEvent(boolean isSuccess, Throwable t) {
        this.isSuccess = isSuccess;
        this.t = t;
    }
}
