package com.hanoch.greatrecipes.bus;


import com.hanoch.greatrecipes.api.great_recipes_api.User;

public class OnUpdateUserRecipesEvent {
    public int action;
    public boolean isSuccess;
    public User user;
    public Throwable t;

    public OnUpdateUserRecipesEvent(int action, boolean isSuccess, User user, Throwable t) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.user = user;
        this.t = t;
    }
}
