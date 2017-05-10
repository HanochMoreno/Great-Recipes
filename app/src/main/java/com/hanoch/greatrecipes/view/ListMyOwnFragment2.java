package com.hanoch.greatrecipes.view;

import com.hanoch.greatrecipes.AppConsts;

import java.util.ArrayList;

public class ListMyOwnFragment2 extends MyListFragment {

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.MY_OWN_RECIPES;
    }

    public void refreshAdapter() {
        adapter.refreshList(new ArrayList<>(appStateManager.user.userRecipes.values()));
    }

}
