package com.hanoch.greatrecipes.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.model.Recipe;
import com.hanoch.greatrecipes.utilities.ImageStorage;
import com.hanoch.greatrecipes.utilities.PicUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;


public class EditRecipeFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_RECIPE_ID = "recipeId";
    private static final int REQ_CODE_PICK_IMAGE = 1;

    private ImageView imageView_recipeImage;
    private ImageView imageView_favourite;
    private ImageView button_deleteImage;
    private ImageView button_choosePicture;
    private EditText editText_recipeTitle;
    private EditText editText_inputIngredient;
    private EditText editText_recipeInstructions;
    private EditText editText_notes;
    private TextView textView_totalTime;
    private TextView textView_servings;
    private ImageView button_addIngredient;
    private ImageView button_addCategory;
    private CardView cardView_yield;
    private CardView cardView_time;
    private LinearLayout layout_ingredientsList;
    private LinearLayout layout_categoriesList;
    private View view;
    private View selectedIngredientView;

    private boolean viewsHaveBeenDestroyed;

    private DbManager dbManager;
    private OnFragmentEditRecipeListener mListener;
    private LayoutInflater inflater;

    private ArrayList<String> ingredientsList;
    private ArrayList<String> categoriesList;

    private long mRecipeId;

    private int totalTimeInSeconds;
    private int servings;

    private int favouriteIndex;
    private boolean imageWasDeleted;
    private Bitmap recipeImage;
    private Bitmap selectedImage;

    private String author;
    private Dialog loginDialog;
    private Dialog servingsDialog;
    private Dialog totalTimeDialog;

//-------------------------------------------------------------------------------------------------

    public interface OnFragmentEditRecipeListener {
        void onRecipeWasSaved(long recipeId);

        void onCancelLoginButtonClicked();

        void onAddCategoriesClick(ArrayList<String> categoriesList);
    }

