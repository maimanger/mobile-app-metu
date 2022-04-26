package edu.neu.madcourse.metu.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChatItem implements Parcelable {

    // the format of the time displayed
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault());
    // member variables
    private String sender;
    private String message;
    private Long timeStamp; // retrieved from System.currentTimeMillis()
    private Boolean isRead;

    public ChatItem() { }

    public ChatItem(String sender, String message, Long timeStamp) {
        this.sender = sender;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isRead = false;
    }

    public ChatItem(String sender, String message, Long timeStamp, Boolean isRead) {
        this.sender = sender;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isRead = isRead;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String generateFormattedTimestamp() {
        Timestamp ts = new Timestamp(this.timeStamp);
        Date date = new Date(ts.getTime());
        // SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault());
        return format.format(date);
    }

    public String generateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Timestamp ts = new Timestamp(this.timeStamp);
        Date date = new Date(ts.getTime());
        return dateFormat.format(date);
    }

    @Override
    public String toString() {
        return "ChatItem{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                ", isRead=" + isRead +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.sender);
        parcel.writeString(this.message);
        parcel.writeLong(this.timeStamp);
        parcel.writeInt(this.isRead? 1:0);
    }

    private ChatItem(Parcel in) {
        this.sender = in.readString();
        this.message = in.readString();
        this.timeStamp = in.readLong();
        this.isRead = in.readInt() == 1;
    }

    public static final Parcelable.Creator<ChatItem> CREATOR = new Creator<ChatItem>() {
        @Override
        public ChatItem createFromParcel(Parcel parcel) {
            return new ChatItem(parcel);
        }

        @Override
        public ChatItem[] newArray(int i) {
            return new ChatItem[i];
        }
    };
}
