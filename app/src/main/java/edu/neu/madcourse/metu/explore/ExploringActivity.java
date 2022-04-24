package edu.neu.madcourse.metu.explore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.daos.RecommendedProfile;
import edu.neu.madcourse.metu.chat.daos.User;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FakeDatabase;
import edu.neu.madcourse.metu.utils.GenderUtils;

public class ExploringActivity extends AppCompatActivity {
    private String username;
    private List<RecommendedUser> recommends;

    // ui components
    AppCompatImageView setting;

    // recycler view
    private RecyclerView exploringPage;
    private RecommendsAdapter recommendsAdapter;

    // for multi threading
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exploring);

        // initialize the recycler view
        exploringPage = findViewById(R.id.exploringPage);

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);
        recommends = new ArrayList<>();

        // init the username
        loadUsername();

        // init data view
        init(savedInstanceState);

    }

    private void loadUsername() {
        this.username = App.getUserId();
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
        recommendsAdapter = new RecommendsAdapter(getApplicationContext(), recommends, username);
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
                            int gender = snapshot.child(Constants.USER_GENDER).getValue(int.class);
                            String avatarUri = snapshot.child(Constants.USER_AVATAR_URI).getValue(String.class);

                            RecommendedUser recommendedUser = new RecommendedUser();
                            recommendedUser.setIsLiked(false);
                            recommendedUser.setGender(gender);
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