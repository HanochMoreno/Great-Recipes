//package com.hanoch.greatrecipes.view;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.CardView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.hanoch.greatrecipes.AnimationHelper;
//import com.hanoch.greatrecipes.AppConsts;
//import com.hanoch.greatrecipes.AppHelper;
//import com.hanoch.greatrecipes.GreatRecipesApplication;
//import com.hanoch.greatrecipes.R;
//import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
//import com.hanoch.greatrecipes.database.DbManager;
//import com.hanoch.greatrecipes.model.ApiProvider;
//import com.hanoch.greatrecipes.model.Recipe;
//import com.hanoch.greatrecipes.model.RecipeSearchResult;
//import com.hanoch.greatrecipes.api.yummly_api.YummlyRecipeResponse2;
//import com.hanoch.greatrecipes.utilities.ImageStorage;
//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;
//
//import java.io.File;
//import java.io.UnsupportedEncodingException;
//import java.net.ConnectException;
//import java.net.URLEncoder;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import rx.Single;
//import rx.Subscriber;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//
//
//public class RecipeReviewFragment extends Fragment implements View.OnClickListener {
//
//    private static final String ARG_EXTRA_SERVING = "serving";
//    private static final String ARG_RECIPE_ID = "recipeId";
//    private static final String ARG_ONLINE_SEARCH_RESULT_ID = "onlineSearchResultId";
//
//    private FragmentRecipeReviewListener mListener;
//
//    private EditText editText_recipeTitle;
//    private TextView textView_recipeAuthor;
//    private ImageView imageView_recipeImage;
//    private ImageView imageView_favourite;
//    private TextView textView_servings;
//    private TextView textView_totalCalories;
//    private EditText editText_instructions;
//    private TextView textView_recipeTime;
//    private TextView textView_noImageAvailable;
//    private EditText editText_notes;
//    private FloatingActionButton floatingButton_getInstructions;
//    private CardView cardView_instructions;
//    private CardView cardView_privateNotes;
//    private CardView cardView_energy;
//    private LinearLayout layout_ingredientsList;
//    private LinearLayout layout_categoriesList;
//    private View view;
//
//    private LayoutInflater inflater;
//
//    private ToolbarMenuSetting mToolbarMenuSetting;
//
//    private boolean viewsHaveBeenDestroyed;
//
//    private String mOnlineSearchResultId;
//    private long mRecipeId;
//    private String recipeUrl;
//    private String extra_serving;
//
//    private ArrayList<String> ingredientsList;
//    private ArrayList<String> categoriesList;
//    private ArrayList<String> translatedCategories;
//
//    private int recipeTotalTime;
//    private Bitmap recipeImage;
//    private String recipeAuthor;
//    private String recipeTitle;
//    private int recipeYield;
//    private int recipeTotalCalories;
//    private int recipeFavouriteIndex;
//    private String recipeInstructions;
//    private String recipeNotes;
//    private int recipeOriginIndex;
//
//    private DbManager dbManager;
//    private Subscriber<RecipeSearchResult> subscriber;
//
////-------------------------------------------------------------------------------------------------
//
//    public interface FragmentRecipeReviewListener {
//        void onRecipeWasSaved(long recipeId);
//
//        void onGetInstructionsClick(String url);
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    public static RecipeReviewFragment newInstance(long recipeId, String onlineSearchResultId, String extra_serving) {
//
//        RecipeReviewFragment fragment = new RecipeReviewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_EXTRA_SERVING, extra_serving);
//        args.putLong(ARG_RECIPE_ID, recipeId);
//        args.putString(ARG_ONLINE_SEARCH_RESULT_ID, onlineSearchResultId);
//        fragment.setArguments(args);
//
//        return fragment;
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        dbManager = ((GreatRecipesApplication) getActivity().getApplication()).getDbManager();
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        setRetainInstance(true);
//
//        this.inflater = inflater;
//
//        if (view != null) return view;
//
//        // Inflate the layout for this fragment
//        view = inflater.inflate(R.layout.fragment_recipe_review, container, false);
//
//        (view.findViewById(R.id.imageView_choosePicture)).setVisibility(View.GONE);
//        (view.findViewById(R.id.imageView_deleteImage)).setVisibility(View.GONE);
//        (view.findViewById(R.id.button_addCategory)).setVisibility(View.GONE);
//        (view.findViewById(R.id.button_addIngredient)).setVisibility(View.GONE);
//
//        editText_recipeTitle = (EditText) view.findViewById(R.id.editText_recipeTitle);
//        editText_recipeTitle.setEnabled(false);
//        editText_recipeTitle.setHint(R.string.no_info);
//        editText_recipeTitle.setHintTextColor(Color.WHITE);
//
//        textView_recipeAuthor = (TextView) view.findViewById(R.id.textView_recipeAuthor);
//
//        textView_servings = (TextView) view.findViewById(R.id.textView_servings);
//        textView_servings.setTextColor(Color.BLACK);
//
//        textView_recipeTime = (TextView) view.findViewById(R.id.textView_totalTime);
//        textView_recipeTime.setTextColor(Color.BLACK);
//
//        textView_totalCalories = (TextView) view.findViewById(R.id.textView_totalCalories);
//
//        editText_notes = (EditText) view.findViewById(R.id.editText_privateNotes);
//        editText_notes.setEnabled(false);
//
//        editText_instructions = (EditText) view.findViewById(R.id.editText_instructions);
//        editText_instructions.setEnabled(false);
//
//        imageView_recipeImage = (ImageView) view.findViewById(R.id.imageView_recipeImage);
//
//        layout_ingredientsList = (LinearLayout) view.findViewById(R.id.layout_ingredientsList);
//        layout_categoriesList = (LinearLayout) view.findViewById(R.id.layout_categoriesList);
//
//        textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);
//        textView_noImageAvailable.setVisibility(View.INVISIBLE);
//
//        imageView_favourite = (ImageView) view.findViewById(R.id.imageView_favourite);
//        imageView_favourite.setVisibility(View.INVISIBLE);
//
//        floatingButton_getInstructions = (FloatingActionButton) view.findViewById(R.id.floatingButton_getInstructions);
//        floatingButton_getInstructions.setOnClickListener(this);
//        floatingButton_getInstructions.setVisibility(View.GONE);
//
//        cardView_instructions = (CardView) view.findViewById(R.id.cardView_instructions);
//        cardView_privateNotes = (CardView) view.findViewById(R.id.cardView_notes);
//        cardView_energy = (CardView) view.findViewById(R.id.cardView_energy);
//
//        ingredientsList = new ArrayList<>();
//        categoriesList = new ArrayList<>();
//        translatedCategories = new ArrayList<>();
//
//        if (savedInstanceState != null) {
//            // Restore recipe details from the state
//
//            extra_serving = savedInstanceState.getString("extra_serving");
//            mOnlineSearchResultId = savedInstanceState.getString("mOnlineSearchResultId");
//            mRecipeId = savedInstanceState.getLong("mRecipeId");
//
//            recipeImage = savedInstanceState.getParcelable("recipeImage");
//
//            if (recipeImage == null) {
//                textView_noImageAvailable.setVisibility(View.VISIBLE);
//            }
//
//            recipeTitle = savedInstanceState.getString("recipeTitle");
//
//            if (recipeTitle != null) {
//                editText_recipeTitle.setText(recipeTitle);
//            }
//
//            recipeAuthor = savedInstanceState.getString("recipeAuthor");
//            recipeTotalTime = savedInstanceState.getInt("recipeTotalTime");
//            recipeYield = savedInstanceState.getInt("recipeYield");
//            ingredientsList = savedInstanceState.getStringArrayList("ingredientsList");
//            categoriesList = savedInstanceState.getStringArrayList("categoriesList");
//            translatedCategories = savedInstanceState.getStringArrayList("translatedCategories");
//
//            recipeTotalCalories = savedInstanceState.getInt("recipeTotalCalories");
//            recipeUrl = savedInstanceState.getString("recipeUrl");
//
//            recipeOriginIndex = savedInstanceState.getInt("recipeOriginIndex");
//            recipeFavouriteIndex = savedInstanceState.getInt("recipeFavouriteIndex");
//
//            recipeInstructions = savedInstanceState.getString("recipeInstructions");
//            recipeNotes = savedInstanceState.getString("recipeNotes");
//
//            setRecipeDetailsView();
//
//        } else {
//
//            Bundle args = getArguments();
//
//            // Getting the RecipeId argument from the bundle:
//            mOnlineSearchResultId = args.getString(ARG_ONLINE_SEARCH_RESULT_ID);
//            mRecipeId = args.getLong(ARG_RECIPE_ID, -1);
//            extra_serving = getArguments().getString(ARG_EXTRA_SERVING);
//
//            if (mOnlineSearchResultId == null) {
//                // Going to show the details of a recipe that exists in
//                // one of the user's lists (has a local recipeId):
//
//                String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + mRecipeId;
//                recipeImage = ImageStorage.getImageBitmapByName(getContext(), imageName);
//
//                Recipe recipe = dbManager.queryRecipeObjectById(mRecipeId);
//
//                recipeTitle = recipe.title;
//                recipeTotalTime = recipe.time;
//                recipeYield = recipe.yield;
//                recipeAuthor = recipe.author;
//                ingredientsList = AppHelper.convertStringToList(recipe.ingredientsList);
//                categoriesList = AppHelper.convertStringToList(recipe.categoriesList);
//                translatedCategories = AppHelper.getTranslatedCategoriesList(getContext(), categoriesList);
//                recipeOriginIndex = recipe.originIndex;
//                recipeFavouriteIndex = recipe.favouriteIndex;
//
//                switch (recipeOriginIndex) {
//
//                    case AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH:
//
//                        recipeTotalCalories = recipe.energy;
//                        recipeUrl = recipe.url;
//
//                        break;
//
//                    case AppConsts.RecipeOrigin.ADDED_MANUALLY:
//
//                        recipeInstructions = recipe.instructions;
//                        recipeNotes = recipe.notes;
//
//                        break;
//                }
//
//                setRecipeDetailsView();
//
//            } else {
//                // Going to show an online search result
//
//                String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + mRecipeId;
//                recipeImage = ImageStorage.getImageBitmapByName(getContext(), imageName);
//
//                imageView_recipeImage.setImageBitmap(recipeImage);
//                cardView_instructions.setVisibility(View.GONE);
//                cardView_privateNotes.setVisibility(View.GONE);
//
//                RecipeSearchResult result = dbManager.queryResultObjectById(mRecipeId);
//
//                if (recipeImage == null) {
//                    textView_noImageAvailable.setVisibility(View.VISIBLE);
//                    if (result.imageUrl != null && !result.imageUrl.isEmpty()) {
//                        tryToGetRecipeImage(result.imageUrl);
//                    }
//
//                } else {
//                    AnimationHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
//                }
//
//                if (result.author == null) {
//                    // Download the rest of the result's details
//
//                    tryToDownloadRecipeDetails();
//
//                } else {
//                    // This result has already been reviewed before, thus it exists in the database
//
//                    updateRecipeFields(result, false);
//                    setRecipeDetailsView();
//
//                    ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
//
//                    if ((extra_serving != null) &&
//                            (extra_serving.equals(AppConsts.Extras.ADD_SERVING_FROM_YUMMLY))) {
//
//                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING_FROM_YUMMLY);
//
//                    } else {
//                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
//                    }
//
//                    mToolbarMenuSetting.setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
//                }
//            }
//        }
//
//        AppHelper.hideTheKeyboard(getActivity());
//        AppHelper.hideKeyboardFrom(getContext(), view);
//
//        return view;
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    private void setRecipeDetailsView() {
//
//        imageView_recipeImage.setImageBitmap(recipeImage);
//
//        if (recipeImage == null) {
//            textView_noImageAvailable.setVisibility(View.VISIBLE);
//
//        } else {
//            AnimationHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
//        }
//
//        if (recipeTitle != null) {
//            editText_recipeTitle.setText(recipeTitle.isEmpty() ? getString(R.string.no_info) : recipeTitle);
//        }
//
//        String stringTotalTime = AppHelper.getStringRecipeTotalTime(getContext(), recipeTotalTime);
//        textView_recipeTime.setText(stringTotalTime);
//
//        if (recipeYield == 0) {
//            textView_servings.setText(getString(R.string.no_info));
//        } else {
//            textView_servings.setText(recipeYield + " " + getString(R.string.servings));
//        }
//
//        if (recipeAuthor == null) {
//            recipeAuthor = getString(R.string.no_info);
//        }
//
//        textView_recipeAuthor.setText(getString(R.string.from) + "\n" + recipeAuthor);
//
//        fillList(layout_ingredientsList, ingredientsList);
//        fillList(layout_categoriesList, translatedCategories);
//        setFavouriteImage(recipeFavouriteIndex);
//
//        switch (recipeOriginIndex) {
//
//            case AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH:
//
//                if (recipeTotalCalories != -1) {
//                    textView_totalCalories.setText(recipeTotalCalories + " " + getString(R.string.calories));
//                }
//
//                floatingButton_getInstructions.setVisibility(View.VISIBLE);
//                AnimationHelper.animateViewFadingIn(getContext(), floatingButton_getInstructions, 1500, 0);
//                AnimationHelper.animateViewFlipping(getContext(), floatingButton_getInstructions, 1500, 0);
//
//                cardView_instructions.setVisibility(View.GONE);
//                cardView_privateNotes.setVisibility(View.GONE);
//
//                break;
//
//            case AppConsts.RecipeOrigin.ADDED_MANUALLY:
//
//                if (recipeInstructions.isEmpty()) {
//                    editText_instructions.setText(getString(R.string.no_info));
//                } else {
//                    editText_instructions.setText(recipeInstructions);
//                }
//
//                if (recipeNotes.isEmpty()) {
//                    editText_notes.setText(getString(R.string.no_info));
//                } else {
//                    editText_notes.setText(recipeNotes);
//                }
//
//                floatingButton_getInstructions.setVisibility(View.GONE);
//                cardView_energy.setVisibility(View.GONE);
//
//                break;
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    public void tryToDownloadRecipeDetails() {
//        // For Example:
//        // http://api.yummly.com/v1/api/recipe/French-Onion-Soup-1292648?_app_id=417b707a&_app_key=249ec501a990bd7d5fa5dd5218bf7e14
//
//        String appId = AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL;
//        String appKey = AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL;
//        String requiredPictureValue = "true";
//
//        try {
//            appId = URLEncoder.encode(AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL, "utf-8");
//            appKey = URLEncoder.encode(AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL, "utf-8");
//            requiredPictureValue = URLEncoder.encode("true", "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        HashMap<String, String> query = new HashMap<>();
//        query.put("_app_id", appId);
//        query.put("_app_key", appKey);
//        query.put("requirePictures", requiredPictureValue);
//
//        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//        progressDialog.setTitle(getString(R.string.loading_info));
//        progressDialog.setMessage(getString(R.string.please_wait));
//        progressDialog.show();
//
//        subscriber = new Subscriber<RecipeSearchResult>() {
//            @Override
//            public void onCompleted() {
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                progressDialog.dismiss();
//                if (e instanceof UnknownHostException || e instanceof ConnectException) {
//                    AppHelper.showSnackBar(view, R.string.internet_error, Color.RED);
//                    ArrayList<String> emptyList = new ArrayList<>();
//                    fillList(layout_ingredientsList, emptyList);
//                    fillList(layout_categoriesList, emptyList);
//
//                } else {
//                    AppHelper.showSnackBar(view, R.string.unexpected_error, Color.RED);
//                }
//
//                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
//                toolbarButtonsList.add(AppConsts.ToolbarButtons.REFRESH);
//
//                mToolbarMenuSetting.setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.PRIMARY, getString(R.string.internet_error));
//            }
//
//            @Override
//            public void onNext(RecipeSearchResult yummlyRecipe) {
//
//                if (RecipeReviewFragment.this.isDetached()
//                        || !RecipeReviewFragment.this.isVisible()
//                        || RecipeReviewFragment.this.isRemoving()) {
//
//                    return;
//                }
//
//                setRecipeDetailsView();
//
//                progressDialog.dismiss();
//
//                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();
//
//                if (extra_serving != null && extra_serving.equals(AppConsts.Extras.ADD_SERVING_FROM_YUMMLY)) {
//                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING_FROM_YUMMLY);
//                } else {
//                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
//                }
//
//                mToolbarMenuSetting.setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
//
//                floatingButton_getInstructions.setVisibility(View.VISIBLE);
//                AnimationHelper.animateViewFadingIn(getContext(), floatingButton_getInstructions, 1500, 0);
//                AnimationHelper.animateViewFlipping(getContext(), floatingButton_getInstructions, 1500, 0);
//            }
//        };
//
//        Single<YummlyRecipeResponse2> getRecipeDetails =
//                ApiProvider.getYummlyApi().getYummlyRecipeByYummlyId(mOnlineSearchResultId, query);
//
//        getRecipeDetails
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(this::generateRecipeResult)
//                .map(searchResult -> updateRecipeFields(searchResult, true))
//                .subscribe(subscriber);
//
//        if (recipeImage == null) {
//            // Retry to download the recipe's image
//            String imageUrl = dbManager.queryResultObjectById(mRecipeId).imageUrl;
//            if (imageUrl != null && !imageUrl.isEmpty()) {
//                tryToGetRecipeImage(imageUrl);
//            }
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    private RecipeSearchResult generateRecipeResult(YummlyRecipeResponse2 yummlyRecipe) {
//        RecipeSearchResult searchResult = new RecipeSearchResult();
//
//        searchResult.recipeId = mRecipeId;
//        searchResult.yummlyId = mOnlineSearchResultId;
//
//        searchResult.title = yummlyRecipe.title;
//        searchResult.yield = yummlyRecipe.yield;
//        searchResult.time = yummlyRecipe.time;
//
//        searchResult.author = AppConsts.Category.NO_INFO;
//        searchResult.sourceUrl = AppConsts.Category.NO_INFO;
//
//        if (yummlyRecipe.source != null) {
//            if (yummlyRecipe.source.containsKey("sourceDisplayName")) {
//                searchResult.author = yummlyRecipe.source.get("sourceDisplayName");
//            }
//
//            if (yummlyRecipe.source.containsKey("sourceRecipeUrl")) {
//                searchResult.sourceUrl = yummlyRecipe.source.get("sourceRecipeUrl");
//            }
//        }
//
//        searchResult.ingredients = AppHelper.convertListToString(yummlyRecipe.ingredients);
//
//        ArrayList<String> categories = new ArrayList<>();
//        if (yummlyRecipe.attributes != null) {
//            categories = yummlyRecipe.attributes.get("course");
//        }
//        searchResult.categories = AppHelper.convertListToString(categories);
//
//        searchResult.energy = -1;
//        if (yummlyRecipe.nutritions != null) {
//            for (HashMap<String, Object> nutrition : yummlyRecipe.nutritions) {
//                if (nutrition.containsKey("attribute") && nutrition.get("attribute").equals("ENERC_KCAL")) {
//                    double calories = (double) nutrition.get("value");
//                    searchResult.energy = (int) calories;
//                    break;
//                }
//            }
//        }
//
//        return searchResult;
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    private RecipeSearchResult updateRecipeFields(RecipeSearchResult searchResult, boolean isToSaveToDb) {
//        recipeTitle = searchResult.title;
//        recipeAuthor = searchResult.author;
//        recipeUrl = searchResult.sourceUrl;
//        recipeTotalTime = searchResult.time;
//        recipeYield = searchResult.yield;
//        recipeTotalCalories = searchResult.energy;
//
//        ingredientsList = AppHelper.convertStringToList(searchResult.ingredients);
//        categoriesList = AppHelper.convertStringToList(searchResult.categories);
//        translatedCategories = AppHelper.getTranslatedCategoriesList(getContext(), categoriesList);
//
//        if (isToSaveToDb) {
//            dbManager.updateFullSearchResultInfo(searchResult);
//        }
//
//        return searchResult;
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    private void tryToGetRecipeImage(String imageUrl) {
//        Target target = new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                imageView_recipeImage.setVisibility(View.VISIBLE);
//
//                if (bitmap == null) {
//                    textView_noImageAvailable.setVisibility(View.VISIBLE);
//                } else {
//                    String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + mRecipeId;
//                    ImageStorage.saveToSdCard(getContext(), bitmap, imageName);
//                    recipeImage = bitmap;
//                    imageView_recipeImage.setImageBitmap(bitmap);
//                    textView_noImageAvailable.setVisibility(View.INVISIBLE);
//                }
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//            }
//        };
//
//        Picasso.with(getContext()).load(imageUrl).into(target);
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    public void onSaveOnlineRecipe() {
//
//        // Add the new recipe (from online search) to database
//
//        String resultStringIngredientsList = AppHelper.convertListToString(ingredientsList);
//        String resultStringCategoriesList = AppHelper.convertListToString(categoriesList);
//
//        Recipe recipe = new Recipe(recipeTitle, recipeAuthor, recipeYield, recipeUrl,
//                resultStringCategoriesList, resultStringIngredientsList, recipeTotalTime, recipeTotalCalories);
//
//        Uri uri = dbManager.addNewRecipe(recipe);
//        long recipeId = dbManager.getRecipeIdFromUri(uri);
//
//        mListener.onRecipeWasSaved(recipeId);
//
//        if (recipeImage != null) {
//            String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + recipeId;
//            File file = ImageStorage.saveToSdCard(getContext(), recipeImage, imageName);
//
//            if (file != null) {
//                Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(file.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
//                imageName = AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + recipeId;
//                ImageStorage.saveToSdCard(getContext(), thumbnail, imageName);
//                thumbnail.recycle();
//            }
//        }
//
//        AppHelper.showSnackBar(view, R.string.added_to_online_list, ContextCompat.getColor(getContext(), R.color.colorSnackbarGreen));
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    public void setFavouriteImage(int favouriteIndex) {
//
//        if (favouriteIndex == AppConsts.FavouriteIndex.FAVOURITE) {
//            AnimationHelper.animateViewFadingIn(getContext(), imageView_favourite, 1000, 0);
//
//        } else {
//            AnimationHelper.animateViewFadingOut(getContext(), imageView_favourite, 1000, 0);
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        try {
//            mListener = (FragmentRecipeReviewListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement FragmentRecipeReviewListener");
//        }
//
//        try {
//            mToolbarMenuSetting = (ToolbarMenuSetting) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement FragmentToolbarMenuListener");
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//
//        mListener = null;
//        mToolbarMenuSetting = null;
//
//        if (subscriber != null && !subscriber.isUnsubscribed()) {
//            Log.d("RecipeReviewFragment", "onDetach: subscriber is going to get unsubscribed");
//            subscriber.unsubscribe();
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
//        // This stops animation on rotation as we have a retained instance.
//        boolean shouldNotAnimate = enter && viewsHaveBeenDestroyed;
//        viewsHaveBeenDestroyed = false;
//        return shouldNotAnimate ? AnimationUtils.loadAnimation(getActivity(), R.anim.none)
//                : super.onCreateAnimation(transit, enter, nextAnim);
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        viewsHaveBeenDestroyed = true;
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        outState.putString("extra_serving", extra_serving);
//        outState.putString("mOnlineSearchResultId", mOnlineSearchResultId);
//        outState.putLong("mRecipeId", mRecipeId);
//        outState.putParcelable("recipeImage", recipeImage);
//        outState.putString("recipeTitle", recipeTitle);
//        outState.putString("recipeAuthor", recipeAuthor);
//        outState.putInt("recipeTotalTime", recipeTotalTime);
//        outState.putInt("recipeYield", recipeYield);
//        outState.putStringArrayList("ingredientsList", ingredientsList);
//        outState.putStringArrayList("categoriesList", categoriesList);
//        outState.putStringArrayList("translatedCategories", translatedCategories);
//        outState.putInt("recipeTotalCalories", recipeTotalCalories);
//        outState.putString("recipeUrl", recipeUrl);
//        outState.putInt("recipeOriginIndex", recipeOriginIndex);
//        outState.putInt("recipeFavouriteIndex", recipeFavouriteIndex);
//        outState.putString("recipeInstructions", recipeInstructions);
//        outState.putString("recipeNotes", recipeNotes);
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//
//            case R.id.floatingButton_getInstructions:
//
//                mListener.onGetInstructionsClick(recipeUrl);
//
//                break;
//        }
//    }
//
////-------------------------------------------------------------------------------------------------
//
//    public void fillList(LinearLayout parent, ArrayList<String> list) {
//
//        if (list.isEmpty()) {
//            list.add(AppConsts.Category.NO_INFO);
//        }
//
//        parent.removeAllViews();
//
//        LinearLayout listItem;
//        TextView textView_listItem;
//
//        for (int i = 0; i < list.size(); i++) {
//            String itemText = list.get(i);
//            listItem = (LinearLayout) inflater.inflate(R.layout.listitem_simple_textview, null);
//            textView_listItem = (TextView) listItem.findViewById(R.id.textView_listItem);
//
//            if (itemText.equals(AppConsts.Category.NO_INFO)) {
//                textView_listItem.setText(getString(R.string.no_info));
//
//            } else {
//                textView_listItem.setText(itemText);
//            }
//
//            if (i == list.size() - 1) {
//                FrameLayout listSeparator = (FrameLayout) listItem.findViewById(R.id.listSeparator);
//                listSeparator.setVisibility(View.INVISIBLE);
//            }
//
//            parent.addView(listItem);
//        }
//    }
//
//}
