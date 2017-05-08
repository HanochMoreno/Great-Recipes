package com.hanoch.greatrecipes.view.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AnimationHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.GGGRecipe2;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Custom adapter for a recipes list
 */

public class RecipesListAdapter2 extends BaseAdapter {

    int MAX_ITEMS = 500;

    private ArrayList<GGGRecipe2> recipesList;
    private ArrayList<String> checkedIds;
    private String selectedId;
    private HashMap<String, View> viewsMap = new HashMap<>();

    private Context context;

//-------------------------------------------------------------------------------------------------

    public class ViewHolder {

        public String id;

        private FrameLayout layout_listItemContainer;
        private TextView textView_title;

        private ImageView imageView_image;
        private ImageView imageView_checked;
        private TextView textView_noImageAvailable;
    }

//----------------------------------------------------------------------------------------------

    public RecipesListAdapter2(Context context, ArrayList<GGGRecipe2> recipesList,
                               ArrayList<String> checkedIds, String selectedId) {

        this.recipesList = recipesList;
        this.selectedId = selectedId;
        this.checkedIds = checkedIds;
        this.context = context;
    }

//-------------------------------------------------------------------------------------------------

    public View getViewById(String id) {

        return viewsMap.get(id);
    }

//-------------------------------------------------------------------------------------------------

    public boolean onRecipeSelected(String id) {

        this.selectedId = id;

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public boolean onNoSelection() {

        selectedId = null;

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getCount() {
        return recipesList.size();
    }

    @Override
    public GGGRecipe2 getItem(int i) {
        return recipesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_recipes_list, viewGroup, false);

        GGGRecipe2 recipe = getItem(i);

        String id = recipe._id;
        String title = recipe.recipeTitle;
        String imageByteArrayAsString = recipe.imageByteArrayAsString;

        ViewHolder holder = new ViewHolder();
        holder.id = id;
        holder.layout_listItemContainer = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        holder.textView_title = (TextView) view.findViewById(R.id.textView_itemTitle);
        holder.imageView_image = (ImageView) view.findViewById(R.id.imageView_itemImage);
        holder.imageView_checked = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        holder.textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);

        holder.textView_title.setText(title);
        holder.imageView_image.setVisibility(View.INVISIBLE);
        holder.textView_noImageAvailable.setVisibility(View.INVISIBLE);

        view.setTag(holder);

        viewsMap.put(id, view);

        GenerateRecipeImage getImageFromSdCard = new GenerateRecipeImage(id, imageByteArrayAsString, holder);
        getImageFromSdCard.execute();

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

//----------------------------------------------------------------------------------------------

    public void refreshList(ArrayList<GGGRecipe2> newRecipesList) {
        recipesList = new ArrayList<>(newRecipesList);
        Collections.sort(newRecipesList, GGGRecipe2.TITLE_COMPARATOR);
        notifyDataSetChanged();
    }

//----------------------------------------------------------------------------------------------

    private class GenerateRecipeImage extends AsyncTask<String, Integer, Bitmap> {

        private String id;
        private ViewHolder holder;
        private String imageByteArrayAsString;

        public GenerateRecipeImage(String id, String imageByteArrayAsString, ViewHolder holder) {

            this.id = id;
            this.holder = holder;
            this.imageByteArrayAsString = imageByteArrayAsString;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            return ImageStorage.convertByteArrayAsStringAsToBitmap(imageByteArrayAsString);
        }

        @Override
        protected void onPostExecute(Bitmap image) {

            if (holder.id.equals(id)) {

                if (checkedIds.contains(id)) {
                    holder.imageView_checked.setVisibility(View.VISIBLE);
                    holder.imageView_image.setAlpha(0.5f);
                }

                if (selectedId != null && selectedId.equals(id)) {
                    AnimationHelper.animateSelectedRecipe(holder.layout_listItemContainer, context);
                }

                if (image == null) {
                    holder.imageView_image.setImageBitmap(null);
                    holder.textView_noImageAvailable.setVisibility(View.VISIBLE);
                    holder.imageView_image.setVisibility(View.VISIBLE);

                } else {
                    holder.imageView_image.setImageBitmap(image);
                    holder.imageView_image.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
