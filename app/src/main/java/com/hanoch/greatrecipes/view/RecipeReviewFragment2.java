package com.hanoch.greatrecipes.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;


public class RecipeReviewFragment2 extends Fragment implements View.OnClickListener {

    private static final String ARG_EXTRA_SERVING = "ARG_EXTRA_SERVING";
    private static final String ARG_RECIPE_ID = "ARG_RECIPE_ID";
    private static final String ARG_IS_YUMMLY_SEARCH_RESULT = "ARG_IS_YUMMLY_SEARCH_RESULT";

    private FragmentRecipeReviewListener mListener;

    private EditText editText_recipeTitle;
    private TextView textView_recipeAuthor;
    private ImageView imageView_recipeImage;
    private ImageView imageView_favourite;
    private TextView textView_servings;
    private TextView textView_totalCalories;
    private EditText editText_instructions;
    private TextView textView_recipeTime;
    private TextView textView_noImageAvailable;
    private EditText editText_notes;
    private FloatingActionButton floatingButton_getInstructions;
    private CardView cardView_instructions;
    private CardView cardView_privateNotes;
    private CardView cardView_energy;
    private LinearLayout layout_ingredientsList;
    private LinearLayout layout_categoriesList;
    private View view;

    private LayoutInflater inflater;

    private boolean viewsHaveBeenDestroyed;

    private boolean isYummlySearchResult;
    private String mRecipeId;

    private String extra_serving;

    private Bitmap recipeImage;
    private AppStateManager appStateManager;

//-------------------------------------------------------------------------------------------------

    public interface FragmentRecipeReviewListener {

        void onGetInstructionsClick(String url);
    }

//-------------------------------------------------------------------------------------------------

    public static RecipeReviewFragment2 newInstance(String recipeId, String extra_serving) {
        return newInstance(recipeId, false, extra_serving) ;
    }

//-------------------------------------------------------------------------------------------------

