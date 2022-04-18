package edu.neu.madcourse.metu.contacts.daos;

import java.io.Serializable;

import edu.neu.madcourse.metu.contacts.daos.User;

public class RecommendedProfile implements Serializable {
    private static final long serialVersionUID = 3L;

    private User recommendUser;
    private boolean isLiked;

    public RecommendedProfile(User recommendUser, boolean isLiked) {
        this.recommendUser = recommendUser;
        this.isLiked = isLiked;
    }

    public User getRecommendUser() {
        return recommendUser;
    }

    public void setRecommendUser(User recommendUser) {
        this.recommendUser = recommendUser;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public void likeClicked() {
        this.isLiked = !this.isLiked;
    }

}
