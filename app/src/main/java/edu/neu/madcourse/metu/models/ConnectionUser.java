package edu.neu.madcourse.metu.models;

public class ConnectionUser {
    private String userId;
    private String nickname;
    private String avatarUri;


    public ConnectionUser(String username, String nickname, String avatarUrl) {
        this.userId = username;
        this.nickname = nickname;
        this.avatarUri = avatarUrl;
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



    @Override
    public String toString() {
        return "ConnectionUser{" +
                "username='" + userId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUri + '\'' +
                '}';
    }
}
