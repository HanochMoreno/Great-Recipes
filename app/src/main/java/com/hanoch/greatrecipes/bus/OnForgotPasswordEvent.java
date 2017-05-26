package com.hanoch.greatrecipes.bus;


public class OnForgotPasswordEvent {

    public boolean isSuccess;
    public String password;
    public Throwable t;

    public OnForgotPasswordEvent(boolean isSuccess, String password,  Throwable t) {
        this.isSuccess = isSuccess;
        this.password = password;
        this.t = t;
    }
}
