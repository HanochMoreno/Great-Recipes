package com.hanoch.greatrecipes.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.database.SqLiteDbManager;
import com.hanoch.greatrecipes.model.RecipeSearchResult;
import com.hanoch.greatrecipes.view.adapters.SearchResultsAdapter;

public class OnlineSearchResultsFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentOnlineSearchListener mListener;
    private SearchResultsAdapter adapter;
    private ListView listView_searchResults;

    private View view;

    private SqLiteDbManager sqliteManager;
    private boolean isToScrollToTop;

//-------------------------------------------------------------------------------------------------

    interface OnFragmentOnlineSearchListener {

        void onSearchResultClick(long resultId, String resultYummlyId);
    }

//-------------------------------------------------------------------------------------------------

    public static OnlineSearchResultsFragment newInstance() {

        return new OnlineSearchResultsFragment();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sqliteManager = new SqLiteDbManager(getContext());
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_online_search_results, container, false);

        listView_searchResults = (ListView) view.findViewById(R.id.listView_searchResults);
        listView_searchResults.setOnItemClickListener(this);
        listView_searchResults.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (((OnlineSearchActivity) getActivity()).searchView != null)
                    ((OnlineSearchActivity) getActivity()).searchView.clearFocus();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        // Adding a gap (divider-like) in the bottom of the listView
        listView_searchResults.addHeaderView(new View(getContext()), null, true);
        listView_searchResults.addFooterView(new View(getContext()), null, true);

        adapter = new SearchResultsAdapter(getActivity(), null);

        listView_searchResults.setAdapter(adapter);

        getLoaderManager().initLoader(1, null, this);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    public void refreshAdapter() {
        adapter.notifyDataSetChanged();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnFragmentOnlineSearchListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnlineSearchFragmentListener");
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
        isToScrollToTop = false;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AppHelper.hideKeyboardFrom(getActivity(), getActivity().getCurrentFocus());

        RecipeSearchResult result = sqliteManager.queryResultObjectById(id);
        String recipeYummlyId = result.yummlyId;
        mListener.onSearchResultClick(id, recipeYummlyId);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getContext(),
                RecipesContract.SearchResults.CONTENT_URI,

                // columns: null = all
                null,

                // rows: null = all
                null,
                null,

                // sort order
                RecipesContract.SearchResults._ID + " ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        adapter.swapCursor(cursor);

        if (isToScrollToTop) {
            isToScrollToTop = false;
            listView_searchResults.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }

//-------------------------------------------------------------------------------------------------

    public void OnGotYummlySearchResultsEvent() {
        isToScrollToTop = true;
    }
}
