package com.hanoch.greatrecipes.view;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.R;

import java.util.ArrayList;


public class ListFavouriteFragment2 extends MyListFragment {

    private Dialog favouritesDialog;
    private boolean showFavouritesDialogOnLoadFinished;

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        floatingButton_addRecipe.setVisibility(View.GONE);
        layout_dialogBubble.setVisibility(View.GONE);

        showFavouritesDialogOnLoadFinished = true;

        if (savedInstanceState != null) {

            boolean favouritesDialogIsShowing = savedInstanceState.getBoolean("favouritesDialogIsShowing");
            if (favouritesDialogIsShowing) {
                boolean currentCheckboxState = savedInstanceState.getBoolean("currentCheckboxState");
                showFavouritesDialog(currentCheckboxState);
            }

            showFavouritesDialogOnLoadFinished = savedInstanceState.getBoolean("showFavouritesDialogOnLoadFinished");
        }

        return view;
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
    }

//-------------------------------------------------------------------------------------------------

    private void showFavouritesDialog(boolean isCheckboxChecked) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(AppConsts.SharedPrefs.FAVOURITES_DIALOG_WAS_SHOWN, true);
        editor.apply();

        favouritesDialog = new Dialog(getContext());
        favouritesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Context context = getActivity();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_favourites, null, false);
        favouritesDialog.setCanceledOnTouchOutside(false);
        favouritesDialog.setContentView(view);

        final CheckBox checkBox = (CheckBox) favouritesDialog.findViewById(R.id.checkBox_dontShowAgain);
        checkBox.setChecked(isCheckboxChecked);

        Button button_gotIt = (Button) favouritesDialog.findViewById(R.id.button_gotIt);
        button_gotIt.setOnClickListener(v -> {

            SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor1 = sp1.edit();

            if (checkBox.isChecked()) {
                editor1.putBoolean(AppConsts.SharedPrefs.SHOW_FAVOURITES_DIALOG, false);
                editor1.apply();
            }

            favouritesDialog.dismiss();
        });

        favouritesDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getContainedListType() {
        return AppConsts.ListType.FAVOURITES_RECIPES;
    }

    public void refreshAdapter() {
        recipesList = new ArrayList<>();
        for (String favouriteId : appStateManager.user.recipes.favouriteRecipesIds) {
            if (appStateManager.user.recipes.userRecipes.containsKey(favouriteId)) {
                recipesList.add(appStateManager.user.recipes.userRecipes.get(favouriteId));
            } else if (appStateManager.user.recipes.yummlyRecipes.containsKey(favouriteId)) {
                recipesList.add(appStateManager.user.recipes.yummlyRecipes.get(favouriteId));
            }
        }
        adapter.refreshList(recipesList);
    }
}
