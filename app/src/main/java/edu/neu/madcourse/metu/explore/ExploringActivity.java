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
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import edu.neu.madcourse.metu.explore.daos.PreferenceSetting;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;

public class ExploringActivity extends BaseCalleeActivity implements ExploreSettingDialog.ExploringSettingDialogListener {
    private String userId;
    private User loginUser;
    private PreferenceSetting setting;

    private List<RecommendedUser> recommends;

    // ui components
    AppCompatImageView preferenceSetting;
    ProgressBar progressBar;

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

        progressBar = findViewById(R.id.progressBarExploringActivity);
        progressBar.setVisibility(View.VISIBLE);

        // initialize the recycler view
        exploringPage = findViewById(R.id.exploringPage);
        // init the layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        exploringPage.setLayoutManager(linearLayoutManager);

        // UI components
        preferenceSetting = findViewById(R.id.imageSetting);

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);
        recommends = new ArrayList<>();

        // init the user
        loadUser();

        // init data view
        init(savedInstanceState);

        // set listener
        setListener();

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Explore more");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
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
        loginUser = ((App) getApplicationContext()).getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getUserId().length() == 0) {
            Log.d("ACTIVITY", "USER HAS LOGGED OUT");
            finish();
            return;
        }
        this.userId = loginUser.getUserId();
        Log.d("ACTIVITY", "EXPLORING ACTIVITY: " + userId);
    }

    private void init(Bundle savedInstanceState) {
        // todo: deal with the direction change
        try {
            fetchData();
        } catch (Exception e) {
            Toast.makeText(this, "OOOPS, something went wrong! Please check the internet connection and refresh the page!", Toast.LENGTH_SHORT).show();
        }
        
    }

    private void initRecyclerView() {
        if (recommendsAdapter == null) {
            recommendsAdapter = new RecommendsAdapter(this, recommends, loginUser);
        }
        exploringPage.setAdapter(recommendsAdapter);
    }

    private void fetchData() throws Exception {

        // todo: matching algorithm
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = RecommendationUtils.getDateKey();
                // fetch the preference
                RecommendationUtils.fetchPreference(userId, preference -> {
                    if (preference == null) {
                        openSettingDialog(true);
                    } else {
                        setting = preference;

                        // try to fetch saved ids from firebase
                        RecommendationUtils.fetchRecommendUserIdsFromDatabase(userId, ids -> {
                            if (ids == null || ids.size() == 0) {
                                // initialize for the first time
                                initRecommendUsers();
                                return;
                            }

                            // otherwise retrieve the data by id
                            for (String recommendUserId: ids) {
                                RecommendationUtils.getSingleRecommendedUserById(userId, recommendUserId, u -> {
                                    recommends.add(u);

                                    if (recommends.size() == ids.size()) {
                                        recommends.sort(new RecommendedUserComparator(setting));
                                        initRecyclerView();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                            }

                        });


                    }
                });
            }
        }).start();



    }

    private void setListener() {
        this.preferenceSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettingDialog(false);
            }
        });
    }

    private void openSettingDialog(boolean isFirstTime) {
        ExploreSettingDialog dialog = new ExploreSettingDialog();
        // put current
        Bundle bundle = new Bundle();
        bundle.putString("USER_ID", userId);
        bundle.putBoolean("IS_FIRST_TIME", isFirstTime);
        dialog.setArguments(bundle);
        if (isFirstTime) {
            dialog.setCancelable(false);
        }
        dialog.show(getSupportFragmentManager(), "Explore Setting Dialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_explore);
    }


    @Override
    public void applyPreference(PreferenceSetting setting) {
        this.setting = setting;
        initRecommendUsers();
    }


    private void initRecommendUsers() {
        String key = RecommendationUtils.getDateKey();
        if (setting != null) {
            RecommendationUtils.getRecommendedUsersByPreference(userId, setting, users -> {
                for (RecommendedUser u: users){
                    recommends.add(u);
                    System.out.println(u);
                    // add it to the database
                    FirebaseDatabase.getInstance().getReference(Constants.RECOMMENDATIONS_STORE)
                            .child(userId)
                            .child(key)
                            .child(u.getUserId())
                            .setValue(true);
                }

                progressBar.setVisibility(View.GONE);
                initRecyclerView();
            });
        } else {
            Toast.makeText(this, "Something went wrong! Please check the internet and refresh...", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}
