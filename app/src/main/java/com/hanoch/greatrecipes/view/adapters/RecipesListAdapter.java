package com.hanoch.greatrecipes.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.AppHelper;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.utilities.ImageStorage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom adapter for a recipes list
 */

public class RecipesListAdapter extends CursorAdapter {

    int MAX_ITEMS = 500;

    private ArrayList<String> checkedIds;
    private long selectedId = -1;
    private HashMap<String, View> viewsMap = new HashMap<>();

    private Context context;

//-------------------------------------------------------------------------------------------------

    private class ViewHolder {

        private long id;

        private FrameLayout layout_listItemContainer;
        private TextView textView_title;

        private ImageView imageView_image;
        private ImageView imageView_checked;
        public TextView textView_noImageAvailable;
    }

//----------------------------------------------------------------------------------------------

    public RecipesListAdapter(Context context, Cursor c, ArrayList<String> checkedIds, long selectedId) {
        super(context, c, 0);

        this.selectedId = selectedId;
        this.checkedIds = checkedIds;
        this.context = context;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.listitem_recipes_list, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.layout_listItemContainer = (FrameLayout) view.findViewById(R.id.layout_listItemContainer);
        holder.textView_title = (TextView) view.findViewById(R.id.textView_itemTitle);
        holder.imageView_image = (ImageView) view.findViewById(R.id.imageView_itemImage);
        holder.imageView_checked = (ImageView) view.findViewById(R.id.imageView_checkedIcon);
        holder.textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);

        view.setTag(holder);

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        long id = cursor.getLong(cursor.getColumnIndex(RecipesContract.Recipes._ID));
        String title = cursor.getString(cursor.getColumnIndex(RecipesContract.Recipes.TITLE));

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.id = id;
        holder.textView_title.setText(title);

        holder.imageView_image.setVisibility(View.INVISIBLE);
        holder.textView_noImageAvailable.setVisibility(View.INVISIBLE);

        viewsMap.put(id + "", view);

        GetImageFromSdCard getImageFromSdCard = new GetImageFromSdCard(id, holder);
        getImageFromSdCard.execute();
    }

//-------------------------------------------------------------------------------------------------

    public View getViewById(String id) {

        return viewsMap.get(id);
    }

//-------------------------------------------------------------------------------------------------

    public boolean onRecipeSelected(long id) {

        this.selectedId = id;

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public boolean onNoSelection() {

        selectedId = -1;

        return true;
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

    class GetImageFromSdCard extends AsyncTask<String, Integer, Bitmap> {

        private long id;
        private ViewHolder holder;

        public GetImageFromSdCard(long id, ViewHolder holder) {

            this.id = id;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String imageName = AppConsts.Images.RECIPE_THUMBNAIL_PREFIX + id;
            return ImageStorage.getImageBitmapByName(context, imageName);
        }

        @Override
        protected void onPostExecute(Bitmap image) {

            if (holder.id == id) {

                if (checkedIds.contains(id + "")) {
                    holder.imageView_checked.setVisibility(View.VISIBLE);
                    holder.imageView_image.setAlpha(0.5f);
                }

                if (selectedId == id) {
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
