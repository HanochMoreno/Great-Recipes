package com.hanoch.greatrecipes.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class RecipesProvider extends ContentProvider {

    RecipesDbOpenHelper openHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {

        openHelper = new RecipesDbOpenHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = openHelper.getWritableDatabase();
        int counter = db.delete(getTableName(uri),selection,selectionArgs);
        if (counter>0) {
            // At least 1 note was deleted:
            // notify the manager:
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return counter;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // uri has the path CONTENT_URI;
        db = openHelper.getWritableDatabase();
        long id = db.insertWithOnConflict(
                getTableName(uri),
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        if (id>0) {
            // New recipe was created:

            // notify the manager:
            getContext().getContentResolver().notifyChange(uri,null);
            // return a Uri with path: CONTENT_URI + id
            return ContentUris.withAppendedId(uri,id);

        }else {
            // Couldn't create a new recipe:
            return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        db = openHelper.getReadableDatabase();

        Cursor cursor = db.query(
                getTableName(uri),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        db = openHelper.getWritableDatabase();

        int counter = db.updateWithOnConflict(
                getTableName(uri),
                values,
                selection,
                selectionArgs,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        if (counter>0) {
            // At least 1 note was updated:
            // notify the manager:
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return counter;
    }

    private String getTableName(Uri uri) {

        return uri.getPathSegments().get(0);
    }
}
