package com.hanoch.greatrecipes.view.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.GenericRecipe;
import com.hanoch.greatrecipes.api.YummlyRecipe;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.model.ApiProvider;
import com.hanoch.greatrecipes.model.Serving;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Custom adapter for the 'Online recipes' list
 */

public class ServingsListAdapter extends BaseAdapter {

    final int MAX_ITEMS = 100;

    private ArrayList<Serving> servingsList;
    private Context context;
    private AppStateManager appStateManager;

    private ArrayList<String> selectedIds;
    private HashMap<String, View> viewsMap = new HashMap<>();

//-------------------------------------------------------------------------------------------------

    public class ViewHolder {

        public String recipeId;
        public String servingId;
        public TextView tv_recipeTitle;
        public TextView tv_servingType;

        public ImageView iv_image;
        public ImageView iv_checked;
        public TextView tv_noImageAvailable;
    }

//----------------------------------------------------------------------------------------------

    public ServingsListAdapter(Context context, ArrayList<String> selectedIds) {

        appStateManager = AppStateManager.getInstance();
        servingsList = new ArrayList<>(appStateManager.user.servings.values());
        this.context = context;
        this.selectedIds = selectedIds;
    }

    public View getViewById(String id) {

        return viewsMap.get(id);
    }

//-------------------------------------------------------------------------------------------------

    /*public boolean onServingUnchecked(String id) {

        selectedIds.remove(id);

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public boolean onServingChecked(String id) {

        selectedIds.add(id);

        return true;
    }*/

//----------------------------------------------------------------------------------------------


    @Override
    public int getCount() {
        return servingsList.size();
    }

    @Override
    public Serving getItem(int i) {
        return servingsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View getView(int i, View convertedView, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_serving, viewGroup, false);

        Serving serving = getItem(i);

        ViewHolder holder = new ViewHolder();

        holder.recipeId = serving.recipeId;
        holder.servingId = serving.servingId;
        holder.tv_recipeTitle = (TextView) view.findViewById(R.id.textView_recipeTitle);
        holder.tv_servingType = (TextView) view.findViewById(R.id.textView_servingType);
        holder.iv_image = (ImageView) view.findViewById(R.id.imageView_servingImage);
        holder.iv_checked = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        holder.tv_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);

        GenericRecipe recipe;

        if (serving.isUserRecipe) {
            recipe = appStateManager.user.userRecipes.get(serving.recipeId);
            if (recipe == null) {
                getUserRecipeFromGreatRecipesApi(holder, serving);
            } else {
                onRecipeDataReceived(holder, serving, recipe);
            }
        } else {
            recipe = appStateManager.user.yummlyRecipes.get(serving.recipeId);
            if (recipe == null) {
                getYummlyRecipeFromGreatRecipesApi(holder, serving);
            } else {
                onRecipeDataReceived(holder, serving, recipe);
            }
        }
//        holder = (ViewHolder) view.getTag();

        String translatedServingTypeName = AppHelper.getTranslatedServingTypeName(context, serving.servingType);
        holder.tv_servingType.setText(translatedServingTypeName);
        holder.iv_image.setVisibility(View.INVISIBLE);
        holder.tv_noImageAvailable.setVisibility(View.INVISIBLE);

        view.setTag(holder);

        viewsMap.put(serving.servingId, view);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    // The next two methods are a trick to cancel the adapter recycling system:

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return MAX_ITEMS;
    }

//-------------------------------------------------------------------------------------------------

    public void getUserRecipeFromGreatRecipesApi(ViewHolder holder, Serving serving) {

        String encoded = serving.recipeId;
        try {
            encoded = URLEncoder.encode(serving.recipeId, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }

        Action1<UserRecipe> subscriber = (recipe -> onRecipeDataReceived(holder, serving, recipe));

        Single<UserRecipe> getUserRecipe =
                ApiProvider.getGreatRecipesApi().getUserRecipe(encoded);

        getUserRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    public void getYummlyRecipeFromGreatRecipesApi(ViewHolder holder, Serving serving) {

        String encoded = serving.recipeId;
        try {
            encoded = URLEncoder.encode(serving.recipeId, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }

        Action1<YummlyRecipe> subscriber = (recipe -> onRecipeDataReceived(holder, serving, recipe));

        Single<YummlyRecipe> getYummlyRecipe =
                ApiProvider.getGreatRecipesApi().getYummlyRecipe(encoded);

        getYummlyRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    private void onRecipeDataReceived(ViewHolder holder, Serving serving, GenericRecipe recipe){
        if (holder.servingId.equals(serving.servingId)) {
            holder.tv_recipeTitle.setText(recipe.recipeTitle);
            String translatedServingTypeName = AppHelper.getTranslatedServingTypeName(context, serving.servingType);
            holder.tv_servingType.setText(translatedServingTypeName);
            holder.iv_image.setVisibility(View.INVISIBLE);
            holder.tv_noImageAvailable.setVisibility(View.INVISIBLE);

            if (selectedIds.contains(serving.servingId)) {
                holder.iv_checked.setVisibility(View.VISIBLE);
                holder.iv_image.setAlpha(0.5f);
            }

            Bitmap image = ImageStorage.convertByteArrayAsStringAsToBitmap(recipe.imageByteArrayAsString);
            if (image == null) {
                holder.tv_noImageAvailable.setVisibility(View.VISIBLE);
            } else {
                holder.iv_image.setImageBitmap(image);
                holder.iv_image.setVisibility(View.VISIBLE);
            }
        }
    }

//-------------------------------------------------------------------------------------------------

    public void refreshAdapter(){
        servingsList = new ArrayList<>(appStateManager.user.servings.values());
        notifyDataSetChanged();
    }
}
