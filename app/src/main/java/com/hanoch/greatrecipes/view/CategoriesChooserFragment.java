package com.hanoch.greatrecipes.view;

import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;

import java.util.ArrayList;


public class CategoriesChooserFragment extends Fragment
        implements View.OnClickListener {

    private static final String ARG_SELECTED_CATEGORIES_LIST = "categoriesList";

    private ArrayList<String> selectedCategoriesNamesList;

    private CardView cardView_appetizers;
    private CardView cardView_beverages;
    private CardView cardView_breads;
    private CardView cardView_breakfast;
    private CardView cardView_cocktails;
    private CardView cardView_sauces;
    private CardView cardView_desserts;
    private CardView cardView_lunch;
    private CardView cardView_mainDishes;
    private CardView cardView_sideDishes;
    private CardView cardView_soups;
    private CardView cardView_salads;

//-------------------------------------------------------------------------------------------------

    public static CategoriesChooserFragment newInstance(ArrayList<String> selectedCategoriesList) {

        CategoriesChooserFragment fragment = new CategoriesChooserFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_SELECTED_CATEGORIES_LIST, selectedCategoriesList);
        fragment.setArguments(args);
        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories_chooser, container, false);

        cardView_desserts = (CardView) view.findViewById(R.id.cardView_desserts);
        fillCategoryCard(cardView_desserts, R.string.desserts, R.drawable.cat_desserts, AppConsts.Category.DESSERTS);
        cardView_desserts.setOnClickListener(this);

        cardView_breads = (CardView) view.findViewById(R.id.cardView_breads);
        fillCategoryCard(cardView_breads, R.string.breads, R.drawable.cat_breads, AppConsts.Category.BREADS);
        cardView_breads.setOnClickListener(this);

        cardView_appetizers = (CardView) view.findViewById(R.id.cardView_appetizers);
        fillCategoryCard(cardView_appetizers, R.string.appetizers, R.drawable.cat_appetizers, AppConsts.Category.APPETIZERS);
        cardView_appetizers.setOnClickListener(this);

        cardView_beverages = (CardView) view.findViewById(R.id.cardView_beverages);
        fillCategoryCard(cardView_beverages, R.string.beverages, R.drawable.cat_beverages, AppConsts.Category.BEVERAGES);
        cardView_beverages.setOnClickListener(this);

        cardView_breakfast = (CardView) view.findViewById(R.id.cardView_breakfast);
        fillCategoryCard(cardView_breakfast, R.string.breakfast_and_brunch, R.drawable.cat_breakfast_and_brunch, AppConsts.Category.BREAKFAST_AND_BRUNCH);
        cardView_breakfast.setOnClickListener(this);

        cardView_cocktails = (CardView) view.findViewById(R.id.cardView_cocktails);
        fillCategoryCard(cardView_cocktails, R.string.cocktails, R.drawable.cat_cocktails, AppConsts.Category.COCKTAILS);
        cardView_cocktails.setOnClickListener(this);

        cardView_sauces = (CardView) view.findViewById(R.id.cardView_sauces);
        fillCategoryCard(cardView_sauces, R.string.condiments_and_sauces, R.drawable.cat_condiments_and_sauces, AppConsts.Category.CONDIMENTS_AND_SAUCES);
        cardView_sauces.setOnClickListener(this);

        cardView_lunch = (CardView) view.findViewById(R.id.cardView_lunch);
        fillCategoryCard(cardView_lunch, R.string.lunch_and_snacks, R.drawable.cat_lunch_and_snacks, AppConsts.Category.LUNCH_AND_SNACKS);
        cardView_lunch.setOnClickListener(this);

        cardView_mainDishes = (CardView) view.findViewById(R.id.cardView_mainDishes);
        fillCategoryCard(cardView_mainDishes, R.string.main_dishes, R.drawable.cat_main_dishes, AppConsts.Category.MAIN_DISHES);
        cardView_mainDishes.setOnClickListener(this);

        cardView_salads = (CardView) view.findViewById(R.id.cardView_salads);
        fillCategoryCard(cardView_salads, R.string.salads, R.drawable.cat_salads, AppConsts.Category.SALADS);
        cardView_salads.setOnClickListener(this);

        cardView_sideDishes = (CardView) view.findViewById(R.id.cardView_sideDishes);
        fillCategoryCard(cardView_sideDishes, R.string.side_dishes, R.drawable.cat_side_dishes, AppConsts.Category.SIDE_DISHES);
        cardView_sideDishes.setOnClickListener(this);

        cardView_soups = (CardView) view.findViewById(R.id.cardView_soups);
        fillCategoryCard(cardView_soups, R.string.soups, R.drawable.cat_soups, AppConsts.Category.SOUPS);
        cardView_soups.setOnClickListener(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean premium = sp.getBoolean(AppConsts.SharedPrefs.PREMIUM_ACCESS, false);

        if (!premium) {
            setFreeTrialCategory(cardView_appetizers);
            setFreeTrialCategory(cardView_beverages);
            setFreeTrialCategory(cardView_breakfast);
            setFreeTrialCategory(cardView_cocktails);
            setFreeTrialCategory(cardView_sauces);
            setFreeTrialCategory(cardView_lunch);
            setFreeTrialCategory(cardView_mainDishes);
            setFreeTrialCategory(cardView_salads);
            setFreeTrialCategory(cardView_sideDishes);
            setFreeTrialCategory(cardView_soups);
        }

        return view;
    }

