package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.api.great_recipes_api.User;


public class OnLoginEvent {

    public boolean isSuccess;
    public User user;
    public Throwable t;

    public OnLoginEvent(boolean isSuccess, User user, Throwable t) {
        this.isSuccess = isSuccess;
        this.user = user;
        this.t = t;
    }
}
