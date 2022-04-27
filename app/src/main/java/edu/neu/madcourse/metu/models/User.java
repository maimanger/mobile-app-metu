package edu.neu.madcourse.metu.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public class User implements Parcelable {
    private String userId;
    private String nickname; // username is used as nickname
    private String password;
    private String email; // email is used as id
    private String location;
    private Integer age;
    private Integer gender; // 0 male; 1 female; 2 undeclared
    private Map<String, Boolean> tags;
    private Map<String, String> stories;
    private String avatarUri;
    private Map<String, Boolean> connections;
    private Long lastLoginTime;
    private Boolean settingMessage;
    private Boolean settingVideo;
    private Boolean settingVibration;
    //private Map<Integer, Boolean> settings; // 1 represent new messages; 2 represent video calls; 3 represent vibration


    public User() {
    }

    public User(String userId, String nickname, String password, String email, String location,
                Integer age, Integer gender, Map<String, Boolean> tags, Map<String, String> stories,
                String avatarUri, Map<String, Boolean> connections, Long lastLoginTime,
                Boolean settingMessage, Boolean settingVideo, Boolean settingVibration) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.location = location;
        this.age = age;
        this.gender = gender;
        this.tags = tags;
        this.stories = stories;
        this.avatarUri = avatarUri;
        this.connections = connections;
        this.lastLoginTime = lastLoginTime;
        this.settingMessage = settingMessage;
        this.settingVideo = settingVideo;
        this.settingVibration = settingVibration;
    }


    public User(String userId, String nickname, String password, String email, long lastLoginTime) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.location = "";
        this.age = 18;
        this.gender = 2;
        this.tags = new HashMap<>();
        this.stories = new HashMap<>();
        this.avatarUri = "";
        this.connections = new HashMap<>();
        this.lastLoginTime = lastLoginTime;
        this.settingMessage = true;
        this.settingVideo = true;
        this.settingVibration = true;
    }


    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public Boolean getSettingMessage() {
        return settingMessage;
    }

    public void setSettingMessage(Boolean settingMessage) {
        this.settingMessage = settingMessage;
    }

    public Boolean getSettingVideo() {
        return settingVideo;
    }

    public void setSettingVideo(Boolean settingVideo) {
        this.settingVideo = settingVideo;
    }

    public Boolean getSettingVibration() {
        return settingVibration;
    }

    public void setSettingVibration(Boolean settingVibration) {
        this.settingVibration = settingVibration;
    }

    protected User(Parcel in) {
        userId = in.readString();
        nickname = in.readString();
        password = in.readString();
        email = in.readString();
        location = in.readString();
        if (in.readByte() == 0) {
            age = null;
        } else {
            age = in.readInt();
        }
        gender = in.readInt();
        avatarUri = in.readString();
        if (in.readByte() == 0) {
            lastLoginTime = null;
        } else {
            lastLoginTime = in.readLong();
        }
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
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
                ", avatarUri='" + avatarUri + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Boolean> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, Boolean> connections) {
        this.connections = connections;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.userId);
        parcel.writeString(this.nickname);
        parcel.writeString(this.password);
        parcel.writeString(this.email);
        parcel.writeString(this.location);
        parcel.writeInt(this.age);
        parcel.writeInt(this.gender);
        parcel.writeMap(this.tags);
        parcel.writeMap(this.stories);
        parcel.writeString(this.avatarUri);
        parcel.writeMap(this.connections);
        parcel.writeLong(this.lastLoginTime);
        parcel.writeBoolean(this.settingMessage);
        parcel.writeBoolean(this.settingVideo);
        parcel.writeBoolean(this.settingVibration);
    }
}
