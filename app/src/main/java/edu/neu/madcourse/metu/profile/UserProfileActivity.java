package edu.neu.madcourse.metu.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.metu.MainActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.SettingActivity;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.contacts.Contact;
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.contacts.ContactsAdapter;
import edu.neu.madcourse.metu.contacts.ContactsPagerAdapter;
import edu.neu.madcourse.metu.explore.ExploringActivity;

public class UserProfileActivity extends AppCompatActivity {
    // TODO(xin): hard-coding, need to interpret from login user and clicked user
    private final String userId = "self";
    private final Boolean isFriend = false;

    private RecyclerView storyRecyclerView;
    private StoryAdapter storyAdapter;
    private List<Story> storyList = new ArrayList<>();
    private Handler handler = new Handler();
    private List<Tag> tagList = new ArrayList<>();
    private RecyclerView tagRecyclerView;
    private TagAdapter tagAdapter;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initItemData(savedInstanceState);
        initTagPager();
        initStoryPager();


//        if (savedInstanceState == null) {
//            // Add Fragment PrivateProfileFragment to UserProfileActivity.
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.private_profile, PrivateProfileFragment.newInstance("hello
//                    world", "haha"), "f1")
//                    .addToBackStack("fname")
//                    .commit();
//        }

        // TODO(xin): get value of userId and isFriend

        if (userId.equals("self")) {
            // Show private profile, without like bar, but with edit button
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.edit_profile_button_fragment,
                            EditProfileButtonFragment.newInstance("hello world", "haha"), "f1")
                    //.addToBackStack("fname")
                    .commit();
        } else if (isFriend) {
            // Show friend profile， with friend's like bar
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.like_bar,
                            FriendFragment.newInstance("hello world", "haha"), "f1")
                    //.addToBackStack("fname")
                    .commit();
        } else {
            // Show public profile, with public like bar
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.like_bar_public,
                            PublicFragment.newInstance("hello world", "haha"), "f1")
                    //.addToBackStack("fname")
                    .commit();
        }

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Profile");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setSelectedItemId(R.id.menu_me);
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
                        startActivity(new Intent(getApplicationContext(), RecentConversationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_me:
                        return true;
                }
                return false;
            }
        });

    }

    public void initItemData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // TODO(xin): recover state from savedInstanceState
        } else {
            Story story1 = new Story(R.drawable.story1);
            Story story2 = new Story(R.drawable.story2);
            Story story3 = new Story(R.drawable.story3);
            Story story4 = new Story(R.drawable.story4);
            storyList.add(story1);
            storyList.add(story2);
            storyList.add(story3);
            storyList.add(story4);
            Tag tag1 = new Tag("rich");
            Tag tag2 = new Tag("happy");
            Tag tag3 = new Tag("sports");
            Tag tag4 = new Tag("hahah");
            tagList.add(tag1);
            tagList.add(tag2);
            tagList.add(tag3);
            tagList.add(tag4);
        }
    }

    public void initStoryPager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (storyList == null) {
                    // TODO(xin): fetch storyList from database
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            storyRecyclerView = findViewById(R.id.story_recycler_view);
                            storyRecyclerView.setHasFixedSize(true);
                            storyRecyclerView.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

                            storyAdapter = new StoryAdapter(storyList);
                            storyRecyclerView.setAdapter(storyAdapter);
                        }
                    });
                }
            }
        }).start();
    }

    public void initTagPager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (tagList == null) {
                    // TODO(xin): fetch tagList from database
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tagRecyclerView = findViewById(R.id.tag_recycler_view);
                            tagRecyclerView.setHasFixedSize(true);
                            tagRecyclerView.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

                            tagAdapter = new TagAdapter(tagList);
                            tagRecyclerView.setAdapter(tagAdapter);
                        }
                    });
                }
            }
        }).start();
    }
}