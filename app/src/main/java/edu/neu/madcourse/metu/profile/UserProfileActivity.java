package edu.neu.madcourse.metu.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.metu.MainActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.contacts.Contact;
import edu.neu.madcourse.metu.contacts.ContactsAdapter;
import edu.neu.madcourse.metu.contacts.ContactsPagerAdapter;

public class UserProfileActivity extends AppCompatActivity {
    // TODO(xin): hard-coding, need to interpret from login user and clicked user
    private final String userId = "dsjkhf";
    private final Boolean isFriend = true;

    private RecyclerView storyRecyclerView;
    private StoryAdapter storyAdapter;
    private List<Story> storyList = new ArrayList<>();
    private Handler handler = new Handler();
    private List<Tag> tagList = new ArrayList<>();
    private RecyclerView tagRecyclerView;
    private TagAdapter tagAdapter;

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
            // Show private profile
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.edit_profile_button_fragment,
                            EditProfileButtonFragment.newInstance("hello world", "haha"), "f1")
                    //.addToBackStack("fname")
                    .commit();
        } else if (isFriend) {
            // Show friend profile
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.like_bar,
                            FriendFragment.newInstance("hello world", "haha"), "f1")
                    //.addToBackStack("fname")
                    .commit();
        } else {
            // Show public profile
        }
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