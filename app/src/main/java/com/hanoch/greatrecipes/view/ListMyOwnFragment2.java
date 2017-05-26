package com.hanoch.greatrecipes.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanoch.greatrecipes.AppConsts;

import java.util.ArrayList;

public class ListMyOwnFragment2 extends MyListFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        refreshAdapter();
        return view;
    }

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.MY_OWN_RECIPES;
    }

    public void refreshAdapter() {
        adapter.refreshList(new ArrayList<>(appStateManager.user.recipes.userRecipes.values()));
    }

}
