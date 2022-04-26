package edu.neu.madcourse.metu.explore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import edu.neu.madcourse.metu.App;

import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;

import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.contacts.ContactsActivity;

import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;

public class ExploringActivity extends BaseCalleeActivity {
    private String userId;

    private List<RecommendedUser> recommends;

    // ui components
    AppCompatImageView preferenceSetting;

    // recycler view
    private RecyclerView exploringPage;
    private RecommendsAdapter recommendsAdapter;

    // for multi threading
    private ExecutorService executorService;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exploring);

        // initialize the recycler view
        exploringPage = findViewById(R.id.exploringPage);

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);
        recommends = new ArrayList<>();

        // init the user
        loadUser();

        // init data view
        init(savedInstanceState);

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Explore more");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setSelectedItemId(R.id.menu_explore);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.menu_explore:
                        return true;
                    case R.id.menu_contacts:
                        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_chats:
                        startActivity(new Intent(getApplicationContext(), RecentConversationActivity.class));
                        overridePendingTransition(0,0);
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

    private void loadUser() {
        // todo: load from App
        this.userId = ((App) getApplicationContext()).getLoginUser().getUserId();
        Log.d("ACTIVITY", "EXPLORING ACTIVITY: " + userId);
    }

    private void init(Bundle savedInstanceState) {
        // todo: deal with the direction change
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                initRecyclerView();
                fetchData();

            }
        });

        // initRecyclerView();
        // recommendsAdapter.notifyDataSetChanged();
        
    }

    private void initRecyclerView() {
        // init and set the adapter
        User loginUser = ((App) getApplication()).getLoginUser();
        recommendsAdapter = new RecommendsAdapter(getApplicationContext(), recommends, loginUser);
        exploringPage.setAdapter(recommendsAdapter);
        // init the layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        exploringPage.setLayoutManager(linearLayoutManager);

    }

    private void fetchData() {

        // todo: matching algorithm

        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            String userId = snapshot.child(Constants.USER_USER_ID).getValue(String.class);
                            String nickname = snapshot.child(Constants.USER_NICKNAME).getValue(String.class);
                            // todo: gender string or int?
                            Integer gender = snapshot.child(Constants.USER_GENDER).getValue(Integer.class);
                            String avatarUri = snapshot.child(Constants.USER_AVATAR_URI).getValue(String.class);

                            RecommendedUser recommendedUser = new RecommendedUser();
                            recommendedUser.setIsLiked(false);

                            // todo: gender categories
                            if (gender != null && gender.equals(Constants.GENDER_FEMALE)) {
                                recommendedUser.setGender(Constants.GENDER_FEMALE_INT);
                            } else if (gender != null && gender.equals(Constants.GENDER_MALE)) {
                                recommendedUser.setGender(Constants.GENDER_MALE_INT);
                            } else {
                                recommendedUser.setGender(Constants.GENDER_UNDEFINE_INT);
                            }

                            recommendedUser.setUserId(userId);
                            recommendedUser.setNickname(nickname);
                            recommendedUser.setAvatarUri(avatarUri);

                            recommends.add(recommendedUser);

                            recommendsAdapter.notifyDataSetChanged();
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



}