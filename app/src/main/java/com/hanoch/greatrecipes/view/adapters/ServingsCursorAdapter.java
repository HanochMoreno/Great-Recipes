package com.hanoch.greatrecipes.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.database.DbManager;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom adapter for the 'Online recipes' list
 */

public class ServingsCursorAdapter extends CursorAdapter {

    int MAX_ITEMS = 100;

    private ArrayList<String> selectedIds;
    private HashMap<String, View> viewsMap = new HashMap<>();

    private DbManager dbManager;

//-------------------------------------------------------------------------------------------------

    private class ViewHolder {

        private long id;
        private long recipeId;
        private TextView textView_recipeTitle;
        private TextView textView_servingType;

        private ImageView imageView_image;
        private ImageView imageView_checked;
        public TextView textView_noImageAvailable;
    }

//----------------------------------------------------------------------------------------------

    public ServingsCursorAdapter(Context context, Cursor c, ArrayList<String> selectedIds) {
        super(context, c, 0);

        this.selectedIds = selectedIds;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.listitem_serving, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.textView_recipeTitle = (TextView) view.findViewById(R.id.textView_recipeTitle);
        holder.textView_servingType = (TextView) view.findViewById(R.id.textView_servingType);

        holder.imageView_image = (ImageView) view.findViewById(R.id.imageView_servingImage);
        holder.imageView_checked = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        holder.textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);

        view.setTag(holder);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        long id = cursor.getLong(cursor.getColumnIndex(RecipesContract.MealPlanning._ID));
        long recipeId = cursor.getLong(cursor.getColumnIndex(RecipesContract.MealPlanning.RECIPE_ID));
        String servingType = cursor.getString(cursor.getColumnIndex(RecipesContract.MealPlanning.SERVING_TYPE));

        ViewHolder holder = (ViewHolder) view.getTag();

        dbManager = DbManager.getInstance(context);

        holder.textView_recipeTitle.setText(dbManager.queryRecipeObjectById(recipeId).getTitle());
        holder.recipeId = recipeId;

        holder.id = id;

        String translatedServingTypeName = AppHelper.getTranslatedServingTypeName(context, servingType);
        holder.textView_servingType.setText(translatedServingTypeName);

        holder.imageView_image.setVisibility(View.INVISIBLE);
        holder.textView_noImageAvailable.setVisibility(View.INVISIBLE);

        viewsMap.put(id + "", view);

        GetImageFromSdCard getImageFromSdCard = new GetImageFromSdCard(context, id,  recipeId, holder);
        getImageFromSdCard.execute();
    }

//-------------------------------------------------------------------------------------------------

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

    class GetImageFromSdCard extends AsyncTask<String, Integer, Bitmap> {

        private Context context;
        private long id;
        private long recipeId;
        private ViewHolder holder;

        public GetImageFromSdCard(Context context, long id, long recipeId, ViewHolder holder) {

            this.context = context;
            this.id = id;
            this.recipeId = recipeId;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String imageName = AppConsts.Images.RECIPE_IMAGE_PREFIX + recipeId;
            return ImageStorage.getImageBitmapByName(context, imageName);
        }

        @Override
        protected void onPostExecute(Bitmap image) {

            if (image != null) {

                if (holder.id == id) {

                    if (selectedIds.contains(id + "")) {
                        holder.imageView_checked.setVisibility(View.VISIBLE);
                        holder.imageView_image.setAlpha(0.5f);
                    }

                    holder.imageView_image.setImageBitmap(image);
                    holder.imageView_image.setVisibility(View.VISIBLE);
                }

            } else if (holder.id == id) {

                if (selectedIds.contains(id + "")) {
                    holder.imageView_checked.setVisibility(View.VISIBLE);
                    holder.imageView_image.setAlpha(0.5f);
                }

                holder.imageView_image.setImageBitmap(null);
                holder.textView_noImageAvailable.setVisibility(View.VISIBLE);
                holder.imageView_image.setVisibility(View.VISIBLE);
            }
        }
    }

}
