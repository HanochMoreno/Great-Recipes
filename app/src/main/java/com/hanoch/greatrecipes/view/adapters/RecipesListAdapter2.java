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

import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipe;
import com.hanoch.greatrecipes.model.MyFragment;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom adapter for a recipes list
 */

public class UserRecipesListAdapter2 extends BaseAdapter {

    int MAX_ITEMS = 500;

    private ArrayList<UserRecipe> recipesList;
    private ArrayList<String> checkedIds;
    private String selectedId = MyFragment.NO_SELECTION;
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

    public UserRecipesListAdapter2(Context context, ArrayList<UserRecipe> recipesList,
                                   ArrayList<String> checkedIds, String selectedId) {

        this.recipesList = recipesList;
        this.selectedId = selectedId;
        this.checkedIds = checkedIds;
        this.context = context;
    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//
//        View view = LayoutInflater.from(context).inflate(R.layout.listitem_recipes_list, parent, false);
//        ViewHolder holder = new ViewHolder();
//
//        holder.layout_listItemContainer = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
//        holder.textView_title = (TextView) view.findViewById(R.id.textView_itemTitle);
//        holder.imageView_image = (ImageView) view.findViewById(R.id.imageView_itemImage);
//        holder.imageView_checked = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
//        holder.textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);
//
//        view.setTag(holder);
//
//        return view;
//    }

//-------------------------------------------------------------------------------------------------

//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//
//        long id = cursor.getLong(cursor.getColumnIndex(RecipesContract.Recipes._ID));
//        String title = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.TITLE));
//
//        ViewHolder holder = (ViewHolder) view.getTag();
//
//        holder.id = id;
//        holder.textView_title.setText(title);
//
//        holder.imageView_image.setVisibility(View.INVISIBLE);
//        holder.textView_noImageAvailable.setVisibility(View.INVISIBLE);
//
//        viewsMap.put(id + "", view);
//
//        GetImageFromSdCard getImageFromSdCard = new GetImageFromSdCard(id, holder);
//        getImageFromSdCard.execute();
//    }

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

        selectedId = MyFragment.NO_SELECTION;

        return true;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getCount() {
        return recipesList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_recipes_list, viewGroup, false);

        UserRecipe userRecipe = recipesList.get(i);
        String id = userRecipe._id;
        String title = userRecipe.recipeTitle;
        String imageByteArrayAsString = userRecipe.imageByteArrayAsString;

        ViewHolder holder = new ViewHolder();
        holder.id = id;
        holder.layout_listItemContainer = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        holder.textView_title = (TextView) view.findViewById(R.id.textView_itemTitle);
        holder.imageView_image = (ImageView) view.findViewById(R.id.imageView_itemImage);
        holder.imageView_checked = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        holder.textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);
        view.setTag(holder);

        holder.textView_title.setText(title);
        holder.imageView_image.setVisibility(View.INVISIBLE);
        holder.textView_noImageAvailable.setVisibility(View.INVISIBLE);

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

                if (selectedId.equals(id)) {
                    AppHelper.setSelectedRecipe(holder.layout_listItemContainer, context);
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
