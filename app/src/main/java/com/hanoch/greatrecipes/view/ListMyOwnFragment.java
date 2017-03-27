package com.hanoch.greatrecipes.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.model.MyFragment;
import com.hanoch.greatrecipes.view.adapters.RecipesListAdapter;

import java.util.ArrayList;

public class ListMyOwnFragment extends MyFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener,
        View.OnClickListener,
        AdapterView.OnItemLongClickListener {

    private static final String ARG_EXTRA_SERVING = "serving";

    private ListFragmentListener mListListener;

    private ListView listView_myRecipes;
    private FloatingActionButton floatingButton_addRecipe;

    private RecipesListAdapter adapter;

    private FrameLayout layout_dialogBubble;

    private ArrayList<String> checkedItemsIdList;

    private Bundle savedInstanceState;

    private String extra_serving;
    private long selectedId;

//-------------------------------------------------------------------------------------------------

    public static ListMyOwnFragment newInstance(String extra_serving) {

        ListMyOwnFragment fragment = new ListMyOwnFragment();
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

        selectedId = AppConsts.NO_SELECTION;

        checkedItemsIdList = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_own_recipes, container, false);

        layout_dialogBubble = (FrameLayout) view.findViewById(R.id.layout_dialogBubble);
        layout_dialogBubble.setVisibility(View.GONE);

        TextView textView_dialogBubble = (TextView) view.findViewById(R.id.textView_dialogBubble);
        textView_dialogBubble.setText(getString(R.string.click_here_to_add_recipe));

        listView_myRecipes = (ListView) view.findViewById(R.id.listView_recipesList);
        listView_myRecipes.setOnItemClickListener(this);

        // Adding a gap (divider-like) at the top of the listView
        listView_myRecipes.addHeaderView(new View(getContext()), null, true);

        floatingButton_addRecipe = (FloatingActionButton) view.findViewById(R.id.floatingButton_addMyOwnRecipe);
        floatingButton_addRecipe.setOnClickListener(this);

        getLoaderManager().initLoader(1, null, this);

        // Getting the serving argument from the bundle:
        extra_serving = getArguments().getString(ARG_EXTRA_SERVING);

        if (extra_serving == null) {
            listView_myRecipes.setOnItemLongClickListener(this);

        } else {
            // The user is choosing a serving
            floatingButton_addRecipe.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {

            selectedId = savedInstanceState.getLong("selectedId");

            boolean isListViewEnabled = savedInstanceState.getBoolean("isListViewEnabled");
            listView_myRecipes.setEnabled(isListViewEnabled);

            checkedItemsIdList = savedInstanceState.getStringArrayList("checkedItemsIdList");

            int floatingButton_addRecipeVisibility = savedInstanceState.getInt("floatingButton_addRecipeVisibility");
            floatingButton_addRecipe.setVisibility(floatingButton_addRecipeVisibility);

            int layout_dialogBubbleVisibility = savedInstanceState.getInt("layout_dialogBubbleVisibility");
            layout_dialogBubble.setVisibility(layout_dialogBubbleVisibility);
        }

        adapter = new RecipesListAdapter(getActivity(), null, checkedItemsIdList, selectedId);
        listView_myRecipes.setAdapter(adapter);

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

        outState.putBoolean("isListViewEnabled", listView_myRecipes.isEnabled());

        outState.putParcelable("listViewState", listView_myRecipes.onSaveInstanceState());

        int floatingButton_addRecipeVisibility = floatingButton_addRecipe.getVisibility();
        outState.putInt("floatingButton_addRecipeVisibility", floatingButton_addRecipeVisibility);

        int layout_dialogBubbleVisibility = layout_dialogBubble.getVisibility();
        outState.putInt("layout_dialogBubbleVisibility", layout_dialogBubbleVisibility);

        outState.putStringArrayList("checkedItemsIdList", checkedItemsIdList);

        outState.putLong("selectedId", selectedId);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (checkedItemsIdList.isEmpty()) {
            // The user wants to review a recipe

            mListListener.onRecipeClick(this, id);

//            if (!isAPhone()) {
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

                floatingButton_addRecipe.setEnabled(false);

                if (floatingButton_addRecipe.getVisibility() == View.VISIBLE) {
                    AppHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);
                }
            }

        } else {
            // The user is checking/unchecking recipes to delete

            if (checkedItemsIdList.contains(id + "")) {
                // Checked item was unchecked

                AppHelper.animateUncheckedRecipe(view, getContext());
                checkedItemsIdList.remove(id + "");

//                if (!isAPhone()) {
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

//                if (!isAPhone()) {
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

//        if (!isAPhone()) {
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

        floatingButton_addRecipe.setEnabled(false);
        AppHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);

        AppHelper.animateCheckedRecipe(view, getContext());

        checkedItemsIdList.add(id + "");

        mListListener.onListItemChecked(this, id, true);

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_addMyOwnRecipe:

                mListListener.onAddNewRecipeClick(this, AppConsts.RecipeOrigin.ADDED_MANUALLY);

//                if (!isAPhone()) {
                if (getResources().getBoolean(R.bool.isTablet)) {

                    floatingButton_addRecipe.setEnabled(false);

                    AppHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);

                    if (layout_dialogBubble.getVisibility() == View.VISIBLE) {
                        AppHelper.animateViewFadingOut(getContext(), layout_dialogBubble, 1000, 0);
                    }
                }

                break;
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
                RecipesContract.Recipes.ORIGIN_INDEX + "=?",
                new String[]{AppConsts.RecipeOrigin.ADDED_MANUALLY + ""},

                // sort order
                RecipesContract.Recipes.TITLE + " COLLATE LOCALIZED ASC"
        );
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        adapter.swapCursor(cursor);

        if (savedInstanceState != null) {

            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
            listView_myRecipes.onRestoreInstanceState(listViewState);
        }

        if (extra_serving == null) {

            if (cursor.getCount() == 0) {
                if (floatingButton_addRecipe.getVisibility() == View.VISIBLE && layout_dialogBubble.getVisibility() != View.VISIBLE) {
                    AppHelper.animateViewFadingIn(getContext(), layout_dialogBubble, 1500, 500);
                }

            } else {
                layout_dialogBubble.setVisibility(View.INVISIBLE);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void backToDefaultDisplay(boolean includeAnimation) {
        // Activated by the activity after closing the toolbar menu

//        if (!isAPhone()) {
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

        listView_myRecipes.setEnabled(true);

        floatingButton_addRecipe.setEnabled(true);

        if (includeAnimation) {

            for (String selectedItemId : checkedItemsIdList) {

                View selectedItemView = adapter.getViewById(selectedItemId);
                if (selectedItemView != null) {
                    AppHelper.animateUncheckedRecipe(selectedItemView, getContext());
                }
            }

            if (floatingButton_addRecipe.getVisibility() != View.VISIBLE) {
                AppHelper.animateViewFadingIn(getContext(), floatingButton_addRecipe, 1000, 1000);
            }

            if (adapter.getCount() == 0) {
                if (layout_dialogBubble.getVisibility() != View.VISIBLE) {
                    AppHelper.animateViewFadingIn(getContext(), layout_dialogBubble, 1000, 1000);
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

            if (floatingButton_addRecipe.getVisibility() != View.VISIBLE) {
                floatingButton_addRecipe.setVisibility(View.VISIBLE);
                floatingButton_addRecipe.setAlpha(1f);
            }

            if (adapter.getCount() == 0) {
                if (layout_dialogBubble.getVisibility() != View.VISIBLE) {
                    layout_dialogBubble.setVisibility(View.VISIBLE);
                    layout_dialogBubble.setAlpha(1f);
                }
            }
        }

        checkedItemsIdList.clear();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.MY_OWN_RECIPES;
    }


}
