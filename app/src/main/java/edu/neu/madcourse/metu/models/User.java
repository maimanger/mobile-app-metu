package edu.neu.madcourse.metu.models;

import java.util.Map;

public class User {
    private String userId;
    private String nickname; // username is used as nickname
    private String password;
    private String email; // email is used as id
    private String location;
    private Integer age;
    private String gender;
    private Map<String, Boolean> tags;
    private Map<String, String> stories;
    private Boolean isOnline;
    private String avatarUri;

    public User() { }

    public User(String username, String password, String email) {
        this.nickname = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, String location, Integer age,
                String gender, Map<String, Boolean> tags, Map<String, String> stories,
                Boolean isOnline, String avatarUri) {
        this.nickname = username;
        this.password = password;
        this.email = email;
        this.location = location;
        this.age = age;
        this.gender = gender;
        this.tags = tags;
        this.stories = stories;
        this.isOnline = isOnline;
        this.avatarUri = avatarUri;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(Map<String, Boolean> tags) {
        this.tags = tags;
    }

    public Map<String, String> getStories() {
        return stories;
    }

    public void setStories(Map<String, String> stories) {
        this.stories = stories;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    @Override
    public String toString() {
        return "NewUser{" +
                "username='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", location='" + location + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", tags=" + tags +
                ", stories=" + stories +
                ", isOnline=" + isOnline +
                ", avatarUri='" + avatarUri + '\'' +
                '}';
    }
}
