package com.hanoch.greatrecipes.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.model.MyFragment;
import com.hanoch.greatrecipes.view.adapters.RecipesListAdapter;

import java.util.ArrayList;


public class SearchInListsFragment extends MyFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String ARG_EXTRA_SERVING = "serving";

    private RecipesListAdapter adapter;

    private ListView listView_searchResults;

    private View view;

    private ListFragmentListener mListListener;

    private ArrayList<String> checkedItemsIdList;

    private String extra_serving;
    private String keyToSearch;
    private Bundle savedInstanceState;
    private long selectedId;
    private boolean searchWasPerformedByUser;

//-------------------------------------------------------------------------------------------------

    public static SearchInListsFragment newInstance(String extra_serving) {

        SearchInListsFragment fragment = new SearchInListsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.savedInstanceState = savedInstanceState;

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        selectedId = AppConsts.NO_SELECTION;

        checkedItemsIdList = new ArrayList<>();

        if (view != null) {
            return view;
        }

        extra_serving = getArguments().getString(ARG_EXTRA_SERVING);

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search_lists, container, false);

        listView_searchResults = (ListView) view.findViewById(R.id.listView_searchResults);
        listView_searchResults.setOnItemClickListener(this);
        listView_searchResults.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (((SearchInListsActivity) getActivity()).searchView != null)
                    ((SearchInListsActivity) getActivity()).searchView.clearFocus();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        // Adding a gap (divider-like) at the top of the listView
        listView_searchResults.addHeaderView(new View(getContext()), null, true);

        getLoaderManager().initLoader(0, null, this);

        if (extra_serving == null) {
            listView_searchResults.setOnItemLongClickListener(this);
        }

        if (savedInstanceState != null) {

            boolean isListViewEnabled = savedInstanceState.getBoolean("isListViewEnabled");
            listView_searchResults.setEnabled(isListViewEnabled);
            selectedId = savedInstanceState.getLong("selectedId");
            checkedItemsIdList = savedInstanceState.getStringArrayList("checkedItemsIdList");
            keyToSearch = savedInstanceState.getString("keyToSearch");

            if (keyToSearch != null) {
                performSearch(keyToSearch, false);
            }
        }

        adapter = new RecipesListAdapter(getActivity(), null, checkedItemsIdList, selectedId);
        listView_searchResults.setAdapter(adapter);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListListener = (ListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ListFragmentListener");
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("isListViewEnabled", listView_searchResults.isEnabled());
        outState.putParcelable("listViewState", listView_searchResults.onSaveInstanceState());
        outState.putLong("selectedId", selectedId);
        outState.putStringArrayList("checkedItemsIdList", checkedItemsIdList);
        outState.putString("keyToSearch", keyToSearch);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDetach() {
        super.onDetach();
        mListListener = null;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (checkedItemsIdList.isEmpty()) {
            // The user wants to review a recipe

            mListListener.onRecipeClick(this, id);

            if (getResources().getBoolean(R.bool.isTablet)) {
                // Tablet only

                if (selectedId == id) {
                    // The selected recipe was clicked

                    AppHelper.setUnselectedRecipe(view);

                    selectedId = AppConsts.NO_SELECTION;
                    adapter.onNoSelection();

                    backToDefaultDisplay(true);

                } else {
                    // An unselected recipe was clicked

                    if (selectedId != AppConsts.NO_SELECTION) {
                        // There is a previous selection

                        View prevSelectedItemView = adapter.getViewById(selectedId + "");
                        if (prevSelectedItemView != null) {
                            AppHelper.setUnselectedRecipe(prevSelectedItemView);
                        }
                    }

                    AppHelper.setSelectedRecipe(view, getContext());

                    selectedId = id;
                    adapter.onRecipeSelected(id);
                }
            }

        } else {
            // The user is checking/unchecking recipes to delete

            if (checkedItemsIdList.contains(id + "")) {
                // Checked item was unchecked

                AppHelper.animateUncheckedRecipe(view, getContext());
                checkedItemsIdList.remove(id + "");

                if (getResources().getBoolean(R.bool.isTablet)) {

                    if (checkedItemsIdList.isEmpty()) {
                        // The last checked item was unchecked

                        AppHelper.setUnselectedRecipe(view);

                    } else {

                        if (selectedId == id) {
                            // The selected item was unchecked

                            AppHelper.setUnselectedRecipe(view);

                            String prevCheckedItemId = checkedItemsIdList.get(checkedItemsIdList.size() - 1);

                            selectedId = (long) Integer.parseInt(prevCheckedItemId);

                            adapter.onRecipeSelected(selectedId);
                            View prevSelectedItemView = adapter.getViewById(prevCheckedItemId);
                            if (prevSelectedItemView != null) {
                                AppHelper.setSelectedRecipe(prevSelectedItemView, getContext());

                            }
                        }
                    }
                }

                if (checkedItemsIdList.isEmpty()) {
                    backToDefaultDisplay(true);
                }

                mListListener.onListItemChecked(this, id, false);

            } else {
                // Unchecked item was checked
                // (at least 1 item was already checked)

                if (getResources().getBoolean(R.bool.isTablet)) {

                    AppHelper.setSelectedRecipe(view, getContext());

                    View previousSelectedItemView = adapter.getViewById(selectedId + "");
                    if (previousSelectedItemView != null) {
                        AppHelper.setUnselectedRecipe(previousSelectedItemView);
                    }

                    selectedId = id;
                    adapter.onRecipeSelected(selectedId);
                }

                checkedItemsIdList.add(id + "");

                AppHelper.animateCheckedRecipe(view, getContext());

                mListListener.onListItemChecked(this, id, true);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (!checkedItemsIdList.isEmpty()) {
            return false;
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet only

            if (selectedId != id) {
                // The selected item isn't the one that checked

                if (selectedId != AppConsts.NO_SELECTION) {
                    // There is a previous selection

                    View previousSelectedItemView = adapter.getViewById(selectedId + "");
                    if (previousSelectedItemView != null) {
                        AppHelper.setUnselectedRecipe(previousSelectedItemView);
                    }
                }

                selectedId = id;

                AppHelper.setSelectedRecipe(view, getContext());

                adapter.onRecipeSelected(selectedId);
            }
        }

        AppHelper.animateCheckedRecipe(view, getContext());

        checkedItemsIdList.add(id + "");

        mListListener.onListItemChecked(this, id, true);

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public void performSearch(String keyToSearch, boolean searchWasPerformedByUser) {

        this.searchWasPerformedByUser = searchWasPerformedByUser;

        this.keyToSearch = keyToSearch;

        getLoaderManager().restartLoader(0, null, SearchInListsFragment.this);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void backToDefaultDisplay(boolean includeAnimation) {

        if (getResources().getBoolean(R.bool.isTablet)) {
            // Tablet only

            if (selectedId != AppConsts.NO_SELECTION) {
                View selectedView = adapter.getViewById(selectedId + "");
                if (selectedView != null) {
                    AppHelper.setUnselectedRecipe(selectedView);
                }
                adapter.onNoSelection();
                selectedId = AppConsts.NO_SELECTION;
            }
        }

        listView_searchResults.setEnabled(true);

        if (includeAnimation) {

            for (String selectedItemId : checkedItemsIdList) {

                View selectedItemView = adapter.getViewById(selectedItemId);
                if (selectedItemView != null) {
                    AppHelper.animateUncheckedRecipe(selectedItemView, getContext());
                }
            }

        } else {
            // No need for unchecked items animation

            for (String selectedItemId : checkedItemsIdList) {

                View selectedItemView = adapter.getViewById(selectedItemId);
                if (selectedItemView != null) {
                    ImageView selectedItemImage = (ImageView) selectedItemView.findViewById(R.id.imageView_itemImage);
                    ImageView checkedIcon = (ImageView) selectedItemView.findViewById(R.id.imageView_checkedIcon);
                    checkedIcon.setAlpha(0f);
                    selectedItemImage.setAlpha(1f);
                }
            }
        }

        checkedItemsIdList.clear();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String selection = RecipesContract.Recipes.TITLE + " LIKE ? ";
        String[] selectionArgs = {"%" + keyToSearch + "%"};

        return new CursorLoader(
                getActivity(),
                RecipesContract.Recipes.CONTENT_URI,

                // columns: null = all
                null,

                // rows:
                selection,
                selectionArgs,

                // sort order
                RecipesContract.Recipes._ID + " ASC"
        );
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.getCount() == 0 && searchWasPerformedByUser) {

            searchWasPerformedByUser = false;
            AppHelper.showSnackBar(view, R.string.no_results, Color.RED);
        }

        adapter.swapCursor(cursor);

        if (savedInstanceState != null) {

            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
            listView_searchResults.onRestoreInstanceState(listViewState);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }

}
