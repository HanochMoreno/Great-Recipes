package com.hanoch.greatrecipes.view;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.model.MyListFragment;

import java.util.ArrayList;

public class ListOnlineFragment2 extends MyListFragment {

//-------------------------------------------------------------------------------------------------

//    public static ListOnlineFragment2 newInstance(String extra_serving) {
//
//        ListOnlineFragment2 fragment = new ListOnlineFragment2();
//        Bundle args = new Bundle();
//        args.putString(ARG_EXTRA_SERVING, extra_serving);
//        fragment.setArguments(args);
//
//        return fragment;
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        this.savedInstanceState = savedInstanceState;
//
//        selectedId = AppConsts.NO_SELECTION;
//
//        checkedItemsIdList = new ArrayList<>();
//
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_online_recipes, container, false);
//
//        layout_dialogBubble = (FrameLayout) view.findViewById(R.id.layout_dialogBubble);
//        layout_dialogBubble.setVisibility(View.GONE);
//
//        TextView textView_dialogBubble = (TextView) view.findViewById(R.id.textView_dialogBubble);
//        textView_dialogBubble.setText(getString(R.string.click_here_to_add_recipe));
//
//        listView_online = (ListView) view.findViewById(R.id.listView_recipesList);
//
//        listView_online.setOnItemClickListener(this);
//
//        // Adding a gap (divider-like) at the top of the listView
//        listView_online.addHeaderView(new View(getContext()), null, true);
//
//        floatingButton_addRecipe = (FloatingActionButton) view.findViewById(R.id.floatingButton_addOnlineRecipe);
//        floatingButton_addRecipe.setOnClickListener(this);
//
//        getLoaderManager().initLoader(1, null, this);
//
//        // Getting the serving argument from the bundle:
//        extra_serving = getArguments().getString(ARG_EXTRA_SERVING);
//
//        if (extra_serving == null) {
//            listView_online.setOnItemLongClickListener(this);
//
//        } else {
//            // The user is choosing a serving
//            floatingButton_addRecipe.setVisibility(View.GONE);
//        }
//
//        if (savedInstanceState != null) {
//
//            selectedId = savedInstanceState.getLong("selectedId");
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
//        adapter = new RecipesListAdapter(getActivity(), null, checkedItemsIdList, selectedId);
//        listView_online.setAdapter(adapter);
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
//        outState.putParcelable("listViewState", listView_online.onSaveInstanceState());
//
//        outState.putLong("selectedId", selectedId);
//
//        outState.putStringArrayList("checkedItemsIdList", checkedItemsIdList);
//
//        int floatingButton_addRecipeVisibility = floatingButton_addRecipe.getVisibility();
//        outState.putInt("floatingButton_addRecipeVisibility", floatingButton_addRecipeVisibility);
//
//        int layout_dialogBubbleVisibility = layout_dialogBubble.getVisibility();
//        outState.putInt("layout_dialogBubbleVisibility", layout_dialogBubbleVisibility);
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//
//            case R.id.floatingButton_addOnlineRecipe:
//
//                mListListener.onAddNewRecipeClick(this, AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH);
//
//                break;
//        }
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        if (checkedItemsIdList.isEmpty()) {
//            // The user wants to review a recipe
//
//            mListListener.onRecipeClick(this, id);
//
//            if (getResources().getBoolean(R.bool.isTablet)) {
//
//                // Tablet only
//
//                if (selectedId == id) {
//                    // The selected recipe was clicked
//
//                    AnimationHelper.animateUnselectedRecipe(view);
//
//                    selectedId = AppConsts.NO_SELECTION;
//                    adapter.onNoSelection();
//
//                    backToDefaultDisplay(true);
//
//                } else {
//                    // An unselected recipe was clicked
//
//                    if (selectedId != AppConsts.NO_SELECTION) {
//                        // There is a previous selection
//
//                        View prevSelectedItemView = adapter.getViewById(selectedId + "");
//                        if (prevSelectedItemView != null) {
//                            AnimationHelper.animateUnselectedRecipe(prevSelectedItemView);
//                        }
//                    }
//
//                    AnimationHelper.animateSelectedRecipe(view,getContext());
//
//                    selectedId = id;
//                    adapter.onRecipeSelected(id);
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
//            // The user is checking/unchecking recipes to delete
//
//            if (checkedItemsIdList.contains(id + "")) {
//                // Checked item was unchecked
//
//                AnimationHelper.animateUncheckedRecipe(view, getContext());
//                checkedItemsIdList.remove(id + "");
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
//                        if (selectedId == id) {
//                            // The selected item was unchecked
//
//                            AnimationHelper.animateUnselectedRecipe(view);
//
//                            String prevCheckedItemId = checkedItemsIdList.get(checkedItemsIdList.size() - 1);
//
//                            selectedId = (long) Integer.parseInt(prevCheckedItemId);
//
//                            adapter.onRecipeSelected(selectedId);
//                            View prevSelectedItemView = adapter.getViewById(prevCheckedItemId);
//                            if (prevSelectedItemView != null) {
//                                AnimationHelper.animateSelectedRecipe(prevSelectedItemView, getContext());
//
//                            }
//                        }
//                    }
//                }
//
//                if (checkedItemsIdList.isEmpty()) {
//                    backToDefaultDisplay(true);
//                }
//
//                mListListener.onListItemChecked(this, id, false);
//
//            } else {
//                // Unchecked item was checked
//                // (at least 1 item was already checked)
//
//                if (getResources().getBoolean(R.bool.isTablet)) {
//
//                    AnimationHelper.animateSelectedRecipe(view, getContext());
//
//                    View previousSelectedItemView = adapter.getViewById(selectedId + "");
//                    if (previousSelectedItemView != null) {
//                        AnimationHelper.animateUnselectedRecipe(previousSelectedItemView);
//                    }
//
//                    selectedId = id;
//                    adapter.onRecipeSelected(selectedId);
//                }
//
//                checkedItemsIdList.add(id + "");
//
//                AnimationHelper.animateCheckedRecipe(view, getContext());
//
//                mListListener.onListItemChecked(this, id, true);
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
//        if (getResources().getBoolean(R.bool.isTablet)) {
//
//            // Tablet only
//
//            if (selectedId != id) {
//                // The selected item isn't the one that checked
//
//                if (selectedId != AppConsts.NO_SELECTION) {
//                    // There is a previous selection
//
//                    View previousSelectedItemView = adapter.getViewById(selectedId + "");
//                    if (previousSelectedItemView != null) {
//                        AnimationHelper.animateUnselectedRecipe(previousSelectedItemView);
//                    }
//                }
//
//                selectedId = id;
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
//        checkedItemsIdList.add(id + "");
//
//        mListListener.onListItemChecked(this, id, true);
//
//        return true;
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void backToDefaultDisplay(boolean includeAnimation) {
//
//        if (getResources().getBoolean(R.bool.isTablet)) {
//
//            // Tablet only
//
//            if (selectedId != AppConsts.NO_SELECTION) {
//                View selectedView = adapter.getViewById(selectedId + "");
//                if (selectedView != null) {
//                    AnimationHelper.animateUnselectedRecipe(selectedView);
//                }
//                adapter.onNoSelection();
//                selectedId = AppConsts.NO_SELECTION;
//            }
//        }
//
//        listView_online.setEnabled(true);
//
//        floatingButton_addRecipe.setEnabled(true);
//
//        if (includeAnimation) {
//
//            for (String selectedItemId : checkedItemsIdList) {
//
//                View checkedItemView = adapter.getViewById(selectedItemId);
//                if (checkedItemView != null) {
//                    AnimationHelper.animateUncheckedRecipe(checkedItemView, getContext());
//                }
//            }
//
//            if (floatingButton_addRecipe.getVisibility() != View.VISIBLE) {
//                AnimationHelper.animateViewFadingIn(getContext(), floatingButton_addRecipe, 1000, 1000);
//            }
//
//        } else {
//            // No need for unchecked items animation
//
//            for (String checkedItemId : checkedItemsIdList) {
//
//                View checkedItemView = adapter.getViewById(checkedItemId);
//                if (checkedItemView != null) {
//                    ImageView checkedItemImage = (ImageView) checkedItemView.findViewById(R.id.imageView_itemImage);
//                    ImageView checkedIcon = (ImageView) checkedItemView.findViewById(R.id.imageView_checkedIcon);
//                    checkedIcon.setAlpha(0f);
//                    checkedItemImage.setAlpha(1f);
//                }
//            }
//
//            if (floatingButton_addRecipe.getVisibility() != View.VISIBLE) {
//                floatingButton_addRecipe.setVisibility(View.VISIBLE);
//                floatingButton_addRecipe.setAlpha(1f);
//            }
//        }
//
//        checkedItemsIdList.clear();
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
//                new String[]{AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH + ""},
//
//                // sort order
//                RecipesContract.Recipes.TITLE + " COLLATE LOCALIZED ASC"
//        );
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//
//        adapter.swapCursor(cursor);
//
//        if (savedInstanceState != null) {
//
//            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
//            listView_online.onRestoreInstanceState(listViewState);
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

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//        adapter.swapCursor(null);
//    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.ONLINE_RECIPES;
    }

    public void refreshAdapter() {
        adapter.refreshList(new ArrayList<>(appStateManager.user.yummlyRecipes.values()));
    }
}
