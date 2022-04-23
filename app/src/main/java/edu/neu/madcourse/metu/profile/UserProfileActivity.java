package edu.neu.madcourse.metu.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

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
            // Show private profile, without like bar, but with edit button
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.edit_profile_button_fragment,
                            EditProfileButtonFragment.newInstance("hello world", "haha", userId), "f1")
                    .add(R.id.add_tag_button_fragment,
                            AddTagButtonFragment.newInstance(), "f1")
                    .add(R.id.add_story_button_fragment, AddStoryButtonFragment.newInstance(
                            "AddStory", "haha"), "AddTagButtonFragment")
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

    @Override
    public void onDataPass(String data) {
        int position = tagList.size();
        tagList.add(position, new Tag(data));
        tagAdapter.notifyItemInserted(position);
    }

    @Override
    public void onStoryDataPass(Uri data) throws IOException {
        int position = storyList.size();
        // Setting image on image view using Bitmap
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data);
        storyList.add(position, new Story(bitmap));
        storyAdapter.notifyItemInserted(position);
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
                                    new DownloadImageTask((ImageView) findViewById(R.id.imageProfile)).execute(avatarUri);
                                }

                            }
                        });
            }
        }).start();
    }

    public void initItemData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // TODO(xin): recover state from savedInstanceState
        } else {
            //TODO(Xin): download image from firebase
            Story story1 = new Story(BitmapFactory.decodeResource(getResources(),
                    R.drawable.story1));
            storyList.add(story1);
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
