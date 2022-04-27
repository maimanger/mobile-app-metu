package edu.neu.madcourse.metu.explore.daos;

import android.os.Parcel;
import android.os.Parcelable;

import edu.neu.madcourse.metu.models.ConnectionUser;

public class RecommendedUser implements Parcelable {
    private String userId;
    private String nickname;
    private String avatarUri;
    private int gender;
    private boolean isLiked;

    public RecommendedUser() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean liked) {
        isLiked = liked;
    }

    public ConnectionUser convertToConnectionUser() {
        ConnectionUser user = new ConnectionUser();
        user.setUserId(userId);
        user.setNickname(nickname);
        user.setAvatarUri(avatarUri);
        user.setIsLiked(isLiked);

        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.userId);
        parcel.writeString(this.nickname);
        parcel.writeString(this.avatarUri);
        parcel.writeInt(this.gender);
        parcel.writeInt(this.isLiked? 1:0);
    }

    private RecommendedUser(Parcel in) {
        this.userId = in.readString();
        this.nickname = in.readString();
        this.avatarUri = in.readString();
        this.gender = in.readInt();
        this.isLiked = in.readInt() == 1;
    }

    private static final Parcelable.Creator<RecommendedUser> CREATOR
            = new Creator<RecommendedUser>() {
        @Override
        public RecommendedUser createFromParcel(Parcel parcel) {
            return new RecommendedUser(parcel);
        }

        @Override
        public RecommendedUser[] newArray(int i) {
            return new RecommendedUser[i];
        }
    };
}
