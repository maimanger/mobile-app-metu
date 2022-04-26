package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.models.ChatItem;

import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.chat.daos.RecentConversation;
import edu.neu.madcourse.metu.models.ConnectionUser;

import edu.neu.madcourse.metu.utils.Constants;


public class RecentConversationActivity extends BaseCalleeActivity {
    private String userId;
    private long countContacts;
    private Map<String, RecentConversation> usernameToConversation;


    // UI components
    private ProgressBar progressBar;

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
        // todo: delete
        System.out.println("showing the progressive bar");
        progressBar.setVisibility(View.VISIBLE);

        // load username
        loadUsername();

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(10);

        // initialize the recent conversation list
        conversationList = new ArrayList<>();
        usernameToConversation = new HashMap<>();

        // initialize the recycler view
        initRecyclerView();

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
        // initialize recycler view and set layout manager
        recentConversation = findViewById(R.id.recentConversationRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recentConversation.setLayoutManager(linearLayoutManager);

        // initialize and set the adapter
        recentConversationAdapter = new RecentConversationAdapter(this, this.conversationList, userId);
        recentConversation.setAdapter(recentConversationAdapter);
    }

    private void fetchData() {
        // for dismissing the progress bar
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // dismiss the progress bar

                if (countContacts == 0) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // fetch all the Connections
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                ConnectionUser user1 = snapshot.child(Constants.CONNECTION_USER1).getValue(ConnectionUser.class);
                ConnectionUser user2 = snapshot.child(Constants.CONNECTION_USER2).getValue(ConnectionUser.class);

                ConnectionUser contact;

                String contactUsername;
                RecentConversation recentConversation;
                String connectionId;

                // check if the connection belongs to the current user
                if (userId.equals(user1.getUserId())) {
                    contact = user2;
                    contactUsername = user2.getUserId();
                } else if (userId.equals(user2.getUserId())) {
                    contact = user1;
                    contactUsername = user1.getUserId();
                } else {
                    // not related to this user
                    return;
                }

                // query the connectionId
                connectionId = snapshot.getKey();
                recentConversation = new RecentConversation();

                countContacts++;

                // todo: set listener to listen changes related to this Connection
                FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                        .child(connectionId)
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                if (snapshot.getKey().equals(Constants.CONNECTION_LAST_MESSAGE) && !usernameToConversation.containsKey(contactUsername)) {
                                    // new conversation
                                    // fetch the chatItem
                                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                                    // fetch the user
                                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                                            .orderByChild(Constants.USER_USER_ID)
                                            .equalTo(contactUsername)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                                            // get and set nickname
                                                            String nickname = dataSnapshot.child(Constants.USER_NICKNAME).getValue(String.class);

                                                            // get and set avatar
                                                            String avatarUri = dataSnapshot.child(Constants.USER_AVATAR_URI).getValue(String.class);

                                                            // todo: change to the real avatar url
                                                            contact.setAvatarUri(avatarUri);

                                                            contact.setNickname(nickname);

                                                            // set the contact
                                                            recentConversation.setRecentContact(contact);
                                                            // set the chatItem
                                                            recentConversation.setLastMessage(chatItem);
                                                            // set the connectionId
                                                            recentConversation.setConnectionId(connectionId);

                                                            // add the recent conversation
                                                            conversationList.add(recentConversation);
                                                            usernameToConversation.put(contact.getUserId(), recentConversation);

                                                            // sort the list by time
                                                            conversationList.sort((conversation1, conversation2)
                                                                    -> conversation1.getLastMessage().getTimeStamp() > conversation2.getLastMessage().getTimeStamp()? -1:1);

                                                            // notify
                                                            recentConversationAdapter.notifyDataSetChanged();

                                                            // dismiss progress bar
                                                            progressBar.setVisibility(View.GONE);

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
                                RecentConversation conversation = usernameToConversation.get(contactUsername);

                                // update the lastMessage shown
                                if (snapshot.exists() && snapshot.getKey().equals(Constants.CONNECTION_LAST_MESSAGE)) {
                                    // update the chatItem
                                    ChatItem chatItem = snapshot.getValue(ChatItem.class);

                                    conversation.setLastMessage(chatItem);

                                    // sort
                                    conversationList.sort((conversation1, conversation2)
                                            -> conversation1.getLastMessage().getTimeStamp() > conversation2.getLastMessage().getTimeStamp()? -1:1);

                                    // notify
                                    recentConversationAdapter.notifyDataSetChanged();
                                }
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

    private void fetchDataFromDatabase() {
        // for dismissing the progress bar
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // dismiss the progress bar
                if (countContacts == 0) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // fetch all Connections
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // check if the Connection is related
                        ConnectionUser user1 = snapshot.child(Constants.CONNECTION_USER1).getValue(ConnectionUser.class);
                        ConnectionUser user2 = snapshot.child(Constants.CONNECTION_USER2).getValue(ConnectionUser.class);

                        String connectionId;
                        ConnectionUser contact;
                        ChatItem lastMessage;
                        RecentConversation recentConversation;

                        if (user1 != null && userId.equals(user1.getUserId())) {
                            contact = user2;
                        } else if (user2 != null && userId.equals(user2.getUserId())) {
                            contact = user1;
                        } else {
                            return;
                        }
                        countContacts++;

                        recentConversation = new RecentConversation();
                        // get the connectionId
                        connectionId = snapshot.getKey();

                        // todo: listen to the contact in 'Users' table
                        // as long as the user changes her/his info, update it

                        // listen to the current connection
                        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                                .child(connectionId)
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                        // works for the case that 2 ppl chat for the first time
                                        if (snapshot.exists()
                                                && snapshot.getKey().equals(Constants.CONNECTION_LAST_MESSAGE)
                                                && !usernameToConversation.containsKey(contact.getUserId())) {
                                            ChatItem newChat = snapshot.getValue(ChatItem.class);
                                            // set contact
                                            recentConversation.setRecentContact(contact);
                                            // set last message
                                            recentConversation.setLastMessage(newChat);
                                            // set connection id
                                            recentConversation.setConnectionId(connectionId);

                                            // add it to the list and hashmap
                                            conversationList.add(recentConversation);
                                            usernameToConversation.put(contact.getUserId(), recentConversation);
                                            // sort
                                            conversationList.sort((conversation1, conversation2)
                                                    -> conversation1.getLastMessage().getTimeStamp() > conversation2.getLastMessage().getTimeStamp()? -1:1);

                                            // notify
                                            // todo: delete
                                            System.out.println("dismiss inside");
                                            recentConversationAdapter.notifyDataSetChanged();

                                            // dismiss the progress bar
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                        // update the current object
                                        RecentConversation conversation = usernameToConversation.get(contact.getUserId());

                                        if (snapshot.exists()) {
                                            if (snapshot.getKey().equals(Constants.CONNECTION_USER1)
                                                    || snapshot.getKey().equals(Constants.CONNECTION_USER2)) {
                                                ConnectionUser user = snapshot.getValue(ConnectionUser.class);
                                                if (user.getUserId().equals(contact.getUserId())) {
                                                    // update the contact
                                                    conversation.setRecentContact(user);

                                                    // no need to sort
                                                    // notify
                                                    recentConversationAdapter.notifyDataSetChanged();
                                                }
                                            } else if (snapshot.getKey().equals(Constants.CONNECTION_LAST_MESSAGE)) {
                                                conversation.setLastMessage(snapshot.getValue(ChatItem.class));
                                                // sort
                                                conversationList.sort((conversation1, conversation2)
                                                        -> conversation1.getLastMessage().getTimeStamp() > conversation2.getLastMessage().getTimeStamp()? -1:1);
                                                // notify
                                                recentConversationAdapter.notifyDataSetChanged();
                                            }
                                        }
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

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            // retrieve from savedInstanceState
            int size = savedInstanceState.getInt("SIZE");
            RecentConversation conversation;
            for (int i = 0; i < size; i++) {
                conversation = (RecentConversation) savedInstanceState.getParcelable("CONVERSATION" + i);
                this.conversationList.add(conversation);
                this.usernameToConversation.put(conversation.getRecentContactNickname(), conversation);
            }

            // dismiss the progress bar
            progressBar.setVisibility(View.GONE);
        } else {
            // fetchData2();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    // onPreExecute
                    // doInBackground
                    //fetchDataFromDatabase();
                    addConnectionListener();
                }
            });

        }
    }

    public void addConnectionListener() {
        // for dismissing the progress bar
        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // dismiss the progress bar
                if (conversationList.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .child(userId)
                .child(Constants.USER_CONNECTIONS)
                .addChildEventListener(new ConnectionListener());
    }



    private void loadUsername() {
        // todo: update it with auth
        this.userId = ((App) getApplication()).getLoginUser().getUserId();

        // todo: delete
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
                                // todo: delete
                                System.out.println(snapshot.getChildrenCount());
                                if (snapshot.exists()) {
                                    for (DataSnapshot connectionData: snapshot.getChildren()) {
                                        // check if the info is correct
                                        // todo: delete
                                        connectionData.getKey();

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

                                        if (usernameToConversation.containsKey(contact.getUserId())) {
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

                                        // todo: delete
                                        System.out.println(conversation);

                                        // notify
                                        recentConversationAdapter.notifyDataSetChanged();

                                        // dismiss the progress bar
                                        progressBar.setVisibility(View.GONE);

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

}