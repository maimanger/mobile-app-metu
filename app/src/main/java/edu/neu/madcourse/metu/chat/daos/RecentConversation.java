package edu.neu.madcourse.metu.chat.daos;

import java.io.Serializable;

import edu.neu.madcourse.metu.models.ChatItem;

public class RecentConversation implements Serializable {
    private static final long serialVersionUID = 4L;

    private User recentContact;
    private ChatItem recentConversation;

    public User getRecentContact() {
        return recentContact;
    }

    public void setRecentContact(User recentContact) {
        this.recentContact = recentContact;
    }

    public ChatItem getRecentConversation() {
        return recentConversation;
    }

    public void setRecentConversation(ChatItem recentConversation) {
        this.recentConversation = recentConversation;
    }

    public String getRecentContactName() {
        return recentContact.getUsername();
    }

    public String getContactAvatar() {
        return recentContact.getAvatar();
    }

    public String getRecentMessage() {
        return recentConversation.getMessage();
    }

    public String getConversationFormattedTimestamp() {
        return recentConversation.generateFormattedTimestamp();
    }
}
