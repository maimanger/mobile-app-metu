package edu.neu.madcourse.metu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import edu.neu.madcourse.metu.home.HomeActivity;
import edu.neu.madcourse.metu.home.LoginActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;

public class SettingActivity extends BaseCalleeActivity {
    SwitchCompat message, video, vibration;
    TextView logout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Settings");

        findViewById(R.id.btn_setting_back).setOnClickListener(view -> onBackPressed());


        message = findViewById(R.id.switch_message);
        video = findViewById(R.id.switch_video);
        vibration = findViewById(R.id.switch_vibration);

        message.setChecked(loginUser.getAllowMessageNotif());
        video.setChecked(loginUser.getAllowVideoNotif());
        vibration.setChecked(loginUser.getAllowVibration());


        String userId = loginUser.getUserId();

        message.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .child(userId).child("allowMessageNotif").setValue(isChecked);

                /*if (isChecked) {
                    message.setChecked(true);
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                            .child(userId).child("allowMessageNotif").setValue(true);
                    *//*((App) getApplication()).setAllowMessageNotif(true);*//*
                }
                else {
                    message.setChecked(false);
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                            .child(userId).child("allowMessageNotif").setValue(false);
                    ((App) getApplication()).setAllowMessageNotif(false);
                }*/
            }
        });

        video.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .child(userId).child("allowVideoNotif").setValue(isChecked);

                /*if (isChecked) {
                    video.setChecked(true);
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                            .child(userId).child("allowVideoNotif").setValue(true);
                    ((App) getApplication()).setAllowVideoNotif(true);
                }
                else {
                    message.setChecked(false);
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                            .child(userId).child("allowVideoNotif").setValue(false);
                    ((App) getApplication()).setAllowVideoNotif(false);
                }*/
            }
        });

        vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                        .child(userId).child("allowVibration").setValue(isChecked);

                /*if (isChecked) {
                    vibration.setChecked(true);
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                            .child(userId).child("allowVibration").setValue(true);
                    ((App) getApplication()).setAllowVibration(true);
                }
                else {
                    vibration.setChecked(false);
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                            .child(userId).child("allowVibration").setValue(false);
                    ((App) getApplication()).setAllowVibration(false);
                }*/
            }
        });


        logout = findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // remove FCM token and inactive FCM status
                FCMTokenUtils.removeFCMToken(userId);
                FCMTokenUtils.setStatusInactive(userId);

                // logout firebase auth
                mAuth.signOut();

                // Remove Firebase database listener
                FirebaseService.getInstance().logoutUserProfile(userId);

                // logout Agora Rtm
                ((App)getApplication()).getRtmClient().logout(new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Setting", "onSuccess: Logout RTM");
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) { }
                });

                ((App)getApplication()).setLoginUser(null);

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    };
}
