package com.hanoch.greatrecipes.model;

import android.support.v4.app.Fragment;


public abstract class MyFragment extends Fragment {

    public abstract void backToDefaultDisplay(boolean includeAnimation);

    public int getContainedListType (){
        return 0;
    }

}
