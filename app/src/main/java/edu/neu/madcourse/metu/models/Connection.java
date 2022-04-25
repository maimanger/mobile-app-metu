package edu.neu.madcourse.metu.models;

public class Connection {

    private ConnectionUser user1;
    private ConnectionUser user2;
    private int connectionPoint;

    private ChatItem lastMessage;

    public Connection(ConnectionUser user1, ConnectionUser user2, int connectPoint, ChatItem lastMessage) {
        this.user1 = user1;
        this.user2 = user2;
        this.connectionPoint = connectPoint;
        this.lastMessage = lastMessage;
    }

    public Connection() {
    }

    public ConnectionUser getUser1() {
        return user1;
    }

    public void setUser1(ConnectionUser user1) {
        this.user1 = user1;
    }

    public ConnectionUser getUser2() {
        return user2;
    }

    public void setUser2(ConnectionUser user2) {
        this.user2 = user2;
    }

    public int getConnectionPoint() {
        return connectionPoint;
    }

    public void setConnectionPoint(int connectionPoint) {
        this.connectionPoint = connectionPoint;
    }

    public ChatItem getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatItem lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "user1=" + user1 +
                ", user2=" + user2 +
                ", connectPoint=" + connectionPoint +
                ", lastMessage=" + lastMessage +
                '}';
    }
}
