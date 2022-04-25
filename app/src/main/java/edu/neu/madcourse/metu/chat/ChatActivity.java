package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;

import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;

import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;
import edu.neu.madcourse.metu.utils.network.ApiClient;
import edu.neu.madcourse.metu.utils.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private String userId;
    private User loginUser;

    private ConnectionUser receiver;

    // todo: check if the connectionId is null
    private String connectionId = "";
    private Boolean isReceiverAvailable = false;
    private String receiverFcmToken;

    // UI components
    private ProgressBar progressBar;
    private FrameLayout sendButton;
    private EditText inputMessage;
    private AppCompatImageView backButton;

    // todo: connect with video
    private AppCompatImageView startVideoChatButton;
    private TextView receiverName;
    private ImageView onlineStatus;

    // recycler view
    private RecyclerView chatHistory;
    private List<ChatItem> chatItemList;
    private ChatHistoryAdapter chatHistoryAdapter;

    // for multi threading
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);

        // initialize UI components
        progressBar = findViewById(R.id.progressBarChatActivity);
        // set the progress bar to be visible
        progressBar.setVisibility(View.VISIBLE);

        sendButton = findViewById(R.id.layoutSend);
        inputMessage = findViewById(R.id.inputMessage);
        backButton = findViewById(R.id.imageBack);
        startVideoChatButton = findViewById(R.id.startVideoChatWithContact);
        receiverName = findViewById(R.id.contactName);
        onlineStatus = findViewById(R.id.onlineStatus);

        // init the current username
        loadUser();

        // initialize chat item list
        chatItemList = new ArrayList<>();

        // set listeners
        setListeners();

        // init data view
        init(savedInstanceState);

    }

    // todo: listen the receiver info changes

    private void listenAvailabilityOfReceiver() {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                .child(receiver.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isReceiverAvailable = snapshot.getValue(Boolean.class);
                        }

                        // todo: delete
                        System.out.println(receiver.getUserId() + ": " + isReceiverAvailable);

                        // todo: change to isOnline
                        if (isReceiverAvailable) {
                            startVideoChatButton.setImageResource(R.drawable.ic_start_chat);
                            onlineStatus.setImageResource(R.drawable.ic_available_status);
                        } else {
                            startVideoChatButton.setImageResource(R.drawable.ic_video_chat_disabled);
                            onlineStatus.setImageResource(R.drawable.ic_step_away_status);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void listenReceiverToken() {
        FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                .child(receiver.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // update token
                            receiverFcmToken = snapshot.getValue(String.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull  Call<String> call, @NonNull  Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (Exception e) {
                        // todo: log it maybe
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                } else {
                    // todo: remove it maybe?
                    showToast("error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }


    /**
     * load the username of current user.
     */
    private void loadUser() {
        // todo: update with auth
        // todo: check if the user is logged in - if not return to the sign in activcity
        this.userId = ((App) getApplication()).getUserId();
        this.loginUser = ((App) getApplication()).getLoginUser();

        Log.d("ACTIVITY", "CHAT ACTIVITY: " + userId);
        Toast.makeText(getApplicationContext(), userId + " is the current user", Toast.LENGTH_SHORT).show();
    }

    /**
     * retrieve the current username, receiver and chat history.
     * @param savedInstanceState
     */
    private void init(Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            // retrieve from savedInstanceState
            // retrieve the current receiver
            this.receiver = savedInstanceState.getParcelable("RECEIVER");

            // retrieve the connectionId
            this.connectionId = savedInstanceState.getString("CONNECTION_ID");

            receiverName.setText(receiver.getNickname());
            // get the size of chat items
            int size = savedInstanceState.getInt("SIZE");
            // retrieve the chat items
            ChatItem chatItem;

            for (int i = 0; i < size; i++) {
                chatItem = (ChatItem) savedInstanceState.getParcelable("CHAT_ITEM" + i);
                this.chatItemList.add(chatItem);
            }

            initRecyclerView();
            // dismiss the progress bar
            progressBar.setVisibility(View.GONE);

        } else {
            // get data from intent
            Bundle extras = getIntent().getExtras();
            // get current receiver
            this.receiver = (ConnectionUser) extras.getParcelable("RECEIVER");

            // todo: delete
            Toast.makeText(getApplicationContext(), receiver.getUserId() + " is the receiver", Toast.LENGTH_SHORT).show();
            // get the connectionId
            this.connectionId = extras.getString("CONNECTION_ID");
            // set the receiver's nickname
            receiverName.setText(receiver.getNickname());
            // initialize the recycler view
            initRecyclerView();

            // todo: delete
            System.out.println("connectionid is: " + connectionId);

            if (connectionId != null && connectionId.length() > 0) {
                fetchData();
            } else {
                // todo: delete
                System.out.println("dismiss the progress bar");
                progressBar.setVisibility(View.GONE);
            }
//            executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    // onPreExecute
//                    // doInBackground
//                    fetchData();
//
//                    // onPostExecute
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // dismiss the progress bar
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    });
//                }
//            });

        }

    }

    private void initRecyclerView() {
        // initialize recycler view and set layout manager
        chatHistory = findViewById(R.id.chatHistory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatHistory.setLayoutManager(linearLayoutManager);

        // initialize and set the adapter
        chatHistoryAdapter = new ChatHistoryAdapter(this, this.chatItemList, this.userId, this.connectionId, null, receiver);

        // initialize the avatar
        if (receiver.getAvatarUri() != null) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Bitmap avatar = BitmapUtils.getBitmapFromUri(receiver.getAvatarUri());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // todo: delete
                            System.out.println(avatar);
                            chatHistoryAdapter.updateReceiverAvatar(avatar);
                            chatHistory.setAdapter(chatHistoryAdapter);
                        }
                    });

                }
            });

        } else {
            // todo: set to be default avatar
            Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_avatar);
            chatHistory.setAdapter(chatHistoryAdapter);
        }

    }

    /**
     * fetch chat history from database.
     */
    private void fetchData() {

        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE).child(this.connectionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        if (count > 0) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    chatHistory.getLayoutManager().scrollToPosition(chatItemList.size() - 1);
                                }
                            }, 200);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE).child(this.connectionId)
                .orderByChild(Constants.MESSAGE_TIMESTAMP)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);

                    // if the message is sent by the contact
                    // set the chatItem to be read
                    if (!chatItem.getSender().equals(userId) && !chatItem.getIsRead()) {
                        chatItem.setIsRead(true);
                        // update it to the database
                        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE)
                                .child(connectionId)
                                .child(snapshot.getKey())
                                .child(Constants.MESSAGE_IS_READ)
                                .setValue(true);
                    }
                    // add new chat Item into the list
                    chatItemList.add(chatItem);

                    // notify dataset changed
                    chatHistoryAdapter.notifyDataSetChanged();

                    // dismiss the progress bar
                    progressBar.setVisibility(View.GONE);

                    // todo: locate to the last position


                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setListeners() {
        // back button
        backButton.setOnClickListener(v -> {
            // go back to recent conversation
//            Intent intent = new Intent(this, RecentConversationActivity.class);
//            startActivity(intent);
            onBackPressed();
        });

        // send button
        sendButton.setOnClickListener(v -> sendMessage());

        // todo: start video chat butttom
    }

    private void sendMessage() {
        String inputText = inputMessage.getText().toString();
        // trim the white space
        inputText = inputText.trim();
        if (inputText != null && inputText.length() > 0) {
            ChatItem message = new ChatItem();
            message.setSender(this.userId);
            message.setTimeStamp(System.currentTimeMillis());
            message.setMessage(inputText);
            message.setIsRead(false);
            // check if the connectionId is empty
            if (connectionId == null || connectionId.length() == 0) {
                // create a new connection
                connectionId = userId + receiver.getUserId();

                // save the connection in connections store
                ConnectionUser user1 = new ConnectionUser();
                user1.setUserId(userId);
                user1.setIsLiked(false);
                user1.setAvatarUri(loginUser.getAvatarUri());
                user1.setNickname(loginUser.getNickname());

                Connection connection = new Connection(user1, receiver, 0, null);
                FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                        .child(connectionId)
                        .setValue(connection);
                // save the connection in both ppl
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .child(userId)
                        .child(Constants.USER_CONNECTIONS)
                        .child(connectionId).setValue(true);
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .child(receiver.getUserId())
                        .child(Constants.USER_CONNECTIONS)
                        .child(connectionId).setValue(true);

                // update the connectionId in the adapter
                chatHistoryAdapter.updateConnectionId(connectionId);

                // add the listener
                fetchData();
            }

            // send the message through firebase
            FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE)
                    .child(connectionId)
                    .push()
                    .setValue(message).addOnSuccessListener(unused -> {
                        // update the lastMessage in the current Connection
                FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                        .child(connectionId)
                        .child(Constants.CONNECTION_LAST_MESSAGE)
                        .setValue(message);
                // todo: cloud messaging
                // send notification
                if (!isReceiverAvailable) {
                    try {
                        JSONArray tokens = new JSONArray();
                        tokens.put(receiverFcmToken);

                        // data to put
                        JSONObject data = new JSONObject();
                        // put the notification type
                        data.put(Constants.NOTIFICATION_TYPE, Constants.NOTIFY_NEW_MSG);
                        // put the info for current user
                        data.put(Constants.MSG_SENDER_USER_ID, userId);
                        data.put(Constants.MSG_SENDER_NICKNAME, loginUser.getNickname());
                        data.put(Constants.MSG_SENDER_AVATAR_URI, loginUser.getAvatarUri());
                        data.put(Constants.MESSAGE_CONTENT, message.getMessage());
                        // put connection id
                        data.put(Constants.CONNECTION_ID, connectionId);

                        JSONObject body = new JSONObject();
                        body.put(Constants.REMOTE_MSG_DATA, data);
                        body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                        sendNotification(body.toString());
                    } catch (Exception e) {
                        // todo: delete
                        showToast(e.getMessage());
                    }
                }


            }).addOnFailureListener(e -> {
                // fail to send message
                Toast.makeText(getApplicationContext(), "Fail to send the message. Please check the internet connection", Toast.LENGTH_SHORT);
            });

        }
        // set the text back to null
        this.inputMessage.setText(null);

        // scroll to the bottom
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chatHistory.getLayoutManager().scrollToPosition(chatItemList.size() - 1);
            }
        }, 200);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // get the size of current chat history
        int size = this.chatItemList == null ? 0 : this.chatItemList.size();
        // save the size
        outState.putInt("SIZE", size);
        // save chat items
        for (int i = 0; i < size; i++) {
            outState.putParcelable("CHAT_ITEM" + i, this.chatItemList.get(i));
        }
        // save current receiver
        outState.putParcelable("RECEIVER", this.receiver);
        // save connectionId
        outState.putString("CONNECTION_ID", this.connectionId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
        listenReceiverToken();
    }
}