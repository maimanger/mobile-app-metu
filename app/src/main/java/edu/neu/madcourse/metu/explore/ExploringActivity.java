package edu.neu.madcourse.metu.explore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.contacts.daos.RecommendedProfile;
import edu.neu.madcourse.metu.contacts.daos.User;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.FakeDatabase;
import edu.neu.madcourse.metu.utils.GenderUtils;

public class ExploringActivity extends AppCompatActivity {
    private String username;
    private List<RecommendedProfile> recommends;

    // ui components
    AppCompatImageView setting;

    // recycler view
    private RecyclerView exploringPage;
    private RecommendsAdapter recommendsAdapter;

    // for multi threading
    private ExecutorService executorService;
    private Handler uiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exploring);

        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Explore more");

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);
        uiThread = new Handler(Looper.getMainLooper());
        recommends = new ArrayList<>();

        // init the username
        loadUsername();

        // init data view
        init(savedInstanceState);


    }

    private void loadUsername() {
        this.username = getSharedPreferences("METU_APP", MODE_PRIVATE)
                .getString("USERNAME", "");
    }

    private void init(Bundle savedInstanceState) {
        // todo: deal with the direction change
        fetchData();
        initRecyclerView();
        recommendsAdapter.notifyDataSetChanged();
        
    }

    private void initRecyclerView() {
        // init the recycler view and set the layout manager
        exploringPage = findViewById(R.id.exploringPage);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        exploringPage.setLayoutManager(linearLayoutManager);

        // init and set the adapter
        recommendsAdapter = new RecommendsAdapter(getApplicationContext(), recommends, username);
        exploringPage.setAdapter(recommendsAdapter);
    }

    private void fetchData() {
        RecommendedProfile profile;
        Bitmap bitmap;
        User user;

        profile = new RecommendedProfile(FakeDatabase.receiver, false);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.harry);
        FakeDatabase.receiver.setGender(GenderUtils.MALE);
        FakeDatabase.receiver.setProfilePhoto(BitmapUtils.encodeImage(bitmap, 300));
        this.recommends.add(profile);

        user = new User();
        user.setUsername("Hermione Granger");
        user.setGender(GenderUtils.FEMALE);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hermione);
        user.setAvatar(BitmapUtils.encodeImage(bitmap, 160));
        user.setProfilePhoto(BitmapUtils.encodeImage(bitmap, 300));
        profile = new RecommendedProfile(user, false);
        this.recommends.add(profile);

        user = new User();
        user.setUsername("Luna Lovegood");
        user.setGender(GenderUtils.FEMALE);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.luna);
        user.setAvatar(BitmapUtils.encodeImage(bitmap, 160));
        user.setProfilePhoto(BitmapUtils.encodeImage(bitmap, 300));
        profile = new RecommendedProfile(user, false);
        this.recommends.add(profile);

    }
}