    public static RecipeReviewFragment2 newInstance(String recipeId, boolean isYummlySearchResult, String extra_serving) {

        RecipeReviewFragment2 fragment = new RecipeReviewFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId);
        args.putBoolean(ARG_IS_YUMMLY_SEARCH_RESULT, isYummlySearchResult);
        args.putString(ARG_EXTRA_SERVING, extra_serving);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        appStateManager = AppStateManager.getInstance();
        this.inflater = inflater;

        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_recipe_review, container, false);

        (view.findViewById(R.id.imageView_choosePicture)).setVisibility(View.GONE);
        (view.findViewById(R.id.imageView_deleteImage)).setVisibility(View.GONE);
        (view.findViewById(R.id.button_addCategory)).setVisibility(View.GONE);
        (view.findViewById(R.id.button_addIngredient)).setVisibility(View.GONE);

        editText_recipeTitle = (EditText) view.findViewById(R.id.editText_recipeTitle);
        editText_recipeTitle.setEnabled(false);
        editText_recipeTitle.setHint(R.string.no_info);
        editText_recipeTitle.setHintTextColor(Color.WHITE);

        textView_recipeAuthor = (TextView) view.findViewById(R.id.textView_recipeAuthor);

        textView_servings = (TextView) view.findViewById(R.id.textView_servings);
        textView_servings.setTextColor(Color.BLACK);

        textView_recipeTime = (TextView) view.findViewById(R.id.textView_totalTime);
        textView_recipeTime.setTextColor(Color.BLACK);

        textView_totalCalories = (TextView) view.findViewById(R.id.textView_totalCalories);

        editText_notes = (EditText) view.findViewById(R.id.editText_privateNotes);
        editText_notes.setEnabled(false);

        editText_instructions = (EditText) view.findViewById(R.id.editText_instructions);
        editText_instructions.setEnabled(false);

        imageView_recipeImage = (ImageView) view.findViewById(R.id.imageView_recipeImage);

        layout_ingredientsList = (LinearLayout) view.findViewById(R.id.layout_ingredientsList);
        layout_categoriesList = (LinearLayout) view.findViewById(R.id.layout_categoriesList);

        textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);
        textView_noImageAvailable.setVisibility(View.INVISIBLE);

        imageView_favourite = (ImageView) view.findViewById(R.id.imageView_favourite);
        imageView_favourite.setVisibility(View.INVISIBLE);

        floatingButton_getInstructions = (FloatingActionButton) view.findViewById(R.id.floatingButton_getInstructions);
        floatingButton_getInstructions.setOnClickListener(this);
        floatingButton_getInstructions.setVisibility(View.GONE);

        cardView_instructions = (CardView) view.findViewById(R.id.cardView_instructions);
        cardView_privateNotes = (CardView) view.findViewById(R.id.cardView_notes);
        cardView_energy = (CardView) view.findViewById(R.id.cardView_energy);

        Bundle args = getArguments();
        mRecipeId = args.getString(ARG_RECIPE_ID);
        extra_serving = args.getString(ARG_EXTRA_SERVING);
        isYummlySearchResult = args.getBoolean(ARG_IS_YUMMLY_SEARCH_RESULT);

        if (savedInstanceState == null) {

            if (isYummlySearchResult) {
                // Going to show an online search result

                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                        appStateManager.yummlySearchResult.imageByteArrayAsString);

            } else {
                // Going to show a recipe from the lists

                if (appStateManager.user.isUserRecipe(mRecipeId)) {
                    recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                            appStateManager.user.userRecipes.get(mRecipeId).imageByteArrayAsString);

                } else {
                    recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                            appStateManager.user.yummlyRecipes.get(mRecipeId).imageByteArrayAsString);
                }
            }
        } else {
            // Restore recipe details from the state
            recipeImage = savedInstanceState.getParcelable("recipeImage");

            if (recipeImage == null) {
                textView_noImageAvailable.setVisibility(View.VISIBLE);
            }
        }

        if (isYummlySearchResult) {
            setYummlyRecipeDetailsView(appStateManager.yummlySearchResult);
        } else if (appStateManager.user.isUserRecipe(mRecipeId)) {
            setUserRecipeDetailsView();
        } else {
            setYummlyRecipeDetailsView(appStateManager.user.yummlyRecipes.get(mRecipeId));
        }

        AppHelper.hideTheKeyboard(getActivity());
        AppHelper.hideKeyboardFrom(getContext(), view);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    private void setUserRecipeDetailsView() {

        UserRecipe userRecipe = appStateManager.user.userRecipes.get(mRecipeId);

        imageView_recipeImage.setImageBitmap(recipeImage);

        if (recipeImage == null) {
            textView_noImageAvailable.setVisibility(View.VISIBLE);
        } else {
            AnimationHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
        }

        if (userRecipe.recipeTitle != null) {
            editText_recipeTitle.setText(userRecipe.recipeTitle.isEmpty() ? getString(R.string.no_info) : userRecipe.recipeTitle);
        }

        String stringTotalTime = AppHelper.getStringRecipeTotalTime(getContext(), userRecipe.cookingTime);
        textView_recipeTime.setText(stringTotalTime);

        if (userRecipe.yield == 0) {
            textView_servings.setText(getString(R.string.no_info));
        } else {
            textView_servings.setText(userRecipe.yield + " " + getString(R.string.servings));
        }

        if (userRecipe.author == null) {
            textView_recipeAuthor.setText(getString(R.string.no_info));
        } else {
            textView_recipeAuthor.setText(getString(R.string.from) + "\n" + userRecipe.author);
        }

        fillList(layout_ingredientsList, userRecipe.ingredientsList);
        fillList(layout_categoriesList, AppHelper.getTranslatedCategoriesList(getContext(), userRecipe.categoriesList));

        setFavouriteImage(userRecipe._id);

        if (userRecipe.instructions.isEmpty()) {
            editText_instructions.setText(getString(R.string.no_info));
        } else {
            editText_instructions.setText(userRecipe.instructions);
        }

        if (userRecipe.notes.isEmpty()) {
            editText_notes.setText(getString(R.string.no_info));
        } else {
            editText_notes.setText(userRecipe.notes);
        }

        floatingButton_getInstructions.setVisibility(View.GONE);
        cardView_energy.setVisibility(View.GONE);
    }

