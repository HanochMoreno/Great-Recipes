package com.hanoch.greatrecipes.control;

import com.hanoch.greatrecipes.model.MyFragment;


public interface ListFragmentListener {
    void onRecipeClick(MyFragment listFragment, long id);

    void onListItemChecked(MyFragment listFragment, long id, boolean isChecked);

    void onAddNewRecipeClick(MyFragment listFragment, int listTypeIndex);

}

