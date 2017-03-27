package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.control.ToolbarMenuSetting;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.model.Recipe;
import com.hanoch.greatrecipes.model.RecipeSearchResult;
import com.hanoch.greatrecipes.utilities.HttpHandler;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class RecipeReviewFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_EXTRA_SERVING = "serving";
    private static final String ARG_RECIPE_ID = "recipeId";
    private static final String ARG_ONLINE_SEARCH_RESULT_ID = "onlineSearchResultId";

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

    private ToolbarMenuSetting mToolbarMenuSetting;

    private boolean viewsHaveBeenDestroyed;

    private String mOnlineSearchResultId;
    private long mRecipeId;
    private String recipeUrl;
    private String extra_serving;

    private ArrayList<String> ingredientsList;
    private ArrayList<String> categoriesList;
    private ArrayList<String> translatedCategories;

    private Recipe recipe;

    private int recipeTotalTime;
    private Bitmap recipeImage;
    private String recipeAuthor;
    private String recipeTitle;
    private int recipeYield;
    private int recipeTotalCalories;
    private int recipeFavouriteIndex;
    private String recipeInstructions;
    private String recipeNotes;
    private int recipeOriginIndex;
    private GetRecipeDetailsFromYummlyApi networkSearch;

    private DbManager dbManager;


//-------------------------------------------------------------------------------------------------

    public interface FragmentRecipeReviewListener {
        void onRecipeWasSaved(long recipeId);

        void onGetInstructionsClick(String url);
    }

