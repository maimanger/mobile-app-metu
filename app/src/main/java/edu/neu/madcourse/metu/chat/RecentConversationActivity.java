package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import edu.neu.madcourse.metu.App;

import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.SettingActivity;
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.models.ChatItem;

import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.chat.daos.RecentConversation;
import edu.neu.madcourse.metu.models.ConnectionUser;

import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.utils.Constants;


public class RecentConversationActivity extends BaseCalleeActivity {
    private String userId;
    private long countContacts;
    private Map<String, RecentConversation> usernameToConversation;

    // UI components
    private ProgressBar progressBar;
    private TextView noRecentChatsTextview;

    // recycler view
    private RecyclerView recentConversation;
    private List<RecentConversation> conversationList;
    private RecentConversationAdapter recentConversationAdapter;

    // for multi threading
    private ExecutorService executorService;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_conversation);

        // initialize the UI components
        progressBar = findViewById(R.id.progressBarRecentConversation);
        // showing the progress bar
        progressBar.setVisibility(View.VISIBLE);
        // initialize recycler view and set layout manager
        recentConversation = findViewById(R.id.recentConversationRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recentConversation.setLayoutManager(linearLayoutManager);
        // textview
        noRecentChatsTextview = findViewById(R.id.noRecentConversation);

        // setting
        ImageView setting = findViewById(R.id.button_conversation_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecentConversationActivity.this, SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // load username
        loadUser();

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(10);

        // initialize the recent conversation list
        conversationList = new ArrayList<>();
        usernameToConversation = new HashMap<>();

        // todo: initialize the recycler view
        // initRecyclerView();

        // initialize data view
        init(savedInstanceState);

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Chats");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setSelectedItemId(R.id.menu_chats);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.menu_explore:
                        startActivity(new Intent(getApplicationContext(), ExploringActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_contacts:
                        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_chats:
                        return true;
                    case R.id.menu_me:
                        startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // get the size
        int size = this.conversationList == null? 0:this.conversationList.size();
        // save the size
        outState.putInt("SIZE", size);

        // save the conversations
        // todo: parcelable
        for (int i = 0; i < size; i++) {
            outState.putParcelable("CONVERSATION" + i, this.conversationList.get(i));
        }
    }

    private void initRecyclerView() {
        // initialize and set the adapter
        if (recentConversationAdapter == null) {
            recentConversationAdapter = new RecentConversationAdapter(this, this.conversationList, userId);
        }
        recentConversation.setAdapter(recentConversationAdapter);
        recentConversation.setVisibility(View.VISIBLE);
        renderView();
    }

    private void renderView() {
        MessageSendingUtils.countConnections(userId, new DataFetchCallback<Long>() {
            @Override
            public void onCallback(Long value) {
                if (value == 0 || value == null) {
                    noRecentChatsTextview.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    noRecentChatsTextview.setVisibility(View.GONE);
                }
            }
        });
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            // retrieve from savedInstanceState
            int size = savedInstanceState.getInt("SIZE");
            RecentConversation conversation;
            for (int i = 0; i < size; i++) {
                conversation = (RecentConversation) savedInstanceState.getParcelable("CONVERSATION" + i);
                this.conversationList.add(conversation);
                this.usernameToConversation.put(conversation.getRecentContact().getUserId(), conversation);
            }

            // dismiss the progress bar
             progressBar.setVisibility(View.GONE);
        }

        addConnectionListener();
        initRecyclerView();
    }

    public void addConnectionListener() {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .child(userId)
                .child(Constants.USER_CONNECTIONS)
                .addChildEventListener(new ConnectionListener());
    }

    private void loadUser() {
        // todo: load from App
        User loginUser = ((App) getApplicationContext()).getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getUserId().length() == 0) {
            Log.d("ACTIVITY", "USER HAS LOGGED OUT");
            finish();
            return;
        }
        this.userId = loginUser.getUserId();
        Log.d("ACTIVITY", "RECENT CONVERSATION ACTIVITY: " + userId);
        // todo: delete
        Toast.makeText(getApplicationContext(), this.userId + " logs in", Toast.LENGTH_SHORT).show();
    }

    // listener
    // each child is a connectionId of a specific user
    // 1. fetch the recent conversations
    // 2. listen to the connection
    // 3. update the conversation
    public class ConnectionListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            countContacts++;

            if (snapshot.exists()) {
                // fetch the connectionId
                String connectionId = snapshot.getKey();
                // todo: delete
                System.out.println("Connection id: " + connectionId);

                // fetch it from the Connections store
                FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                        .orderByKey()
                        .equalTo(connectionId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    for (DataSnapshot connectionData: snapshot.getChildren()) {
                                        // check if the info is correct
                                        ConnectionUser user1 = connectionData.child(Constants.CONNECTION_USER1).getValue(ConnectionUser.class);
                                        ConnectionUser user2 = connectionData.child(Constants.CONNECTION_USER2).getValue(ConnectionUser.class);
                                        ChatItem chatItem = connectionData.child(Constants.CONNECTION_LAST_MESSAGE).getValue(ChatItem.class);
                                        ConnectionUser contact;
                                        RecentConversation conversation;

                                        if (user1 != null && user1.getUserId().equals(userId)) {
                                            // user2 is the contact
                                            contact = user2;
                                        } else if (user2 != null && user2.getUserId().equals(userId)) {
                                            // user1 is the contact
                                            contact = user1;
                                        } else {
                                            return;
                                        }

                                        // if never chat before
                                        if (chatItem == null) {
                                            return;
                                        }

                                        // todo: delete
                                        System.out.println("the receiver id: " + contact.getUserId());
                                        if (usernameToConversation.containsKey(contact.getUserId())) {
                                            System.out.println("it is in the map");
                                            conversation = usernameToConversation.get(contact.getUserId());
                                        } else {
                                            conversation = new RecentConversation();
                                            conversation.setConnectionId(connectionId);

                                            // add into the list
                                            conversationList.add(conversation);
                                            usernameToConversation.put(contact.getUserId(), conversation);
                                        }

                                        conversation.setLastMessage(chatItem);
                                        conversation.setRecentContact(contact);

                                        // sort
                                        conversationList.sort((conversation1, conversation2)
                                                -> conversation1.getLastMessage().getTimeStamp() > conversation2.getLastMessage().getTimeStamp()? -1:1);

                                        recentConversation.setAdapter(null);
                                        recentConversation.setAdapter(recentConversationAdapter);

                                        // todo: dismiss the progress bar
                                         progressBar.setVisibility(View.GONE);
                                         noRecentChatsTextview.setVisibility(View.GONE);

                                        return;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_chats);
        Log.d("Recent Conversation", "onResume");
        if (recentConversationAdapter != null && recentConversation != null) {
            recentConversation.setAdapter(recentConversationAdapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Recent Conversation", "onStop");
        recentConversation.setAdapter(null);
    }
}
