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
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.ImageView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.models.Contact;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.Utils;
import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;


public class UserProfileActivity extends BaseCalleeActivity implements
        AddTagButtonFragment.OnDataPass,
        AddStoryButtonFragment.OnStoryDataPass {
    private String profileUserId;
    private String loginUserId;
    private Boolean isSelf;
    private Boolean isFriend;

    private User profileUser;
    private User loginUser;

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

    private String connectionId;
    private List<Contact> contactsList;

    private ValueEventListener firebaseEventListener;


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("PROFILE_USER", profileUser);
        outState.putString("CONNECTION_ID", connectionId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initUserProfileData();

        /*initItemData(savedInstanceState);
        initTagPager();
        initStoryPager();
        initFragments();
        initOnlineStatus();*/

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
    protected void onDestroy() {
        if (!isSelf) {
            FirebaseDatabase.getInstance().getReference().child("users").child(profileUserId)
                    .removeEventListener(firebaseEventListener);
        }
        super.onDestroy();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!isSelf) {
            initOnlineStatus(profileUserId);
        } else {
            refreshLoginUser();
        }
    }

    @Override
    public void onDataPass(String data) {
        int position = tagList.size();
        tagList.add(position, new Tag(data));
        tagAdapter.notifyItemInserted(position);
        FirebaseService.getInstance().addTag(profileUserId, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
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


    private void initUserProfileDataFromBundle(Bundle savedInstanceState) {
        new Thread(() -> {
            profileUser = savedInstanceState.getParcelable("PROFILE_USER");
            loginUser = ((App) getApplication()).getLoginUser();
            loginUserId = loginUser.getUserId();
            profileUserId = profileUser.getUserId();
            isSelf = profileUserId.equals(loginUserId);
            isFriend = connectionPoint > 0;
            connectionId = savedInstanceState.getParcelable("CONNECTION_ID");
            if (!isSelf) {
                initTags(profileUser.getTags());
                initStories(profileUser.getStories());
                initUserProfile(isFriend);
            } else {
                initTags(loginUser.getTags());
                initStories(loginUser.getStories());
                initPrivateProfile();
            }
        }).start();
    }



    private void refreshLoginUser() {
        // TODO: compare old loginUser and the new one, if not equals, refresh Profile
        loginUser = ((App) getApplication()).getLoginUser();
        initTags(loginUser.getTags());
        initStories(loginUser.getStories());
        initPrivateProfile();
    }



    private void initUserProfileData() {

        // If Entering profile from CanceledCallNotification, remove this notification Id from App
        if (getIntent().hasExtra("NOTIFICATION_ID")) {
            int notifId = getIntent().getIntExtra("NOTIFICATION_ID", 0);
            ((App) getApplicationContext()).removeCanceledCallNotificationId(notifId);
        }

        // Enter profile from Contacts/Exploring/Chat, must have PROFILE_USER_ID intent
        loginUser = ((App) getApplication()).getLoginUser();
        loginUserId = loginUser.getUserId();
        connectionPoint = 0;
        if (getIntent().hasExtra("PROFILE_USER_ID")) {
            profileUserId = getIntent().getStringExtra("PROFILE_USER_ID");
            connectionPoint = getIntent().getIntExtra("CONNECTION_POINT", 0);
            connectionId = getIntent().getStringExtra("CONNECTION_ID");
            Log.d(TAG, "initUserProfileData: " + profileUserId);
        } else {
            profileUserId = loginUserId;
        }
        isSelf = profileUserId.equals(loginUserId);
        isFriend = connectionPoint > 0;


        firebaseEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profileUser = snapshot.getValue(User.class);
                assert profileUser != null;
                initTags(profileUser.getTags());
                initStories(profileUser.getStories());
                initUserProfile(isFriend);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        new Thread(() -> {
            if (!isSelf) {
                FirebaseDatabase.getInstance().getReference().child("users").child(profileUserId)
                        .addValueEventListener(firebaseEventListener);
            } else {
                initTags(loginUser.getTags());
                initStories(loginUser.getStories());
                initPrivateProfile();
            }
        }).start();


        /*if (!isSelf) {
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
                                ImageView profileAvatar = findViewById(R.id.imageProfile);
                                profileAvatar.setImageResource(R.drawable.user_avatar);


                                }
                            });
                }
            }).start();
        }*/
    }

    /*private void initItemData(Bundle savedInstanceState) {
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
                                .commitAllowingStateLoss();
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
                                .commitAllowingStateLoss();
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
                                .commitAllowingStateLoss();
                    }
                });
    }*/


    private void initPrivateProfile() {
        runOnUiThread(() -> {
            ((TextView) findViewById(R.id.text_username)).setText(loginUser.getNickname());
            ((TextView) findViewById(R.id.text_age)).setText(loginUser.getAge().toString() + " years");
            ((TextView) findViewById(R.id.text_location)).setText(loginUser.getLocation());
            String avatarUri = loginUser.getAvatarUri();
            if (avatarUri != null && !avatarUri.isEmpty()) {
                Log.e("initUserProfileData", avatarUri);
                new Utils.DownloadImageTask((ImageView) findViewById(R.id.imageProfile)).execute(avatarUri);
            }

            findViewById(R.id.image_profile_onlineStatus).setVisibility(View.INVISIBLE);

            initTagsView();
            initStoriesView();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.edit_profile_button_fragment,
                            EditProfileButtonFragment.newInstance(profileUserId),
                            "EditProfileButtonFragment")
                    .add(R.id.add_tag_button_fragment,
                            AddTagButtonFragment.newInstance(), "AddTagButtonFragment")
                    .add(R.id.add_story_button_fragment,
                            AddStoryButtonFragment.newInstance(),
                            "AddTagButtonFragment")
                    .commitAllowingStateLoss();
        });
    }


    private void initTags(Map<String, Boolean> tagsMap) {
        if (tagsMap != null) {
            tagList.clear();
            for (Map.Entry<String, Boolean> entry : tagsMap.entrySet()) {
                String key = entry.getKey();
                tagList.add(new Tag(key));
            }
        }
    }


    private void initTagsView() {
        tagRecyclerView = findViewById(R.id.tag_recycler_view);
        tagRecyclerView.setHasFixedSize(true);
        tagRecyclerView.setLayoutManager(new LinearLayoutManager(
                UserProfileActivity.this,
                LinearLayoutManager.HORIZONTAL, false));

        tagAdapter = new TagAdapter(tagList);
        tagRecyclerView.setAdapter(tagAdapter);
    }

    private void initStories(Map<String, String> storiesMap) {
        if (storiesMap != null) {
            storyList.clear();
            for (Map.Entry<String, String> entry : storiesMap.entrySet()) {
                String storyUri = entry.getValue();
                storyList.add(new Story(storyUri));
            }
        }
    }

    private void initStoriesView() {
        storyRecyclerView = findViewById(R.id.story_recycler_view);
        storyRecyclerView.setHasFixedSize(true);
        storyRecyclerView.setLayoutManager(new LinearLayoutManager(
                UserProfileActivity.this,
                LinearLayoutManager.HORIZONTAL, false));

        storyAdapter = new StoryAdapter(storyList);
        storyRecyclerView.setAdapter(storyAdapter);
    }

    private void initUserProfile(boolean isFriend) {
        runOnUiThread(() -> {
            ((TextView) findViewById(R.id.text_username)).setText(profileUser.getNickname());
            ((TextView) findViewById(R.id.text_age)).setText(profileUser.getAge().toString() + " years");
            ((TextView) findViewById(R.id.text_location)).setText(profileUser.getLocation());
            String avatarUri = profileUser.getAvatarUri();
            if (avatarUri != null && !avatarUri.isEmpty()) {
                Log.e("initUserProfileData", avatarUri);
                new Utils.DownloadImageTask((ImageView) findViewById(R.id.imageProfile)).execute(avatarUri);
            }

            initStoriesView();
            initTagsView();

            if (isFriend) {
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
                        .commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.like_button,
                                LikeButtonFragment.newInstance(profileUserId),
                                "LikeButtonFragment")
                        .add(R.id.chat_button,
                                ChatButtonFragment.newInstance(profileUser,
                                        isLikedByLoginUser, loginUserId),
                                "ChatButtonFragment")
                        .commitAllowingStateLoss();
            }


        });
    }

    //TODO: Check friend/public profile online status and update UI
    private void initOnlineStatus(String peerId) {
        new Thread(() -> {
            Set<String> peersIdSet = new HashSet<>();
            peersIdSet.add(peerId);
            ((App) getApplication()).queryPeerOnlineStatus(peersIdSet,
                    new ResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onSuccess(Map<String, Boolean> peersOnlineStatus) {
                            boolean isOnline = peersOnlineStatus.get(peerId);
                            runOnUiThread(() -> {
                                findViewById(R.id.image_profile_onlineStatus).setVisibility(isOnline ? View.VISIBLE : View.INVISIBLE);
                            });
                        }

                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
                        }
                    });
        }).start();
    }


 /*   private void initOnlineStatus() {
        User loginUser = ((App) getApplication()).getLoginUser();
        if (loginUser != null) {
            String myUserId = loginUser.getUserId();
            Map<String, Boolean> myConnections = loginUser.getConnections();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Fetch Contacts from Firebase
                    FirebaseService.getInstance().fetchContacts(myUserId, myConnections,
                            (List<Contact> fetchedContacts) -> {
                                Log.d(TAG, "fetchContactsList: " + fetchedContacts.size());

                                Set<String> contactsId = fetchedContacts.stream()
                                        .map(Contact::getContactUserId).collect(Collectors.toSet());

                                // Subscribe contacts online status from Agora Rtm (Realtime update)
                                ((App) getApplication()).rtmSubscribePeer(contactsId);

                                // Query contacts online status from Agora Rtm (one time)
                                ((App) getApplication()).queryPeerOnlineStatus(contactsId,
                                        new ResultCallback<Map<String, Boolean>>() {
                                            @Override
                                            public void onSuccess(Map<String, Boolean> peerOnlineStatus) {
                                                Log.d(TAG, "onSuccess: query peers online status");
                                                for (Map.Entry<String, Boolean> entry :
                                                        peerOnlineStatus.entrySet()) {
                                                    String userId = entry.getKey();
                                                    Boolean isOnline = entry.getValue();
                                                    if (isSelf) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                findViewById(R.id.image_profile_onlineStatus).setVisibility(View.GONE);
                                                            }
                                                        });

                                                    } else if (profileUserId.equals(userId)) {
                                                        if (!isOnline) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ((ImageView) findViewById(R.id.image_profile_onlineStatus))
                                                                            .setImageResource(R.drawable.ic_unavailable_status);
                                                                }
                                                            });

                                                        }
                                                        break;
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(ErrorInfo errorInfo) {
                                            }
                                        });
                            });
                }
            }).start();


        }
    }*/
}


