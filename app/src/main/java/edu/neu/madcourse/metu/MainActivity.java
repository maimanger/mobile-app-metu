package edu.neu.madcourse.metu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.home.SplashActivity;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.EditProfileActivity;
import edu.neu.madcourse.metu.profile.UserProfileActivity;

import android.widget.Button;

import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;
import edu.neu.madcourse.metu.video.VideoActivity;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private String username;

    private Button okButton;
    private EditText inputName;
    private Button recentChats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button splashButton = (Button) findViewById(R.id.openSplash);
        splashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        Button settingButton = (Button) findViewById(R.id.openSetting);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });


        Button profileButton = findViewById(R.id.button_profile);
        Button updateProfileButton = findViewById(R.id.button_update_profile);

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        updateProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        initCurrentUser();
        initRecentConversations();

        Button explore = findViewById(R.id.openExploringActivity);
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExploringActivity.class);
                startActivity(intent);
            }
        });

    }


    public void onClickContacts(View view) {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
    }

    public void onClickVideo(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    public void onClickRtmLogin(View view) {
        String userId = ((TextView)findViewById(R.id.editText_fakeUserId)).getText().toString();
        ((App)getApplication()).rtmLogin(userId);
        new Thread(() -> {
            FirebaseService.getInstance().fetchUserProfileData(userId,
                    (User user) -> {
                if (user != null) {
                    Log.d("MainActivity", "login profile fetched " + user.getUserId());
                } else {
                    Log.d("MainActivity", "login profile fetched null");
                }

                ((App)getApplication()).setLoginUser(user);
                    });
        }).start();
    }

    public void initCurrentUser() {
        okButton = findViewById(R.id.okButton);
        inputName = findViewById(R.id.inputUsername);

        okButton.setOnClickListener(view -> {
            String userInputName = inputName.getText().toString();
            // if the username input is valid and not the current one
            if (userInputName != null && userInputName.trim().length() > 0 && !userInputName.equals(username)) {
                // auth the user somehow
                Log.d("LOGIN", userInputName + " trying to log in ");
                // todo: real login
                // fetch the user info from the database
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .orderByChild(Constants.USER_USER_ID)
                        .equalTo(userInputName)
                        .limitToFirst(1)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot d: snapshot.getChildren()) {
                                    String userId = d.child(Constants.USER_USER_ID).getValue(String.class);
                                    String nickname = d.child(Constants.USER_NICKNAME).getValue(String.class);
                                    String avatarUri = d.child(Constants.USER_AVATAR_URI).getValue(String.class);

                                    User loginUser = new User();
                                    loginUser.setUserId(userId);
                                    loginUser.setAvatarUri(avatarUri);
                                    loginUser.setNickname(nickname);

                                    // set the current user
                                    ((App) getApplication()).setLoginUser(loginUser);

                                    // update the token
                                    FCMTokenUtils.updateFCMToken(userId);
                                    ((App) getApplication()).setFcmToken(FCMTokenUtils.fcmToken);
                                    // set the status
                                    FCMTokenUtils.setStatusActive(userId);

                                    // rmt login
                                    ((App)getApplication()).rtmLogin(userId);
                                    return;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }

        });
    }

    public void initRecentConversations() {
        recentChats = findViewById(R.id.openRecentConversation);
        recentChats.setOnClickListener(view -> {
            if (((App) getApplication()).getLoginUser() != null) {
                Intent intent = new Intent(MainActivity.this, RecentConversationActivity.class);
                startActivity(intent);
            }
        });
    }
}
