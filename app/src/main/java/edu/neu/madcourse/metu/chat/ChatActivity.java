package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;

import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.Constants;

import edu.neu.madcourse.metu.utils.Utils;
import edu.neu.madcourse.metu.video.VideoActivity;

public class ChatActivity extends BaseCalleeActivity {
    private String userId;
    private User loginUser;

    private ConnectionUser receiver;

    private String connectionId = "";
    private String receiverFcmToken;
    // status of receiver
    private Boolean isReceiverAvailable = false;
    private Boolean isReceiverOnline = false;
    // relationship with receiver
    private long connectionPoint = 0;
    private boolean isFriend = false ;

    // UI components
    private ProgressBar progressBar;
    private FrameLayout sendButton;
    private EditText inputMessage;
    private AppCompatImageView backButton;

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

        // initialize recycler view and set layout manager
        chatHistory = findViewById(R.id.chatHistory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatHistory.setLayoutManager(linearLayoutManager);


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
                        switchVideoButtonStatus();
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

    private void loadUser() {
        this.loginUser = ((App) getApplication()).getLoginUser();
        // if the loginUser is null
        if (this.loginUser == null || loginUser.getUserId() == null || loginUser.getUserId().length() == 0) {
            Log.d("ACTIVITY", "USER HAS LOGGED OUT");
            finish();
            return;
        }

        this.userId = loginUser.getUserId();
        Log.d("ACTIVITY", "CHAT ACTIVITY: " + userId);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save current receiver
        outState.putParcelable("RECEIVER", this.receiver);
        // save connectionId
        outState.putString("CONNECTION_ID", this.connectionId);
        // save the connection point
        outState.putLong("CONNECTION_POINT", connectionPoint);
        // save if they are friends
        outState.putBoolean("IS_FRIEND", isFriend);
        // FCM token
        outState.putString("FCM_TOKEN", receiverFcmToken);
    }

    private void initFromBundle(Bundle savedInstanceState) {
        this.receiver = savedInstanceState.getParcelable("RECEIVER");
        // retrieve the connectionId
        this.connectionId = savedInstanceState.getString("CONNECTION_ID", "");
        // connection point
        this.connectionPoint = savedInstanceState.getLong("CONNECTION_POINT", 0);
        // get if they are friends
        this.isFriend = savedInstanceState.getBoolean("IS_FRIEND", false);
        // FCM token
        this.receiverFcmToken = savedInstanceState.getString("FCM_TOKEN", "");
    }

    private void initFromIntent() {
        // get data from intent
        Bundle extras = getIntent().getExtras();
        // get current receiver
        this.receiver = (ConnectionUser) extras.getParcelable("RECEIVER");
        // get the connectionId
        this.connectionId = extras.getString("CONNECTION_ID", "");
    }



    private void init(Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("RECEIVER")) {
            // retrieve from savedInstanceState
            initFromBundle(savedInstanceState);

            receiverName.setText(receiver.getNickname());
        } else {
            initFromIntent();
        }

        // subscribe the user
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<String> set = new HashSet<>();
                set.add(receiver.getUserId());
                ((App)getApplication()).rtmSubscribePeer(set);
                Log.d("RMT", "user subscribe: " + receiver.getUserId());
            }
        }).start();
        // set the receiver's nickname
        receiverName.setText(receiver.getNickname());
        // initialize the recycler view
        initRecyclerView();

        if (connectionId != null && connectionId.length() > 0) {
            fetchData();
        } else {
            progressBar.setVisibility(View.GONE);
        }



    }

    private void initRecyclerView() {
        // initialize and set the adapter
        if (chatHistoryAdapter == null) {
            chatHistoryAdapter = new ChatHistoryAdapter(this, this.chatItemList, this.userId, this.connectionId, null, receiver);
        }

        // initialize the avatar
        if (receiver.getAvatarUri() != null && receiver.getAvatarUri().length() > 0) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Bitmap avatar = BitmapUtils.getBitmapFromUri(receiver.getAvatarUri());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                        chatHistoryAdapter.notifyItemChanged(databaseList().length - 1);
                        //chatHistory.setAdapter(null);
                        //chatHistory.setAdapter(chatHistoryAdapter);
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
        // receiver name to profile
        // add click listener for card
        receiverName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                intent.putExtra("PROFILE_USER_ID", receiver.getUserId());
                intent.putExtra("CONNECTION_POINT", connectionPoint);
                intent.putExtra("CONNECTION_ID", connectionId == null? "":connectionId);

                startActivity(intent);
            }
        });


        // back button
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // send message button
        sendButton.setOnClickListener(v -> sendMessage());

        // start video chat button
        startVideoChatButton.setOnClickListener(v -> {
            // check
            if (receiver == null) {
                return;
            }

            if (!isFriend) {
              showToast("You and " + receiver.getNickname() + " haven't been friends yet!");
            } else if (!isReceiverOnline) {
              showToast(receiver.getNickname() + " is offline");
            } else {
                // open video activity
                startVideoChat();
            }

        });
    }

    private void startVideoChat() {
        // todo: check permission
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("CALLEE_ID", receiver.getUserId());
        intent.putExtra("CALLEE_NAME", receiver.getNickname());
        intent.putExtra("CALLEE_AVATAR", receiver.getAvatarUri());
        intent.putExtra("CONNECTION_POINT", connectionPoint);
        intent.putExtra("CONNECTION_ID", connectionId);

        startActivity(intent);

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
                        listenToConnection();
                    } else {
                        Toast.makeText(this, "Something went wrong! Please check the internet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }

            // send the message and update the connectionPoint
            MessageSendingUtils
                    .addMessageInMessagesStore(sender, receiver, connectionId, message, isFriend);

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
            showToast("Something went wrong! Please check the internet");
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
        listenReceiverToken();
        // through connection store
        listenToConnection();
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
                switchVideoButtonStatus();
            });
        }
    }

    private void switchReceiverStatus() {
        // see if the user is available
        if (!isReceiverOnline) {
            // set the status to be not-online
            onlineStatus.setVisibility(View.INVISIBLE);
            // onlineStatus.setImageResource(R.drawable.ic_not_online);
            return;
        }

        if (isReceiverAvailable) {
            // set the status to be available
            onlineStatus.setImageResource(R.drawable.ic_available_status);
        } else {
            // set the status to be step-away
            onlineStatus.setImageResource(R.drawable.ic_step_away_status);
        }
        onlineStatus.setVisibility(View.VISIBLE);
    }

    private void switchVideoButtonStatus() {
        if (isReceiverOnline && isFriend) {
            startVideoChatButton.setClickable(true);
            startVideoChatButton.setImageResource(R.drawable.ic_start_chat);
        } else {
            startVideoChatButton.setClickable(false);
            startVideoChatButton.setImageResource(R.drawable.ic_video_chat_disabled);
        }
    }

    // listen to the connection point
    private void listenToConnection() {
        if (connectionId == null || connectionId.length() == 0) {
            return;
        }

        try {
            FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists() && snapshot.hasChild(connectionId)) {
                                // add listener
                                snapshot.child(connectionId).getRef().addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Connection connection = snapshot.getValue(Connection.class);

                                        if (connection != null) {
                                            connectionPoint = connection.getConnectionPoint();
                                        }

                                        if (connection.getUser1() != null && connection.getUser2() != null) {
                                            isFriend = connection.getUser1().getIsLiked() && connection.getUser2().getIsLiked();
                                        }

                                        // update the video button status
                                        switchVideoButtonStatus();


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
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
