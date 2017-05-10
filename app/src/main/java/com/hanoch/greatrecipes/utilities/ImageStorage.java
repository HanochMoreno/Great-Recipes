package com.hanoch.greatrecipes.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Image storage to SD card (External Memory).
 * Used for storing recipes images in the SD card
 */

public class ImageStorage {

    public static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";

    public static File saveToSdCard(Context context, Bitmap bitmap, String filename) {

        File sdcard = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (sdcard==null) return null;

        File folder = new File(sdcard.getAbsoluteFile(), "Great Recipes");
        // A dot addition (".Great Recipes") will make the directory invisible to the user.

        folder.mkdir();

        File file = new File(folder.getAbsoluteFile(), filename + ".jpg");

        // in case we don't want to allow overriding:
        /* if (file.exists())
            return stored ; */

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();
            //stored = "success";

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("ImageStoragePrivate", "New image was added with path: " + file.getAbsolutePath());

        return file;
    }

//-------------------------------------------------------------------------------------------------

    /*public static void scanFile(Context context, File file) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }*/

//-------------------------------------------------------------------------------------------------

    public static File getImageFileByName(Context context, String imageName) {

        File mediaImage = null;
        try {
            File myDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (myDir==null) return null;

            mediaImage = new File(myDir.getPath() + "/Great Recipes/" + imageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

//-----------------------------------------------------------------------------

    public static Bitmap getImageBitmapByName(Context context, String imageName) {

        File file = ImageStorage.getImageFileByName(context, "/" + imageName + ".jpg");

        if (file == null) return null;

        String path = file.getAbsolutePath();

        return BitmapFactory.decodeFile(path);
    }

//-----------------------------------------------------------------------------

    public static Boolean deleteImageByImageName(Context context, String imageName) {

        File file = ImageStorage.getImageFileByName(context, "/" + imageName + ".jpg");

        String path = null;
        if (file != null) {
            path = file.getAbsolutePath();
        }

        Log.d("deleteImageByImageName", "Image path: " + path);

        Boolean deleted = false;

        if (path != null) {
            File f = new File(path);
            deleted = f.delete();
        }

        Log.d("ImageStorage", imageName + " Deleted? = " + deleted);

        return deleted;
    }

//-----------------------------------------------------------------------------

    public static boolean deleteAllImages(Context context) {

        File myDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (myDir == null) {

            Log.d("ImageStorage", "The images were not deleted");
            return false;
        }

        if (myDir.exists()) {
            File[] files = myDir.listFiles();

            for (File file : files) {
                file.delete();
            }
        }

        Log.d("ImageStorage", "All the images were deleted");

        return true;
    }

//-------------------------------------------------------------------------------------------------

    public static boolean imageExists(Context context, String imageName) {

        File file = ImageStorage.getImageFileByName(context, "/" + imageName + ".jpg");

        Log.d("ImageStorage", "is " + imageName + " exists? = " + (file != null));

        return file != null;
    }

//-------------------------------------------------------------------------------------------------

    public static Bitmap decodeSampledBitmapFromFile(String path, Bitmap.Config config, int minWidth, int minHeight) {

        /*
        Downscale image resolution and size
         */

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = config;
        //First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        final int height = options.outHeight;
        final int width = options.outWidth;

        // 2. find the best re-sample factor (this has to be a power of 2 : 1,2,4,8,16...)
        //    as long as we can divide by 2 but keep the image size
        //    larger than minWidth X minHeight - do it
        int factor = 1;
        while (width / (factor * 2) >= minWidth && height / (factor * 2) >= minHeight) {
            factor *= 2;
        }

        // Decode bitmap with inSampleSize set
        options.inSampleSize = factor;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

//-------------------------------------------------------------------------------------------------

    public static int getExifOrientation(String path) {

        // Returns the image orientation index. Associated with the
        // function orientFromExif(Bitmap bitmap, int exifOrientation) below
        try {
            ExifInterface exif = new ExifInterface(path);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

//-------------------------------------------------------------------------------------------------

    public static Bitmap orientFromExif(Bitmap bitmap, int exifOrientation) {
        Matrix matrix = new Matrix();
        switch (exifOrientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            default:             // any other case - just return the original
                return bitmap;
        }

        // return a re-oriented copy, recycle the old bitmap!
        Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return bmRotated;
    }

//-------------------------------------------------------------------------------------------------

    public static Uri getTempUri(Context context) {
        return Uri.fromFile(getTempFile(context));
    }

//-------------------------------------------------------------------------------------------------

    public static String convertBitmapToByteArrayAsString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

//-------------------------------------------------------------------------------------------------

    public static Bitmap convertByteArrayAsStringAsToBitmap(String base64Str) {
        if (base64Str.isEmpty()) {
            return null;
        }

        byte[] decodedBytes = Base64.decode(base64Str.substring(base64Str.indexOf(",")  + 1), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

//-------------------------------------------------------------------------------------------------

    public static File getTempFile(Context context) {

        if (isSDCARDMounted()) {

            File f = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TEMP_PHOTO_FILE);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return f;
        } else {
            return null;
        }
    }

//-------------------------------------------------------------------------------------------------

    private static boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();

        return (status.equals(Environment.MEDIA_MOUNTED));
    }

}

