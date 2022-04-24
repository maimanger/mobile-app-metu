package edu.neu.madcourse.metu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import edu.neu.madcourse.metu.contacts.ContactsActivity;
import edu.neu.madcourse.metu.profile.EditProfileActivity;
import edu.neu.madcourse.metu.profile.UserProfileActivity;

import android.widget.Button;

import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.FakeDatabase;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private String username;

    private Button okButton;
    private EditText inputName;
    private Button recentChats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        initCurrentUser();
        initRecentConversations();

        Button explore = findViewById(R.id.openExploringActivity);
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExploringActivity.class);
                startActivity(intent);
            }
        });

    }


    public void onClickContacts(View view) {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);

    }

    public void initCurrentUser() {
        okButton = findViewById(R.id.okButton);
        inputName = findViewById(R.id.inputUsername);

        okButton.setOnClickListener(view -> {
            String userInputName = inputName.getText().toString();
            // if the username input is valid and not the current one
            if (userInputName != null && userInputName.trim().length() > 0 && !userInputName.equals(username)) {
                // auth the user somehow
                Log.d("LOGIN", userInputName + " trying to log in ");
                App.userLogin(userInputName);
//                FirebaseDatabase.getInstance().getReference("Users")
//                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.exists() && snapshot.hasChild(userInputName)) {
//                                    // todo: save the username and generate the token
////                                    editor = getSharedPreferences("METU_APP", MODE_PRIVATE).edit();
////                                    editor.putString("USERNAME", userInputName);
////                                    editor.apply();
//                                    App.userLogin(userInputName);
//                                    // todo: delete
//                                    System.out.println(App.getUsername());
//                                    username = userInputName;
//
//                                    // todo: generate and update the token
//                                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
//                                        // update and save the token if success
//                                        FirebaseDatabase.getInstance()
//                                                .getReference(Constants.FCM_TOKENS_STORE)
//                                                .child(userInputName)
//                                                .setValue(s)
//                                                .addOnSuccessListener(unused -> {
//                                                    Toast.makeText(getApplicationContext(), "Token updated!", Toast.LENGTH_SHORT).show();
//                                                })
//                                                .addOnFailureListener(e -> {
//                                                     Toast.makeText(getApplicationContext(), "Failed to update token", Toast.LENGTH_SHORT).show();
//                                        });
//                                        // save the token locally
//                                        App.setFcmToken(s);
//                                    }).addOnFailureListener(e -> {
//                                        Toast.makeText(getApplicationContext(), "Fail to get token", Toast.LENGTH_SHORT).show();
//                                    });
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });


            }

        });
    }

    public void initRecentConversations() {
        recentChats = findViewById(R.id.openRecentConversation);
        recentChats.setOnClickListener(view -> {
            if (App.isLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, RecentConversationActivity.class);
                startActivity(intent);
            }
        });
    }
}
