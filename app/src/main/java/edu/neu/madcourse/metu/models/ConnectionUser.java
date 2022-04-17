package edu.neu.madcourse.metu.models;

public class ConnectionUser {
    private String username;
    private String nickname;
    private String avatarUrl;
    private boolean isOnline;

    public ConnectionUser(String username, String nickname, String avatarUrl, boolean isOnline) {
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.isOnline = isOnline;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    @Override
    public String toString() {
        return "ConnectionUser{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", online=" + isOnline +
                '}';
    }
}
