package com.hanoch.greatrecipes.view;

import com.hanoch.greatrecipes.AppConsts;
import java.util.ArrayList;

public class ListOnlineFragment2 extends MyListFragment {

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.ONLINE_RECIPES;
    }

    public void refreshAdapter() {
        adapter.refreshList(new ArrayList<>(appStateManager.user.recipes.yummlyRecipes.values()));
    }
}
