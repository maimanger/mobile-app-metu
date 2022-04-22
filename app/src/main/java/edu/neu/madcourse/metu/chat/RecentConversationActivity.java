package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.contacts.daos.RecentConversation;
import edu.neu.madcourse.metu.contacts.daos.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.FakeDatabase;
import edu.neu.madcourse.metu.utils.GenderUtils;

public class RecentConversationActivity extends AppCompatActivity {
    private String username;

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
        progressBar.setVisibility(View.VISIBLE);

        // load username
        loadUsername();

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);

        // initialize the recent conversation list
        conversationList = new ArrayList<>();

        // initialize data view
        init(savedInstanceState);

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
        for (int i = 0; i < size; i++) {
            outState.putSerializable("CONVERSATION" + i, this.conversationList.get(i));
        }
    }

    private void initRecyclerView() {
        // initialize recycler view and set layout manager
        recentConversation = findViewById(R.id.recentConversationRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recentConversation.setLayoutManager(linearLayoutManager);

        // initialize and set the adapter
        recentConversationAdapter = new RecentConversationAdapter(this, this.conversationList, username);
        recentConversation.setAdapter(recentConversationAdapter);
    }

    // todo: fetch from database
    private void fetchData() {
        RecentConversation conversation;
        User user;
        ChatItem chatItem;
        Bitmap bitmap;

        user = new User();
        user.setUsername("Hermione Granger");
        user.setGender(GenderUtils.FEMALE);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hermione);
        user.setAvatar(BitmapUtils.encodeImage(bitmap, 160));
        user.setProfilePhoto(BitmapUtils.encodeImage(bitmap, 300));
        conversation = new RecentConversation();
        conversation.setRecentContact(user);
        conversation.setRecentConversation(new ChatItem("Hermione Granger", "come with you", System.currentTimeMillis() - 100000));
        this.conversationList.add(conversation);

        user = new User();
        user.setUsername("Luna Lovegood");
        user.setGender(GenderUtils.FEMALE);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.luna);
        user.setAvatar(BitmapUtils.encodeImage(bitmap, 160));
        user.setProfilePhoto(BitmapUtils.encodeImage(bitmap, 300));
        conversation = new RecentConversation();
        conversation.setRecentContact(user);
        conversation.setRecentConversation(new ChatItem("Luna Lovegood", "thats a good one!!!!", System.currentTimeMillis() - 50000));
        this.conversationList.add(conversation);

        user = new User();
        user.setUsername("Harry Potter");
        user.setGender(GenderUtils.MALE);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.harry);
        user.setAvatar(BitmapUtils.encodeImage(bitmap, 160));
        user.setProfilePhoto(BitmapUtils.encodeImage(bitmap, 300));
        conversation = new RecentConversation();
        conversation.setRecentContact(user);
        conversation.setRecentConversation(new ChatItem("Harry Potter", "got it", System.currentTimeMillis()));
        this.conversationList.add(conversation);


    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            // retrieve from savedInstanceState
            int size = savedInstanceState.getInt("SIZE");
            RecentConversation conversation;
            for (int i = 0; i < size; i++) {
                conversation = (RecentConversation) savedInstanceState.getSerializable("CONVERSATION" + i);
                this.conversationList.add(conversation);
            }

            // init the recycler view
            initRecyclerView();
            // dismiss the progress bar
            progressBar.setVisibility(View.GONE);
        } else {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    // onPreExecute
                    // doInBackground
                    fetchData();

                    // onPostExecute
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initRecyclerView();
                            // dismiss the progress bar
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
            });

        }
    }

    private void loadUsername() {
        this.username = getSharedPreferences("METU_APP", MODE_PRIVATE)
                .getString("USERNAME", "");
    }


}