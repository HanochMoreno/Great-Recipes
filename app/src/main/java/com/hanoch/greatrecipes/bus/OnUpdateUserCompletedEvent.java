package com.hanoch.greatrecipes.bus;

import com.hanoch.greatrecipes.api.great_recipes_api.User;


public class OnUpdateUserCompletedEvent {

    public boolean isSuccess;
    public User user;
    public Throwable t;

    public OnUpdateUserCompletedEvent(boolean isSuccess, User user, Throwable t) {
        this.isSuccess = isSuccess;
        this.user = user;
        this.t = t;
    }
}
