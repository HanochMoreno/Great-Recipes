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

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;


public class RecipeReviewFragment2 extends Fragment implements View.OnClickListener {

    private static final String ARG_ACTION = "ARG_ACTION";
    private static final String ARG_EXTRA = "ARG_EXTRA";

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

    /**
     * Optional actions for this fragment:
     * REVIEW_YUMMLY_RECIPE
     * REVIEW_USER_RECIPE
     * REVIEW_SHARED_USER_RECIPE
     * REVIEW_SHARED_YUMMLY_RECIPE
     * REVIEW_YUMMLY_ONLINE
     * REVIEW_SERVING
     */
    private int action;

    /**
     * Could be a recipeId or servingId
     */
    private String extra;

    private Bitmap recipeImage;
    private AppStateManager appStateManager;

//-------------------------------------------------------------------------------------------------

    public interface FragmentRecipeReviewListener {

        void onGetInstructionsClick(String url);
    }

//-------------------------------------------------------------------------------------------------

    public static RecipeReviewFragment2 newInstance(int action, String extra) {
        RecipeReviewFragment2 fragment = new RecipeReviewFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION, action);
        args.putString(ARG_EXTRA, extra);
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
        action = args.getInt(ARG_ACTION);
        extra = args.getString(ARG_EXTRA);

        if (savedInstanceState == null) {

            if (action == AppConsts.Actions.REVIEW_YUMMLY_ONLINE) {
                // Going to show an online search result

                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                        appStateManager.yummlySearchResult.imageByteArrayAsString);

            } else if (action == AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {
                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                        appStateManager.sharedUserRecipe.imageByteArrayAsString);

            } else if (action == AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                        appStateManager.sharedYummlyRecipe.imageByteArrayAsString);

            } else if (action == AppConsts.Actions.REVIEW_SERVING) {
                Serving serving = appStateManager.user.servings.get(extra);
                if (serving.isUserRecipe) {
                    recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                            serving.userRecipe.imageByteArrayAsString);
                } else {
                    recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                            serving.yummlyRecipe.imageByteArrayAsString);
                }
            } else {
                // Going to show a recipe from the lists

                if (appStateManager.user.isUserRecipe(extra)) {
                    recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                            appStateManager.user.recipes.userRecipes.get(extra).imageByteArrayAsString);

                } else {
                    recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(
                            appStateManager.user.recipes.yummlyRecipes.get(extra).imageByteArrayAsString);
                }
            }
        } else {
            // Restore recipe details from the state
            action = savedInstanceState.getInt("action");
            extra = savedInstanceState.getString("extra");
            recipeImage = savedInstanceState.getParcelable("recipeImage");

            if (recipeImage == null) {
                textView_noImageAvailable.setVisibility(View.VISIBLE);
            }
        }

        if (action == AppConsts.Actions.REVIEW_YUMMLY_ONLINE) {
            setYummlyRecipeDetailsView(appStateManager.yummlySearchResult);
        } else if (action == AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {
            setUserRecipeDetailsView(appStateManager.sharedUserRecipe);
        } else if (action == AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
            setYummlyRecipeDetailsView(appStateManager.sharedYummlyRecipe);
        } else if (action == AppConsts.Actions.REVIEW_SERVING) {
            Serving serving = appStateManager.user.servings.get(extra);
            if (serving.isUserRecipe) {
                setUserRecipeDetailsView(serving.userRecipe);
            } else {
                setYummlyRecipeDetailsView(serving.yummlyRecipe);
            }
        } else if (appStateManager.user.isUserRecipe(extra)) {
            setUserRecipeDetailsView(appStateManager.user.recipes.userRecipes.get(extra));
        } else {
            setYummlyRecipeDetailsView(appStateManager.user.recipes.yummlyRecipes.get(extra));
        }

        AppHelper.hideTheKeyboard(getActivity());
        AppHelper.hideKeyboardFrom(getContext(), view);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    private void setUserRecipeDetailsView(UserRecipe userRecipe) {

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

        if (action != AppConsts.Actions.REVIEW_SHARED_USER_RECIPE) {
            setFavouriteImage(userRecipe._id);
        }

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

        if (action != AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
            setFavouriteImage(yummlyRecipe._id);
        }

        floatingButton_getInstructions.setVisibility(View.VISIBLE);
        AnimationHelper.animateViewFadingIn(getContext(), floatingButton_getInstructions, 1500, 0);
        AnimationHelper.animateViewFlipping(getContext(), floatingButton_getInstructions, 1500, 0);

        cardView_instructions.setVisibility(View.GONE);
        cardView_privateNotes.setVisibility(View.GONE);
    }

//-------------------------------------------------------------------------------------------------

    public void setFavouriteImage(String recipeId) {
        setFavouriteImage(appStateManager.isRecipeFavourite(recipeId));
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
        outState.putInt("action", action);
        outState.putString("extra", extra);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_getInstructions:
                YummlyRecipe yummlyRecipe;
                if (action == AppConsts.Actions.REVIEW_YUMMLY_ONLINE) {
                    yummlyRecipe = appStateManager.yummlySearchResult;
                } else if (action == AppConsts.Actions.REVIEW_SHARED_YUMMLY_RECIPE) {
                    yummlyRecipe = appStateManager.sharedYummlyRecipe;
                } else if (action == AppConsts.Actions.REVIEW_SERVING) {
                    yummlyRecipe = appStateManager.user.servings.get(extra).yummlyRecipe;
                } else { // action == AppConsts.Actions.REVIEW_YUMMLY_RECIPE
                    yummlyRecipe = appStateManager.user.recipes.yummlyRecipes.get(extra);
                }
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
