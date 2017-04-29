package com.hanoch.greatrecipes.model;

import android.support.v4.app.Fragment;


public abstract class MyFragment extends Fragment {

    public static final String NO_SELECTION  = "NO_SELECTION";

    public abstract void backToDefaultDisplay(boolean includeAnimation);
    public abstract void onListReady(RecipesListsDownloadedEvent event);

    public int getContainedListType (){
        return 0;
    }

}
