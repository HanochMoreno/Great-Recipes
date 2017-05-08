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

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.GreatRecipesApplication;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ListFragmentListener;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.model.MyListFragment;
import com.hanoch.greatrecipes.view.adapters.ServingsCursorAdapter;
import com.hanoch.greatrecipes.view.adapters.ServingsListAdapter;

import java.util.ArrayList;


public class ServingsListFragment extends MyListFragment implements
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private ListFragmentListener mListListener;
    private FragmentServingsListListener mListener;
    private ServingsListAdapter adapter;

    private Bundle savedInstanceState;

    private ListView listView_servingsList;
    private FloatingActionButton floatingButton_addServing;
    private FrameLayout layout_dialogBubble;

    private ArrayList<String> selectedItemsIdList;

//-------------------------------------------------------------------------------------------------

    public static ServingsListFragment newInstance() {
        return new ServingsListFragment();
    }

//-------------------------------------------------------------------------------------------------

    public interface FragmentServingsListListener {

        void listSizeChanged(int listSize);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.savedInstanceState = savedInstanceState;

        selectedItemsIdList = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_meal, container, false);

        listView_servingsList = (ListView) view.findViewById(R.id.listView_servingsList);
        // Adding gaps (divider-like) at the top and bottom of the listView
        listView_servingsList.addFooterView(new View(getContext()), null, true);
        listView_servingsList.addHeaderView(new View(getContext()), null, true);

        listView_servingsList.setOnItemClickListener(this);
        listView_servingsList.setOnItemLongClickListener(this);

        floatingButton_addServing = (FloatingActionButton) view.findViewById(R.id.floatingButton_addServing);
        floatingButton_addServing.setOnClickListener(this);

        layout_dialogBubble = (FrameLayout) view.findViewById(R.id.layout_dialogBubble);
        layout_dialogBubble.setVisibility(View.GONE);

        TextView textView_dialogBubble = (TextView) view.findViewById(R.id.textView_dialogBubble);
        textView_dialogBubble.setText(getString(R.string.click_here_to_start_planning_meal));

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState != null) {

            selectedItemsIdList = savedInstanceState.getStringArrayList("selectedItemsIdList");

            int floatingButton_addServingVisibility = savedInstanceState.getInt("floatingButton_addServingVisibility");
            floatingButton_addServing.setVisibility(floatingButton_addServingVisibility);

            int layout_dialogBubbleVisibility = savedInstanceState.getInt("layout_dialogBubbleVisibility");
            layout_dialogBubble.setVisibility(layout_dialogBubbleVisibility);
        }

        adapter = new ServingsListAdapter(getContext(), selectedItemsIdList);
        listView_servingsList.setAdapter(adapter);

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

        outState.putStringArrayList("selectedItemsIdList", selectedItemsIdList);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_addServing:

                mListListener.onAddNewRecipeClick(this, -222);
                AppHelper.hideTheKeyboard(getActivity());
                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!selectedItemsIdList.isEmpty()) {

            if (selectedItemsIdList.contains(id + "")) {
                // Checked item was unchecked

                AnimationHelper.animateUncheckedServing(view, getContext());
                selectedItemsIdList.remove(id + "");

                //adapter.onServingUnchecked(id + "");

                if (selectedItemsIdList.isEmpty()) {
                    backToDefaultDisplay(true);
                }

                mListListener.onListItemChecked(this, id, false);

            } else {
                // Unchecked item was checked (at least 2 item are checked now)

                //adapter.onServingChecked(id + "");

                selectedItemsIdList.add(id + "");

                AnimationHelper.animateCheckedServing(view, getContext());

                mListListener.onListItemChecked(this, id, true);
            }

        } else {
            DbManager dbManager = ((GreatRecipesApplication) getActivity().getApplication()).getDbManager();
            long recipeId = dbManager.queryServingObjectById(id).recipeId;
            mListListener.onRecipeClick(this, recipeId);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (!selectedItemsIdList.isEmpty()) {
            return false;
        }

        floatingButton_addServing.setEnabled(false);
        AnimationHelper.animateViewFadingOut(getContext(), floatingButton_addServing, 1000, 0);

        AnimationHelper.animateCheckedServing(view, getContext());

        selectedItemsIdList.add(id + "");

        //adapter.onServingChecked(id + "");

        mListListener.onListItemChecked(this, id, true);

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(
                getActivity(),
                RecipesContract.MealPlanning.CONTENT_URI,

                // columns: null = all
                null,

                // rows:
                null,
                null,

                // sort order
                RecipesContract.MealPlanning._ID + " ASC"
        );
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        adapter.swapCursor(cursor);

        mListener.listSizeChanged(cursor.getCount());

        if (savedInstanceState != null) {

            Parcelable listViewState = savedInstanceState.getParcelable("listViewState");
            listView_servingsList.onRestoreInstanceState(listViewState);
        }

        if (cursor.getCount() == 0) {

            if (floatingButton_addServing.getVisibility() == View.VISIBLE && layout_dialogBubble.getVisibility() != View.VISIBLE) {
                AnimationHelper.animateViewFadingIn(getContext(), layout_dialogBubble, 1500, 500);
            }

        } else {
            layout_dialogBubble.setVisibility(View.INVISIBLE);
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

        floatingButton_addServing.setEnabled(true);

        if (includeAnimation) {

            for (String selectedItemId : selectedItemsIdList) {

                View selectedItemView = adapter.getViewById(selectedItemId);
                if (selectedItemView != null) {
                    AnimationHelper.animateUncheckedServing(selectedItemView, getContext());
                }
            }

            AnimationHelper.animateViewFadingIn(getContext(), floatingButton_addServing, 1000, 1000);

        } else {
            // After deleting an item(s) - no need for unchecked items animation

            for (String selectedItemId : selectedItemsIdList) {

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

        selectedItemsIdList.clear();
    }
}


