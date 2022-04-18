package edu.neu.madcourse.metu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RenderNode;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    public static Bitmap getBitmapFromString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static String encodeImage(Bitmap bitmap, int previewWidth) {
//        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {

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
}
