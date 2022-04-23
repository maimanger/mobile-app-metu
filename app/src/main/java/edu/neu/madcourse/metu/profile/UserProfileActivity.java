package edu.neu.madcourse.metu.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayoutMediator;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.metu.MainActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.SettingActivity;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.Utils;
import edu.neu.madcourse.metu.contacts.Contact;
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.contacts.ContactsAdapter;
import edu.neu.madcourse.metu.contacts.ContactsPagerAdapter;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.models.NewUser;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.service.FirebaseService;

public class UserProfileActivity extends AppCompatActivity implements
        AddTagButtonFragment.OnDataPass,
        AddStoryButtonFragment.OnStoryDataPass {
    public static Bitmap avatarBitmap;
    // TODO(xin): hard-coding, need to interpret from login user and clicked user
    private final String userId = "tom@tomDOTcom";
    private final Boolean isSelf = true;
    private final Boolean isFriend = false;

    private RecyclerView storyRecyclerView;
    private StoryAdapter storyAdapter;
    private List<Story> storyList = new ArrayList<>();
    private Handler handler = new Handler();
    private List<Tag> tagList = new ArrayList<>();
    private RecyclerView tagRecyclerView;
    private TagAdapter tagAdapter;
    BottomNavigationView bottomNavigationView;
    private String avatarStoragePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initUserProfileData(savedInstanceState);
        initItemData(savedInstanceState);
        initTagPager();
        initStoryPager();

        // TODO(xin): get value of userId and isFriend

        if (isSelf) {
            // Show private profile
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.edit_profile_button_fragment, EditProfileButtonFragment.newInstance("hello world", "haha", userId), "f1")
                    .add(R.id.add_tag_button_fragment, AddTagButtonFragment.newInstance(), "f1")
                    .add(R.id.add_story_button_fragment, AddStoryButtonFragment.newInstance("AddStory", "haha"), "AddTagButtonFragment")
                    .commit();
        } else if (isFriend) {
            // Show friend profile
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.like_button, LikeButtonFragment.newInstance("hello world", "haha"), "f1")
                    .add(R.id.star_button, StarButtonFragment.newInstance("a", "b"), "f")
                    .add(R.id.chat_button, ChatButtonFragment.newInstance("a", "b"), "f")
                    .add(R.id.video_button, VideoButtonFragment.newInstance("a", "b"), "f")
                    .commit();
        } else {
            // Show public profile
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.like_button, LikeButtonFragment.newInstance("hello world", "haha"), "f1")
                    .add(R.id.chat_button, ChatButtonFragment.newInstance("a", "b"), "f")
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

    @Override
    public void onDataPass(String data) {
        int position = tagList.size();
        tagList.add(position, new Tag(data));
        tagAdapter.notifyItemInserted(position);
        FirebaseService.getInstance().addTag(userId, data);
    }

    @Override
    public void onStoryDataPass(Uri localPath, String storyImageUri) throws IOException {
        int position = storyList.size();
        // Setting image on image view using Bitmap
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localPath);
        storyList.add(position, new Story(storyImageUri));
        storyAdapter.notifyItemInserted(position);
        FirebaseService.getInstance().addStory(userId, storyImageUri.toString());
        Log.e("story", String.valueOf(storyList.size()));
    }

    public void initUserProfileData(Bundle savedInstanceState) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseService.getInstance().fetchUserProfileData(userId,
                        new DataFetchCallback<NewUser>() {
                            @Override
                            public void onCallback(NewUser user) {
                                ((TextView) findViewById(R.id.text_username)).setText(user.getUsername());
                                ((TextView) findViewById(R.id.text_age)).setText(user.getAge().toString() + " years");
                                ((TextView) findViewById(R.id.text_location)).setText(user.getLocation());
                                String avatarUri = user.getAvatarUri();
                                if (avatarUri != null && !avatarUri.isEmpty()) {
                                    Log.e("initUserProfileData", avatarUri);
                                    new Utils.DownloadImageTask((ImageView) findViewById(R.id.imageProfile)).execute(avatarUri);
                                }

                            }
                        });
            }
        }).start();
    }

    public void initItemData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // TODO(xin): recover state from savedInstanceState
        }
    }

    public void initStoryPager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseService.getInstance().fetchStoryList(userId,
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
                                        storyRecyclerView.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

                                        storyAdapter = new StoryAdapter(storyList);
                                        storyRecyclerView.setAdapter(storyAdapter);
                                    }
                                });
                            }
                        });
            }
        }).start();
    }

    public void initTagPager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseService.getInstance().fetchTagList(userId,
                        new DataFetchCallback<Map<String, Boolean>>() {
                            @Override
                            public void onCallback(Map<String, Boolean> map) {
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
                                        tagRecyclerView.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

                                        tagAdapter = new TagAdapter(tagList);
                                        tagRecyclerView.setAdapter(tagAdapter);
                                    }
                                });
                            }
                        });
            }
        }).start();
    }
}
