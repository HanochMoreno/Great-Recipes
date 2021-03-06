package com.hanoch.greatrecipes.control;

import com.hanoch.greatrecipes.view.MyListFragment;


public interface ListFragmentListener {
    void onRecipeClick(MyListFragment listFragment, String id);

    void onListItemChecked(MyListFragment listFragment, String id, boolean isChecked);

    void onAddNewRecipeClick(MyListFragment listFragment);

}

