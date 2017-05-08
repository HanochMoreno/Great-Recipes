package com.hanoch.greatrecipes.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.support.v4.app.Fragment;
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

import com.google.gson.Gson;
import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.User;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.database.GreatRecipesDbManager;
import com.hanoch.greatrecipes.google.AnalyticsHelper;
import com.hanoch.greatrecipes.utilities.ImageStorage;
import com.hanoch.greatrecipes.utilities.PicUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;


public class EditRecipeFragment2 extends Fragment implements View.OnClickListener {

    private static final String ARG_RECIPE_ID = "ARG_RECIPE_ID";
    private static final String ARG_ACTION = "ARG_ACTION";

    private static final int REQ_CODE_PICK_IMAGE = 1;

    private View mainView;

    private ImageView imageView_recipeImage;
    private View image_favourite;
    private View button_deleteImage;
    private View button_choosePicture;

    private EditText editText_recipeTitle;
    private EditText editText_inputIngredient;
    private EditText editText_recipeInstructions;
    private EditText editText_notes;

    private TextView textView_totalTime;
    private TextView textView_servings;

    private ImageView button_addCategory;
    private LinearLayout layout_ingredientsList;
    private LinearLayout layout_categoriesList;
    private View selectedIngredientView;

    private boolean viewsHaveBeenDestroyed;

    //    private DbManager dbManager;
    private OnFragmentEditRecipeListener mListener;
    private LayoutInflater inflater;

    private String mRecipeId;
    private UserRecipe userRecipe;

    private boolean isFavourite;

    private boolean imageWasDeleted;
    private Bitmap recipeImage;
    private Bitmap selectedImage;

    private Dialog loginDialog;
    private Dialog servingsDialog;
    private Dialog totalTimeDialog;

    private ProgressDialog progressDialog;
    private GreatRecipesDbManager dbManager;
//    private Subscriber<UserRecipe> subscriber;

//-------------------------------------------------------------------------------------------------

    public interface OnFragmentEditRecipeListener {

        void onCancelLoginButtonClicked();

        void onAddCategoriesClick(ArrayList<String> categoriesList);
    }

//-------------------------------------------------------------------------------------------------