//-------------------------------------------------------------------------------------------------

    public static EditRecipeFragment newInstance(long recipeId) {

        EditRecipeFragment fragment = new EditRecipeFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECIPE_ID, recipeId);
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

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (view != null) {

            if (savedInstanceState != null) {

                boolean loginDialogIsShowing = savedInstanceState.getBoolean("loginDialogIsShowing");
                if (loginDialogIsShowing) {
                    String currentUserInput = savedInstanceState.getString("currentUserInput");
                    showLoginDialog(currentUserInput);

                } else {
                    boolean servingDialogIsShowing = savedInstanceState.getBoolean("servingDialogIsShowing");
                    if (servingDialogIsShowing) {
                        int currentServingsValue = savedInstanceState.getInt("currentServingsValue");
                        showServingsChooserDialog(currentServingsValue);

                    } else {
                        boolean totalTimeDialogIsShowing = savedInstanceState.getBoolean("totalTimeDialogIsShowing");
                        if (totalTimeDialogIsShowing) {
                            int currentHoursValue = savedInstanceState.getInt("currentHoursValue");
                            int currentMinutesValue = savedInstanceState.getInt("currentMinutesValue");
                            showTimeChooserDialog(currentHoursValue, currentMinutesValue);
                        }
                    }
                }
            }

            return view;
        }

        this.inflater = inflater;

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);


        (view.findViewById(R.id.textView_noImageAvailable)).setVisibility(View.GONE);
        (view.findViewById(R.id.textView_recipeAuthor)).setVisibility(View.GONE);
        (view.findViewById(R.id.cardView_energy)).setVisibility(View.GONE);


        layout_ingredientsList = (LinearLayout) view.findViewById(R.id.layout_ingredientsList);
        layout_categoriesList = (LinearLayout) view.findViewById(R.id.layout_categoriesList);

        editText_recipeTitle = (EditText) view.findViewById(R.id.editText_recipeTitle);

        textView_servings = (TextView) view.findViewById(R.id.textView_servings);

        cardView_yield = (CardView) view.findViewById(R.id.cardView_yield);
        cardView_yield.setOnClickListener(this);

        cardView_time = (CardView) view.findViewById(R.id.cardView_totalTime);
        cardView_time.setOnClickListener(this);

        textView_totalTime = (TextView) view.findViewById(R.id.textView_totalTime);

        editText_recipeInstructions = (EditText) view.findViewById(R.id.editText_instructions);
        editText_notes = (EditText) view.findViewById(R.id.editText_privateNotes);

        imageView_recipeImage = (ImageView) view.findViewById(R.id.imageView_recipeImage);
        imageView_recipeImage.setOnClickListener(this);

        imageView_favourite = (ImageView) view.findViewById(R.id.imageView_favourite);
        imageView_favourite.setVisibility(View.INVISIBLE);

        button_deleteImage = (ImageView) view.findViewById(R.id.imageView_deleteImage);
        button_deleteImage.setVisibility(View.INVISIBLE);
        button_deleteImage.setOnClickListener(this);

        button_choosePicture = (ImageView) view.findViewById(R.id.imageView_choosePicture);
        button_choosePicture.setOnClickListener(this);

        button_addIngredient = (ImageView) view.findViewById(R.id.button_addIngredient);
        button_addIngredient.setOnClickListener(this);

        button_addCategory = (ImageView) view.findViewById(R.id.button_addCategory);
        button_addCategory.setOnClickListener(this);

        AppHelper.hideTheKeyboard(getActivity());

        if (savedInstanceState != null) {

            ingredientsList = savedInstanceState.getStringArrayList("ingredientsList");
            categoriesList = savedInstanceState.getStringArrayList("categoriesList");
            mRecipeId = savedInstanceState.getLong("mRecipeId");
            totalTimeInSeconds = savedInstanceState.getInt("totalTimeInSeconds");
            servings = savedInstanceState.getInt("servings");
            favouriteIndex = savedInstanceState.getInt("favouriteIndex");
            imageWasDeleted = savedInstanceState.getBoolean("imageWasDeleted");
            recipeImage = savedInstanceState.getParcelable("recipeImage");
            selectedImage = savedInstanceState.getParcelable("selectedImage");
            author = savedInstanceState.getString("author");

        } else {

            //get the arguments bundle:
            Bundle args = getArguments();
            mRecipeId = args.getLong(ARG_RECIPE_ID);

            if (mRecipeId != AppConsts.NEW_RECIPE) {
                // Editing an existing recipe from 'My Own Recipes' list

                Recipe recipe = dbManager.queryRecipeObjectById(mRecipeId);

                author = recipe.author;

                favouriteIndex = recipe.favouriteIndex;

                editText_recipeTitle.setText(recipe.title);

                totalTimeInSeconds = recipe.time;

                servings = recipe.yield;

                editText_recipeInstructions.setText(recipe.instructions);
                editText_notes.setText(recipe.notes);

                String ingredientsStringList = recipe.ingredientsList;

                ingredientsList = AppHelper.stringToListConverter(ingredientsStringList);

                String categoriesStringList = recipe.categoriesList;

                categoriesList = AppHelper.stringToListConverter(categoriesStringList);

                String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + mRecipeId;
                recipeImage = ImageStorage.getImageBitmapByName(getContext(), imageName);

            } else {
                // Adding a new recipe to 'My Own Recipes' list

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                author = sp.getString(AppConsts.SharedPrefs.USER_NAME, AppConsts.SharedPrefs.NEW_USER);

                if (author.equals(AppConsts.SharedPrefs.NEW_USER)) {
                    showLoginDialog("");
                }

                favouriteIndex = AppConsts.FavouriteIndex.NOT_FAVOURITE;

                servings = 0;

                totalTimeInSeconds = 0;

                ingredientsList = new ArrayList<String>();
                ingredientsList.add(AppConsts.Category.NO_INFO);

                categoriesList = new ArrayList<String>();
            }
        }

        setRecipeView();

        return view;
    }

