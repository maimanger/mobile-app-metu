package edu.neu.madcourse.metu.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * class to handle FCM token and user availabilities
 */
public class FCMTokenUtils {
    public static String fcmToken = "";

    public static void removeFCMToken(String userId) {
        Log.d("FCM", "removeFCMToken method being called");
        if (userId == null || userId.length() == 0) {
            return;
        }
        FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                .child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("FCM", "Successfully removed");
            }
        });
        fcmToken = "";
    }


    public static void updateFCMToken(String userId) {
        if (userId == null || userId.length() == 0) {
            return;
        }
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            // remove all the records with token == s
            FirebaseDatabase.getInstance()
                    .getReference(Constants.FCM_TOKENS_STORE)
                    .orderByValue()
                    .equalTo(s)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                    if (!dataSnapshot.getKey().equals(userId)) {
                                        // remove this record
                                        dataSnapshot.getRef().removeValue();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            // update and save the token if success
            FirebaseDatabase.getInstance()
                    .getReference(Constants.FCM_TOKENS_STORE)
                    .child(userId)
                    .setValue(s)
                    .addOnSuccessListener(unused -> {
                        Log.d("FCM", "token updated for user: " + userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.d("FCM", "failed to updated token for user: " + userId);
                    });
            // save the token locally
            fcmToken = s;
        }).addOnFailureListener(e -> {
            Log.d("FCM", "failed to fetch the token");
        });
    }

    public static void setStatusActive(String userId) {
        if (userId == null || userId.length() == 0) {
            return;
        }
        FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                .child(userId)
                .setValue(true);
    }

    public static void setStatusInactive(String userId) {
        if (userId == null || userId.length() == 0) {
            return;
        }
        FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                .child(userId)
                .setValue(false);
    }


}
