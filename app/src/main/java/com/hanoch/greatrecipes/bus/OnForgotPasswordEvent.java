package com.hanoch.greatrecipes.bus;


public class OnForgotPasswordEvent {

    public boolean isSuccess;
    public String email;
    public String password;
    public Throwable t;

    public OnForgotPasswordEvent(boolean isSuccess, String email, String password,  Throwable t) {
        this.isSuccess = isSuccess;
        this.email = email;
        this.password = password;
        this.t = t;
    }
}
