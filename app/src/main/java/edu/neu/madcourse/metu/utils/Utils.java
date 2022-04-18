package edu.neu.madcourse.metu.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.neu.madcourse.metu.R;

public class Utils {

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
}