    public static EditRecipeFragment2 newInstance(String action, String recipeId) {

        EditRecipeFragment2 fragment = new EditRecipeFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_ACTION, action);
        args.putString(ARG_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbManager = GreatRecipesDbManager.getInstance();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (mainView != null) {

            if (savedInstanceState != null) {

                boolean loginDialogIsShowing = savedInstanceState.getBoolean("loginDialogIsShowing");
                if (loginDialogIsShowing) {
                    String currentEmail = savedInstanceState.getString("currentEmail");
                    String currentUsername = savedInstanceState.getString("currentUsername");
                    String currentPassword = savedInstanceState.getString("currentPassword");
                    String currentRetypePassword = savedInstanceState.getString("currentRetypePassword");
                    showLoginDialog(currentEmail, currentUsername, currentPassword, currentRetypePassword);

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

            return mainView;
        }

        this.inflater = inflater;
        mainView = inflater.inflate(R.layout.fragment_edit_recipe, container, false);

        (mainView.findViewById(R.id.textView_noImageAvailable)).setVisibility(View.GONE);
        (mainView.findViewById(R.id.textView_recipeAuthor)).setVisibility(View.GONE);
        (mainView.findViewById(R.id.cardView_energy)).setVisibility(View.GONE);

        layout_ingredientsList = (LinearLayout) mainView.findViewById(R.id.layout_ingredientsList);
        layout_categoriesList = (LinearLayout) mainView.findViewById(R.id.layout_categoriesList);

        editText_recipeTitle = (EditText) mainView.findViewById(R.id.editText_recipeTitle);

        textView_servings = (TextView) mainView.findViewById(R.id.textView_servings);

        View yieldChooser = mainView.findViewById(R.id.cardView_yield);
        yieldChooser.setOnClickListener(this);

        View timeChooser = mainView.findViewById(R.id.cardView_totalTime);
        timeChooser.setOnClickListener(this);

        textView_totalTime = (TextView) mainView.findViewById(R.id.textView_totalTime);

        editText_recipeInstructions = (EditText) mainView.findViewById(R.id.editText_instructions);
        editText_notes = (EditText) mainView.findViewById(R.id.editText_privateNotes);

        imageView_recipeImage = (ImageView) mainView.findViewById(R.id.imageView_recipeImage);
        imageView_recipeImage.setOnClickListener(this);

        image_favourite = mainView.findViewById(R.id.imageView_favourite);
        image_favourite.setVisibility(View.INVISIBLE);

        button_deleteImage = mainView.findViewById(R.id.imageView_deleteImage);
        button_deleteImage.setVisibility(View.INVISIBLE);
        button_deleteImage.setOnClickListener(this);

        button_choosePicture = mainView.findViewById(R.id.imageView_choosePicture);
        button_choosePicture.setOnClickListener(this);

        View button_addIngredient = mainView.findViewById(R.id.button_addIngredient);
        button_addIngredient.setOnClickListener(this);

        button_addCategory = (ImageView) mainView.findViewById(R.id.button_addCategory);
        button_addCategory.setOnClickListener(this);

        AppHelper.hideTheKeyboard(getActivity());

        if (savedInstanceState == null) {

            Bundle args = getArguments();
            mRecipeId = args.getString(ARG_RECIPE_ID, null);
            User user = AppStateManager.getInstance().user;

            if (mRecipeId == null || !user.userRecipes.containsKey(mRecipeId)) {
                // Adding a new recipe to 'My Own Recipes' list

                // TODO: cannot happen, unless in test mode:
                if (user.author == null) {
                    showLoginDialog("", "", "", "");
                }

                userRecipe = new UserRecipe();
            } else {
                // Editing an existing recipe from 'My Own Recipes' list

                userRecipe = new UserRecipe(user.userRecipes.get(mRecipeId));

                editText_recipeTitle.setText(userRecipe.recipeTitle);
                editText_recipeInstructions.setText(userRecipe.instructions);
                editText_notes.setText(userRecipe.notes);
                recipeImage = ImageStorage.convertByteArrayAsStringAsToBitmap(userRecipe.imageByteArrayAsString);
                isFavourite = isRecipeFavourite(mRecipeId);
            }
        } else {

            userRecipe = new Gson().fromJson(savedInstanceState.getString("userRecipeAsJson"), UserRecipe.class);
            mRecipeId = savedInstanceState.getString("mRecipeId");
            imageWasDeleted = savedInstanceState.getBoolean("imageWasDeleted");
            isFavourite = savedInstanceState.getBoolean("isFavourite");
            recipeImage = savedInstanceState.getParcelable("recipeImage");
            selectedImage = savedInstanceState.getParcelable("selectedImage");
        }

        setRecipeView();

        return mainView;
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

        if (isFavourite) {
            AnimationHelper.animateViewFadingIn(getContext(), image_favourite, 500, 0);
        }

        if (userRecipe.cookingTime > 0) {
            textView_totalTime.setTextColor(Color.BLACK);
        }

        textView_totalTime.setText(AppHelper.getStringRecipeTotalTime(getContext(), userRecipe.cookingTime));

        if (userRecipe.yield > 0) {
            textView_servings.setTextColor(Color.BLACK);
        }

        textView_servings.setText(AppHelper.getStringRecipeServings(getContext(), userRecipe.yield));

        fillIngredientsList();

        updateCategoriesList(userRecipe.categoriesList);
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

        outState.putString("mRecipeId", mRecipeId);
        outState.putString("userRecipeAsJson", new Gson().toJson(userRecipe, UserRecipe.class));
        outState.putBoolean("imageWasDeleted", imageWasDeleted);
        outState.putBoolean("isFavourite", isFavourite);

        if (loginDialog != null && loginDialog.isShowing()) {
            outState.putBoolean("loginDialogIsShowing", true);

            EditText editText_email = (EditText) loginDialog.findViewById(R.id.editText_email);
            EditText editText_username = (EditText) loginDialog.findViewById(R.id.editText_username);
            EditText editText_password = (EditText) loginDialog.findViewById(R.id.editText_password);
            EditText editText_repeatPassword = (EditText) loginDialog.findViewById(R.id.editText_repeatPassword);

            String currentEmail = editText_email.getText().toString();
            outState.putString("currentEmail", currentEmail);

            String currentUsername = editText_username.getText().toString();
            outState.putString("currentUsername", currentUsername);

            String currentPassword = editText_password.getText().toString();
            outState.putString("currentPassword", currentPassword);

            String currentRetypePassword = editText_repeatPassword.getText().toString();
            outState.putString("currentRetypePassword", currentRetypePassword);

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
                            AppHelper.showSnackBar(mainView, R.string.the_image_not_supported, Color.RED);
                            return;
                        }
                    }

                    if (selectedImage == null) {
                        AppHelper.showSnackBar(mainView, R.string.the_image_not_supported, Color.RED);
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
            AppHelper.showSnackBar(mainView, R.string.you_should_approve_permission, Color.RED);
        }
    }

//-------------------------------------------------------------------------------------------------

    public static boolean isPermissionGranted(Activity activity, int permissionCode) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
            return true;

        String permissionToCheck = AppConsts.permissionsMap.get(permissionCode);

        if (permissionToCheck == null) {
            return false; // Permission is not in the map - should never happen
        }

        if (activity.checkSelfPermission(permissionToCheck) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

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

//-------------------------------------------------------------------------------------------------

    public void setFavouriteImage() {

        if (isFavourite) {
            AnimationHelper.animateViewFadingIn(getContext(), image_favourite, 500, 0);

        } else {
            AnimationHelper.animateViewFadingOut(getContext(), image_favourite, 500, 0);
        }
    }

//-------------------------------------------------------------------------------------------------

    public UserRecipe onSaveUserRecipeClicked() {
        User user = AppStateManager.getInstance().user;

        // Get the user's inputs:
        String title = editText_recipeTitle.getText().toString();
        if (title.trim().isEmpty()) {
            title = "";
        }

        String instructions = editText_recipeInstructions.getText().toString();
        if (instructions.trim().isEmpty()) {
            instructions = "";
        }

        String notes = editText_notes.getText().toString();
        if (notes.trim().isEmpty()) {
            notes = "";
        }

        final String recipeId = getArguments().getString(ARG_RECIPE_ID);

        userRecipe._id = recipeId;
        userRecipe.author = user.author;
        userRecipe.userId = user._id;
        userRecipe.recipeTitle = title;
        userRecipe.instructions = instructions;
        userRecipe.notes = notes;

        if (recipeId == null) {
            // Adding a new recipe
            if (selectedImage == null || imageWasDeleted) {
                // No image selected
                userRecipe.imageByteArrayAsString = "";
                userRecipe.thumbnailByteArrayAsString = "";
            } else {
                userRecipe.imageByteArrayAsString = ImageStorage.convertBitmapToByteArrayAsString(selectedImage);

                String imageName = AppConsts.Images.RECIPE_IMAGE;
                File file = ImageStorage.saveToSdCard(getContext(), selectedImage, imageName);

                if (file != null) {
                    Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(file.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
                    userRecipe.thumbnailByteArrayAsString = ImageStorage.convertBitmapToByteArrayAsString(thumbnail);
                    thumbnail.recycle();
                }
                selectedImage.recycle();
            }

        } else {
            // Editing an existing recipe

            if (imageWasDeleted) {
                // No image selected as a replacement

                userRecipe.imageByteArrayAsString = "";
                userRecipe.thumbnailByteArrayAsString = "";

            } else if (selectedImage != null) {
                // The original image replaced by another

                userRecipe.imageByteArrayAsString = ImageStorage.convertBitmapToByteArrayAsString(selectedImage);

                String imageName = AppConsts.Images.RECIPE_IMAGE;
                File file = ImageStorage.saveToSdCard(getContext(), selectedImage, imageName);

                if (file != null) {
                    Bitmap thumbnail = ImageStorage.decodeSampledBitmapFromFile(file.getPath(), Bitmap.Config.ARGB_8888, 100, 100);
                    userRecipe.thumbnailByteArrayAsString = ImageStorage.convertBitmapToByteArrayAsString(thumbnail);
                    thumbnail.recycle();
                }
                selectedImage.recycle();
            }
        }

        if (recipeImage != null) {
            recipeImage.recycle();
        }

        return userRecipe;
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
                final int position = userRecipe.ingredientsList.indexOf(ingredientText);

                userRecipe.ingredientsList.remove(position);
                layout_ingredientsList.removeView(selectedIngredientView);

                if (userRecipe.ingredientsList.size() == 1) {
                    FrameLayout listSeparator = (FrameLayout) selectedIngredientView.findViewById(R.id.listSeparator);
                    listSeparator.setVisibility(View.INVISIBLE);
                }

                fillIngredientsList();

                break;

        }

        return super.onContextItemSelected(item);
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cardView_yield:

                int currentValue = userRecipe.yield;

                showServingsChooserDialog(currentValue);

                break;

            case R.id.cardView_totalTime:

                int currentHours = userRecipe.cookingTime / 3600;
                int currentMinutes = (userRecipe.cookingTime % 3600) / 60;

                showTimeChooserDialog(currentHours, currentMinutes);

                break;

            case R.id.imageView_choosePicture:
            case R.id.imageView_recipeImage:
                // Choose a picture from gallery
                boolean isWriteStoragePermissionGranted = isPermissionGranted(getActivity(), AppConsts.Permissions.PERMISSION_REQ_CODE_READ_STORAGE);

                if (isWriteStoragePermissionGranted) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.select_gallery)), REQ_CODE_PICK_IMAGE);
                }
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

                for (int i = 0; i < userRecipe.categoriesList.size(); i++) {
                    selectedCategoriesNamesList.add(userRecipe.categoriesList.get(i));
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

        buttonSet.setOnClickListener(v -> {
            totalTimeDialog.dismiss();
            userRecipe.cookingTime = (hoursPicker.getValue() * 3600 + minutesPicker.getValue() * 60);

            if (userRecipe.cookingTime > 0) {
                textView_totalTime.setTextColor(Color.BLACK);
            } else {
                textView_totalTime.setTextColor(Color.RED);
            }

            textView_totalTime.setText(AppHelper.getStringRecipeTotalTime(getContext(), userRecipe.cookingTime));
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

        buttonSet.setOnClickListener(v -> {

            userRecipe.yield = servingsPicker.getValue();
            textView_servings.setText(AppHelper.getStringRecipeServings(getContext(), userRecipe.yield));

            if (userRecipe.yield > 0) {
                textView_servings.setTextColor(Color.BLACK);
            } else {
                textView_servings.setTextColor(Color.RED);
            }

            servingsDialog.dismiss();
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
        button_addIngredient.setOnClickListener(v -> {

            // Get the input text and add it to the list
            String ing = editText_inputIngredient.getText().toString();

            if (userRecipe.ingredientsList.get(0).equals(AppConsts.Category.NO_INFO)) {
                userRecipe.ingredientsList.set(0, ing);
            } else {
                userRecipe.ingredientsList.add(ing);
            }

            fillIngredientsList();
            dialog.dismiss();

        });

        Button btnCancel = (Button) dialog.findViewById(R.id.buttonAlert_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private void onEditIngredientClicked(final View ingredientView) {
        // pop-up an alertDialog with the ingredient details to edit
        // save it back to the list when "save" button clicked

        TextView textView_ingredient = (TextView) ingredientView.findViewById(R.id.textView_listItem);
        String ingredientText = textView_ingredient.getText().toString();
        final int position = userRecipe.ingredientsList.indexOf(ingredientText);

        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        View view = inflater.inflate(R.layout.dialog_add_ingredient, null, false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);

        editText_inputIngredient = (EditText) dialog.findViewById(R.id.editText_userInput);
        editText_inputIngredient.setText(ingredientText);

        Button button_addIngredient = (Button) dialog.findViewById(R.id.buttonAlert_add);
        button_addIngredient.setOnClickListener(v -> {

            // Get the input text and add it to the list
            String ing = editText_inputIngredient.getText().toString();
            userRecipe.ingredientsList.set(position, ing);

            fillIngredientsList();
            dialog.dismiss();
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.buttonAlert_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

//-------------------------------------------------------------------------------------------------

    public void updateCategoriesList(ArrayList<String> updatedCategoriesList) {

        if (updatedCategoriesList == null || updatedCategoriesList.isEmpty()
                || updatedCategoriesList.get(0).equals(AppConsts.Category.NO_INFO)) {

            updatedCategoriesList = new ArrayList<>();
        }

        userRecipe.categoriesList = updatedCategoriesList;

        if (userRecipe.categoriesList.isEmpty()) {
            button_addCategory.setImageResource(R.mipmap.btn_add_categories_chooser);
        } else {
            button_addCategory.setImageResource(R.mipmap.btn_open_categories_chooser);
        }

        fillCategoriesList();
    }

//-------------------------------------------------------------------------------------------------

    public void fillIngredientsList() {

        layout_ingredientsList.removeAllViews();

        if (userRecipe.ingredientsList == null || userRecipe.ingredientsList.isEmpty()) {
            userRecipe.ingredientsList = new ArrayList<>();
            userRecipe.ingredientsList.add(AppConsts.Category.NO_INFO);
        }

        LinearLayout listItem;
        TextView listItemText;
        for (int i = 0; i < userRecipe.ingredientsList.size(); i++) {
            String ingredient = userRecipe.ingredientsList.get(i);
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

            if (i == userRecipe.ingredientsList.size() - 1) {
                FrameLayout listSeparator = (FrameLayout) listItem.findViewById(R.id.listSeparator);
                listSeparator.setVisibility(View.INVISIBLE);
            }

            layout_ingredientsList.addView(listItem);
        }
    }

//-------------------------------------------------------------------------------------------------

    public void fillCategoriesList() {
        layout_categoriesList.removeAllViews();

        if (userRecipe.categoriesList == null || userRecipe.categoriesList.isEmpty()) {
            userRecipe.categoriesList = new ArrayList<>();
            userRecipe.categoriesList.add(AppConsts.Category.NO_INFO);
        }

        ArrayList<String> translatedCategories = AppHelper.getTranslatedCategoriesList(getContext(), userRecipe.categoriesList);

        LinearLayout listItem;
        TextView listItemText;
        for (int i = 0; i < translatedCategories.size(); i++) {
            String category = translatedCategories.get(i);
            listItem = (LinearLayout) inflater.inflate(R.layout.listitem_simple_textview, null);
            listItemText = (TextView) listItem.findViewById(R.id.textView_listItem);
            listItemText.setText(category);

            if (userRecipe.categoriesList.get(0).equals(AppConsts.Category.NO_INFO)) {
                listItemText.setTextColor(Color.RED);
            }

            if (i == userRecipe.categoriesList.size() - 1) {
                FrameLayout listSeparator = (FrameLayout) listItem.findViewById(R.id.listSeparator);
                listSeparator.setVisibility(View.INVISIBLE);
            }

            layout_categoriesList.addView(listItem);
        }
    }

//-------------------------------------------------------------------------------------------------

    private void showLoginDialog(String currentEmail, String currentUsername, String  currentPassword, String currentRetypePassword) {
        // TODO: handle register

        loginDialog = new Dialog(getContext());
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View view = inflater.inflate(R.layout.dialog_login, null, false);

        loginDialog.setCancelable(false);
        loginDialog.setContentView(view);

        TextView textView_dialogTitleNote = (TextView) loginDialog.findViewById(R.id.textView_titleNote);

        String titleNote = getString(R.string.to_add_your_own_recipe_you_have_to_login) +
                "\n" + getString(R.string.the_username_used_as_author);

        textView_dialogTitleNote.setText(titleNote);

        final EditText editText_email = (EditText) loginDialog.findViewById(R.id.editText_email);
        final EditText editText_username = (EditText) loginDialog.findViewById(R.id.editText_username);
        final EditText editText_password = (EditText) loginDialog.findViewById(R.id.editText_password);
        final EditText editText_repeatPassword = (EditText) loginDialog.findViewById(R.id.editText_repeatPassword);

        editText_email.setText(currentEmail);
        editText_username.setText(currentUsername);
        editText_password.setText(currentPassword);
        editText_repeatPassword.setText(currentRetypePassword);

        // Relocating the selection after the last char of the editText
        int pos = editText_email.getText().length();
        editText_email.setSelection(pos);

        pos = editText_username.getText().length();
        editText_username.setSelection(pos);

        pos = editText_password.getText().length();
        editText_password.setSelection(pos);

        pos = editText_repeatPassword.getText().length();
        editText_repeatPassword.setSelection(pos);

        Button button_save = (Button) loginDialog.findViewById(R.id.button_save);
        button_save.setOnClickListener(v -> {

            // Get the input text
            String email = editText_email.getText().toString();
            String author = editText_username.getText().toString();
            String password = editText_password.getText().toString();
            String retypePassword = editText_repeatPassword.getText().toString();

//            if (usernameTooShort(author)) {
//                // Less than 6 letters
//                AppHelper.showSnackBar(view, R.string.at_least_6_characters_are_required, Color.RED);
//                return;
//            }
//
//            if (usernameTooLong(author)) {
//                // More than 20 letters
//                AppHelper.showSnackBar(view, R.string.max_20_chars_are_allowed, Color.RED);
//                return;
//            }

            if (areFieldsValid(email, author, password, retypePassword)) {
                dbManager.register(email, author, password);

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(AppConsts.SharedPrefs.USER_NAME, author);
                editor.apply();

                AnalyticsHelper.sendEvent(EditRecipeFragment2.this, AppConsts.Analytics.CATEGORY_LOGIN, "Register successfully", author);

                loginDialog.dismiss();
            }

//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            SharedPreferences.Editor editor = sp.edit();
//            editor.putString(AppConsts.SharedPrefs.USER_NAME, author);
//            editor.apply();
//
//            AnalyticsHelper.sendEvent(EditRecipeFragment2.this, AppConsts.Analytics.CATEGORY_LOGIN, "Register successfully", author);
//
//            loginDialog.dismiss();
        });

        Button btnCancel = (Button) loginDialog.findViewById(R.id.button_cancel);
        btnCancel.setText(R.string.not_now);
        btnCancel.setOnClickListener(v -> {
            loginDialog.dismiss();
            mListener.onCancelLoginButtonClicked();
        });

        loginDialog.show();
    }

//-------------------------------------------------------------------------------------------------

    private boolean areFieldsValid(String email, String author, String password, String retypePassword) {
        //TODO: Handle validation
        if (usernameTooShort(author)) {
            AppHelper.showSnackBar(getView(), R.string.at_least_6_characters_are_required, Color.RED);
            return false;
        } else if (usernameTooLong(author)) {
            AppHelper.showSnackBar(getView(), R.string.max_20_chars_are_allowed, Color.RED);
            return false;
        } else if (!password.equals(retypePassword)) {
            AppHelper.showSnackBar(getView(), R.string.passwords_are_not_identical, Color.RED);
            return false;
        }

        return true;
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


