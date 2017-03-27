package com.hanoch.greatrecipes.model;


public class MyIllegalStateException extends Exception {

    private static final long serialVersionUID = 1L;

    //Parameterless Constructor
    public MyIllegalStateException() {}

    //Constructor that accepts a message
    public MyIllegalStateException(String message)
    {
        super(message);
    }
}
