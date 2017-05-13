package com.hanoch.greatrecipes.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanoch.greatrecipes.AppConsts;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.database.RecipesContract;
import com.hanoch.greatrecipes.utilities.ImageStorage;
import com.hanoch.greatrecipes.utilities.MyFonts;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Custom adapter for the 'recipe search results' list
 */

public class SearchResultsAdapter extends CursorAdapter {

//----------------------------------------------------------------------------------------------

    public SearchResultsAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

//----------------------------------------------------------------------------------------------

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.listitem_search_result_card, parent, false);

        return view;
    }

//----------------------------------------------------------------------------------------------

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final String resultYummlyId = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.YUMMLY_ID));
        String resultTitle = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.TITLE));
        String resultImageUrl = cursor.getString(cursor.getColumnIndex(RecipesContract.SearchResults.IMAGE_URL));

        final TextView textView_resultTitle = (TextView) view.findViewById(R.id.textView_resultTitle);
        textView_resultTitle.setText(resultTitle);
        textView_resultTitle.setTypeface(MyFonts.getInstance(context).getAliceFont());

        final ImageView imageView_resultImage = (ImageView) view.findViewById(R.id.imageView_resultImage);
        final TextView textView_noImageAvailable = (TextView) view.findViewById(R.id.textView_noImageAvailable);

        String imageName = AppConsts.Images.RESULT_IMAGE_PREFIX + resultYummlyId;
        Bitmap bitmap = ImageStorage.getImageBitmapByName(context, imageName);
        if (bitmap != null) {
            imageView_resultImage.setImageBitmap(bitmap);
            textView_noImageAvailable.setVisibility(View.INVISIBLE);

        } else {

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ImageStorage.saveToSdCard(context, bitmap, imageName);
                    imageView_resultImage.setImageBitmap(bitmap);
                    textView_noImageAvailable.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            Picasso.with(context).load(resultImageUrl).into(target);
        }
    }

}
