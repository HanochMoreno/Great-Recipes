package com.hanoch.greatrecipes.control;

import com.hanoch.greatrecipes.model.MyFragment;


public interface ListFragmentListener {
    void onRecipeClick(MyFragment listFragment, String id);

    void onListItemChecked(MyFragment listFragment, String id, boolean isChecked);

    void onAddNewRecipeClick(MyFragment listFragment, int listTypeIndex);

}

