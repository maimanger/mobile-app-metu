package edu.neu.madcourse.metu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.neu.madcourse.metu.home.HomeActivity;

public class SettingActivity extends BaseCalleeActivity {

    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);

    TextView toolbar = findViewById(R.id.toolbartag);
    toolbar.setText("Settings");

};
    public void Logout(View view) {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }
}
