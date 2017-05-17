package com.hanoch.greatrecipes.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;

import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.GenericRecipe;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;

import java.util.ArrayList;


public class SearchInListsFragment2 extends MyListFragment {

    private static final String ARG_ACTION = "ARG_ACTION";
    private View view;

    private String keyToSearch;

//-------------------------------------------------------------------------------------------------

    public static SearchInListsFragment2 newInstance(int action) {

        SearchInListsFragment2 fragment = new SearchInListsFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION, action);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (view != null) {
            return view;
        }

        listView_recipes.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (((SearchInListsActivity) getActivity()).searchView != null)
                    ((SearchInListsActivity) getActivity()).searchView.clearFocus();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        floatingButton_addRecipe.setVisibility(View.GONE);
        layout_dialogBubble.setVisibility(View.GONE);

        if (savedInstanceState != null) {

            keyToSearch = savedInstanceState.getString("keyToSearch");

            if (keyToSearch != null) {
                performSearch(keyToSearch, false);
            }
        }

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("keyToSearch", keyToSearch);
    }

//-------------------------------------------------------------------------------------------------

    public void performSearch(String keyToSearch, boolean searchWasPerformedByUser) {

        this.keyToSearch = keyToSearch;
        refreshAdapter();
        if (adapter.getCount() == 0 && searchWasPerformedByUser) {
            AppHelper.showSnackBar(view, R.string.no_results, Color.RED);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void refreshAdapter() {
        User user = AppStateManager.getInstance().user;

        ArrayList<GenericRecipe> searchResults = new ArrayList<>();

        for (UserRecipe recipe : user.userRecipes.values()) {
            if (recipe.recipeTitle.contains(keyToSearch)) {
                searchResults.add(recipe);
            }
        }

        for (YummlyRecipe recipe : user.yummlyRecipes.values()) {
            if (recipe.recipeTitle.contains(keyToSearch)) {
                searchResults.add(recipe);
            }
        }

        adapter.refreshList(searchResults);
    }
}
