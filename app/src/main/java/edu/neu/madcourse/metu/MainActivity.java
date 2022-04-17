package edu.neu.madcourse.metu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.FakeDatabase;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}