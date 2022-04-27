package edu.neu.madcourse.metu.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
    private Button mRegister;
    private EditText mUsername, mEmail, mPassword;
    private FirebaseAuth mAuth;
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
                    runOnUiThread(() -> {
                        findViewById(R.id.progressBar_register_loading).setVisibility(View.VISIBLE);
                    });
                    autoRegister(username, email, password);
                }
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

                                // Save new User locally
                                ((App) getApplication()).setLoginUser(newUser);

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

}