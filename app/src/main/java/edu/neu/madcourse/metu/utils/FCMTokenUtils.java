package edu.neu.madcourse.metu.utils;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.neu.madcourse.metu.models.User;

/**
 * class to handle FCM token and user availabilities
 */
public class FCMTokenUtils {
    public static String fcmToken = "";

    public static void removeFCMToken(String userId) {
        FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                .child(userId)
                .setValue(null);
        fcmToken = "";
    }


    public static void updateFCMToken(String userId) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
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
        FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                .child(userId)
                .setValue(true);
    }

    public static void setStatusInactive(String userId) {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                .child(userId)
                .setValue(false);
    }


}
