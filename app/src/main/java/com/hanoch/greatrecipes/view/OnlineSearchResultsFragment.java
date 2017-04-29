package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.model.AllergensAndDietPrefItem;
import com.hanoch.greatrecipes.model.RecipeSearchResult;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.yummly_api.SearchResultsResponse;
import com.hanoch.greatrecipes.view.adapters.SearchResultsAdapter;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OnlineSearchResultsFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentOnlineSearchListener mListener;
    private SearchResultsAdapter adapter;
    private ListView listView_searchResults;

    private View view;

    private DbManager dbManager;
    private Subscriber<SearchResultsResponse> subscriber;
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

        dbManager = ((GreatRecipesApplication) getActivity().getApplication()).getDbManager();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
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

        if (subscriber != null && !subscriber.isUnsubscribed()) {
            subscriber.unsubscribe();
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AppHelper.hideKeyboardFrom(getActivity(), getActivity().getCurrentFocus());

        RecipeSearchResult result = dbManager.queryResultObjectById(id);
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

    public void performOnlineSearch(String keyToSearch) {

        if (searchKeyTooShort(keyToSearch)) {
            // checking if 'recipe's title search' field contains at least 2 chars
            // will return 'true' also in case of input contains only spaces

            AppHelper.showSnackBar(view, R.string.at_least_2_chars_required, Color.RED);
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.searching));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        // Getting the user Diet & Allergies preferences:
        ArrayList<AllergensAndDietPrefItem> allowedDietList = AppHelper.getUserAllowedDietPrefsList(getContext());
        ArrayList<AllergensAndDietPrefItem> allowedAllergiesList = AppHelper.getUserAllowedAllergiesPrefsList(getContext());

        // Getting the user's Max. Online Search Results preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        String maxOnlineSearchResultsPref;
        if (premium) {
            maxOnlineSearchResultsPref = sp.getString(AppConsts.SharedPrefs.MAX_ONLINE_SEARCH_RESULTS, "10");

        } else {
            maxOnlineSearchResultsPref = "5";
        }

        try {
            HashMap<String, String> query = new HashMap<>();
            query.put("q", URLEncoder.encode(keyToSearch, "utf-8"));
            query.put("start", URLEncoder.encode("0", "utf-8"));
            query.put("maxResult", URLEncoder.encode(maxOnlineSearchResultsPref, "utf-8"));
            query.put("_app_id", URLEncoder.encode(AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL, "utf-8"));
            query.put("_app_key", URLEncoder.encode(AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL, "utf-8"));

            List<String> dietList = new ArrayList<>();
            for (AllergensAndDietPrefItem dietItem : allowedDietList) {
                dietList.add(URLEncoder.encode(dietItem.searchKeyName, "utf-8"));
            }

            List<String> allergensList = new ArrayList<>();
            for (AllergensAndDietPrefItem allergenItem : allowedAllergiesList) {
                allergensList.add(URLEncoder.encode(allergenItem.searchKeyName, "utf-8"));
            }

            subscriber = new Subscriber<SearchResultsResponse>() {
                @Override
                public final void onCompleted() {
                }

                @Override
                public final void onError(Throwable e) {
                    progressDialog.dismiss();
                    Log.e("subscriber", "onError: message: " + e.getMessage() + ", cause: " + e.getCause());
                    if (e instanceof UnknownHostException || e instanceof ConnectException) {
                        AppHelper.showSnackBar(view, R.string.internet_error, Color.RED);
                    } else {
                        AppHelper.showSnackBar(view, R.string.unexpected_error, Color.RED);
                    }
                }

                @Override
                public final void onNext(SearchResultsResponse response) {
                    progressDialog.dismiss();

                    if (response.matches == null || response.matches.isEmpty()) {
                        // No results
                        AppHelper.showSnackBar(view, R.string.no_results, Color.RED);
                        return;
                    }

                    // Deleting old search results
                    dbManager.deleteAllSearchResults();

                    // Adding the new search results
                    dbManager.addSearchResults(response.matches);

                    isToScrollToTop = true;
                }
            };

            Single<SearchResultsResponse> getSearchResults = ((GreatRecipesApplication) getActivity().getApplication()).getYummlyApi().getSearchResults(query, dietList, allergensList);
            getSearchResults
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);

        } catch (UnsupportedEncodingException e) {
            progressDialog.dismiss();
            AppHelper.showSnackBar(view, R.string.unexpected_error, Color.RED);
            e.printStackTrace();
        }
    }

//-------------------------------------------------------------------------------------------------

    private boolean searchKeyTooShort(String keyToSearch) {
        // checks if an editText has less than 2 letters excluding spaces.
        int trimmedLength = keyToSearch.trim().length();

        return (trimmedLength < 2);
    }
}
