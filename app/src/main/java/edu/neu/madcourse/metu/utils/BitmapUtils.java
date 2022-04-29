package edu.neu.madcourse.metu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.RenderNode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.jgabrielfreitas.core.BlurImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.neu.madcourse.metu.R;

public class BitmapUtils {
    public static Bitmap getBitmapFromString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static String encodeImage(Bitmap bitmap, int previewWidth) {
        if (bitmap == null) {
            return "";
        }
//        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {

        if (bitmap == null) {
            return null;
        }

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        Bitmap outputBitmap = Bitmap.createBitmap(previewBitmap);

        RenderScript renderScript = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

        Allocation tempIn = Allocation.createFromBitmap(renderScript, previewBitmap);
        Allocation tempOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        blurScript.setRadius(radius);
        blurScript.setInput(tempIn);
        blurScript.forEach(tempOut);

        tempOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static Bitmap getBitmapFromUri(String src) {
        Bitmap result = null;
        if (src == null) {
            return null;
        }

        try {
            URL url = new URL(src);
            InputStream inputStream = url.openStream();
            result = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }

        return result;

    }

    public static class DownloadBlurredImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Context context;
        int radius;

        public DownloadBlurredImageTask(ImageView bmImage, Context context, int radius) {
            this.bmImage = bmImage;
            this.context = context;
            this.radius = radius;
            bmImage.setImageResource(R.drawable.ic_loading);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return blurBitmap(context, bitmap, radius);
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                bmImage.setImageResource(R.drawable.ic_default_avatar);
            } else {
                bmImage.setImageBitmap(result);
            }
        }
    }

}
