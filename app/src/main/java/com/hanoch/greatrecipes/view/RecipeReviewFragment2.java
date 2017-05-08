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
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;


public class RecipeReviewFragment2 extends Fragment implements View.OnClickListener {

    private static final String ARG_EXTRA_SERVING = "ARG_EXTRA_SERVING";
    private static final String ARG_RECIPE_AS_JSON = "ARG_RECIPE_AS_JSON";
    private static final String ARG_IS_USER_RECIPE = "ARG_IS_USER_RECIPE";

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

    private boolean isUserRecipe;
    private UserRecipe mUserRecipe;
    private YummlyRecipe mYummlyRecipe;

    private String extra_serving;

    private Bitmap recipeImage;

//-------------------------------------------------------------------------------------------------

    public interface FragmentRecipeReviewListener {

        void onGetInstructionsClick(String url);
    }

//-------------------------------------------------------------------------------------------------

    public static RecipeReviewFragment2 newInstance(String recipeId, boolean isUserRecipe, String extra_serving) {

        User user = AppStateManager.getInstance().user;
        String recipeAsJson;
        if (isUserRecipe) {
            UserRecipe userRecipe = user.userRecipes.get(recipeId);
            recipeAsJson = new Gson().toJson(userRecipe, UserRecipe.class);
        } else {
            YummlyRecipe yummlyRecipe = user.yummlyRecipes.get(recipeId);
            recipeAsJson = new Gson().toJson(yummlyRecipe, YummlyRecipe.class);
        }

        RecipeReviewFragment2 fragment = new RecipeReviewFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
        args.putString(ARG_RECIPE_AS_JSON, recipeAsJson);
        args.putBoolean(ARG_IS_USER_RECIPE, isUserRecipe);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);

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

        if (savedInstanceState == null) {

            Bundle args = getArguments();

            extra_serving = getArguments().getString(ARG_EXTRA_SERVING);
            isUserRecipe = args.getBoolean(ARG_IS_USER_RECIPE);

            if (isUserRecipe) {
                mUserRecipe = new Gson().fromJson(getArguments().getString(ARG_EXTRA_SERVING), UserRecipe.class);
                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(mUserRecipe.imageByteArrayAsString);
                setUserRecipeDetailsView();
            } else {
                // Going to show an online search result

                mYummlyRecipe = new Gson().fromJson(getArguments().getString(ARG_EXTRA_SERVING), YummlyRecipe.class);
                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(mYummlyRecipe.imageByteArrayAsString);
                setYummlyRecipeDetailsView();
            }
        } else {
            // Restore recipe details from the state

            extra_serving = savedInstanceState.getString("extra_serving");
            isUserRecipe = savedInstanceState.getBoolean("isUserRecipe");
            recipeImage = savedInstanceState.getParcelable("recipeImage");

            if (recipeImage == null) {
                textView_noImageAvailable.setVisibility(View.VISIBLE);
            }

            if (isUserRecipe) {
                mUserRecipe = new Gson().fromJson(savedInstanceState.getString("mUserRecipeAsJson"), UserRecipe.class);
            } else {
                mYummlyRecipe = new Gson().fromJson(savedInstanceState.getString("mYummlyRecipeAsJson"), YummlyRecipe.class);
            }

            if (mUserRecipe != null) {
                setUserRecipeDetailsView();
            } else if (mYummlyRecipe != null) {
                setYummlyRecipeDetailsView();
            }

        }

        AppHelper.hideTheKeyboard(getActivity());
        AppHelper.hideKeyboardFrom(getContext(), view);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    private void setUserRecipeDetailsView() {

        imageView_recipeImage.setImageBitmap(recipeImage);

        if (recipeImage == null) {
            textView_noImageAvailable.setVisibility(View.VISIBLE);
        } else {
            AnimationHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
        }

        if (mUserRecipe.recipeTitle != null) {
            editText_recipeTitle.setText(mUserRecipe.recipeTitle.isEmpty() ? getString(R.string.no_info) : mUserRecipe.recipeTitle);
        }

        String stringTotalTime = AppHelper.getStringRecipeTotalTime(getContext(), mUserRecipe.cookingTime);
        textView_recipeTime.setText(stringTotalTime);

        if (mUserRecipe.yield == 0) {
            textView_servings.setText(getString(R.string.no_info));
        } else {
            textView_servings.setText(mUserRecipe.yield + " " + getString(R.string.servings));
        }

        if (mUserRecipe.author == null) {
            textView_recipeAuthor.setText(getString(R.string.no_info));
        } else {
            textView_recipeAuthor.setText(getString(R.string.from) + "\n" + mUserRecipe.author);
        }

        fillList(layout_ingredientsList, mUserRecipe.ingredientsList);
        fillList(layout_categoriesList, AppHelper.getTranslatedCategoriesList(getContext(), mUserRecipe.categoriesList));

        setFavouriteImage(mUserRecipe._id);

        if (mUserRecipe.instructions.isEmpty()) {
            editText_instructions.setText(getString(R.string.no_info));
        } else {
            editText_instructions.setText(mUserRecipe.instructions);
        }

        if (mUserRecipe.notes.isEmpty()) {
            editText_notes.setText(getString(R.string.no_info));
        } else {
            editText_notes.setText(mUserRecipe.notes);
        }

        floatingButton_getInstructions.setVisibility(View.GONE);
        cardView_energy.setVisibility(View.GONE);
    }

//-------------------------------------------------------------------------------------------------

    private void setYummlyRecipeDetailsView() {

        imageView_recipeImage.setImageBitmap(recipeImage);

        if (recipeImage == null) {
            textView_noImageAvailable.setVisibility(View.VISIBLE);

        } else {
            AnimationHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
        }

        if (mYummlyRecipe.recipeTitle != null) {
            editText_recipeTitle.setText(mYummlyRecipe.recipeTitle.isEmpty() ? getString(R.string.no_info) : mYummlyRecipe.recipeTitle);
        }

        String stringTotalTime = AppHelper.getStringRecipeTotalTime(getContext(), mYummlyRecipe.cookingTime);
        textView_recipeTime.setText(stringTotalTime);

        if (mYummlyRecipe.yield == 0) {
            textView_servings.setText(getString(R.string.no_info));
        } else {
            textView_servings.setText(mYummlyRecipe.yield + " " + getString(R.string.servings));
        }

        if (mYummlyRecipe.author == null) {
            textView_recipeAuthor.setText(getString(R.string.no_info));
        } else {
            textView_recipeAuthor.setText(getString(R.string.from) + "\n" + mYummlyRecipe.author);
        }

        if (mYummlyRecipe.energy == 0) {
            textView_totalCalories.setText(getString(R.string.no_info));
        } else {
            textView_totalCalories.setText(mYummlyRecipe.energy + " " + getString(R.string.calories));
        }

        fillList(layout_ingredientsList, mYummlyRecipe.ingredientsList);
        fillList(layout_categoriesList, AppHelper.getTranslatedCategoriesList(getContext(), mYummlyRecipe.categoriesList));

        setFavouriteImage(mYummlyRecipe._id);

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

        outState.putString("extra_serving", extra_serving);
        outState.putBoolean("isUserRecipe", isUserRecipe);
        outState.putParcelable("recipeImage", recipeImage);

        if (isUserRecipe) {
            outState.putString("mUserRecipeAsJson", new Gson().toJson(mUserRecipe));
        } else {
            outState.putString("mYummlyRecipeAsJson", new Gson().toJson(mYummlyRecipe, YummlyRecipe.class));
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_getInstructions:

                mListener.onGetInstructionsClick(mYummlyRecipe.url);

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
