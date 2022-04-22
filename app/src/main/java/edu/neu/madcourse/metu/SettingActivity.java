package edu.neu.madcourse.metu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.neu.madcourse.metu.home.HomeActivity;

public class SettingActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);

    TextView toolbar = findViewById(R.id.toolbartag);
    toolbar.setText("Settings");

    // Bottom navigation
    bottomNavigationView = findViewById(R.id.bottom_navi);
    bottomNavigationView.setSelectedItemId(R.id.menu_explore);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.menu_explore:

                        return true;
                    case R.id.menu_contacts:
                        return true;

                    case R.id.menu_chats:
                        return true;

                    case R.id.menu_me:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }

        });

};
}
