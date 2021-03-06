package edu.neu.madcourse.metu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    private String connectionId;
    private String contactUserId;
    private String contactName;
    private boolean isOnline;
    private int connectionPoint;
    private String contactAvatarUri;
    public Contact() {
    }


    public Contact(String connectionId, String contactUserId, String contactName,
                   String contactAvatarUri, boolean contactOnlineStatus, int connectionPoint) {
        this.connectionId = connectionId;
        this.contactUserId = contactUserId;
        this.contactName = contactName;
        this.isOnline = contactOnlineStatus;
        this.connectionPoint = connectionPoint;
        this.contactAvatarUri = contactAvatarUri;
    }

    public Contact(String connectionId, String contactUserId, String contactName,
                   String contactAvatarUri, int connectionPoint) {
        this.connectionId = connectionId;
        this.contactUserId = contactUserId;
        this.contactName = contactName;
        this.connectionPoint = connectionPoint;
        this.contactAvatarUri = contactAvatarUri;
        this.isOnline = false;
    }


    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    public int getConnectionPoint() {
        return connectionPoint;
    }

    public void setConnectionPoint(int connectionPoint) {
        this.connectionPoint = connectionPoint;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.connectionId);
        dest.writeString(this.contactUserId);
        dest.writeString(this.contactName);
        dest.writeInt(this.isOnline ? 1 : 0);
        dest.writeInt(this.connectionPoint);
        dest.writeString(this.contactAvatarUri);
    }

    public static final Parcelable.Creator<Contact> CREATOR
            = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    private Contact(Parcel in) {
        this.connectionId= in.readString();
        this.contactUserId = in.readString();
        this.contactName = in.readString();
        this.isOnline = in.readInt() == 1;
        this.connectionPoint = in.readInt();
        this.contactAvatarUri = in.readString();
    }

    public String getContactAvatarUri() {
        return contactAvatarUri;
    }
}
