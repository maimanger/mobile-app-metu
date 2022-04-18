package edu.neu.madcourse.metu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;

import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.home.SplashActivity;
import edu.neu.madcourse.metu.profile.EditProfileActivity;
import edu.neu.madcourse.metu.profile.UserProfileActivity;

import android.widget.Button;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.FakeDatabase;
import edu.neu.madcourse.metu.video.VideoActivity;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button splashButton = (Button) findViewById(R.id.openSplash);
        splashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
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

        editor = getSharedPreferences("METU_APP", MODE_PRIVATE).edit();
        editor.putString("USERNAME", FakeDatabase.sender.getUsername());
        editor.apply();

        Button chat = findViewById(R.id.openChatActivity);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.harry);
                FakeDatabase.receiver.setAvatar(BitmapUtils.encodeImage(bitmap, 150));

                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ron);
                FakeDatabase.sender.setAvatar(BitmapUtils.encodeImage(bitmap, 150));

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("RECEIVER", FakeDatabase.receiver);
                startActivity(intent);
            }
        });

        Button explore = findViewById(R.id.openExploringActivity);
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExploringActivity.class);
                startActivity(intent);
            }
        });

        Button recentChat = findViewById(R.id.openRecentConversation);
        recentChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecentConversationActivity.class);
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
}
