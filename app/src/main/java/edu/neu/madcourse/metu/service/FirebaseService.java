package edu.neu.madcourse.metu.service;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;

import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.models.Contact;
import edu.neu.madcourse.metu.models.User;

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

    public void updateUserProfile(User user) {
        // Firebase does not allow '.' character in key.
        String key = user.getEmail().replace(".", "");
        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put("userId", user.getUserId());
        userValues.put("nickname", user.getNickname());
        userValues.put("password", user.getPassword());
        userValues.put("location", user.getLocation());
        userValues.put("age", user.getAge());
        userValues.put("gender", user.getGender());
        userValues.put("avatarUri", user.getAvatarUri());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + key, userValues);

        databaseRef.updateChildren(childUpdates);
    }

    public void updateUserAvatar(String userId, String imageFirebaseUri) {
        databaseRef.child("users").child(userId).child("avatarUri").setValue(imageFirebaseUri);
    }


    public void fetchUserProfileDataOneTime(String userId, DataFetchCallback<User> callback) {
        databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback(snapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }


    public void fetchUserProfileData(String userId, DataFetchCallback<User> callback) {
        databaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback(snapshot.getValue(User.class));
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

    public void fetchContacts(String userId, Map<String, Boolean> connections,
                              DataFetchCallback<List<Contact>> callback) {
        if (connections == null || connections.isEmpty()) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        databaseRef.child("connections").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Contact> fetchedContacts = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String connectionId = child.getKey();

                    if (connections.containsKey(connectionId)) {
                        Connection fetchedConnection = child.getValue(Connection.class);
                        ConnectionUser connectionContact =
                                fetchedConnection.getUser1().getUserId().equals(userId) ?
                                        child.child("user2").getValue(ConnectionUser.class) :
                                        child.child("user1").getValue(ConnectionUser.class);
                        Contact fetchedContact = new Contact(
                                connectionId,
                                connectionContact.getUserId(),
                                connectionContact.getNickname(),
                                connectionContact.getAvatarUri(),
                                fetchedConnection.getConnectionPoint());
                        fetchedContacts.add(fetchedContact);
                    }
                }

                callback.onCallback(fetchedContacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void updateVideoHistory(String connectionId, DataFetchCallback<Long> callback) {
        String date = new SimpleDateFormat("yyyy-M-dd").format(new Date());
        databaseRef.child("videos").child(connectionId).child(date).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long newCount = 1;
                        if (snapshot.exists()) {
                            newCount = (long) snapshot.getValue() + 1;
                        }
                        databaseRef.child("videos").child(connectionId).child(date).setValue(newCount);
                        //snapshot.getRef().setValue(newCount);
                        callback.onCallback(newCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                }
        );
    }

    public void addConnectionPoint(int pointIncrement, String connectionId) {
        databaseRef.child("connections").child(connectionId).child("connectionPoint")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long newPoint = (long) snapshot.getValue() + pointIncrement;
                        snapshot.getRef().setValue(newPoint);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
