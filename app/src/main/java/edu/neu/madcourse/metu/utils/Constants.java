package edu.neu.madcourse.metu.utils;

import java.util.HashMap;
import java.util.Map;

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
    // for messages count
    // messagesCount {
    //      connectionId:
    //          yyyy-mm-dd: count
    // }
    public static final String MESSAGES_COUNT_STORE = "messagesCount";
    public static final long POINTS_ADDED_BY_CHAT_EACH_DAY = 10;
    public static final long POINTS_ADDED_ONE_MESSAGE = 1;

    // for Users
    public static final String USERS_STORE = "users";
    public static final String USER_USER_ID = "userId";
    public static final String USER_NICKNAME = "nickname";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    public static final String USER_LOCATION = "location";
    public static final String USER_AGE = "age";
    public static final String USER_GENDER = "gender";
    public static final String USER_TAGS = "tags";
    public static final String USER_STORIES = "stories";
    public static final String USER_AVATAR_URI = "avatarUri";
    public static final String USER_CONNECTIONS = "connections";
    public static final String USER_LAST_LOGIN_TIME = "lastLoginTime";
    public static final String USER_SETTING_MESSAGE = "settingMessage";
    public static final String USER_SETTING_VIDEO = "settingVideo";
    public static final String USER_SETTING_VIBRATION = "settingVibration";

    // for recommendations
    // recommendations {
    //      userId
    //          yyyymmdd:
    //              recommended userId: true
    //              ....
    // }
    public static final String RECOMMENDATIONS_STORE = "recommendations";


    // user gender
    // todo: add more gender
    public static final int GENDER_MALE_INT = 0;
    public static final int GENDER_FEMALE_INT = 1;
    public static final int GENDER_UNDEFINE_INT = 2;
    public static final String GENDER_MALE_STRING = "male";
    public static final String GENDER_FEMALE_STRING = "female";
    public static final String GENDER_UNDEFINED_STRING = "undefined";

    public static final Map<Integer, String> GENDER_MAP = new HashMap<Integer, String>() {
        {
            put(GENDER_MALE_INT, GENDER_MALE_STRING);
            put(GENDER_FEMALE_INT, GENDER_FEMALE_STRING);
            put(GENDER_UNDEFINE_INT, GENDER_UNDEFINED_STRING);
        }
    };

    // user explore settings
    public static final String EXPLORE_SETTINGS_STORE = "exploreSettings";
    public static final String EXPLORE_SETTING_LOCATION = "locationPreference";
    public static final String EXPLORE_SETTING_GENDER = "genderPreference";
    public static final String EXPLORE_SETTING_AGE_MIN = "ageMin";
    public static final String EXPLORE_SETTING_AGE_MAX = "ageMax";
    // 0: only male checked
    // 1: only female checked
    // 2: only other checked
    // 3: male and female
    // 4: male and other
    // 5: female and other
    // 6: all
    public static final int EXPLORE_MAN_ONLY = 0;
    public static final int EXPLORE_WOMAN_ONLY = 1;
    public static final int EXPLORE_OTHER_ONLY = 2;
    public static final int EXPLORE_MAN_WOMAN = 3;
    public static final int EXPLORE_MAN_OTHER = 4;
    public static final int EXPLORE_WOMAN_OTHER = 5;
    public static final int EXPLORE_ALL = 6;


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
