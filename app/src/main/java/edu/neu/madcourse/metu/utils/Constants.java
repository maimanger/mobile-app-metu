package edu.neu.madcourse.metu.utils;

import java.util.HashMap;

public class Constants {
    // for FCM tokens
    public static final String FCM_TOKENS_STORE = "FCMTokens";

    // for firebase cloud messaging and notifications
    public static final String REMOTE_MSG_AUTH = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static HashMap<String, String> remoteMsgHeaders = null;
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    // notification content
    public static final String NOTIFICATION_CONTENT = "notificationContent";
    public static final String MSG_SENDER_USER_ID = "sender_userId";
    public static final String MSG_RECEIVER_USER_ID = "receiver_userId";
    public static final String MSG_SENDER_NICKNAME = "sender_nickname";
    public static final String MSG_RECEIVER_NICKNAME = "receiver_nickname";
    public static final String MSG_SENDER_AVATAR_URI = "sender_avatarUri";
    public static final String MSG_RECEIVER_AVATAR_URI = "receiver_avatarUri";
    public static final String MSG_SENDER_IS_LIKED = "sender_isLiked";
    public static final String MSG_RECEIVER_IS_LIKED = "receiver_isLiked";

    public static final String NOTIFICATION_TYPE = "notificationType";
    public static final String NOTIFY_GET_A_LIKE = "get_a_liked";
    public static final String NOTIFY_NEW_MSG = "new_msg";

    //public static final String FCM_TOKEN = "fcmToken";

    // for Connections
    public static final String CONNECTIONS_STORE = "connections";
    public static final String CONNECTION_POINT = "connectionPoint";
    public static final String CONNECTION_LAST_MESSAGE = "lastMessage";
    public static final String CONNECTION_USER1 = "user1";
    public static final String CONNECTION_USER2 = "user2";
    public static final String CONNECTION_USER_IS_LIKED = "isLiked";

    // for sending the msg
    public static final String CONNECTION_ID = "connectionId";

    // for Messages
    public static final String MESSAGES_STORE = "messages";
    public static final String MESSAGE_IS_READ = "isRead";
    public static final String MESSAGE_CONTENT = "message";
    public static final String MESSAGE_SENDER = "sender";
    public static final String MESSAGE_TIMESTAMP = "timeStamp";

    // for Users
    public static final String USERS_STORE = "users";
    public static final String USER_USER_ID = "userId";
    public static final String USER_NICKNAME = "nickname";
    public static final String USER_AVATAR_URI = "avatarUri";
    public static final String USER_CONNECTIONS = "connections";
    public static final String USER_GENDER = "gender";

    // user gender
    // todo: add more gender
    public static final int GENDER_MALE_INT = 0;
    public static final int GENDER_FEMALE_INT = 1;
    public static final int GENDER_UNDEFINE_INT = 2;
    public static final String GENDER_MALE_STRING = "male";
    public static final String GENDER_FEMALE_STRING = "female";

    // for user availability
    public static final String USERS_AVAILABILITY_STORE = "availabilities";

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTH,
                    // todo: update it
                    "key=AAAA9MOrMTs:APA91bExIh_BrVhWyBNLfIEv5V2uFwgyTV3XIMAKYoQayy-JtJ4dfeNus31bwwZx1P22ln29n0PDgVWZ-Zrtk26RcWvRhHyNvX7U6tcuRgLowAauXAldBbqMg-TzYu38bLuCGIcbU9sh"
                    );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }

        return remoteMsgHeaders;
    }

}