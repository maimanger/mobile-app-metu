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
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // userID
                mEmail = (EditText) findViewById(R.id.email);
                String email = mEmail.getText().toString();
                String userID = email.replaceAll("\\.", "");

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // call FCMTokenUtils
                    FCMTokenUtils.updateFCMToken(userID);
                    ((App) getApplication()).setFcmToken(FCMTokenUtils.fcmToken);
                    FCMTokenUtils.setStatusActive(userID);

                    // RTM login
                    ((App)getApplication()).rtmLogin(userID);
                    new Thread(() -> {
                        FirebaseService.getInstance().fetchUserProfileData(userID,
                                (User userRTM) -> {
                                    Log.d("RegisterActivity", "login profile fetched ");
                                    ((App)getApplication()).setLoginUser(userRTM);
                                });
                    }).start();

                    mEmail = (EditText) findViewById(R.id.email);

                    Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    //return;
                }
            }
        };

        mRegister = (Button) findViewById(R.id.signupbtn);
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUsername.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ) {
                    Toast.makeText(RegisterActivity.this, "Please enter username, email and password.", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this,"Sign up error!",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //String userID = mAuth.getCurrentUser().getUid();
                                String userID = email.replaceAll("\\.", "");

//                                Map<Integer,Boolean> settings =  new HashMap<Integer, Boolean>() {
//                                    {
//                                        put(1, true);
//                                        put(2, true);
//                                        put(3, true);
//                                    }
//                                };
                                User newUser = new User(userID,username,password,email,"",0,2, new HashMap<>(),new HashMap<>(),"",new HashMap<>(),System.currentTimeMillis(),true,true,true);
                                FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE).child(userID).setValue(newUser);
                                // save User locally
                                ((App) getApplication()).setLoginUser(newUser);
//                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE).child(emailDOT);
//                                Map userInfo = new HashMap<>();
//                                userInfo.put("username", username);
//                                userInfo.put("email", email);
//                                userInfo.put("password", password);
//                                currentUserDb.updateChildren(userInfo);
                            }
                        }
                    });
                }
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