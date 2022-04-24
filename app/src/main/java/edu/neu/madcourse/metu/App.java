package edu.neu.madcourse.metu;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.neu.madcourse.metu.utils.Constants;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    // todo: save the current user info
    private static String userId = "";
    private static String nickname = "";
    private static String avatarUri = "";
    private static String fcmToken = "";

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    public static void userLogin(String s) {
        if (isLoggedIn()) {
            userLogout();
        }

        // fetch the user info from the database
        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .orderByChild(Constants.USER_USER_ID)
                .equalTo(s)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot d: snapshot.getChildren()) {
                    userId = d.child(Constants.USER_USER_ID).getValue(String.class);
                    nickname = d.child(Constants.USER_NICKNAME).getValue(String.class);
                    avatarUri = d.child(Constants.USER_AVATAR_URI).getValue(String.class);
                    // update the token
                    updateFcmToken(userId);
                    // set to be available
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                            .child(userId)
                            .setValue(true);

                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // todo:
    public static void userLogout() {
        // set the current user to be not available
        if (isLoggedIn()) {
            // set to be inactive
            FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                    .child(userId)
                    .setValue(false);

            // remove the fcm token
            FirebaseDatabase.getInstance().getReference(Constants.FCM_TOKENS_STORE)
                    .child(userId)
                    .setValue(null);

            // remove the current user info
            userId = "";
            nickname = "";
            fcmToken = "";
            avatarUri = "";
        }
    }

    public static void setFcmToken(String token) {
        fcmToken = token;
    }

    public static String getFcmToken() {
        return fcmToken;
    }

    public static String getUserId() {
        return userId;
    }

    public static String getNickname() { return nickname; }

    public static String getAvatarUri() {
        return avatarUri;
    }


    // todo: update with auth
    public static boolean isLoggedIn() {
        return userId != null && userId.length() > 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        // reset the username
        userId = "";
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            // todo: check if the user has logged in
            if (isLoggedIn()) {
                // set the user to be available
                FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                        .child(userId)
                        .setValue(true);
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // Orientation change will also trigger activity onStop
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            // start the service only when the user has logged in

            if(isLoggedIn()) {
                // set the user to be not available
                FirebaseDatabase.getInstance().getReference(Constants.USERS_AVAILABILITY_STORE)
                        .child(userId)
                        .setValue(false);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    private static void updateFcmToken(String username) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            // update and save the token if success
            FirebaseDatabase.getInstance()
                    .getReference(Constants.FCM_TOKENS_STORE)
                    .child(username)
                    .setValue(s)
                    .addOnSuccessListener(unused -> {
                        Log.d("FCM", "token updated");
                    })
                    .addOnFailureListener(e -> {
                        Log.d("FCM", "failed to updated token");
                    });
            // save the token locally
            App.setFcmToken(s);
        }).addOnFailureListener(e -> {
            Log.d("FCM", "failed to fetch the token");
        });
    }

}
