package com.hanoch.greatrecipes.view;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.api.GGGRecipe2;
import com.hanoch.greatrecipes.model.MyListFragment;

import java.util.ArrayList;

public class ListMyOwnFragment2 extends MyListFragment {

//-------------------------------------------------------------------------------------------------

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        this.savedInstanceState = savedInstanceState;
//
//        checkedItemsIdList = new ArrayList<>();
//
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_my_own_recipes, container, false);
//
//        layout_dialogBubble = (FrameLayout) view.findViewById(R.id.layout_dialogBubble);
//        layout_dialogBubble.setVisibility(View.GONE);
//
//        TextView textView_dialogBubble = (TextView) view.findViewById(R.id.textView_dialogBubble);
//        textView_dialogBubble.setText(getString(R.string.click_here_to_add_recipe));
//
//        listView_myRecipes = (ListView) view.findViewById(R.id.listView_recipesList);
//        listView_myRecipes.setOnItemClickListener(this);
//
//        // Adding a gap (divider-like) at the top of the listView
//        listView_myRecipes.addHeaderView(new View(getContext()), null, true);
//
//        floatingButton_addRecipe = (FloatingActionButton) view.findViewById(R.id.floatingButton_addMyOwnRecipe);
//        floatingButton_addRecipe.setOnClickListener(this);
//
//        // Getting the serving argument from the bundle:
//        extra_serving = getArguments().getString(ARG_EXTRA_SERVING);
//
//        if (extra_serving == null) {
//            listView_myRecipes.setOnItemLongClickListener(this);
//
//        } else {
//            // The user is choosing a serving
//            floatingButton_addRecipe.setVisibility(View.GONE);
//        }
//
//        if (savedInstanceState != null) {
//
//            selectedId = savedInstanceState.getString("selectedId");
//
//            boolean isListViewEnabled = savedInstanceState.getBoolean("isListViewEnabled");
//            listView_myRecipes.setEnabled(isListViewEnabled);
//
//            checkedItemsIdList = savedInstanceState.getStringArrayList("checkedItemsIdList");
//
//            int floatingButton_addRecipeVisibility = savedInstanceState.getInt("floatingButton_addRecipeVisibility");
//            floatingButton_addRecipe.setVisibility(floatingButton_addRecipeVisibility);
//
//            int layout_dialogBubbleVisibility = savedInstanceState.getInt("layout_dialogBubbleVisibility");
//            layout_dialogBubble.setVisibility(layout_dialogBubbleVisibility);
//        }
//
//        adapter = new RecipesListAdapter2(getActivity(), userRecipesList, checkedItemsIdList, selectedId);
//        listView_myRecipes.setAdapter(adapter);
//
//        return view;
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        try {
//            mListListener = (ListFragmentListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement ListFragmentListener");
//        }
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListListener = null;
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        outState.putBoolean("isListViewEnabled", listView_recipes.isEnabled());
//
//        outState.putParcelable("listViewState", listView_recipes.onSaveInstanceState());
//
//        int floatingButton_addRecipeVisibility = floatingButton_addRecipe.getVisibility();
//        outState.putInt("floatingButton_addRecipeVisibility", floatingButton_addRecipeVisibility);
//
//        int layout_dialogBubbleVisibility = layout_dialogBubble.getVisibility();
//        outState.putInt("layout_dialogBubbleVisibility", layout_dialogBubbleVisibility);
//
//        outState.putStringArrayList("checkedItemsIdList", checkedItemsIdList);
//
//        outState.putString("selectedId", selectedId);
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        String recipeId = ((RecipesListAdapter2.ViewHolder) view.getTag()).id;
//        if (checkedItemsIdList.isEmpty()) {
//            // The user wants to review a recipe
//
//            mListListener.onRecipeClick(this, recipeId);
//
//            if (getResources().getBoolean(R.bool.isTablet)) {
//
//                // Tablet only
//
//                if (selectedId.equals(recipeId)) {
//                    // The selected recipe was clicked
//
//                    AnimationHelper.animateUnselectedRecipe(view);
//
//                    selectedId = null;
//                    adapter.onNoSelection();
//
//                    backToDefaultDisplay(true);
//
//                } else {
//                    // An unselected recipe was clicked
//
//                    if (selectedId != null) {
//                        // There is a previous selection
//
//                        View prevSelectedItemView = adapter.getViewById(selectedId);
//                        if (prevSelectedItemView != null) {
//                            AnimationHelper.animateUnselectedRecipe(prevSelectedItemView);
//                        }
//                    }
//
//                    AnimationHelper.animateSelectedRecipe(view, getContext());
//
//                    selectedId = recipeId;
//                    adapter.onRecipeSelected(recipeId);
//                }
//
//                floatingButton_addRecipe.setEnabled(false);
//
//                if (floatingButton_addRecipe.getVisibility() == View.VISIBLE) {
//                    AnimationHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);
//                }
//            }
//
//        } else {
//            // The user is checking/unChecking recipes to delete
//
//            if (checkedItemsIdList.contains(recipeId)) {
//                // Checked item was unchecked
//
//                AnimationHelper.animateUncheckedRecipe(view, getContext());
//                checkedItemsIdList.remove(recipeId);
//
//                if (getResources().getBoolean(R.bool.isTablet)) {
//
//                    if (checkedItemsIdList.isEmpty()) {
//                        // The last checked item was unchecked
//
//                        AnimationHelper.animateUnselectedRecipe(view);
//
//                    } else {
//
//                        if (selectedId.equals(recipeId)) {
//                            // The selected item was unchecked
//
//                            AnimationHelper.animateUnselectedRecipe(view);
//
//                            String prevCheckedItemId = checkedItemsIdList.get(checkedItemsIdList.size() - 1);
//
//                            selectedId = prevCheckedItemId;
//
//                            adapter.onRecipeSelected(selectedId);
//                            View prevSelectedItemView = adapter.getViewById(prevCheckedItemId);
//                            if (prevSelectedItemView != null) {
//                                AnimationHelper.animateSelectedRecipe(prevSelectedItemView, getContext());
//                            }
//                        }
//                    }
//                }
//
//                if (checkedItemsIdList.isEmpty()) {
//                    backToDefaultDisplay(true);
//                }
//
//                mListListener.onListItemChecked(this, recipeId, false);
//
//            } else {
//                // Unchecked item was checked
//                // (at least 1 item was already checked)
//
//                if (getResources().getBoolean(R.bool.isTablet)) {
//
//                    AnimationHelper.animateSelectedRecipe(view, getContext());
//
//                    View previousSelectedItemView = adapter.getViewById(selectedId);
//                    if (previousSelectedItemView != null) {
//                        AnimationHelper.animateUnselectedRecipe(previousSelectedItemView);
//                    }
//
//                    selectedId = recipeId;
//                    adapter.onRecipeSelected(selectedId);
//                }
//
//                checkedItemsIdList.add(recipeId);
//
//                AnimationHelper.animateCheckedRecipe(view, getContext());
//
//                mListListener.onListItemChecked(this, recipeId, true);
//            }
//        }
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//        if (!checkedItemsIdList.isEmpty()) {
//            return false;
//        }
//
//        String recipeId = ((RecipesListAdapter2.ViewHolder) view.getTag()).id;
//
//        if (getResources().getBoolean(R.bool.isTablet)) {
//
//            // Tablet only
//
//            if (selectedId.equals(recipeId)) {
//                // The selected item isn't the one that checked
//
//                if (selectedId != null) {
//                    // There is a previous selection
//
//                    View previousSelectedItemView = adapter.getViewById(selectedId);
//                    if (previousSelectedItemView != null) {
//                        AnimationHelper.animateUnselectedRecipe(previousSelectedItemView);
//                    }
//                }
//
//                selectedId = recipeId;
//
//                AnimationHelper.animateSelectedRecipe(view, getContext());
//
//                adapter.onRecipeSelected(selectedId);
//            }
//        }
//
//        floatingButton_addRecipe.setEnabled(false);
//        AnimationHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);
//
//        AnimationHelper.animateCheckedRecipe(view, getContext());
//
//        checkedItemsIdList.add(recipeId);
//
//        mListListener.onListItemChecked(this, recipeId, true);
//
//        return true;
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//
//            case R.id.floatingButton_addMyOwnRecipe:
//
//                mListListener.onAddNewRecipeClick(this, AppConsts.RecipeOrigin.ADDED_MANUALLY);
//
//                if (getResources().getBoolean(R.bool.isTablet)) {
//
//                    floatingButton_addRecipe.setEnabled(false);
//
//                    AnimationHelper.animateViewFadingOut(getContext(), floatingButton_addRecipe, 1000, 0);
//
//                    if (layout_dialogBubble.getVisibility() == View.VISIBLE) {
//                        AnimationHelper.animateViewFadingOut(getContext(), layout_dialogBubble, 1000, 0);
//                    }
//                }
//
//                break;
//        }
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//
//        return new CursorLoader(
//                getActivity(),
//                RecipesContract.Recipes.CONTENT_URI,
//
//                // columns: null = all
//                null,
//
//                // rows:
//                RecipesContract.Recipes.ORIGIN_INDEX + "=?",
//                new String[]{AppConsts.RecipeOrigin.ADDED_MANUALLY + ""},
//
//                // sort order
//                RecipesContract.Recipes.TITLE + " COLLATE LOCALIZED ASC"
//        );
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//
//        adapter.swapCursor(cursor);
//
//        if (savedInstanceState != null) {
//
//            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
//            listView_myRecipes.onRestoreInstanceState(listViewState);
//        }
//
//        if (extra_serving == null) {
//
//            if (cursor.getCount() == 0) {
//                if (floatingButton_addRecipe.getVisibility() == View.VISIBLE && layout_dialogBubble.getVisibility() != View.VISIBLE) {
//                    AnimationHelper.animateViewFadingIn(getContext(), layout_dialogBubble, 1500, 500);
//                }
//
//            } else {
//                layout_dialogBubble.setVisibility(View.INVISIBLE);
//            }
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//        adapter.swapCursor(null);
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void backToDefaultDisplay(boolean includeAnimation) {
//        // Activated by the activity after closing the toolbar menu
//
//        if (getResources().getBoolean(R.bool.isTablet)) {
//
//            // Tablet only
//
//            if (selectedId != null) {
//                View selectedView = adapter.getViewById(selectedId);
//                if (selectedView != null) {
//                    AnimationHelper.animateUnselectedRecipe(selectedView);
//                }
//                adapter.onNoSelection();
//                selectedId = null;
//            }
//        }
//
//        listView_recipes.setEnabled(true);
//
//        floatingButton_addRecipe.setEnabled(true);
//
//        if (includeAnimation) {
//
//            for (String selectedItemId : checkedItemsIdList) {
//
//                View selectedItemView = adapter.getViewById(selectedItemId);
//                if (selectedItemView != null) {
//                    AnimationHelper.animateUncheckedRecipe(selectedItemView, getContext());
//                }
//            }
//
//            if (floatingButton_addRecipe.getVisibility() != View.VISIBLE) {
//                AnimationHelper.animateViewFadingIn(getContext(), floatingButton_addRecipe, 1000, 1000);
//            }
//
//            if (adapter.getCount() == 0) {
//                if (layout_dialogBubble.getVisibility() != View.VISIBLE) {
//                    AnimationHelper.animateViewFadingIn(getContext(), layout_dialogBubble, 1000, 1000);
//                }
//            }
//
//        } else {
//            // No need for unchecked items animation
//
//            for (String selectedItemId : checkedItemsIdList) {
//
//                View selectedItemView = adapter.getViewById(selectedItemId);
//                if (selectedItemView != null) {
//                    ImageView selectedItemImage = (ImageView) selectedItemView.findViewById(R.id.imageView_itemImage);
//                    ImageView checkedIcon = (ImageView) selectedItemView.findViewById(R.id.imageView_checkedIcon);
//                    checkedIcon.setAlpha(0f);
//                    selectedItemImage.setAlpha(1f);
//                }
//            }
//
//            if (floatingButton_addRecipe.getVisibility() != View.VISIBLE) {
//                floatingButton_addRecipe.setVisibility(View.VISIBLE);
//                floatingButton_addRecipe.setAlpha(1f);
//            }
//
//            if (adapter.getCount() == 0) {
//                if (layout_dialogBubble.getVisibility() != View.VISIBLE) {
//                    layout_dialogBubble.setVisibility(View.VISIBLE);
//                    layout_dialogBubble.setAlpha(1f);
//                }
//            }
//        }
//
//        checkedItemsIdList.clear();
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    @Subscribe
//    public void onListReady(RecipesListsDownloadedEvent event) {
//        adapter.notifyDataSetChanged();
//    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.MY_OWN_RECIPES;
    }

    public void refreshAdapter() {
        adapter.refreshList(new ArrayList<>(appStateManager.user.userRecipes.values()));
    }

}