//-------------------------------------------------------------------------------------------------

    public static RecipeReviewFragment newInstance(long recipeId, String onlineSearchResultId, String extra_serving) {

        RecipeReviewFragment fragment = new RecipeReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
        args.putLong(ARG_RECIPE_ID, recipeId);
        args.putString(ARG_ONLINE_SEARCH_RESULT_ID, onlineSearchResultId);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbManager = DbManager.getInstance(getContext());
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);

        this.inflater = inflater;

        if (view != null) return view;

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recipe_review, container, false);

        (view.findViewById(R.id.imageView_choosePicture)).setVisibility(View.GONE);
        (view.findViewById(R.id.imageView_deleteImage)).setVisibility(View.GONE);
        (view.findViewById(R.id.button_addCategory)).setVisibility(View.GONE);
        (view.findViewById(R.id.button_addIngredient)).setVisibility(View.GONE);

        editText_recipeTitle = (EditText) view.findViewById(R.id.editText_recipeTitle);
        editText_recipeTitle.setEnabled(false);
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

        ingredientsList = new ArrayList<>();
        categoriesList = new ArrayList<>();
        translatedCategories = new ArrayList<>();

        if (savedInstanceState != null) {
            // Restore recipe details from the state

            extra_serving = savedInstanceState.getString("extra_serving");
            mOnlineSearchResultId = savedInstanceState.getString("mOnlineSearchResultId");
            mRecipeId = savedInstanceState.getLong("mRecipeId");

            recipeImage = savedInstanceState.getParcelable("recipeImage");

            if (recipeImage == null) {
                textView_noImageAvailable.setVisibility(View.VISIBLE);
            }

            recipeTitle = savedInstanceState.getString("recipeTitle");

            if (recipeTitle != null) {
                editText_recipeTitle.setText(recipeTitle);
            }

            recipeAuthor = savedInstanceState.getString("recipeAuthor");
            recipeTotalTime = savedInstanceState.getInt("recipeTotalTime");
            recipeYield = savedInstanceState.getInt("recipeYield");
            ingredientsList = savedInstanceState.getStringArrayList("ingredientsList");
            categoriesList = savedInstanceState.getStringArrayList("categoriesList");
            translatedCategories = savedInstanceState.getStringArrayList("translatedCategories");

            recipeTotalCalories = savedInstanceState.getInt("recipeTotalCalories");
            recipeUrl = savedInstanceState.getString("recipeUrl");

            recipeOriginIndex = savedInstanceState.getInt("recipeOriginIndex");
            recipeFavouriteIndex = savedInstanceState.getInt("recipeFavouriteIndex");

            recipeInstructions = savedInstanceState.getString("recipeInstructions");
            recipeNotes = savedInstanceState.getString("recipeNotes");

            setRecipeDetailsView();

        } else {

            Bundle args = getArguments();

            // Getting the RecipeId argument from the bundle:
            mOnlineSearchResultId = args.getString(ARG_ONLINE_SEARCH_RESULT_ID);
            mRecipeId = args.getLong(ARG_RECIPE_ID, -1);
            extra_serving = getArguments().getString(ARG_EXTRA_SERVING);

            if (mOnlineSearchResultId == null) {
                // Going to show the details of a recipe that exists in
                // one of the user's lists (has a local recipeId):

                String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + mRecipeId;
                recipeImage = ImageStorage.getImageBitmapByName(getContext(), imageName);

                recipe = dbManager.queryRecipeObjectById(mRecipeId);

                recipeFavouriteIndex = recipe.getFavouriteIndex();
                recipeTitle = recipe.getTitle();
                recipeTotalTime = recipe.getTime();
                recipeYield = recipe.getYield();
                recipeAuthor = recipe.getAuthor();
                ingredientsList = AppHelper.stringToListConverter(recipe.getIngredientsList());
                categoriesList = AppHelper.stringToListConverter(recipe.getCategoriesList());
                translatedCategories = AppHelper.getTranslatedCategoriesList(getContext(), categoriesList);
                recipeOriginIndex = recipe.getOriginIndex();

                switch (recipeOriginIndex) {

                    case AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH:

                        recipeTotalCalories = recipe.getEnergy();
                        recipeUrl = recipe.getUrl();

                        break;

                    case AppConsts.RecipeOrigin.ADDED_MANUALLY:

                        recipeInstructions = recipe.getInstructions();
                        recipeNotes = recipe.getNotes();

                        break;
                }

                setRecipeDetailsView();

            } else {
                // Going to show an online search result

                String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + mRecipeId;
                recipeImage = ImageStorage.getImageBitmapByName(getContext(), imageName);

                imageView_recipeImage.setImageBitmap(recipeImage);

                if (recipeImage == null) {
                    textView_noImageAvailable.setVisibility(View.VISIBLE);

                } else {
                    AppHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
                }

                cardView_instructions.setVisibility(View.GONE);

                cardView_privateNotes.setVisibility(View.GONE);

                RecipeSearchResult result = dbManager.queryResultObjectById(mRecipeId);

                if (result.getAuthor() == null) {
                    // Download the rest of the result's details

                    tryToDownloadRecipeDetails();

                } else {
                    // This result has already been reviewed before, thus it exists in the database

                    recipeTitle = result.getTitle();
                    recipeAuthor = result.getAuthor();
                    recipeTotalTime = result.getTime();
                    recipeUrl = result.getSourceUrl();
                    recipeYield = result.getYield();
                    recipeTotalCalories = result.getEnergy();

                    ingredientsList = AppHelper.stringToListConverter(result.getIngredients());
                    categoriesList = AppHelper.stringToListConverter(result.getCategories());
                    translatedCategories = AppHelper.getTranslatedCategoriesList(getContext(), categoriesList);

                    setRecipeDetailsView();

                    ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                    if ((extra_serving != null) &&
                            (extra_serving.equals(AppConsts.Extras.ADD_SERVING))) {

                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);

                    } else {
                        toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
                    }

                    mToolbarMenuSetting.setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);
                }
            }
        }

        AppHelper.hideTheKeyboard(getActivity());
        AppHelper.hide_keyboard_from(getContext(), view);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    private void setRecipeDetailsView() {

        imageView_recipeImage.setImageBitmap(recipeImage);

        if (recipeImage == null) {
            textView_noImageAvailable.setVisibility(View.VISIBLE);

        } else {
            AppHelper.animateViewFadingIn(getContext(), imageView_recipeImage, 2500, 0);
        }

        if (recipeTitle != null) {
            editText_recipeTitle.setText(recipeTitle.isEmpty() ? getString(R.string.no_info) : recipeTitle);
        }

        String stringTotalTime = AppHelper.getStringRecipeTotalTime(getContext(), recipeTotalTime);
        textView_recipeTime.setText(stringTotalTime);

        if (recipeYield == 0) {
            textView_servings.setText(getString(R.string.no_info));
        } else {
            textView_servings.setText(recipeYield + " " + getString(R.string.servings));
        }

        if (recipeAuthor == null) {
            recipeAuthor = getString(R.string.no_info);
        }

        textView_recipeAuthor.setText(getString(R.string.from) + "\n" + recipeAuthor);

        fillList(layout_ingredientsList, ingredientsList);

        if (translatedCategories.isEmpty()) {
            translatedCategories.add(AppConsts.Category.NO_INFO);
        }

        fillList(layout_categoriesList, translatedCategories);

        setFavouriteImage(recipeFavouriteIndex);

        switch (recipeOriginIndex) {

            case AppConsts.RecipeOrigin.FROM_ONLINE_SEARCH:

                if (recipeTotalCalories != -1) {
                    textView_totalCalories.setText(recipeTotalCalories + " " + getString(R.string.calories));
                }

                floatingButton_getInstructions.setVisibility(View.VISIBLE);
                AppHelper.animateViewFadingIn(getContext(), floatingButton_getInstructions, 1500, 0);
                AppHelper.animateViewFlipping(getContext(), floatingButton_getInstructions, 1500, 0);

                cardView_instructions.setVisibility(View.GONE);
                cardView_privateNotes.setVisibility(View.GONE);

                break;

            case AppConsts.RecipeOrigin.ADDED_MANUALLY:

                if (recipeInstructions.equals("")) {
                    editText_instructions.setText(getString(R.string.no_info));
                } else {
                    editText_instructions.setText(recipeInstructions);
                }

                if (recipeNotes.equals("")) {
                    editText_notes.setText(getString(R.string.no_info));
                } else {
                    editText_notes.setText(recipeNotes);
                }

                floatingButton_getInstructions.setVisibility(View.GONE);
                cardView_energy.setVisibility(View.GONE);

                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    public void tryToDownloadRecipeDetails() {

        RecipeSearchResult result = dbManager.queryResultObjectById(mRecipeId);

        networkSearch = new GetRecipeDetailsFromYummlyApi();
        networkSearch.execute(AppConsts.ApiAccess.API_ADDRESS_YUMMLY_RECIPE_SEARCH + mOnlineSearchResultId);

        if (recipeImage == null) {
            // Retry to download the recipe's image

            GetBitmapFromApi getBitmapFromApi = new GetBitmapFromApi(mRecipeId);
            getBitmapFromApi.execute(result.getImageUrl());
        }
    }

//-------------------------------------------------------------------------------------------------

    public void onSaveOnlineRecipe() {

        // Add the new recipe (from online search) to database

        String resultStringIngredientsList = AppHelper.listToStringConverter(ingredientsList);

        String resultStringCategoriesList = AppHelper.listToStringConverter(categoriesList);

        recipe = new Recipe(recipeTitle, recipeAuthor, recipeYield, recipeUrl,
                resultStringCategoriesList, resultStringIngredientsList, recipeTotalTime, recipeTotalCalories);

        Uri uri = dbManager.addNewRecipe(recipe);
        long recipeId = dbManager.getRecipeIdFromUri(uri);

        mListener.onRecipeWasSaved(recipeId);

        if (recipeImage != null) {
            String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + recipeId;
            File file = ImageStorage.saveToSdCard(getContext(), recipeImage, imageName);

            if (file !=null) {
                Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(file.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
                imageName = AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + recipeId;
                ImageStorage.saveToSdCard(getContext(), thumbnail, imageName);
                thumbnail.recycle();
            }

            //recipeImage.recycle();
        }

        Snackbar snack = Snackbar.make(view, R.string.added_to_online_list, Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorSnackbarGreen));
        snack.show();
    }

//-------------------------------------------------------------------------------------------------

    public void setFavouriteImage(int favouriteIndex) {

        if (favouriteIndex == AppConsts.FavouriteIndex.FAVOURITE) {

            AppHelper.animateViewFadingIn(getContext(), imageView_favourite, 1000, 0);

        } else {
            AppHelper.animateViewFadingOut(getContext(), imageView_favourite, 1000, 0);
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

        try {
            mToolbarMenuSetting = (ToolbarMenuSetting) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FragmentToolbarMenuListener");
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
        mToolbarMenuSetting = null;

        if (networkSearch != null) networkSearch.cancel(true);
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
        outState.putString("mOnlineSearchResultId", mOnlineSearchResultId);
        outState.putLong("mRecipeId", mRecipeId);
        outState.putParcelable("recipeImage", recipeImage);
        outState.putString("recipeTitle", recipeTitle);
        outState.putString("recipeAuthor", recipeAuthor);
        outState.putInt("recipeTotalTime", recipeTotalTime);
        outState.putInt("recipeYield", recipeYield);
        outState.putStringArrayList("ingredientsList", ingredientsList);
        outState.putStringArrayList("categoriesList", categoriesList);
        outState.putStringArrayList("translatedCategories", translatedCategories);
        outState.putInt("recipeTotalCalories", recipeTotalCalories);
        outState.putString("recipeUrl", recipeUrl);
        outState.putInt("recipeOriginIndex", recipeOriginIndex);
        outState.putInt("recipeFavouriteIndex", recipeFavouriteIndex);
        outState.putString("recipeInstructions", recipeInstructions);
        outState.putString("recipeNotes", recipeNotes);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.floatingButton_getInstructions:

                mListener.onGetInstructionsClick(recipeUrl);

                break;
        }
    }

//-------------------------------------------------------------------------------------------------

    private class GetRecipeDetailsFromYummlyApi extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

        //****************************************************

        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getString(R.string.searching));
            progressDialog.setMessage(getString(R.string.please_wait));

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (progressDialog != null) {
                        progressDialog.show();
                    }
                }
            }, 600);

        }

        //****************************************************

        @Override
        protected String doInBackground(String... params) {

            String address = params[0];
            String queryString = null;
            try {

                // For Example:
                // http://api.yummly.com/v1/api/recipe/French-Onion-Soup-1292648?_app_id=417b707a&_app_key=249ec501a990bd7d5fa5dd5218bf7e14

                queryString =
                        "&_app_id=" + URLEncoder.encode(AppConsts.ApiAccess.APP_ID_YUMMLY_MICHAL, "utf-8")
                                + "&_app_key=" + URLEncoder.encode(AppConsts.ApiAccess.APP_KEY_YUMMLY_MICHAL, "utf-8")
                                + "&requirePictures=" + URLEncoder.encode("true", "utf-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return HttpHandler.getFromApi(address, queryString);
        }

        //****************************************************

        @Override
        protected void onPostExecute(String result) {

            ingredientsList = new ArrayList<>();
            categoriesList = new ArrayList<>();

            progressDialog.dismiss();

            progressDialog = null;

            if (RecipeReviewFragment.this.isDetached()) return;

            if (result == null) {
                // Internet error

                mToolbarMenuSetting.setToolbarAttr(null, AppConsts.ToolbarColor.PRIMARY, getString(R.string.online_search));

                Snackbar snack = Snackbar.make(view, R.string.internet_error, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(Color.RED);
                snack.show();

                ingredientsList.add(getString(R.string.no_info));
                fillList(layout_ingredientsList, ingredientsList);

                categoriesList.add(getString(R.string.no_info));
                fillList(layout_categoriesList, categoriesList);

                recipeTotalCalories = -1;

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                toolbarButtonsList.add(AppConsts.ToolbarButtons.REFRESH);

                mToolbarMenuSetting.setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.PRIMARY, getString(R.string.internet_error));

                return;
            }

            JSONObject resultRecipe;
            try {
                resultRecipe = new JSONObject(result);

                recipeAuthor = AppConsts.Category.NO_INFO;

                try {
                    JSONObject source = resultRecipe.getJSONObject("source");
                    recipeAuthor = source.getString("sourceDisplayName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                recipeTitle = resultRecipe.getString("name");

                recipeTotalTime = 0;
                try {
                    recipeTotalTime = resultRecipe.getInt("totalTimeInSeconds");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                recipeUrl = getString(R.string.no_info);
                try {
                    recipeUrl = resultRecipe.getJSONObject("source").getString("sourceRecipeUrl");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                recipeYield = 0;
                try {
                    recipeYield = resultRecipe.getInt("numberOfServings");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray nutritionEstimatesJsonArray = resultRecipe.getJSONArray("nutritionEstimates");
                String nutritionName;
                JSONObject nutritionJsonObject;
                recipeTotalCalories = -1;

                for (int i = 0; i < nutritionEstimatesJsonArray.length(); i++) {
                    nutritionJsonObject = nutritionEstimatesJsonArray.getJSONObject(i);
                    nutritionName = nutritionJsonObject.getString("attribute");
                    if (nutritionName.equals("ENERC_KCAL")) {
                        recipeTotalCalories = (int) nutritionJsonObject.getDouble("value");
                    }
                }

                /***************************/

                JSONArray ingredientsJsonArray = new JSONArray();

                ingredientsJsonArray.put(AppConsts.Category.NO_INFO);
                try {
                    ingredientsJsonArray = resultRecipe.getJSONArray("ingredientLines");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < ingredientsJsonArray.length(); i++) {
                    ingredientsList.add(ingredientsJsonArray.getString(i));
                }

                /***************************/

                JSONArray coursesJsonArray = new JSONArray();
                coursesJsonArray.put(AppConsts.Category.NO_INFO);
                try {
                    coursesJsonArray = resultRecipe.getJSONObject("attributes").getJSONArray("course");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < coursesJsonArray.length(); i++) {
                    categoriesList.add(coursesJsonArray.getString(i));
                }

                translatedCategories = AppHelper.getTranslatedCategoriesList(getContext(), categoriesList);

                /***************************/

                if (RecipeReviewFragment.this.isDetached()) return;
                if (!RecipeReviewFragment.this.isVisible()) return;
                if (RecipeReviewFragment.this.isRemoving()) return;

                setRecipeDetailsView();

                ArrayList<Integer> toolbarButtonsList = new ArrayList<>();

                if ((extra_serving != null) &&
                        (extra_serving.equals(AppConsts.Extras.ADD_SERVING))) {

                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_SERVING);

                } else {
                    toolbarButtonsList.add(AppConsts.ToolbarButtons.ADD_TO_LIST);
                }

                mToolbarMenuSetting.setToolbarAttr(toolbarButtonsList, AppConsts.ToolbarColor.ACCENT, null);

                String resultStringIngredientsList = AppHelper.listToStringConverter(ingredientsList);
                String resultStringCategoriesList = AppHelper.listToStringConverter(categoriesList);

                RecipeSearchResult searchResult = new RecipeSearchResult(mRecipeId, mOnlineSearchResultId,
                        recipeTitle, recipeAuthor, recipeYield, recipeTotalTime, resultStringIngredientsList,
                        recipeTotalCalories, resultStringCategoriesList, recipeUrl, null);

                dbManager.updateSearchResult(searchResult);

                floatingButton_getInstructions.setVisibility(View.VISIBLE);
                AppHelper.animateViewFadingIn(getContext(), floatingButton_getInstructions, 1500, 0);
                AppHelper.animateViewFlipping(getContext(), floatingButton_getInstructions, 1500, 0);

            } catch (JSONException e) {
                e.printStackTrace();

                Snackbar snack = Snackbar.make(view, R.string.internet_error, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(Color.RED);
                snack.show();
            }
        }

        //********

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        }
    }

//----------------------------------------------------------------------------------------------

    private class GetBitmapFromApi extends AsyncTask<String, Integer, Bitmap> {

        private long id;

        public GetBitmapFromApi(long id) {

            this.id = id;
        }

        //****************************************************

        @Override
        protected Bitmap doInBackground(String... params) {

            String address = params[0];

            Bitmap bitmap = HttpHandler.getBitmap(address, null);
            if (bitmap != null) {
                String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + id;
                ImageStorage.saveToSdCard(getContext(), bitmap, imageName);
            }

            return bitmap;
        }

        //****************************************************

        @Override
        protected void onPostExecute(Bitmap image) {

            imageView_recipeImage.setVisibility(View.VISIBLE);

            if (image != null) {
                recipeImage = image;
                imageView_recipeImage.setImageBitmap(image);
                textView_noImageAvailable.setVisibility(View.INVISIBLE);

            } else {
                textView_noImageAvailable.setVisibility(View.VISIBLE);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    public void fillList(LinearLayout parent, ArrayList<String> list) {

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
