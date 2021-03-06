package edu.neu.madcourse.metu.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.service.LocatorService;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private Button mRegister;
    private EditText mUsername, mEmail, mPassword;
    private FirebaseAuth mAuth;

    private String inputUserId;
    private String inputUserName;
    private String inputEmail;
    private String inputPassword;
    private Boolean isAgreedPrivatePolicy = false;

    //private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        /*firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        };*/

        mRegister = (Button) findViewById(R.id.signupbtn);
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mEmail = findViewById(R.id.email);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUsername.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    runOnUiThread(() -> {
                        if (TextUtils.isEmpty(username))
                            mUsername.setError("Please enter your username");
                        if (TextUtils.isEmpty(email))
                            mEmail.setError("Please enter your email.");
                        if (TextUtils.isEmpty(password))
                            mPassword.setError("Please enter your password.");
                    });
                    //Toast.makeText(RegisterActivity.this, "Please enter username, email and password.", Toast.LENGTH_LONG).show();
                } else {
                    inputUserId = email.replaceAll("\\.", "");
                    /*updateLatestLocation(inputUserId);
                    if (isAgreedPrivatePolicy == true) {
                        autoRegister(username, email, password);
                    }
                    else {
                        showDialog();
                    }*/
                    inputEmail = email;
                    inputUserName = username;
                    inputPassword = password;
                    startRegister(inputUserId, username, email, password);
                }
            }
        });
    }


    private void startRegister(String inputUserId, String username, String email, String password) {
        if (updateLatestLocation(inputUserId)) {
            if (isAgreedPrivatePolicy == true) {
                autoRegister(username, email, password);
            }
            else {
                showDialog();
            }
        }
    }


    public void showDialog() {
        String username = mUsername.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.privacy_policy_dialog, null);

        Button agreeButton = view.findViewById(R.id.agreeButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        alertDialog.show();

        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAgreedPrivatePolicy = true;
                alertDialog.dismiss();
                autoRegister(username, email, password);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAgreedPrivatePolicy = false;
                alertDialog.dismiss();
            }
        });
    }

    public void autoRegister(String username, String email, String password) {
        new Thread(() -> {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                runOnUiThread(() -> {
                                    mUsername.setError("Please enter your username again.");
                                    mEmail.setError("Please enter your email again.");
                                    mPassword.setError("Please enter your password again.");
                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Sign up error!",
                                            Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                // Start showing loading progress bar
                                runOnUiThread(() -> {
                                    findViewById(R.id.progressBar_register_loading).setVisibility(View.VISIBLE);
                                });

                                String userId = email.replaceAll("\\.", "");
                                User newUser = new User(userId, username, password, email, System.currentTimeMillis());

                                // Save new User in Firebase database
                                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE).child(userId).setValue(newUser);

                                // Save new User locally && bind a long-lived listener to User change in Database
                                FirebaseService.getInstance().fetchUserProfileData(userId,
                                        (User user) -> {
                                            ((App) getApplication()).setLoginUser(user);
                                        });

                                // update FCM token and FCM status
                                FCMTokenUtils.updateFCMToken(userId);
                                ((App) getApplication()).setFcmToken(FCMTokenUtils.fcmToken);
                                FCMTokenUtils.setStatusActive(userId);

                                // rmt login
                                ((App) getApplication()).rtmLogin(userId);

                                // Go to ProfileActivity
                                Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
        }).start();
    }



    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    private boolean updateLatestLocation(String inputUserId) {
        if (checkLocatingPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkLocatingPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            // start locating service
            Intent locatingServiceIntent = new Intent(getApplicationContext(), LocatorService.class);
            locatingServiceIntent.putExtra("USER_ID", inputUserId);
            startService(locatingServiceIntent);
            return true;
        }
        return false;
    }

    private boolean checkLocatingPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            // Permission denied, cannot start precisely locating
            if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                this.runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),
                            "Without your permission, MetU can't recommend a precise match for you.",
                            Toast.LENGTH_LONG).show();
                });
            }
            // Permission granted, start locating service
            else {
                //start locating service
                Intent locatingServiceIntent = new Intent(getApplicationContext(), LocatorService.class);
                locatingServiceIntent.putExtra("USER_ID", inputUserId);
                startService(locatingServiceIntent);
            }

            if (isAgreedPrivatePolicy == true) {
                autoRegister(inputUserName, inputEmail, inputPassword);
            }
            else {
                showDialog();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (((App)getApplication()).getAliveActivityCount() > 1) {
            super.onBackPressed();
        }
    }

}