//-------------------------------------------------------------------------------------------------

    private void setYummlyRecipeDetailsView(YummlyRecipe yummlyRecipe) {

        imageView_recipeImage.setImageBitmap(recipeImage);

        if (recipeImage == null) {
            textView_noImageAvailable.setVisibility(View.VISIBLE);

        } else {
            AnimationHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
        }

        if (yummlyRecipe.recipeTitle != null) {
            editText_recipeTitle.setText(yummlyRecipe.recipeTitle.isEmpty() ? getString(R.string.no_info) : yummlyRecipe.recipeTitle);
        }

        String stringTotalTime = AppHelper.getStringRecipeTotalTime(getContext(), yummlyRecipe.cookingTime);
        textView_recipeTime.setText(stringTotalTime);

        if (yummlyRecipe.yield == 0) {
            textView_servings.setText(getString(R.string.no_info));
        } else {
            textView_servings.setText(yummlyRecipe.yield + " " + getString(R.string.servings));
        }

        if (yummlyRecipe.author == null) {
            textView_recipeAuthor.setText(getString(R.string.no_info));
        } else {
            textView_recipeAuthor.setText(getString(R.string.from) + "\n" + yummlyRecipe.author);
        }

        if (yummlyRecipe.energy == 0) {
            textView_totalCalories.setText(getString(R.string.no_info));
        } else {
            textView_totalCalories.setText(yummlyRecipe.energy + " " + getString(R.string.calories));
        }

        fillList(layout_ingredientsList, yummlyRecipe.ingredientsList);
        fillList(layout_categoriesList, AppHelper.getTranslatedCategoriesList(getContext(), yummlyRecipe.categoriesList));

        setFavouriteImage(yummlyRecipe._id);

        floatingButton_getInstructions.setVisibility(View.VISIBLE);
        AnimationHelper.animateViewFadingIn(getContext(), floatingButton_getInstructions, 1500, 0);
        AnimationHelper.animateViewFlipping(getContext(), floatingButton_getInstructions, 1500, 0);

        cardView_instructions.setVisibility(View.GONE);
        cardView_privateNotes.setVisibility(View.GONE);
    }

//-------------------------------------------------------------------------------------------------

    public void setFavouriteImage(String recipeId) {
        setFavouriteImage(AppStateManager.getInstance().isRecipeFavourite(recipeId));
    }

//-------------------------------------------------------------------------------------------------

    public void setFavouriteImage(boolean isFavourite) {
        if (isFavourite) {
            AnimationHelper.animateViewFadingIn(getContext(), imageView_favourite, 1000, 0);
        } else {
            AnimationHelper.animateViewFadingOut(getContext(), imageView_favourite, 1000, 0);
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (FragmentRecipeReviewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentRecipeReviewListener");
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
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        // This stops animation on rotation as we have a retained instance.
        boolean shouldNotAnimate = enter && viewsHaveBeenDestroyed;
        viewsHaveBeenDestroyed = false;
        return shouldNotAnimate ? AnimationUtils.loadAnimation(getActivity(), R.anim.none)
                : super.onCreateAnimation(transit, enter, nextAnim);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewsHaveBeenDestroyed = true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("recipeImage", recipeImage);

    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_getInstructions:
                YummlyRecipe yummlyRecipe = appStateManager.user.yummlyRecipes.get(mRecipeId);
                mListener.onGetInstructionsClick(yummlyRecipe.url);

                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    public void fillList(LinearLayout parent, ArrayList<String> list) {

        if (list.isEmpty()) {
            list.add(AppConsts.Category.NO_INFO);
        }

        parent.removeAllViews();

        LinearLayout listItem;
        TextView textView_listItem;

        for (int i = 0; i < list.size(); i++) {
            String itemText = list.get(i);
            listItem = (LinearLayout) inflater.inflate(R.layout.listitem_simple_textview, null);
            textView_listItem = (TextView) listItem.findViewById(R.id.textView_listItem);

            if (itemText.equals(AppConsts.Category.NO_INFO)) {
                textView_listItem.setText(getString(R.string.no_info));

            } else {
                textView_listItem.setText(itemText);
            }

            if (i == list.size() - 1) {
                FrameLayout listSeparator = (FrameLayout) listItem.findViewById(R.id.listSeparator);
                listSeparator.setVisibility(View.INVISIBLE);
            }

            parent.addView(listItem);
        }
    }

}
