package edu.neu.madcourse.metu.service;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import edu.neu.madcourse.metu.contacts.daos.User;
import edu.neu.madcourse.metu.models.NewUser;

public class FirebaseService {
    private static FirebaseService singleton_instance = null;
    private final DatabaseReference databaseRef;
    private final StorageReference storageRef;

    private FirebaseService() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
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

    public void updateUserAvatar(String userId, String imageFirebaseUri) {
        databaseRef.child("users").child(userId).child("avatarUri").setValue(imageFirebaseUri);
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

    public void fetchTagList(String userId, DataFetchCallback<Map<String, Boolean>> callback) {
        databaseRef.child("users").child(userId).child("tags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback((Map<String, Boolean>) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void addTag(String userId, String tagContent) {
        databaseRef.child("users").child(userId).child("tags").child(tagContent).setValue(true);
    }

    public void fetchStoryList(String userId, DataFetchCallback<Map<String, String>> callback) {
        databaseRef.child("users").child(userId).child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback((Map<String, String>) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void addStory(String userId, String storyImageUri) {
        String key = databaseRef.child("users").child(userId).child("stories").push().getKey();
        databaseRef.child("users").child(userId).child("stories").child(key).setValue(storyImageUri);
    }

    public void fetchAvatarUri(String userId, DataFetchCallback<Uri> callback) {
        databaseRef.child("users").child(userId).child("avatarUri").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback(Uri.parse(snapshot.getValue(String.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });


    }
}
