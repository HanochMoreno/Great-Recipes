package com.hanoch.greatrecipes.model;


import java.util.ArrayList;

public class Preferences {

    public String email;
    public String username;
    public String password;
    public int maxOnlineSearchResults;
    public ArrayList<String> dietAndAllergensList;
    public boolean vibration;
    public boolean colorfulMenu;

    public Preferences(String email, String username, String password, int maxOnlineSearchResults,
                       ArrayList<String> dietAndAllergensList, boolean vibration, boolean colorfulMenu) {

        this.email = email;
        this.username = username;
        this.password = password;
        this.maxOnlineSearchResults = maxOnlineSearchResults;
        this.dietAndAllergensList = dietAndAllergensList;
        this.vibration = vibration;
        this.colorfulMenu = colorfulMenu;
    }

    public Preferences(Preferences preferences) {
        // CTOR to generate a copy

        this.email = preferences.email;
        this.username = preferences.username;
        this.password = preferences.password;
        this.maxOnlineSearchResults =preferences. maxOnlineSearchResults;
        this.dietAndAllergensList = preferences.dietAndAllergensList;
        this.vibration = preferences.vibration;
        this.colorfulMenu = preferences.colorfulMenu;
    }
}
