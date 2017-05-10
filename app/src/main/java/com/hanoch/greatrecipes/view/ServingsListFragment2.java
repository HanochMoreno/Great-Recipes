package com.hanoch.greatrecipes.view;

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

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.view.adapters.ServingsListAdapter;

import java.util.ArrayList;


public class ServingsListFragment2 extends Fragment implements
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private FragmentServingsListListener mListener;
    public ServingsListAdapter adapter;

    private Bundle savedInstanceState;

    private ListView listView_servingsList;
    private FloatingActionButton floatingButton_addServing;
    private FrameLayout layout_dialogBubble;

    private ArrayList<String> checkedItemsIdList;

//-------------------------------------------------------------------------------------------------

    public static ServingsListFragment2 newInstance() {
        return new ServingsListFragment2();
    }

//-------------------------------------------------------------------------------------------------

    public interface FragmentServingsListListener {
        void showRecipeDetails(Serving serving);

        void onServingChecked(Serving serving, boolean isChecked);

        void onAddNewServingClick();

        void onListSizeChanged(int listSize);
    }

//-------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;

        checkedItemsIdList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_create_meal, container, false);

        layout_dialogBubble = (FrameLayout) view.findViewById(R.id.layout_dialogBubble);
        layout_dialogBubble.setVisibility(View.GONE);

        TextView textView_dialogBubble = (TextView) view.findViewById(R.id.textView_dialogBubble);
        textView_dialogBubble.setText(getString(R.string.click_here_to_start_planning_meal));

        listView_servingsList = (ListView) view.findViewById(R.id.listView_servingsList);
        listView_servingsList.setOnItemClickListener(this);

        // Adding gaps (divider-like) at the top and bottom of the listView
        listView_servingsList.addFooterView(new View(getContext()), null, true);
        listView_servingsList.addHeaderView(new View(getContext()), null, true);

        floatingButton_addServing = (FloatingActionButton) view.findViewById(R.id.floatingButton_addServing);
        floatingButton_addServing.setOnClickListener(this);

        listView_servingsList.setOnItemLongClickListener(this);

        if (savedInstanceState != null) {

            checkedItemsIdList = savedInstanceState.getStringArrayList("checkedItemsIdList");

            int floatingButton_addServingVisibility = savedInstanceState.getInt("floatingButton_addServingVisibility");
            floatingButton_addServing.setVisibility(floatingButton_addServingVisibility);

            int layout_dialogBubbleVisibility = savedInstanceState.getInt("layout_dialogBubbleVisibility");
            layout_dialogBubble.setVisibility(layout_dialogBubbleVisibility);
        }

        adapter = new ServingsListAdapter(getContext(), checkedItemsIdList);
        listView_servingsList.setAdapter(adapter);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (FragmentServingsListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentServingsListListener");
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("listViewState", listView_servingsList.onSaveInstanceState());

        int floatingButton_addServingVisibility = floatingButton_addServing.getVisibility();
        outState.putInt("floatingButton_addServingVisibility", floatingButton_addServingVisibility);

        int layout_dialogBubbleVisibility = layout_dialogBubble.getVisibility();
        outState.putInt("layout_dialogBubbleVisibility", layout_dialogBubbleVisibility);

        outState.putStringArrayList("checkedItemsIdList", checkedItemsIdList);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_addServing:

                mListener.onAddNewServingClick();
                AppHelper.hideTheKeyboard(getActivity());
                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String servingId = ((ServingsListAdapter.ViewHolder) view.getTag()).servingId;
        Serving serving = null;
        for (Serving servingModel : AppStateManager.getInstance().user.servings.values()) {
            if (servingModel.servingId.equals(servingId)) {
                serving = servingModel;
                break;
            }
        }

        if (serving != null) {

            if (checkedItemsIdList.isEmpty()) {
                mListener.showRecipeDetails(serving);
            } else {
                // The user is checking/unChecking recipes to delete

                if (checkedItemsIdList.contains(servingId)) {
                    // Checked item was unchecked

                    AnimationHelper.animateUncheckedServing(view, getContext());
                    checkedItemsIdList.remove(servingId);

                    //adapter.onServingUnchecked(id + "");

                    if (checkedItemsIdList.isEmpty()) {
                        // The last checked item was unchecked

                        backToDefaultDisplay(true);
                    }

                    mListener.onServingChecked(serving, false);

                } else {
                    // Unchecked item was checked (at least 2 item are checked now)

                    //adapter.onServingChecked(id + "");

                    checkedItemsIdList.add(servingId);

                    AnimationHelper.animateCheckedServing(view, getContext());

                    mListener.onServingChecked(serving, true);
                }

            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (!checkedItemsIdList.isEmpty()) {
            return false;
        }

        String servingId = ((ServingsListAdapter.ViewHolder) view.getTag()).servingId;

        floatingButton_addServing.setEnabled(false);
        AnimationHelper.animateViewFadingOut(getContext(), floatingButton_addServing, 1000, 0);

        AnimationHelper.animateCheckedServing(view, getContext());

        checkedItemsIdList.add(servingId);

        Serving serving = AppStateManager.getInstance().user.servings.get(servingId);
        mListener.onServingChecked(serving, true);

        return true;
    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//
//        return new CursorLoader(
//                getActivity(),
//                RecipesContract.MealPlanning.CONTENT_URI,
//
//                // columns: null = all
//                null,
//
//                // rows:
//                null,
//                null,
//
//                // sort order
//                RecipesContract.MealPlanning._ID + " ASC"
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
//        mListener.onListSizeChanged(cursor.getCount());
//
//        if (savedInstanceState != null) {
//
//            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
//            listView_servingsList.onRestoreInstanceState(listViewState);
//        }
//
//        if (cursor.getCount() == 0) {
//
//            if (floatingButton_addServing.getVisibility() == View.VISIBLE && layout_dialogBubble.getVisibility() != View.VISIBLE) {
//                AnimationHelper.animateViewFadingIn(getContext(), layout_dialogBubble, 1500, 500);
//            }
//
//        } else {
//            layout_dialogBubble.setVisibility(View.INVISIBLE);
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

    public void backToDefaultDisplay(boolean includeAnimation) {

        floatingButton_addServing.setEnabled(true);

        if (includeAnimation) {

            for (String selectedItemId : checkedItemsIdList) {

                View selectedItemView = adapter.getViewById(selectedItemId);
                if (selectedItemView != null) {
                    AnimationHelper.animateUncheckedServing(selectedItemView, getContext());
                }
            }

            AnimationHelper.animateViewFadingIn(getContext(), floatingButton_addServing, 1000, 1000);

        } else {
            // After deleting an item(s) - no need for unchecked items animation

            for (String selectedItemId : checkedItemsIdList) {

                View selectedItemView = adapter.getViewById(selectedItemId);
                if (selectedItemView != null) {
                    ImageView selectedItemImage = (ImageView) selectedItemView.findViewById(R.id.imageView_servingImage);
                    ImageView checkedIcon = (ImageView) selectedItemView.findViewById(R.id.imageView_checkedIcon);
                    checkedIcon.setAlpha(0f);
                    selectedItemImage.setAlpha(1f);
                }
            }

            floatingButton_addServing.setVisibility(View.VISIBLE);
            floatingButton_addServing.setAlpha(1f);
        }

        checkedItemsIdList.clear();
    }

}


