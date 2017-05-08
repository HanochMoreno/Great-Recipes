package com.hanoch.greatrecipes.view;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.MyFragment;
import com.hanoch.greatrecipes.view.adapters.RecipesListAdapter;

import java.util.ArrayList;


public class ListFavouriteFragment extends MyFragment
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_EXTRA_SERVING = "serving";

    private ListFragmentListener mListListener;

    private ListView listView_favourites;
    private RecipesListAdapter adapter;
    private Bundle savedInstanceState;

    private long selectedId;
    private Dialog favouritesDialog;
    private boolean showFavouritesDialogOnLoadFinished;
    private String extra_serving;

//-------------------------------------------------------------------------------------------------

    public static ListFavouriteFragment newInstance(String extra_serving) {

        ListFavouriteFragment fragment = new ListFavouriteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.savedInstanceState = savedInstanceState;

        showFavouritesDialogOnLoadFinished = true;

        selectedId = AppConsts.NO_SELECTION;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourite_recipes, container, false);

        listView_favourites = (ListView) view.findViewById(R.id.listView_recipesList);

        listView_favourites.setOnItemClickListener(this);

        // Adding a gap (divider-like) at the top of the listView
        listView_favourites.addHeaderView(new View(getContext()), null, true);

        getLoaderManager().initLoader(1, null, this);

        // Getting the serving argument from the bundle:
        extra_serving = getArguments().getString(ARG_EXTRA_SERVING);

        if (savedInstanceState != null) {

            boolean favouritesDialogIsShowing = savedInstanceState.getBoolean("favouritesDialogIsShowing");
            if (favouritesDialogIsShowing) {
                boolean currentCheckboxState = savedInstanceState.getBoolean("currentCheckboxState");
                showFavouritesDialog(currentCheckboxState);
            }

            showFavouritesDialogOnLoadFinished = savedInstanceState.getBoolean("showFavouritesDialogOnLoadFinished");

            selectedId = savedInstanceState.getLong("selectedId");

            boolean isListViewEnabled = savedInstanceState.getBoolean("isListViewEnabled");
            listView_favourites.setEnabled(isListViewEnabled);
        }

        adapter = new RecipesListAdapter(getActivity(), null, new ArrayList<String>(), selectedId);
        listView_favourites.setAdapter(adapter);

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
    public void onDetach() {
        super.onDetach();
        mListListener = null;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (favouritesDialog != null && favouritesDialog.isShowing()) {
            outState.putBoolean("favouritesDialogIsShowing", true);

            CheckBox checkBox = (CheckBox) favouritesDialog.findViewById(R.id.checkBox_dontShowAgain);
            outState.putBoolean("currentCheckboxState", checkBox.isChecked());
        }

        outState.putBoolean("showFavouritesDialogOnLoadFinished", showFavouritesDialogOnLoadFinished);

        outState.putLong("selectedId", selectedId);

        outState.putBoolean("isListViewEnabled", listView_favourites.isEnabled());

        outState.putParcelable("listViewState", listView_favourites.onSaveInstanceState());
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
    }

//-------------------------------------------------------------------------------------------------

    private void showFavouritesDialog(boolean isCheckboxChecked) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(AppConsts.SharedPrefs.FAVOURITES_DIALOG_WAS_SHOWN, true);
        editor.commit();

        favouritesDialog = new Dialog(getContext());

        // hide to default title for Dialog
        favouritesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Context context = getActivity();

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_favourites, null, false);
        favouritesDialog.setCanceledOnTouchOutside(false);
        favouritesDialog.setContentView(view);

        final CheckBox checkBox = (CheckBox) favouritesDialog.findViewById(R.id.checkBox_dontShowAgain);
        checkBox.setChecked(isCheckboxChecked);

        Button button_gotIt = (Button) favouritesDialog.findViewById(R.id.button_gotIt);
        button_gotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sp.edit();

                if (checkBox.isChecked()) {
                    editor.putBoolean(AppConsts.SharedPrefs.SHOW_FAVOURITES_DIALOG, false);
                    editor.apply();
                }

                // Close the favouritesDialog
                favouritesDialog.dismiss();
            }
        });

        // Display the favouritesDialog
        favouritesDialog.show();
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
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(
                getActivity(),
                RecipesContract.Recipes.CONTENT_URI,

                // columns: null = all
                null,

                // rows:
                RecipesContract.Recipes.FAVOURITE_INDEX + "=?",
                new String[]{AppConsts.FavouriteIndex.FAVOURITE + ""},

                // sort order
                RecipesContract.Recipes._ID + " ASC"
        );
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        adapter.swapCursor(cursor);

        if (savedInstanceState != null) {

            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
            listView_favourites.onRestoreInstanceState(listViewState);
        }

        if (cursor.getCount() == 0 && showFavouritesDialogOnLoadFinished && extra_serving == null) {

            // Getting the user "Show Favourites Dialog" preference
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean showFavouritesDialogPref = sp.getBoolean(AppConsts.SharedPrefs.SHOW_FAVOURITES_DIALOG, true);

            if (showFavouritesDialogPref)
                showFavouritesDialogPref = !sp.getBoolean(AppConsts.SharedPrefs.FAVOURITES_DIALOG_WAS_SHOWN, false);

            int rateUsDialogCounterPref = sp.getInt(AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER, 1);
            boolean showRateUsDialogPref = (rateUsDialogCounterPref != AppConsts.SharedPrefs.RATE_US_DIALOG_NEVER_SHOW_AGAIN && rateUsDialogCounterPref % AppConsts.SharedPrefs.RATE_US_DIALOG_COUNTER_REPEAT == 0);

            if (showFavouritesDialogPref && !showRateUsDialogPref) showFavouritesDialog(false);
        }

        showFavouritesDialogOnLoadFinished = false;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.FAVOURITES_RECIPES;
    }

}
