package edu.neu.madcourse.metu.models;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChatItem implements Serializable {
    private static final long serialVersionUID = 2L;

    // todo: delete receiver
    // the format of the time displayed
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault());
    // member variables
    private String sender;
//    private String receiver;
    private String message;
    private long timeStamp; // retrieved from System.currentTimeMillis()
    private boolean isRead;

    public ChatItem() { }

    public ChatItem(String sender, String message, long timeStamp) {
        this.sender = sender;
//        this.receiver = receiver;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isRead = false;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String generateFormattedTimestamp() {
        Timestamp ts = new Timestamp(this.timeStamp);
        Date date = new Date(ts.getTime());
        // SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault());
        return format.format(date);
    }

    @Override
    public String toString() {
        return "ChatItem{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
