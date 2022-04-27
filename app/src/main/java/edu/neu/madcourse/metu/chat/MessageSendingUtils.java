package edu.neu.madcourse.metu.chat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.metu.chat.daos.RecentConversation;
import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.MessagingService;
import edu.neu.madcourse.metu.utils.MetUException;

public class MessageSendingUtils {

    // create a new connection and return the new connectionId
    public static void createNewConnection(ConnectionUser sender, ConnectionUser receiver, DataFetchCallback<String> callback) throws Exception {
        String connectionId = sender.getUserId() + receiver.getUserId();
        // build a new connection
        Connection connection = new Connection(sender, receiver, 0, null);
        // add this connection to the connection stores
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .child(connectionId)
                .setValue(connection).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // save this connectionId into both users
                // for the sender
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .child(sender.getUserId())
                        .child(Constants.USER_CONNECTIONS)
                        .child(connectionId).setValue(true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // for the receiver
                                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                                        .child(receiver.getUserId())
                                        .child(Constants.USER_CONNECTIONS)
                                        .child(connectionId).setValue(true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                callback.onCallback(connectionId);
                                            }
                                        });
                            }
                        });

            }

        });




    }



    // return the connectionId
    public static void addMessageInMessagesStore(ConnectionUser sender, ConnectionUser receiver, String connectionId, String message, boolean isFriend) throws Exception {
        // prerequisite: the connectionId is valid
        if (connectionId == null || connectionId.length() == 0) {
            throw new MetUException("The connectionId is invalid");
        }

        if (message == null || message.length() == 0) {
            throw new MetUException("The message content is empty");
        }

        // initialize the chatItem object
        ChatItem chatItem = new ChatItem();
        chatItem.setMessage(message);
        chatItem.setIsRead(false);
        chatItem.setTimeStamp(System.currentTimeMillis());
        chatItem.setSender(sender.getUserId());

        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE)
                .child(connectionId)
                .push()
                .setValue(chatItem).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // then update the lastMessage
                FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                        .child(connectionId)
                        .child(Constants.CONNECTION_LAST_MESSAGE)
                        .setValue(chatItem);

                // if they are friends => increase the connection point by counting the msg sent today
                // fetch the message count for today
                if (!isFriend) {
                    return;
                }
                fetchMessageCountForToday(connectionId,
                        chatItem.generateDate(), (count) -> {
                    if (count == null) {
                        count = 0l;
                    }
                    if (count < Constants.POINTS_ADDED_BY_CHAT_EACH_DAY) {
                        // increase point
                        // increase message count
                        try {
                            increaseConnectionPoint(connectionId, 1);
                            increaseMessageCount(connectionId, chatItem.generateDate(), count+1);
                        } catch (Exception e) {

                        }
                    }
                        });

            }
        });

    }

    public static void sendNewMessageNotification(ConnectionUser sender , String connectionId, String receiverToken, String message) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            // data to put
            JSONObject data = new JSONObject();
            // put the notification type
            data.put(Constants.NOTIFICATION_TYPE, Constants.NOTIFY_NEW_MSG);
            // put the info for current user
            data.put(Constants.MSG_SENDER_USER_ID, sender.getUserId());
            data.put(Constants.MSG_SENDER_NICKNAME, sender.getNickname());
            data.put(Constants.MSG_SENDER_AVATAR_URI, sender.getAvatarUri());
            data.put(Constants.MESSAGE_CONTENT, message);
            // put connection id
            data.put(Constants.CONNECTION_ID, connectionId);

            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(body.toString());
        } catch (Exception e) {

        }


    }

    public static void sendLikeMessageNotification(ConnectionUser receiver, String connectionId, String message) {
        if (receiver == null) {
            return;
        }

        fetchFCMTokenForUser(receiver.getUserId(), (token) -> {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(token);

                // data to put
                JSONObject data = new JSONObject();
                // put the notification type
                data.put(Constants.NOTIFICATION_TYPE, Constants.NOTIFY_GET_A_LIKE);
                // put the info for current user
                data.put(Constants.MESSAGE_CONTENT, String.format(message));

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                MessagingService.sendNotification(body.toString());
            } catch (Exception e) {
                Log.d("FCM", e.getMessage());
            }

        });
    }

    public static void fetchFCMTokenForUser(String userId, DataFetchCallback<String> callback) {
        if (userId == null || userId.length() == 0) {
            callback.onCallback("");
            return;
        }

        FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || !snapshot.hasChild(userId)) {
                            callback.onCallback("");
                            return;
                        }

                        try {
                            String token = snapshot.child(userId).getValue(String.class);
                            callback.onCallback(token);
                        } catch (Exception e) {
                            callback.onCallback("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void increaseMessageCount(String connectionId, String date, long newCount) throws Exception {
        if (connectionId == null || connectionId.length() == 0) {
            throw new MetUException("Invalid connection id");
        }

        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_COUNT_STORE)
                .child(connectionId)
                .child(date)
                .setValue(newCount);

    }


    public static void increaseConnectionPoint(String connectionId, int pointAdded) throws Exception {
        if (connectionId == null || connectionId.length() == 0) {
            throw new MetUException("Invalid connection id");
        }

        // get the current point
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .child(connectionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int point = 0;
                        if (snapshot.exists() && snapshot.hasChild(Constants.CONNECTION_POINT)) {
                            point = snapshot.child(Constants.CONNECTION_POINT).getValue(int.class);
                        }
                        point += pointAdded;

                        // set the point back
                        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                                .child(connectionId)
                                .child(Constants.CONNECTION_POINT)
                                .setValue(point);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void fetchMessageCountForToday(String connectionId, String date, DataFetchCallback<Long> callback) {

        try {
            FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_COUNT_STORE)
                    .child(connectionId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Long count = 0l;
                            if (snapshot.exists() && snapshot.hasChild(date)) {
                                count = snapshot.child(date).getValue(Long.class);
                            }

                            callback.onCallback(count);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendLike(ConnectionUser sender, ConnectionUser receiver) {
        if (sender == null || receiver == null) {
            return;
        }

        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // don't have a connection

                        if (!snapshot.exists()
                                || (!snapshot.hasChild(sender.getUserId() + receiver.getUserId())
                                && !snapshot.hasChild(receiver.getUserId() + sender.getUserId()))) {
                            // create a connection
                            receiver.setIsLiked(true);

                            Log.d("CREATE A NEW CONNECTION FOR: ", receiver.getNickname() + ", " + sender.getNickname());
                            // create a new connection => not friend
                            try {
                                createNewConnection(sender, receiver, (connectionId) -> {
                                    Log.d("CONNECTION ID", connectionId);
                                    if (connectionId != null && connectionId.length() > 0) {
                                        String notification = sender.getNickname() + " liked you!";
                                        String message = String.format("[%s]", notification);
                                        try {
                                            // todo: send the message to the receiver
                                            addMessageInMessagesStore(sender, receiver, connectionId, message, false);
                                            // todo: send the notification to the receiver
                                            sendLikeMessageNotification(receiver, connectionId, notification);
                                            return;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    }
                                });
                            } catch (Exception e) {
                                Log.d("LIKE BUTTON", "CREATE NEW CONNECTION FAILED");
                            }
                            return;
                        }

                        // have a connection
                        String connectionId = "";
                        if (snapshot.hasChild(sender.getUserId() + receiver.getUserId())) {
                            // check if the receiver is liked
                            connectionId = sender.getUserId() + receiver.getUserId();
                        } else if (snapshot.hasChild(receiver.getUserId() + sender.getUserId())) {
                            connectionId = receiver.getUserId() + sender.getUserId();
                        }

                        Log.d("CONNECTION ID EXISTS", connectionId);

                        Connection connection = snapshot.child(connectionId).getValue(Connection.class);

                        if (connection != null) {
                            ConnectionUser storedReceiver;
                            ConnectionUser storedSender;
                            String receiverStoredPosition;

                            if (connection.getUser1() != null && receiver.getUserId().equals(connection.getUser1().getUserId())) {
                                storedReceiver = connection.getUser1();
                                storedSender = connection.getUser2();
                                receiverStoredPosition = Constants.CONNECTION_USER1;
                            } else if (connection.getUser2() != null && receiver.getUserId().equals(connection.getUser2().getUserId())) {
                                storedReceiver = connection.getUser2();
                                storedSender = connection.getUser1();
                                receiverStoredPosition = Constants.CONNECTION_USER2;
                            } else {
                                // this connection is wrong
                                // delete this connection and suggest user to try again
                                snapshot.child(connectionId).getRef().setValue(null);
                                // and throw the exception
                                return;
                            }

                            // check if the stored receiver is liked
                            if (storedReceiver.getIsLiked()) {
                                // no action need to take
                                return;
                            }

                            receiver.setIsLiked(true);

                            String message;
                            String notification;
                            boolean isFriend = false;
                            if (storedSender.getIsLiked()) {
                                message = "[" + sender.getNickname() + " liked you back!!]";
                                notification = sender.getNickname() + "liked you back!! You are matched now!";
                                // increase the connectionPoint
                                connection.setConnectionPoint(connection.getConnectionPoint() + 2);
                                isFriend = true;
                            } else {
                                message = "[" + sender.getNickname() + "liked you!]";
                                notification = sender.getNickname() + "liked you!";
                            }

                            // update the connection
                            snapshot.child(connectionId)
                                    .child(receiverStoredPosition)
                                    .getRef()
                                    .setValue(receiver);
                            snapshot.child(connectionId)
                                    .child(Constants.CONNECTION_POINT)
                                    .getRef()
                                    .setValue(connection.getConnectionPoint());

                            // if is: send the notification and message, increase the connectionPoint
                            try {
                                addMessageInMessagesStore(sender, receiver, connectionId, message, isFriend);
                                sendLikeMessageNotification(receiver, connectionId, notification);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void fetchConnectionId(String senderId, String receiverId, DataFetchCallback<String> callback) {
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onCallback("");
                        } else if (snapshot.hasChild(senderId + receiverId)) {
                            callback.onCallback(senderId + receiverId);
                        } else if (snapshot.hasChild(receiverId + senderId)) {
                            callback.onCallback(receiverId + senderId);
                        } else {
                            callback.onCallback("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback("");
                    }
                });
    }

    // listen the connectionId list of loginUser
    // according to the new connectionId
    // fetch the connection
    // if the connectionId is in the map:
        // update the conversation
        // otherwise put the conversation into the list and sort it
    public void listenToConnection(String connectionId, DataFetchCallback<Connection> callback) {
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild(connectionId)) {
                            snapshot.child(connectionId).getRef()
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Connection connection = snapshot.getValue(Connection.class);
                                            callback.onCallback(connection);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            callback.onCallback(null);
                                        }
                                    });
                        } else {
                            callback.onCallback(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback(null);
                    }
                });
    }

    public void fetchConnections(String userId, DataFetchCallback<List<String>> callback) {
        List<String> connectionIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .child(userId)
                .child(Constants.CONNECTIONS_STORE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
