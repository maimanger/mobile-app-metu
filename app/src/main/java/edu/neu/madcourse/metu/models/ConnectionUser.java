package edu.neu.madcourse.metu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectionUser implements Parcelable {
    private String userId;
    private String nickname;
    private String avatarUri;
    private boolean isLiked;

    public ConnectionUser(String userId, String nickname, String avatarUri, boolean isLiked) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatarUri = avatarUri;
        this.isLiked = isLiked;
    }

    public ConnectionUser() {

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

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean liked) {
        this.isLiked = liked;
    }

    @Override
    public String toString() {
        return "ConnectionUser{" +
                "userId='" + userId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUri + '\'' +
                ", isLiked=" + isLiked +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(nickname);
        parcel.writeString(avatarUri);
        parcel.writeInt(isLiked ? 1: 0);
    }

    private ConnectionUser(Parcel in) {
        this.userId = in.readString();
        this.nickname = in.readString();
        this.avatarUri = in.readString();
        this.isLiked = in.readInt() == 1;
    }

    public static final Parcelable.Creator<ConnectionUser> CREATOR
            = new Parcelable.Creator<ConnectionUser>() {
        @Override
        public ConnectionUser createFromParcel(Parcel parcel) {
            return new ConnectionUser(parcel);
        }

        @Override
        public ConnectionUser[] newArray(int i) {
            return new ConnectionUser[i];
        }
    };
}