package edu.neu.madcourse.metu.profile;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.MenuItem;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.widget.ImageView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.Utils;
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.service.FirebaseService;


public class UserProfileActivity extends BaseCalleeActivity implements
        AddTagButtonFragment.OnDataPass,
        AddStoryButtonFragment.OnStoryDataPass {
    private String profileUserId;
    private String loginUserId;
    private Boolean isSelf = true;
    private Boolean isFriend = true;

    private RecyclerView storyRecyclerView;
    private StoryAdapter storyAdapter;
    private List<Story> storyList = new ArrayList<>();
    private Handler handler = new Handler();
    private List<Tag> tagList = new ArrayList<>();
    private RecyclerView tagRecyclerView;
    private TagAdapter tagAdapter;
    BottomNavigationView bottomNavigationView;
    private Boolean isLikedByLoginUser = true;
    private int connectionPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initUserProfileData(savedInstanceState);
        initItemData(savedInstanceState);
        initTagPager();
        initStoryPager();
        initFragments();

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Profile");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setSelectedItemId(R.id.menu_me);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_explore:
                        startActivity(new Intent(getApplicationContext(), ExploringActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_contacts:
                        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_chats:
                        startActivity(new Intent(getApplicationContext(),
                                RecentConversationActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.menu_me:
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onDataPass(String data) {
        int position = tagList.size();
        tagList.add(position, new Tag(data));
        tagAdapter.notifyItemInserted(position);
        FirebaseService.getInstance().addTag(profileUserId, data);
    }

    @Override
    public void onStoryDataPass(Uri localPath, String storyImageUri) throws IOException {
        int position = storyList.size();
        // Setting image on image view using Bitmap
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localPath);
        storyList.add(position, new Story(storyImageUri));
        storyAdapter.notifyItemInserted(position);
        FirebaseService.getInstance().addStory(profileUserId, storyImageUri.toString());
        Log.e("story", String.valueOf(storyList.size()));
    }

    private void initUserProfileData(Bundle savedInstanceState) {
        // Enter profile from Contacts/Exploring/Chat, must have PROFILE_USER_ID intent
        loginUserId = ((App) getApplication()).getLoginUser().getUserId();
        int connectionPoints = 0;
        if (getIntent().hasExtra("PROFILE_USER_ID")) {
            profileUserId = getIntent().getStringExtra("PROFILE_USER_ID");
            connectionPoints = getIntent().getIntExtra("CONNECTION_POINT", 0);
            Log.d(TAG, "initUserProfileData: " + profileUserId);
        } else {
            profileUserId = loginUserId;
        }

        isSelf = profileUserId.equals(loginUserId);
        isFriend = connectionPoints > 0;

        if (!isSelf) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FirebaseService.getInstance().fetchUserProfileData(profileUserId,
                            new DataFetchCallback<User>() {
                                @Override
                                public void onCallback(User user) {
                                    ((TextView) findViewById(R.id.text_username)).setText(user.getNickname());
                                    ((TextView) findViewById(R.id.text_age)).setText(user.getAge().toString() + " years");
                                    ((TextView) findViewById(R.id.text_location)).setText(user.getLocation());
                                    String avatarUri = user.getAvatarUri();
                                    //TODO: set default profile avatar
                                    if (avatarUri != null && !avatarUri.isEmpty()) {
                                        Log.e("initUserProfileData", avatarUri);
                                        new Utils.DownloadImageTask((ImageView) findViewById(R.id.imageProfile)).execute(avatarUri);
                                    }
                                /*ImageView profileAvatar = findViewById(R.id.imageProfile);
                                profileAvatar.setImageResource(R.drawable.user_avatar);*/


                                }
                            });
                }
            }).start();
        }


    }

    private void initItemData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
        }
    }

    private void initStoryPager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseService.getInstance().fetchStoryList(profileUserId,
                        new DataFetchCallback<Map<String, String>>() {
                            @Override
                            public void onCallback(Map<String, String> map) {
                                if (map != null) {
                                    storyList.clear();
                                    for (Map.Entry<String, String> entry : map.entrySet()) {
                                        String storyUri = entry.getValue();
                                        storyList.add(new Story(storyUri));
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        storyRecyclerView = findViewById(R.id.story_recycler_view);
                                        storyRecyclerView.setHasFixedSize(true);
                                        storyRecyclerView.setLayoutManager(new LinearLayoutManager(
                                                UserProfileActivity.this,
                                                LinearLayoutManager.HORIZONTAL, false));

                                        storyAdapter = new StoryAdapter(storyList);
                                        storyRecyclerView.setAdapter(storyAdapter);
                                    }
                                });
                            }
                        });
            }
        }).start();
    }

    private void initTagPager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseService.getInstance().fetchTagList(profileUserId,
                        map -> {
                            if (map != null) {
                                tagList.clear();
                                for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                                    String key = entry.getKey();
                                    tagList.add(new Tag(key));
                                }
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tagRecyclerView = findViewById(R.id.tag_recycler_view);
                                    tagRecyclerView.setHasFixedSize(true);
                                    tagRecyclerView.setLayoutManager(new LinearLayoutManager(
                                            UserProfileActivity.this,
                                            LinearLayoutManager.HORIZONTAL, false));

                                    tagAdapter = new TagAdapter(tagList);
                                    tagRecyclerView.setAdapter(tagAdapter);
                                }
                            });
                        });
            }
        }).start();
    }

    private void initFragments() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseService.getInstance().fetchUserProfileData(profileUserId,
                        user -> {
                            ((TextView) findViewById(R.id.text_username)).setText(user.getNickname());
                            ((TextView) findViewById(R.id.text_age)).setText(user.getAge().toString() + " years");
                            ((TextView) findViewById(R.id.text_location)).setText(user.getLocation());
                            String avatarUri = user.getAvatarUri();
                            if (avatarUri != null && !avatarUri.isEmpty()) {
                                Log.e("initUserProfileData", avatarUri);
                                new Utils.DownloadImageTask((ImageView) findViewById(R.id.imageProfile)).execute(avatarUri);
                            }
                        });
            }
        }).start();

        FirebaseService.getInstance().fetchUserProfileData(profileUserId,
                profileUser -> {
                    if (isSelf) {
                        // Show private profile
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.edit_profile_button_fragment,
                                        EditProfileButtonFragment.newInstance(profileUserId),
                                        "EditProfileButtonFragment")
                                .add(R.id.add_tag_button_fragment,
                                        AddTagButtonFragment.newInstance(), "AddTagButtonFragment")
                                .add(R.id.add_story_button_fragment,
                                        AddStoryButtonFragment.newInstance(),
                                        "AddTagButtonFragment")
                                .commit();
                    } else if (isFriend) {
                        // Show friend profile
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.star_button,
                                        StarButtonFragment.newInstance(this.connectionPoint),
                                        "StarButtonFragment")
                                .add(R.id.chat_button,
                                        ChatButtonFragment.newInstance(profileUser,
                                                isLikedByLoginUser, loginUserId),
                                        "ChatButtonFragment")
                                .add(R.id.video_button, VideoButtonFragment.newInstance(),
                                        "VideoButtonFragment")
                                .commit();
                    } else {
                        // Show public profile
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.like_button,
                                        LikeButtonFragment.newInstance(profileUserId),
                                        "LikeButtonFragment")
                                .add(R.id.chat_button,
                                        ChatButtonFragment.newInstance(profileUser,
                                                isLikedByLoginUser, loginUserId),
                                        "ChatButtonFragment")
                                .commit();
                    }
                });
    }
}