//-------------------------------------------------------------------------------------------------

    private void setRecipeView() {

        if (!imageWasDeleted) {

            if (selectedImage != null) {
                button_choosePicture.setVisibility(View.INVISIBLE);
                button_deleteImage.setVisibility(View.VISIBLE);
                imageView_recipeImage.setImageBitmap(selectedImage);

            } else if (recipeImage != null) {
                button_choosePicture.setVisibility(View.INVISIBLE);
                button_deleteImage.setVisibility(View.VISIBLE);
                imageView_recipeImage.setImageBitmap(recipeImage);
            }
        }

        setFavouriteImage(favouriteIndex);

        if (totalTimeInSeconds > 0) {
            textView_totalTime.setTextColor(Color.BLACK);
        }

        textView_totalTime.setText(AppHelper.getStringRecipeTotalTime(getContext(), totalTimeInSeconds));

        if (servings > 0) {
            textView_servings.setTextColor(Color.BLACK);
        }

        textView_servings.setText(AppHelper.getStringRecipeServings(getContext(), servings));

        fillIngredientsList();

        updateCategoriesList(getContext(), categoriesList);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        AnalyticsHelper.setScreenName(this);

        try {
            mListener = (OnFragmentEditRecipeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentEditRecipeListener");
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

        outState.putStringArrayList("ingredientsList", ingredientsList);
        outState.putStringArrayList("categoriesList", categoriesList);
        outState.putLong("mRecipeId", mRecipeId);
        outState.putInt("totalTimeInSeconds", totalTimeInSeconds);
        outState.putInt("servings", servings);
        outState.putInt("favouriteIndex", favouriteIndex);
        outState.putBoolean("imageWasDeleted", imageWasDeleted);

        if (loginDialog != null && loginDialog.isShowing()) {
            outState.putBoolean("loginDialogIsShowing", true);

            EditText editText_userInput = (EditText) loginDialog.findViewById(R.id.editText_userInput);
            String currentUserInput = editText_userInput.getText().toString();
            outState.putString("currentUserInput", currentUserInput);

        } else if (servingsDialog != null && servingsDialog.isShowing()) {
            outState.putBoolean("servingDialogIsShowing", true);

            NumberPicker servingsPicker = (NumberPicker) servingsDialog.findViewById(R.id.numberPicker_servings);
            int currentServingsValue = servingsPicker.getValue();
            outState.putInt("currentServingsValue", currentServingsValue);

        } else if (totalTimeDialog != null && totalTimeDialog.isShowing()) {
            outState.putBoolean("totalTimeDialogIsShowing", true);

            NumberPicker hoursPicker = (NumberPicker) totalTimeDialog.findViewById(R.id.numberPicker_hours);
            NumberPicker minutesPicker = (NumberPicker) totalTimeDialog.findViewById(R.id.numberPicker_minutes);

            int currentHoursValue = hoursPicker.getValue();
            int currentMinutesValue = minutesPicker.getValue();

            outState.putInt("currentHoursValue", currentHoursValue);
            outState.putInt("currentMinutesValue", currentMinutesValue);
        }

        if (recipeImage != null && !recipeImage.isRecycled()) {
            outState.putParcelable("recipeImage", recipeImage);
        }

        if (selectedImage != null && !selectedImage.isRecycled()) {
            outState.putParcelable("selectedImage", selectedImage);
        }

        outState.putString("author", author);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        // The user chose an image to his recipe

        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case REQ_CODE_PICK_IMAGE:

                    if (imageReturnedIntent == null) return;

                    if (selectedImage != null) {
                        selectedImage.recycle();
                    }

                    imageView_recipeImage.setImageBitmap(null);

                    if (recipeImage != null) {
                        recipeImage.recycle();
                        recipeImage = null;
                    }

                    String picturePath = null;

                    Uri selectedImageUri = imageReturnedIntent.getData();

                    try {
                        picturePath = PicUtils.getPathFromUri(getActivity(), selectedImageUri);
                        if (picturePath == null) throw new Exception();

                        selectedImage = ImageStorage.decodeSampledBitmapFromFile(picturePath, Bitmap.Config.ARGB_8888, 300, 300);

                    } catch (Exception e) {

                        try {
                            ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(selectedImageUri, "r");
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            selectedImage = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                            parcelFileDescriptor.close();

                        } catch (Exception e2) {
                            Snackbar snack = Snackbar.make(view, R.string.the_image_not_supported, Snackbar.LENGTH_LONG);
                            ViewGroup group = (ViewGroup) snack.getView();
                            group.setBackgroundColor(Color.RED);
                            snack.show();
                            return;
                        }
                    }

                    if (selectedImage == null) {
                        Snackbar snack = Snackbar.make(view, R.string.the_image_not_supported, Snackbar.LENGTH_LONG);
                        ViewGroup group = (ViewGroup) snack.getView();
                        group.setBackgroundColor(Color.RED);
                        snack.show();
                        return;
                    }

                    if (picturePath != null) {
                        selectedImage = ImageStorage.orientFromExif(selectedImage, ImageStorage.getExifOrientation(picturePath));
                    }

                    imageView_recipeImage.setImageBitmap(selectedImage);
                    imageWasDeleted = false;
                    button_choosePicture.setVisibility(View.INVISIBLE);
                    button_deleteImage.setVisibility(View.VISIBLE);

                    break;
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {

                case AppConsts.Permissions.PERMISSION_REQ_CODE_READ_STORAGE:
                case AppConsts.Permissions.PERMISSION_REQ_CODE_WRITE_STORAGE:
                    button_choosePicture.performClick();
                    break;
            }
        } else {
            Snackbar snack = Snackbar.make(view, R.string.you_should_approve_permission, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(Color.RED);
            snack.show();
        }
    }

//-------------------------------------------------------------------------------------------------

    public static boolean isPermissionGranted(Activity activity, int permissionCode) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
            return true;

        String permissionToCheck = AppConsts.permissionsMap.get(permissionCode);

        if (permissionToCheck == null)
            return false; // Permission is not in the map - should never happen

        if (activity.checkSelfPermission(permissionToCheck) == PackageManager.PERMISSION_GRANTED) {
            return true;

        } else {
            if (activity.shouldShowRequestPermissionRationale(permissionToCheck)) {
                // Currently will show the android generic request permissionCode popup again,
                // unless the user clicked the "don't show again" button last time.
                // TODO: replace with my dialog, explaining why I need his permissionCode

                activity.requestPermissions(new String[]{permissionToCheck}, permissionCode);

                return false;
            } else {
                // Permission not granted yet, requesting generic permissionCode
                activity.requestPermissions(new String[]{permissionToCheck}, permissionCode);
                return false;
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    public void setFavouriteImage(int favouriteIndex) {

        this.favouriteIndex = favouriteIndex;

        if (favouriteIndex == AppConsts.FavouriteIndex.FAVOURITE) {

            AppHelper.animateViewFadingIn(getContext(), imageView_favourite, 500, 0);

        } else {
            AppHelper.animateViewFadingOut(getContext(), imageView_favourite, 500, 0);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void onSaveRecipe() {

        // Get the user's inputs:
        String title = editText_recipeTitle.getText().toString();
        if (title.trim().isEmpty()) title = "";

        String instructions = editText_recipeInstructions.getText().toString();
        if (instructions.trim().isEmpty()) instructions = "";

        String notes = editText_notes.getText().toString();
        if (notes.trim().isEmpty()) notes = "";

        String StringIngredientsList = AppHelper.listToStringConverter(ingredientsList);

        String StringCategoriesList = AppHelper.listToStringConverter(categoriesList);

        long recipeId = getArguments().getLong(ARG_RECIPE_ID, AppConsts.NEW_RECIPE);

        Recipe recipe = new Recipe(recipeId, title, author, servings,
                AppConsts.NO_RATING, favouriteIndex, notes,
                totalTimeInSeconds, StringCategoriesList, StringIngredientsList,
                instructions);

        if (recipeId == AppConsts.NEW_RECIPE) {

            Uri uri = dbManager.addNewRecipe(recipe);
            recipeId = dbManager.getRecipeIdFromUri(uri);

            Snackbar snack = Snackbar.make(view, R.string.added_to_my_own, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorSnackbarGreen));
            snack.show();

            if (!imageWasDeleted && selectedImage != null) {
                String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + recipeId;
                File file = ImageStorage.saveToSdCard(getContext(), selectedImage, imageName);

                if (file != null) {
                    Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(file.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
                    imageName = AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + recipeId;
                    ImageStorage.saveToSdCard(getContext(), thumbnail, imageName);
                    thumbnail.recycle();
                }

                selectedImage.recycle();
            }

        } else {
            dbManager.updateRecipe(recipe);

            Snackbar snack = Snackbar.make(view, R.string.saved_successfully, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            group.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorSnackbarGreen));
            snack.show();

            if (imageWasDeleted) {
                String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + recipeId;
                ImageStorage.deleteImageByImageName(getContext(), imageName);

                imageName = AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + recipeId;
                ImageStorage.deleteImageByImageName(getContext(), imageName);

            } else {

                if (selectedImage != null) {
                    // Saving the selected image to SD card

                    String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + recipeId;
                    File file = ImageStorage.saveToSdCard(getContext(), selectedImage, imageName);

                    if (file != null) {
                        Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(file.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
                        imageName = AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + recipeId;
                        ImageStorage.saveToSdCard(getContext(), thumbnail, imageName);
                        thumbnail.recycle();
                    }

                    selectedImage.recycle();
                }
            }
        }

        if (recipeImage != null) {
            recipeImage.recycle();
        }

        mListener.onRecipeWasSaved(recipeId);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        selectedIngredientView = v;

        getActivity().getMenuInflater().inflate(R.menu.menu_context_ingredients_list, menu);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_editIngredient:

                onEditIngredientClicked(selectedIngredientView);

                break;

            case R.id.action_deleteIngredient:

                TextView textView_ingredient = (TextView) selectedIngredientView.findViewById(R.id.textView_listItem);
                String ingredientText = textView_ingredient.getText().toString();
                final int position = ingredientsList.indexOf(ingredientText);

                ingredientsList.remove(position);
                layout_ingredientsList.removeView(selectedIngredientView);

                if (ingredientsList.size() == 1) {
                    FrameLayout listSeparator = (FrameLayout) selectedIngredientView.findViewById(R.id.listSeparator);
                    listSeparator.setVisibility(View.INVISIBLE);
                }

                if (ingredientsList.isEmpty()) {
                    ingredientsList.add(AppConsts.Category.NO_INFO);

                    fillIngredientsList();
                }

                break;

        }

        return super.onContextItemSelected(item);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cardView_yield:

                int currentValue = servings;

                showServingsChooserDialog(currentValue);

                break;

            case R.id.cardView_totalTime:

                int currentHours = totalTimeInSeconds / 3600;
                int currentMinutes = (totalTimeInSeconds % 3600) / 60;

                showTimeChooserDialog(currentHours, currentMinutes);

                break;

            case R.id.imageView_choosePicture:
            case R.id.imageView_recipeImage:
                // Choose a picture from gallery

                /*Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra("crop", "true");
                photoPickerIntent.putExtra("aspectX", 1);
                photoPickerIntent.putExtra("aspectY", 1);
                photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, ImageStorage.getTempUri(getContext()));
                photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);*/

                boolean isWriteStoragePermissionGranted = isPermissionGranted(getActivity(), AppConsts.Permissions.PERMISSION_REQ_CODE_READ_STORAGE);

                if (isWriteStoragePermissionGranted) {

                    /*Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    photoPickerIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{photoPickerIntent});

                    startActivityForResult(chooserIntent, REQ_CODE_PICK_IMAGE);*/

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.select_gallery)), REQ_CODE_PICK_IMAGE);
                }

                /*Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(photoPickerIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, REQ_CODE_PICK_IMAGE);*/

                break;

            case R.id.imageView_deleteImage:

                imageView_recipeImage.setImageBitmap(null);
                imageWasDeleted = true;
                button_deleteImage.setVisibility(View.INVISIBLE);
                button_choosePicture.setVisibility(View.VISIBLE);

                break;

            case R.id.button_addIngredient:

                onAddIngredientClicked();

                break;

            case R.id.button_addCategory:

                AppHelper.hideTheKeyboard(getActivity());

                ArrayList<String> selectedCategoriesNamesList = new ArrayList<>();

                for (int i = 0; i < categoriesList.size(); i++) {
                    selectedCategoriesNamesList.add(categoriesList.get(i));
                }

                mListener.onAddCategoriesClick(selectedCategoriesNamesList);

                break;

            default: // ingredient item clicked
                v.showContextMenu();
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showTimeChooserDialog(int currentHours, int currentMinutes) {

        totalTimeDialog = new Dialog(getContext());
        totalTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        totalTimeDialog.setContentView(R.layout.dialog_number_picker_time);
        Button buttonSet = (Button) totalTimeDialog.findViewById(R.id.buttonSet);
        final NumberPicker hoursPicker = (NumberPicker) totalTimeDialog.findViewById(R.id.numberPicker_hours);
        final NumberPicker minutesPicker = (NumberPicker) totalTimeDialog.findViewById(R.id.numberPicker_minutes);

        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(20);
        hoursPicker.setValue(currentHours);

        hoursPicker.setWrapSelectorWheel(false);

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(60);
        minutesPicker.setValue(currentMinutes);

        minutesPicker.setWrapSelectorWheel(false);

        buttonSet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                totalTimeDialog.dismiss();
                totalTimeInSeconds = (hoursPicker.getValue() * 3600 + minutesPicker.getValue() * 60);

                if (totalTimeInSeconds > 0) {
                    textView_totalTime.setTextColor(Color.BLACK);
                } else {
                    textView_totalTime.setTextColor(Color.RED);
                }

                textView_totalTime.setText(AppHelper.getStringRecipeTotalTime(getContext(), totalTimeInSeconds));
            }
        });

        totalTimeDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void showServingsChooserDialog(int currentServingsValue) {

        servingsDialog = new Dialog(getContext());
        servingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        servingsDialog.setContentView(R.layout.dialog_number_picker_servings);
        Button buttonSet = (Button) servingsDialog.findViewById(R.id.buttonSet);
        final NumberPicker servingsPicker = (NumberPicker) servingsDialog.findViewById(R.id.numberPicker_servings);

        servingsPicker.setMinValue(0);
        servingsPicker.setMaxValue(20);

        servingsPicker.setValue(currentServingsValue);

        servingsPicker.setWrapSelectorWheel(false);

        buttonSet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                servings = servingsPicker.getValue();
                textView_servings.setText(AppHelper.getStringRecipeServings(getContext(), servings));

                if (servings > 0) {
                    textView_servings.setTextColor(Color.BLACK);
                } else {
                    textView_servings.setTextColor(Color.RED);
                }

                servingsDialog.dismiss();
            }
        });

        servingsDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void onAddIngredientClicked() {
        // pop-up custom alert-dialog to insert a new ingredient:

        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());

        // hide the default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        View view = inflater.inflate(R.layout.dialog_add_ingredient, null, false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);

        editText_inputIngredient = (EditText) dialog.findViewById(R.id.editText_userInput);

        Button button_addIngredient = (Button) dialog.findViewById(R.id.buttonAlert_add);
        button_addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the input text and add it to the list
                String ing = editText_inputIngredient.getText().toString();

                if (ingredientsList.get(0).equals(AppConsts.Category.NO_INFO)) {
                    ingredientsList.set(0, ing);

                } else {
                    ingredientsList.add(ing);
                    /*ingredientsListAdapter.notifyDataSetChanged();*/
                }

                fillIngredientsList();

                // Dismiss the dialog
                dialog.dismiss();

            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.buttonAlert_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Display the dialog
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void onEditIngredientClicked(final View ingredientView) {
        // pop-up an alertDialog with the ingredient details to edit
        // save it back to the list when "save" button clicked

        TextView textView_ingredient = (TextView) ingredientView.findViewById(R.id.textView_listItem);
        String ingredientText = textView_ingredient.getText().toString();
        final int position = ingredientsList.indexOf(ingredientText);

        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_add_ingredient, null, false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);

        editText_inputIngredient = (EditText) dialog.findViewById(R.id.editText_userInput);
        editText_inputIngredient.setText(ingredientText);

        Button button_addIngredient = (Button) dialog.findViewById(R.id.buttonAlert_add);
        button_addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the input text and add it to the list
                String ing = editText_inputIngredient.getText().toString();
                ingredientsList.set(position, ing);

                fillIngredientsList();

                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.buttonAlert_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Display the dialog
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    public void updateCategoriesList(Context context, ArrayList<String> updatedCategoriesList) {

        if (!updatedCategoriesList.isEmpty()
                && updatedCategoriesList.get(0).equals(AppConsts.Category.NO_INFO)) {

            updatedCategoriesList.clear();
        }

        categoriesList = updatedCategoriesList;

        if (categoriesList.isEmpty()) {
            categoriesList.add(AppConsts.Category.NO_INFO);

            button_addCategory.setImageResource(R.mipmap.btn_add_categories_chooser);

        } else {
            button_addCategory.setImageResource(R.mipmap.btn_open_categories_chooser);
        }

        fillCategoriesList(context);
    }

