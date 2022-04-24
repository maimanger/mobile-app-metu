package edu.neu.madcourse.metu.chat.daos;

import android.os.Parcel;
import android.os.Parcelable;

import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.ConnectionUser;

public class RecentConversation implements Parcelable {
    private String connectionId;
    private ConnectionUser recentContact;
    private ChatItem lastMessage;

    public RecentConversation() {
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public ConnectionUser getRecentContact() {
        return recentContact;
    }

    public void setRecentContact(ConnectionUser recentContact) {
        this.recentContact = recentContact;
    }

    public ChatItem getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatItem lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getRecentContactNickname() {
        return recentContact.getNickname();
    }

    public String getContactAvatar() {
        return recentContact.getAvatarUri();
    }

    public String getRecentMessage() {
        return lastMessage.getMessage();
    }

    public String getConversationFormattedTimestamp() {
        return lastMessage.generateFormattedTimestamp();
    }

    @Override
    public String toString() {
        return "RecentConversation{" +
                "connectionId='" + connectionId + '\'' +
                ", recentContact=" + recentContact +
                ", recentConversation=" + lastMessage +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.connectionId);
        parcel.writeParcelable(this.recentContact, i);
        parcel.writeParcelable(this.lastMessage, i);
    }

    private RecentConversation(Parcel in) {
        this.connectionId = in.readString();
        this.recentContact = in.readParcelable(ConnectionUser.class.getClassLoader());
        this.lastMessage = in.readParcelable(ChatItem.class.getClassLoader());

    }

    public static final Parcelable.Creator<RecentConversation> CREATOR = new Creator<RecentConversation>() {
        @Override
        public RecentConversation createFromParcel(Parcel parcel) {
            return new RecentConversation(parcel);
        }

        @Override
        public RecentConversation[] newArray(int i) {
            return new RecentConversation[i];
        }
    };
}
