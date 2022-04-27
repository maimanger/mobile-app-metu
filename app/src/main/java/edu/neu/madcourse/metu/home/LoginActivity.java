package edu.neu.madcourse.metu.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;

public class LoginActivity extends AppCompatActivity {
    private Button mLogin;
    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                }
            }
        };

        mLogin = (Button) findViewById(R.id.loginbtn);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password.", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Sign in error!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String userId = email.replaceAll("\\.", "");
                                Log.d("App", "login success: " + userId);

                                // update lastLoginTime and send to firebase
                                Long currentTime = System.currentTimeMillis();
                                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE).child(userId).child("lastLoginTime").setValue(currentTime);

                                // fetch the user info from the database
                                FirebaseService.getInstance().fetchUserProfileData(userId,
                                        (User user) -> {
                                            if (user != null) {
                                                Log.d("LoginActivity", "login profile fetched " + user.getUserId());
                                            } else {
                                                Log.d("LoginActivity", "login profile fetched null");
                                            }
                                            ((App)getApplication()).setLoginUser(user);

                                            // update the token
                                            FCMTokenUtils.updateFCMToken(userId);
                                            ((App) getApplication()).setFcmToken(FCMTokenUtils.fcmToken);
                                            // set the status
                                            FCMTokenUtils.setStatusActive(userId);

                                            // rmt login
                                            ((App)getApplication()).rtmLogin(userId);
                                            //return;

                                            Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        });
                            }
                        }
                    });
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.register);
        linkSignUp.setPaintFlags(linkSignUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

}