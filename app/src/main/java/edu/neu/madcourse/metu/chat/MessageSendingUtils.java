package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.MessagingService;
import edu.neu.madcourse.metu.utils.MetUException;

public class MessageSendingUtils {

    // create a new connection
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
    public static void addMessageInMessagesStore(ConnectionUser sender, ConnectionUser receiver, String connectionId, String message) throws Exception {
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

                // fetch the message count for today
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
}
