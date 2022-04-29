package edu.neu.madcourse.metu.home;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;

public class HomeActivity extends AppCompatActivity {
    FrameLayout bgLayout;
    GifView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        Fade fade = new Fade();
        fade.setDuration(1500);
        getWindow().setEnterTransition(fade);
        setContentView(R.layout.activity_home);

        bgLayout = findViewById(R.id.frame_homeBackground);
        gifView = new GifView(this);

        Button loginButton = (Button) findViewById(R.id.loginbtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Button registerButton = (Button) findViewById(R.id.registerbtn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        bgLayout.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bgLayout.addView(gifView);
    }

    @Override
    public void onBackPressed() {
    }

}