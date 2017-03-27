package com.hanoch.greatrecipes.model;

import com.squareup.otto.Bus;

public class MyBus extends Bus {

    private static MyBus instance;

    private MyBus(){}

    public static MyBus getInstance() {

        if (instance == null) {

            instance = new MyBus();
        }

        return instance;
    }
}
