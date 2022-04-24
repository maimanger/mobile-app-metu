package edu.neu.madcourse.metu.models;

public class ConnectionUser {
    private String userId;
    private String nickName;
    private String avatarUrl;


    public ConnectionUser(String username, String nickname, String avatarUrl) {
        this.userId = username;
        this.nickName = nickname;
        this.avatarUrl = avatarUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }



    @Override
    public String toString() {
        return "ConnectionUser{" +
                "username='" + userId + '\'' +
                ", nickname='" + nickName + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