//-------------------------------------------------------------------------------------------------

    public void fillIngredientsList() {

        layout_ingredientsList.removeAllViews();

        LinearLayout listItem;
        TextView listItemText;
        for (int i = 0; i < ingredientsList.size(); i++) {
            String ingredient = ingredientsList.get(i);
            listItem = (LinearLayout) inflater.inflate(R.layout.listitem_simple_textview, null);
            listItemText = (TextView) listItem.findViewById(R.id.textView_listItem);

            if (ingredient.equals(AppConsts.Category.NO_INFO)) {
                listItemText.setTextColor(Color.RED);
                listItemText.setText(getString(R.string.no_info));

            } else {
                listItemText.setText(ingredient);
                registerForContextMenu(listItem);
                listItem.setOnClickListener(this);
            }

            if (i == ingredientsList.size() - 1) {
                FrameLayout listSeparator = (FrameLayout) listItem.findViewById(R.id.listSeparator);
                listSeparator.setVisibility(View.INVISIBLE);
            }

            layout_ingredientsList.addView(listItem);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void fillCategoriesList(Context context) {

        ArrayList<String> translatedCategories = AppHelper.getTranslatedCategoriesList(context, categoriesList);

        layout_categoriesList.removeAllViews();

        LinearLayout listItem;
        TextView listItemText;
        for (int i = 0; i < translatedCategories.size(); i++) {
            String category = translatedCategories.get(i);
            listItem = (LinearLayout) inflater.inflate(R.layout.listitem_simple_textview, null);
            listItemText = (TextView) listItem.findViewById(R.id.textView_listItem);
            listItemText.setText(category);

            if (categoriesList.get(0).equals(AppConsts.Category.NO_INFO)) {
                listItemText.setTextColor(Color.RED);
            }

            if (i == categoriesList.size() - 1) {
                FrameLayout listSeparator = (FrameLayout) listItem.findViewById(R.id.listSeparator);
                listSeparator.setVisibility(View.INVISIBLE);
            }

            layout_categoriesList.addView(listItem);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showLoginDialog(String currentUserName) {

        loginDialog = new Dialog(getContext());

        // hide to default title for Dialog
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        final View view = inflater.inflate(R.layout.dialog_login, null, false);

        loginDialog.setCancelable(false);

        loginDialog.setContentView(view);

        TextView textView_dialogTitleNote = (TextView) loginDialog.findViewById(R.id.textView_titleNote);

        String titleNote = getString(R.string.to_add_your_own_recipe_you_have_to_login) +
                "\n" + getString(R.string.the_username_used_as_author);

        textView_dialogTitleNote.setText(titleNote);

        final EditText editText_userInput = (EditText) loginDialog.findViewById(R.id.editText_userInput);
        editText_userInput.setText(currentUserName);

        // Relocating the selection after the last char of the editText
        int pos = editText_userInput.getText().length();
        editText_userInput.setSelection(pos);

        Button button_save = (Button) loginDialog.findViewById(R.id.button_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the input text
                author = editText_userInput.getText().toString();

                if (usernameTooShort(author)) {
                    // Less than 6 letters

                    Snackbar.make(view, R.string.at_least_6_characters_are_required, Snackbar.LENGTH_LONG).show();

                    return;
                }

                if (usernameTooLong(author)) {
                    // Less than 6 letters

                    Snackbar snack = Snackbar.make(view, R.string.max_20_chars_are_allowed, Snackbar.LENGTH_LONG);
                    ViewGroup group = (ViewGroup) snack.getView();
                    group.setBackgroundColor(Color.RED);
                    snack.show();

                    return;
                }

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(AppConsts.SharedPrefs.USER_NAME, author);
                editor.commit();

                AnalyticsHelper.sendEvent(EditRecipeFragment.this, AppConsts.Analytics.CATEGORY_LOGIN, "Login successfully", author);

                // Dismiss the dialog
                loginDialog.dismiss();
            }
        });

        Button btnCancel = (Button) loginDialog.findViewById(R.id.button_cancel);
        btnCancel.setText(R.string.not_now);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                loginDialog.dismiss();

                mListener.onCancelLoginButtonClicked();
            }
        });

        // Display the dialog
        loginDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private boolean usernameTooShort(String username) {
        // checks if the username has less than 6 letters excluding spaces.
        int trimmedLength = username.trim().length();
        if (trimmedLength >= 6) {
            return false;
        } else {
            return true;
        }
    }

//-------------------------------------------------------------------------------------------------

    private boolean usernameTooLong(String username) {
        // checks if the username has less than 6 letters excluding spaces.
        int trimmedLength = username.trim().length();
        if (trimmedLength > 20) {
            return true;
        } else {
            return false;
        }
    }

}


