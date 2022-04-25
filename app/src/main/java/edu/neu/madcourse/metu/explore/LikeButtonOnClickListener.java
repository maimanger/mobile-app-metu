package edu.neu.madcourse.metu.explore;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.MessagingService;

public class LikeButtonOnClickListener implements View.OnClickListener {

    private Button button;
    private RecommendedUser recommendedUser;
    private User loginUser;

    public LikeButtonOnClickListener(Button button, RecommendedUser recommendedUser, User loginUser) {
        this.recommendedUser = recommendedUser;
        this.button = button;
        this.loginUser = loginUser;
    }

    public void sendNotification(String receiverId, String message) {
        // fetch the token
        FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                .child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String receiverFcmToken = snapshot.getValue(String.class);

                            try {
                                JSONArray tokens = new JSONArray();
                                tokens.put(receiverFcmToken);

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

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onClick(View view) {
        // if the user is not liked
        if (!recommendedUser.getIsLiked()) {
            // set the user to be liked
            recommendedUser.setIsLiked(true);
            // set the button to be liked
            button.setBackgroundResource(R.drawable.ic_like);
            // remove its listener
            button.setOnClickListener(null);
            button.setClickable(false);
            // create a connection if there was no connection between them
            FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                if (dataSnapshot.getKey().equals(loginUser.getUserId() + recommendedUser.getUserId())
                                        || dataSnapshot.getKey().equals(recommendedUser.getUserId() + loginUser.getUserId())) {
                                    // exists a connection between them already
                                    String connectionId = dataSnapshot.getKey();
                                    ConnectionUser user1 = dataSnapshot.child(Constants.CONNECTION_USER1).getValue(ConnectionUser.class);
                                    ConnectionUser user2 = dataSnapshot.child(Constants.CONNECTION_USER2).getValue(ConnectionUser.class);

                                    // update the connection
                                    if (user1 != null && user1.getUserId().equals(recommendedUser.getUserId())) {
                                        user1.setIsLiked(true);
                                        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                                                .child(connectionId)
                                                .child(Constants.CONNECTION_USER1)
                                                .setValue(user1);
                                    } else if (user2 != null && user2.getUserId().equals(recommendedUser.getUserId())) {
                                        user2.setIsLiked(true);
                                        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                                                .child(connectionId)
                                                .child(Constants.CONNECTION_USER2)
                                                .setValue(user2);
                                    }

                                    // send the message and notification automatically
                                    ChatItem chatItem = new ChatItem();
                                    chatItem.setIsRead(false);
                                    chatItem.setSender(loginUser.getUserId());
                                    chatItem.setTimeStamp(System.currentTimeMillis());
                                    chatItem.setMessage(String.format("[%s likes you!]", loginUser.getNickname()));

                                    FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                                            .child(connectionId)
                                            .child(Constants.CONNECTION_LAST_MESSAGE)
                                            .setValue(chatItem);

                                    // save it into messages store
                                    FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE)
                                            .child(connectionId)
                                            .push()
                                            .setValue(chatItem);

                                    // send notification
                                    sendNotification(recommendedUser.getUserId()
                                            , String.format("[%s likes you!]", loginUser.getNickname()));

                                    return;
                                }
                            }
                            // create a new connection
                            // the current user
                            ConnectionUser user1 = new ConnectionUser();
                            user1.setUserId(loginUser.getUserId());
                            user1.setNickname(loginUser.getNickname());
                            user1.setAvatarUri(loginUser.getAvatarUri());
                            user1.setIsLiked(false);
                            // the recommend user
                            ConnectionUser user2 = new ConnectionUser();
                            user2.setUserId(recommendedUser.getUserId());
                            user2.setNickname(recommendedUser.getNickname());
                            user2.setAvatarUri(recommendedUser.getAvatarUri());
                            user2.setIsLiked(true);

                            // todo: automatically send a new massage
                            ChatItem chatItem = new ChatItem();
                            chatItem.setIsRead(false);
                            chatItem.setSender(user1.getUserId());
                            chatItem.setTimeStamp(System.currentTimeMillis());
                            chatItem.setMessage(String.format("[%s likes you!]", loginUser.getNickname()));

                            // todo: connection point starts from 0?
                            String connectionId = user1.getUserId() + user2.getUserId();
                            Connection connection = new Connection(user1, user2, 0, chatItem);

                            // save the massage
                            FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE)
                                    .child(connectionId)
                                    .push()
                                    .setValue(chatItem);

                            // push to connections store
                            FirebaseDatabase.getInstance().getReference(Constants.USER_CONNECTIONS)
                                    .child(connectionId)
                                    .setValue(connection)
                                    .addOnSuccessListener(unused -> {
                                        // push to both users' connections
                                        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                                                .child(user1.getUserId())
                                                .child(Constants.USER_CONNECTIONS)
                                                .child(connectionId).setValue(true);

                                        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                                                .child(user2.getUserId())
                                                .child(Constants.USER_CONNECTIONS)
                                                .child(connectionId).setValue(true);
                                    });

                            // send notification to the person
                            // fetch the token
                            FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                                    .child(user2.getUserId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                String receiverFcmToken = snapshot.getValue(String.class);

                                                try {
                                                    JSONArray tokens = new JSONArray();
                                                    tokens.put(receiverFcmToken);

                                                    // data to put
                                                    JSONObject data = new JSONObject();
                                                    // put the notification type
                                                    data.put(Constants.NOTIFICATION_TYPE, Constants.NOTIFY_GET_A_LIKE);
                                                    // put the info for current user
                                                    data.put(Constants.MESSAGE_CONTENT, String.format("[%s likes you]", loginUser.getNickname()));

                                                    JSONObject body = new JSONObject();
                                                    body.put(Constants.REMOTE_MSG_DATA, data);
                                                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                                    MessagingService.sendNotification(body.toString());
                                                } catch (Exception e) {
                                                    Log.d("FCM", e.getMessage());
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }}
