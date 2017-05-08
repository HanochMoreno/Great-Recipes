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
import com.hanoch.greatrecipes.api.GGGRecipe2;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.model.MyListFragment;

import java.util.ArrayList;


public class SearchInListsFragment2 extends MyListFragment {

    private View view;

    private String keyToSearch;

//-------------------------------------------------------------------------------------------------

    public static SearchInListsFragment2 newInstance(String extra_serving) {

        SearchInListsFragment2 fragment = new SearchInListsFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
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

        User user = AppStateManager.getInstance().user;

        ArrayList<GGGRecipe2> searchResults = new ArrayList<>();

        for (UserRecipe userRecipe : user.userRecipes.values()) {
            if (userRecipe.recipeTitle.contains(keyToSearch)) {
                searchResults.add(userRecipe);
            }
        }
        for (YummlyRecipe yummlyRecipe : user.yummlyRecipes.values()) {
            if (yummlyRecipe.recipeTitle.contains(keyToSearch)) {
                searchResults.add(yummlyRecipe);
            }
        }

        adapter.refreshList(searchResults);

        if (searchResults.isEmpty() && searchWasPerformedByUser) {
            AppHelper.showSnackBar(view, R.string.no_results, Color.RED);
        }
    }

}
