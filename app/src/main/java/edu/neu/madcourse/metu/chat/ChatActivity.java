package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import edu.neu.madcourse.metu.App;

import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;

import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.ConnectionUser;

import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.Constants;

import edu.neu.madcourse.metu.utils.Utils;
import edu.neu.madcourse.metu.utils.network.ApiClient;
import edu.neu.madcourse.metu.utils.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseCalleeActivity {
    private String userId;
    private User loginUser;

    private ConnectionUser receiver;

    // todo: check if the connectionId is null
    private String connectionId = "";
    private Boolean isReceiverAvailable = false;
    private Boolean isReceiverOnline = false;
    private long connectionPoint = 0;
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

    private void listenAvailabilityOfReceiver() {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                .child(receiver.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isReceiverAvailable = snapshot.getValue(Boolean.class);
                        }

                        switchReceiverStatus();
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
        this.loginUser = ((App) getApplication()).getLoginUser();
        // if the loginUser is null
        if (this.loginUser == null || loginUser.getUserId() == null || loginUser.getUserId().length() == 0) {
            Log.d("ACTIVITY", "USER HAS LOGGED OUT");
            finish();
            return;
        }

        this.userId = loginUser.getUserId();
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
            // todo: save status
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
            // todo: subscribe the user
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // subscribe the receiver
                    HashSet<String> set = new HashSet<>();
                    set.add(receiver.getUserId());
                    ((App)getApplication()).rtmSubscribePeer(set);
                    Log.d("RMT", "user subscribe: " + receiver.getUserId());
                }
            }).start();
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
                progressBar.setVisibility(View.GONE);
            }

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
        if (receiver.getAvatarUri() != null && receiver.getAvatarUri().length() > 0) {
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
            chatHistory.setAdapter(chatHistoryAdapter);
        }

    }

    private void fetchData() {

        // for dismissing the progress bar
        // todo: dismiss the progress bar inside of adapter
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

        // listen to messages under this connection
        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_STORE).child(this.connectionId)
                .orderByChild(Constants.MESSAGE_TIMESTAMP)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    // todo: delete
                    System.out.println(snapshot.getKey());
                    try {
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

                    } catch (Exception e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                    }

                }

                progressBar.setVisibility(View.GONE);


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
            onBackPressed();
        });

        // send button
        // todo: which is better
        sendButton.setOnClickListener(v -> sendMessage());

        // todo: start video chat button
        startVideoChatButton.setOnClickListener(v -> {
            // check
            if (receiver == null) {
                return;
            }
            if (!isReceiverOnline) {
                Toast.makeText(v.getContext(), receiver.getNickname() + "is not online", Toast.LENGTH_SHORT).show();
            } else if (Utils.calculateFriendLevel((int)connectionPoint) < 1) {
                Toast.makeText(v.getContext(), "Your connection level is not getting there yet! " + Utils.calculateFriendLevel((int)connectionPoint), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String inputText = inputMessage.getText().toString();
        // trim the white space
        String message = inputText.trim();

        if (message == null || message.length() == 0) {
            return;
        }

        // prepare for the sender
        ConnectionUser sender = new ConnectionUser();
        sender.setUserId(userId);
        sender.setIsLiked(false);
        sender.setAvatarUri(loginUser.getAvatarUri());
        sender.setNickname(loginUser.getNickname());

        try {
            // create a new user
            if (connectionId == null || connectionId.length() == 0) {
                MessageSendingUtils.createNewConnection(sender, receiver, (newConnectionId) -> {
                    // set the connectionId
                    if (newConnectionId != null) {
                        connectionId = newConnectionId;
                        // update the connectionId in the adapter
                        chatHistoryAdapter.updateConnectionId(connectionId);

                        // add the listener
                        fetchData();
                        listenToConnectionPointChange();
                    } else {
                        Toast.makeText(this, "Something went wrong! Please check the internet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }

            // send the message and update the connectionPoint
            MessageSendingUtils
                    .addMessageInMessagesStore(sender, receiver, connectionId, message);

            // send notification if the receiver is not active
            if (!isReceiverAvailable) {
                MessageSendingUtils
                        .sendNewMessageNotification(sender, connectionId, receiverFcmToken, message);
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

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong! Please check the internet", Toast.LENGTH_SHORT).show();
        }

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
        // through connection store
        listenToConnectionPointChange();
    }

    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
        super.onPeersOnlineStatusChanged(map);

        boolean changed = false;
        synchronized (this) {
            if (receiver != null && map.containsKey(receiver.getUserId())) {
                isReceiverOnline = map.get(receiver.getUserId()) == 0;
                changed = true;
            }
        }
        if (changed) {
            runOnUiThread(() -> {
                switchReceiverStatus();
                Toast.makeText(getApplicationContext(), "status changed: " + isReceiverOnline, Toast.LENGTH_SHORT).show();
            });
        }
    }

    // todo: check video button
    private void switchReceiverStatus() {
        // see if the user is available
        if (!isReceiverOnline) {
            // set the status to be not-online
            onlineStatus.setImageResource(R.drawable.ic_not_online);
            // ban the video button
            startVideoChatButton.setImageResource(R.drawable.ic_video_chat_disabled);
            return;
        }
        if (isReceiverAvailable) {
            // set the status to be available
            onlineStatus.setImageResource(R.drawable.ic_available_status);
            // check the connectionPoint
        } else {
            // set the status to be step-away
            onlineStatus.setImageResource(R.drawable.ic_step_away_status);
            // check the connectionPoint
        }
    }

    private void listenToConnectionPointChange() {
        if (connectionId == null || connectionId.length() == 0) {
            return;
        }

        try {
            FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                    .child(connectionId)
                    .child(Constants.CONNECTION_POINT)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                connectionPoint = snapshot.getValue(long.class);
                            } else {
                                connectionPoint = 0;
                            }

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
