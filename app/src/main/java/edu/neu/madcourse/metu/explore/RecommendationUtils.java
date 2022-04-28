package edu.neu.madcourse.metu.explore;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.metu.explore.daos.PreferenceSetting;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.utils.Constants;

public class RecommendationUtils {
    public static String getDateKey() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Date date = new Date(ts.getTime());
        return dateFormat.format(date);

    }

    public static void fetchPreference(String userId, DataFetchCallback<PreferenceSetting> callback) {
        if (userId == null || userId.length() == 0) {
            callback.onCallback(null);
        }
        PreferenceSetting preferenceSetting = new PreferenceSetting();
        preferenceSetting.setUserId(userId);

        FirebaseDatabase.getInstance().getReference(Constants.EXPLORE_SETTINGS_STORE)
                .orderByChild(Constants.USER_USER_ID)
                .equalTo(userId)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot setting: snapshot.getChildren()) {
                                // todo: location
                                String location = setting.child(Constants.EXPLORE_SETTING_LOCATION).getValue(String.class);
                                // 0: only male checked
                                // 1: only female checked
                                // 2: only other checked
                                // 3: male and female
                                // 4: male and other
                                // 5: female and other
                                // 6: all
                                if (setting.hasChild(Constants.EXPLORE_SETTING_GENDER)) {
                                    int gender = setting.child(Constants.EXPLORE_SETTING_GENDER).getValue(int.class);
                                    preferenceSetting.setGenderPreference(gender);
                                }

                                if (setting.hasChild(Constants.EXPLORE_SETTING_AGE_MIN)
                                        && setting.hasChild(Constants.EXPLORE_SETTING_AGE_MAX)) {
                                    float ageMin = setting.child(Constants.EXPLORE_SETTING_AGE_MIN).getValue(float.class);
                                    float ageMax = setting.child(Constants.EXPLORE_SETTING_AGE_MAX).getValue(float.class);

                                    preferenceSetting.setAgeMin(ageMin);
                                    preferenceSetting.setAgeMax(ageMax);

                                }

                                callback.onCallback(preferenceSetting);
                            }
                        } else {
                            callback.onCallback(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback(null);
                    }
                });


    }

    public static void fetchRecommendUsers(String userId, DataFetchCallback<List<RecommendedUser>> callback) {
        // if we already have saved recommendation list for today
        List<RecommendedUser> recommendedUsers = new ArrayList<>();

        fetchRecommendUserIdsFromDatabase(userId, (ids) -> {
            if (ids != null && ids.size() > 0) {
                // get recommendedUsers by ids
                for (String id: ids) {
                    // getRecommendedUserById()
                }
            } else {


            }





        });
    }

    public static void getRecommendedUserById(String senderId, String receiverId, DataFetchCallback<RecommendedUser> callback) {
        RecommendedUser recommendedUser = new RecommendedUser();
        recommendedUser.setUserId(receiverId);
        FirebaseDatabase.getInstance().getReference(Constants.USER_STORIES)
                .child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                if (snapshot.hasChild(Constants.USER_NICKNAME)) {
                                    String nickname = snapshot.child(Constants.USER_NICKNAME).getValue(String.class);
                                    recommendedUser.setNickname(nickname);
                                }

                                if (snapshot.hasChild(Constants.USER_GENDER)) {
                                    int gender = snapshot.child(Constants.USER_GENDER).getValue(int.class);
                                    recommendedUser.setGender(gender);
                                }

                                if (snapshot.hasChild(Constants.USER_AVATAR_URI)) {
                                    String avatarUri = snapshot.child(Constants.USER_AVATAR_URI).getValue(String.class);
                                    recommendedUser.setAvatarUri(avatarUri);
                                }

                                // fetch the connection





                            } catch (Exception e) {

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    public static void fetchConnection(String userId1, String userId2, DataFetchCallback<Connection> callback) {
        if (userId1 == null || userId1.length() == 0 || userId2 == null || userId2.length() == 0) {
            callback.onCallback(null);
        }

        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild(userId1 + userId1)) {

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    public static void fetchRecommendUserIdsFromDatabase(String userId, DataFetchCallback<List<String>> callback) {
        String date = getDateKey();
        List<String> ids = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference(Constants.RECOMMENDATIONS_STORE)
                .child(userId)
                .child(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot id: snapshot.getChildren()) {
                                try {
                                    String idValue = id.getValue(String.class);
                                    ids.add(idValue);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }

                            callback.onCallback(ids);
                        } else {
                            callback.onCallback(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback(null);
                    }
                });

    }




}
