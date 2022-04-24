package edu.neu.madcourse.metu.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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

    private static Integer[] filterId = new Integer[]{
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


    private static List<Integer> FILTERS_SET = new ArrayList<>(Arrays.asList(filterId));

    public static int getFiltersSize(int friendLevel) {
        if (friendLevel > 0 && friendLevel < 2) {
            return 8;
        } else if (friendLevel >= 2 && friendLevel < 3) {
            return FILTERS_SET.size() - 1;
        } else {
            return FILTERS_SET.size();
        }
    }

    public static int getCurrentFilter(int currentFilterIdx) {
        return FILTERS_SET.get(currentFilterIdx);
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

    public static void loadImgUri(String uri, ImageView imageView) {
        Picasso.get().load(uri).into(imageView);
    }

    public static Bitmap getBitmapFromUri(String uri) {
        Bitmap mBitmap = null;
        try {
            mBitmap = Picasso.get().load(uri).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mBitmap;
    }
}
