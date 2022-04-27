package edu.neu.madcourse.metu.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.neu.madcourse.metu.R;
import io.agora.rtm.RemoteInvitation;

public class Utils {
    public static int CALLER_NAME = 0;
    public static int CALLER_AVATAR = 1;
    public static int CALL_CONNECTION_POINT = 2;
    public static int CALL_CONNECTION_ID = 3;
    public static int CALLER_ID = 4;

    private static Integer[] filterIdHigh = new Integer[]{
            R.drawable.church_window,
            R.drawable.blue_mosaic,
            R.drawable.bubbles,
            R.drawable.sparkling,
            R.drawable.mosaic,
            R.drawable.symbols_frame,
            R.drawable.purple_ink_splash,
            R.drawable.yellow_ink_splash,
            R.drawable.cherry_blossom,
            R.drawable.smoke,
            R.drawable.rainy_window,
            R.drawable.blue_brush,
            R.drawable.purple_brush,
            R.drawable.yellow_brush,
            R.drawable.yellow_watercolor,
            R.drawable.light_blue_mosaic,
            -9999};

    private static Integer[] filterIdMid = new Integer[]{
            R.drawable.church_window,
            R.drawable.blue_mosaic,
            R.drawable.bubbles,
            R.drawable.sparkling,
            R.drawable.mosaic,
            R.drawable.symbols_frame,
            R.drawable.purple_ink_splash,
            R.drawable.yellow_ink_splash,
            R.drawable.cherry_blossom,
            R.drawable.smoke,
            R.drawable.rainy_window,
            R.drawable.blue_brush,
            R.drawable.purple_brush,
            R.drawable.yellow_brush,
            R.drawable.yellow_watercolor,
            R.drawable.light_blue_mosaic};

    private static Integer[] filterIdStart = new Integer[] {
            R.drawable.church_window,
            R.drawable.blue_mosaic,
            R.drawable.bubbles,
            R.drawable.sparkling,
            R.drawable.mosaic,
            R.drawable.symbols_frame,
            R.drawable.purple_ink_splash,
            R.drawable.yellow_ink_splash,
    };

    private static List<Integer> FILTERS_SET_HIGH = new ArrayList<>(Arrays.asList(filterIdHigh));
    private static List<Integer> FILTERS_SET_MID = new ArrayList<>(Arrays.asList(filterIdMid));
    private static List<Integer> FILTERS_SET_START =  new ArrayList<>(Arrays.asList(filterIdStart));


    public static int getFiltersSize(int friendLevel) {
        switch (friendLevel) {
            case 1 :
                return FILTERS_SET_START.size();
            case 2 :
                return FILTERS_SET_MID.size();
            default:
                return FILTERS_SET_HIGH.size();
        }
    }

    public static int getCurrentFilter(int friendLevel, int currentFilterIdx) {
        switch (friendLevel) {
            case 1 :
                return FILTERS_SET_START.get(currentFilterIdx);
            case 2 :
                return FILTERS_SET_MID.get(currentFilterIdx);
            default:
                return FILTERS_SET_HIGH.get(currentFilterIdx);
        }
    }


    public static int calculateFriendLevel(int connectionPoint) {
        if (connectionPoint == 0) {
            return 0;
        } else if (connectionPoint > 0 && connectionPoint < 10) {
            return 1;
        } else if (connectionPoint >= 10 && connectionPoint < 40) {
            return 2;
        } else {
            return 3;
        }
    }

    public static String createCallInvitationContent(String callerName, String callerAvatarUrl,
                                                     int connectionPoint, String connectionId) {
        return callerName + "-&-" + callerAvatarUrl + "-&-"
                + connectionPoint + "-&-" + connectionId;
    }

    public static String createCallInvitationContent(String callerName, String callerAvatarUrl,
                                                     int connectionPoint, String connectionId, String callerId) {
        return callerName + "-&-" + callerAvatarUrl + "-&-"
                + connectionPoint + "-&-" + connectionId + "-&-" + callerId;
    }

    public static String getRemoteInvitationContent(RemoteInvitation remoteInvitation, int contentIdx) {
        return remoteInvitation.getContent().split("-&-")[contentIdx];
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    public static void loadImgUri(String uri, ImageView imageView, Callback callback) {
        if (uri == null
                || (!uri.startsWith("http://") && !uri.startsWith("https://"))
                || (!Patterns.WEB_URL.matcher(uri).matches())) {
            callback.onError(new Exception());
            return;
        }
        Picasso.get().load(uri).into(imageView, callback);

    }

    public static Bitmap getBitmapFromUri(String uri) {
        Bitmap mBitmap = null;
        if (uri == null
                || (!uri.startsWith("http://") && !uri.startsWith("https://"))
                || (!Patterns.WEB_URL.matcher(uri).matches())) {
            return mBitmap;
        }

        try {
            mBitmap = Picasso.get().load(uri).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mBitmap;
    }
}
