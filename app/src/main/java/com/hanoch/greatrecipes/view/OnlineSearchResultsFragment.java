package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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

import com.android.volley.NoConnectionError;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.model.AllergensAndDietPrefItem;
import com.hanoch.greatrecipes.model.RecipeSearchResult;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.model.RecipeSearchResultsResponse;
import com.hanoch.greatrecipes.model.ThinRecipeSearchResult;
import com.hanoch.greatrecipes.view.adapters.SearchResultsAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class OnlineSearchResultsFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentOnlineSearchListener mListener;
    private SearchResultsAdapter adapter;
    private String keyToSearch;
    private ArrayList<AllergensAndDietPrefItem> allowedDietList;
    private ArrayList<AllergensAndDietPrefItem> allowedAllergiesList;
    private View view;

    private DbManager dbManager;
    private Call<RecipeSearchResultsResponse> getSearchResults;

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

        ListView listView_searchResults = (ListView) view.findViewById(R.id.listView_searchResults);
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

        if (getSearchResults != null && getSearchResults.isExecuted()) {
            getSearchResults.cancel();
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

            Snackbar snack = Snackbar.make(view, R.string.at_least_2_chars_required, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(Color.RED);
            snack.show();

            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.searching));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        this.keyToSearch = keyToSearch;

        // Getting the user Diet & Allergies preferences:
        allowedDietList = AppHelper.getUserAllowedDietPrefsList(getContext());
        allowedAllergiesList = AppHelper.getUserAllowedAllergiesPrefsList(getContext());

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
            query.put("q", URLEncoder.encode(this.keyToSearch, "utf-8"));
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

            getSearchResults = ((GreatRecipesApplication) getActivity().getApplication()).getYummlyService().getSearchResults(query, dietList, allergensList);
            getSearchResults.enqueue(new Callback<RecipeSearchResultsResponse>() {
                @Override
                public void onResponse(Call<RecipeSearchResultsResponse> call, retrofit2.Response<RecipeSearchResultsResponse> response) {
                    progressDialog.dismiss();
                    if (response == null || response.body() == null) {

                        Snackbar snack = Snackbar.make(view, R.string.internet_error, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(Color.RED);
                        snack.show();

                        return;
                    }

                    List<ThinRecipeSearchResult> results = response.body().matches;
                    if (results.size() == 0) {

                        Snackbar snack = Snackbar.make(view, R.string.no_results, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(Color.RED);
                        snack.show();

                        return;
                    }

                    // Deleting old search results
                    dbManager.deleteAllSearchResults();

                    // Adding the new search results
                    dbManager.addSearchResults(results);
                }

                @Override
                public void onFailure(Call<RecipeSearchResultsResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    String errorMessage;
                    if (t instanceof NoConnectionError) {
                        errorMessage = getString(R.string.internet_error);

                    } else {
                        errorMessage = getString(R.string.unexpected_error);
                    }

                    Snackbar snack = Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG);
                    ViewGroup group = (ViewGroup) snack.getView();
                    group.setBackgroundColor(Color.RED);
                    snack.show();
                }
            });

        } catch (UnsupportedEncodingException e) {
            progressDialog.dismiss();

            e.printStackTrace();
            Snackbar snack = Snackbar.make(view, R.string.unexpected_error, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(Color.RED);
            snack.show();
        }
    }

//-------------------------------------------------------------------------------------------------

    private boolean searchKeyTooShort(String keyToSearch) {
        // checks if an editText has less than 2 letters excluding spaces.
        int trimmedLength = keyToSearch.trim().length();

        return (trimmedLength < 2);
    }
}