//-------------------------------------------------------------------------------------------------

    private void fillCategoryCard(CardView cardView, int catTitleRes, int catImageRes, String catOriginalName) {

        ((TextView) cardView.findViewById(R.id.textView_categoryOriginalName)).setText(catOriginalName);
        ((TextView) cardView.findViewById(R.id.textView_categoryName)).setText(getString(catTitleRes));
        ((ImageView) cardView.findViewById(R.id.imageView_categoryImage)).setImageResource(catImageRes);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            selectedCategoriesNamesList = getArguments().getStringArrayList(ARG_SELECTED_CATEGORIES_LIST);

        } else {
            selectedCategoriesNamesList = savedInstanceState.getStringArrayList("selectedCategoriesNamesList");
        }

        if (selectedCategoriesNamesList != null) {

            if (selectedCategoriesNamesList.isEmpty() || selectedCategoriesNamesList.get(0).equals(AppConsts.Category.NO_INFO)) {
                selectedCategoriesNamesList = new ArrayList<>();
            }
        }

        checkCurrentCategories(selectedCategoriesNamesList);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("selectedCategoriesNamesList", selectedCategoriesNamesList);
    }

//-------------------------------------------------------------------------------------------------

    private void setFreeTrialCategory(CardView cardView_category) {

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        ImageView imageView_categoryImage = (ImageView) cardView_category.findViewById(R.id.imageView_categoryImage);
        imageView_categoryImage.setColorFilter(filter);

        TextView textView_premium = (TextView) cardView_category.findViewById(R.id.textView_premium);
        textView_premium.setVisibility(View.VISIBLE);
        textView_premium.setRotation(30f);
    }

//-------------------------------------------------------------------------------------------------

    private void checkCurrentCategories(ArrayList<String> currentCategories) {

        setCategoryCheckedStatus(cardView_appetizers, currentCategories);
        setCategoryCheckedStatus(cardView_beverages, currentCategories);
        setCategoryCheckedStatus(cardView_breads, currentCategories);
        setCategoryCheckedStatus(cardView_breakfast, currentCategories);
        setCategoryCheckedStatus(cardView_cocktails, currentCategories);
        setCategoryCheckedStatus(cardView_sauces, currentCategories);
        setCategoryCheckedStatus(cardView_desserts, currentCategories);
        setCategoryCheckedStatus(cardView_lunch, currentCategories);
        setCategoryCheckedStatus(cardView_mainDishes, currentCategories);
        setCategoryCheckedStatus(cardView_salads, currentCategories);
        setCategoryCheckedStatus(cardView_sideDishes, currentCategories);
        setCategoryCheckedStatus(cardView_soups, currentCategories);
    }

//-------------------------------------------------------------------------------------------------

    private void setCategoryCheckedStatus(CardView cardView_category, ArrayList<String> currentCategories) {

        TextView textView_categoryOriginalName = (TextView) cardView_category.findViewById(R.id.textView_categoryOriginalName);

        if (currentCategories.contains(textView_categoryOriginalName.getText().toString())) {
            ImageView imageView_checkedIcon = (ImageView) cardView_category.findViewById(R.id.imageView_checkedIcon);
            ImageView imageView_categoryImage = (ImageView) cardView_category.findViewById(R.id.imageView_categoryImage);
            imageView_checkedIcon.setVisibility(View.VISIBLE);
            imageView_categoryImage.setAlpha(0.5f);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        TextView textView_premium = (TextView) v.findViewById(R.id.textView_premium);
        if (textView_premium.getVisibility() == View.VISIBLE) {

            Animation animation = new ScaleAnimation(1f, 2f, 1f, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(700);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setRepeatCount(1);
            textView_premium.startAnimation(animation);

            return;
        }

        TextView textView_categoryOriginalName = (TextView) v.findViewById(R.id.textView_categoryOriginalName);
        String categoryOriginalName = textView_categoryOriginalName.getText().toString();
        if (selectedCategoriesNamesList.contains(categoryOriginalName)) {
            // Checked category was unchecked
            selectedCategoriesNamesList.remove(categoryOriginalName);
            AppHelper.animateUncheckedCategory(v, getContext());

        } else {
            // Unchecked category was checked
            selectedCategoriesNamesList.add(categoryOriginalName);
            AppHelper.animateCheckedCategory(v, getContext());
        }
    }

//-------------------------------------------------------------------------------------------------

    public ArrayList<String> getSelectedCategoriesNamesList() {
        return selectedCategoriesNamesList;
    }

}

