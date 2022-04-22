package edu.neu.madcourse.metu.service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.neu.madcourse.metu.models.NewUser;

public class FirebaseService {
    private static FirebaseService singleton_instance = null;
    private final DatabaseReference databaseRef;

    private FirebaseService() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public static FirebaseService getInstance() {
        if (singleton_instance == null) {
            singleton_instance = new FirebaseService();
        }
        return singleton_instance;
    }

    public void updateUserProfile(String username, String email, String location, Integer age,
                                  String gender) {
        // Firebase does not allow '.' character in key.
        String key = email.replace(".", "DOT");
        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put("username", username);
        userValues.put("email", email);
        userValues.put("location", location);
        userValues.put("age", age);
        userValues.put("gender", gender);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + key, userValues);

        databaseRef.updateChildren(childUpdates);
    }

    public void fetchUserProfileData(String userId, DataFetchCallback<NewUser> callback) {
        databaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback(snapshot.getValue(NewUser.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
}
