package com.hanoch.greatrecipes.model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.hanoch.greatrecipes.api.GGGRecipe2;
import com.hanoch.greatrecipes.bus.RecipesListsDownloadedEvent;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.view.adapters.RecipesListAdapter2;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;


public abstract class MyFragment extends Fragment
        implements AdapterView.OnItemClickListener,
        View.OnClickListener,
        AdapterView.OnItemLongClickListener {

    protected static final String ARG_EXTRA_SERVING = "serving";

    protected ListFragmentListener mListListener;

    protected ListView listView_recipes;
    protected FloatingActionButton floatingButton_addRecipe;

    //    private RecipesListAdapter adapter;
    protected RecipesListAdapter2 adapter;

    protected FrameLayout layout_dialogBubble;

    protected ArrayList<String> checkedItemsIdList;
    protected ArrayList<GGGRecipe2> recipesList = new ArrayList<>();

    protected Bundle savedInstanceState;

    protected String extra_serving;
    protected String selectedId;

    public static final String NO_SELECTION = "NO_SELECTION";

    public int getContainedListType() {
        return 0;
    }

//-------------------------------------------------------------------------------------------------

    public static MyFragment newInstance(MyFragment fragment, String extra_serving) {
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;

        checkedItemsIdList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_recipes_list, container, false);

        layout_dialogBubble = (FrameLayout) view.findViewById(R.id.layout_dialogBubble);
        layout_dialogBubble.setVisibility(View.GONE);

        TextView textView_dialogBubble = (TextView) view.findViewById(R.id.textView_dialogBubble);
        textView_dialogBubble.setText(getString(R.string.click_here_to_add_recipe));

        listView_recipes = (ListView) view.findViewById(R.id.listView_recipesList);
        listView_recipes.setOnItemClickListener(this);

        // Adding a gap (divider-like) at the top of the listView
        listView_recipes.addHeaderView(new View(getContext()), null, true);

        floatingButton_addRecipe = (FloatingActionButton) view.findViewById(R.id.floatingButton_addRecipe);
        floatingButton_addRecipe.setOnClickListener(this);

        extra_serving = getArguments().getString(ARG_EXTRA_SERVING);

        if (extra_serving == null) {
            listView_recipes.setOnItemLongClickListener(this);

        } else {
            // The user is choosing a serving
            floatingButton_addRecipe.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {

            selectedId = savedInstanceState.getString("selectedId");

            boolean isListViewEnabled = savedInstanceState.getBoolean("isListViewEnabled");
            listView_recipes.setEnabled(isListViewEnabled);

            checkedItemsIdList = savedInstanceState.getStringArrayList("checkedItemsIdList");

            int floatingButton_addRecipeVisibility = savedInstanceState.getInt("floatingButton_addRecipeVisibility");
            floatingButton_addRecipe.setVisibility(floatingButton_addRecipeVisibility);

            int layout_dialogBubbleVisibility = savedInstanceState.getInt("layout_dialogBubbleVisibility");
            layout_dialogBubble.setVisibility(layout_dialogBubbleVisibility);
        }

        adapter = new RecipesListAdapter2(getActivity(), recipesList, checkedItemsIdList, selectedId);

        listView_recipes.setAdapter(adapter);

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

        outState.putParcelable("listViewState", listView_recipes.onSaveInstanceState());

        outState.putString("selectedId", selectedId);

        outState.putStringArrayList("checkedItemsIdList", checkedItemsIdList);

        int floatingButton_addRecipeVisibility = floatingButton_addRecipe.getVisibility();
        outState.putInt("floatingButton_addRecipeVisibility", floatingButton_addRecipeVisibility);

        int layout_dialogBubbleVisibility = layout_dialogBubble.getVisibility();
        outState.putInt("layout_dialogBubbleVisibility", layout_dialogBubbleVisibility);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String recipeId = ((RecipesListAdapter2.ViewHolder) view.getTag()).id;
        if (checkedItemsIdList.isEmpty()) {
            // The user wants to review a recipe

            mListListener.onRecipeClick(this, recipeId);

            if (getResources().getBoolean(R.bool.isTablet)) {

                // Tablet only

                if (selectedId.equals(recipeId)) {
                    // The selected recipe was clicked

                    AppHelper.setUnselectedRecipe(view);

                    selectedId = null;
                    adapter.onNoSelection();

                    backToDefaultDisplay(true);

                } else {
                    // An unselected recipe was clicked

                    if (selectedId != null) {
                        // There is a previous selection

                        View prevSelectedItemView = adapter.getViewById(selectedId);
                        if (prevSelectedItemView != null) {
                            AppHelper.setUnselectedRecipe(prevSelectedItemView);
                        }
                    }

                    AppHelper.setSelectedRecipe(view, getContext());

                    selectedId = recipeId;
                    adapter.onRecipeSelected(recipeId);
                }

                floatingButton_addRecipe.setEnabled(false);

                if (floatingButton_addRecipe.getVisibility() == View.VISIBLE) {
                    AppHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);
                }
            }

        } else {
            // The user is checking/unChecking recipes to delete

            if (checkedItemsIdList.contains(recipeId)) {
                // Checked item was unchecked

                AppHelper.animateUncheckedRecipe(view, getContext());
                checkedItemsIdList.remove(recipeId);

                if (getResources().getBoolean(R.bool.isTablet)) {

                    if (checkedItemsIdList.isEmpty()) {
                        // The last checked item was unchecked

                        AppHelper.setUnselectedRecipe(view);

                    } else {

                        if (selectedId.equals(recipeId)) {
                            // The selected item was unchecked

                            AppHelper.setUnselectedRecipe(view);

                            String prevCheckedItemId = checkedItemsIdList.get(checkedItemsIdList.size() - 1);

                            selectedId = prevCheckedItemId;

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

                mListListener.onListItemChecked(this, recipeId, false);

            } else {
                // Unchecked item was checked
                // (at least 1 item was already checked)

                if (getResources().getBoolean(R.bool.isTablet)) {

                    AppHelper.setSelectedRecipe(view, getContext());

                    View previousSelectedItemView = adapter.getViewById(selectedId);
                    if (previousSelectedItemView != null) {
                        AppHelper.setUnselectedRecipe(previousSelectedItemView);
                    }

                    selectedId = recipeId;
                    adapter.onRecipeSelected(selectedId);
                }

                checkedItemsIdList.add(recipeId);

                AppHelper.animateCheckedRecipe(view, getContext());

                mListListener.onListItemChecked(this, recipeId, true);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (!checkedItemsIdList.isEmpty()) {
            return false;
        }

        String recipeId = ((RecipesListAdapter2.ViewHolder) view.getTag()).id;

        if (getResources().getBoolean(R.bool.isTablet)) {

            // Tablet only

            if (selectedId.equals(recipeId)) {
                // The selected item isn't the one that checked

                if (selectedId != null) {
                    // There is a previous selection

                    View previousSelectedItemView = adapter.getViewById(selectedId);
                    if (previousSelectedItemView != null) {
                        AppHelper.setUnselectedRecipe(previousSelectedItemView);
                    }
                }

                selectedId = recipeId;

                AppHelper.setSelectedRecipe(view, getContext());

                adapter.onRecipeSelected(selectedId);
            }
        }

        floatingButton_addRecipe.setEnabled(false);
        AppHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);

        AppHelper.animateCheckedRecipe(view, getContext());

        checkedItemsIdList.add(recipeId);

        mListListener.onListItemChecked(this, recipeId, true);

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_addRecipe:

                mListListener.onAddNewRecipeClick(this, AppConsts.RecipeOrigin.ADDED_MANUALLY);

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

    public void backToDefaultDisplay(boolean includeAnimation) {
        // Activated by the activity after closing the toolbar menu

        if (getResources().getBoolean(R.bool.isTablet)) {

            // Tablet only

            if (selectedId != null) {
                View selectedView = adapter.getViewById(selectedId);
                if (selectedView != null) {
                    AppHelper.setUnselectedRecipe(selectedView);
                }
                adapter.onNoSelection();
                selectedId = null;
            }
        }

        listView_recipes.setEnabled(true);

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

    @Subscribe
    public void onEvent(RecipesListsDownloadedEvent event) {
        adapter.notifyDataSetChanged();
    }
//-------------------------------------------------------------------------------------------------

}
