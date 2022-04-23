package edu.neu.madcourse.metu.profile;

import android.graphics.Bitmap;
import android.net.Uri;

public class Story {
    private String storyImageUri;

    public Story() {
    }

    public Story(String storyImageUri) {
        this.storyImageUri = storyImageUri;
    }

    public String getStoryImageUri() {
        return storyImageUri;
    }

    public void setStoryImageUri(String storyImageUri) {
        this.storyImageUri = storyImageUri;
    }
}
