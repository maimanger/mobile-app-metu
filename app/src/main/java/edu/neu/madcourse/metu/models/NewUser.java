package edu.neu.madcourse.metu.models;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class NewUser {
    private String username; // username is used as nickname
    private String password;
    private String email; // email is used as id
    private String location;
    private Integer age;
    private String gender;
    private Map<String, Boolean> tags;
    private Map<String, Boolean> stories;
    private Boolean isOnline;
    private String avatarUrl;

    public NewUser() { }

    public NewUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public NewUser(String username, String password, String email, String location, Integer age,
                   String gender, Map<String, Boolean> tags, Map<String, Boolean> stories,
                   Boolean isOnline, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.location = location;
        this.age = age;
        this.gender = gender;
        this.tags = tags;
        this.stories = stories;
        this.isOnline = isOnline;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Map<String, Boolean> getStories() {
        return stories;
    }

    public void setStories(Map<String, Boolean> stories) {
        this.stories = stories;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "NewUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", location='" + location + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", tags=" + tags +
                ", stories=" + stories +
                ", isOnline=" + isOnline +
                ", avatarUrl=" + avatarUrl +
                '}';
    }
}
