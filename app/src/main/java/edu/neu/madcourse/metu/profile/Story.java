package edu.neu.madcourse.metu.profile;

import android.graphics.Bitmap;

public class Story {
    private Bitmap storyImageBitmap;

    public Story() {
    }

    public Story(Bitmap storyImageBitmap) {
        this.storyImageBitmap = storyImageBitmap;
    }

    public Bitmap getStoryImageBitmap() {
        return storyImageBitmap;
    }

    public void setStoryImageBitmap(Bitmap storyImageBitmap) {
        this.storyImageBitmap = storyImageBitmap;
    }
}
