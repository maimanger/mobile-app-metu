package edu.neu.madcourse.metu.explore;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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
import edu.neu.madcourse.metu.utils.MetUException;

public class RecommendationUtils {
    public static String getDateKey() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Date date = new Date(ts.getTime());
        return dateFormat.format(date);
    }

    public static boolean checkLocationPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static void fetchUserLatestLocation(String userId, DataFetchCallback<String> callback) {
        if (userId == null || userId.length() == 0) {
            callback.onCallback(null);
        }

        FirebaseDatabase.getInstance().getReference(Constants.USERS_LATEST_LOCATION_STORES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild(userId)) {
                            try {
                                String location = snapshot.child(userId).child(Constants.USER_STATE).getValue(String.class);
                                callback.onCallback(location);
                                return;
                            } catch (Exception e) {
                                callback.onCallback(null);
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
                                // String location = setting.child(Constants.EXPLORE_SETTING_LOCATION).getValue(String.class);
                                if (setting.hasChild(Constants.EXPLORE_SETTING_SHOW_PEOPLE_NEAR_ME)) {
                                    Boolean showPeopleNearMe = setting.child(Constants.EXPLORE_SETTING_SHOW_PEOPLE_NEAR_ME).getValue(boolean.class);
                                    preferenceSetting.setShowPeopleNearMe(showPeopleNearMe);
                                }
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


    public static void getRecommendedUsersByPreference(String userId, PreferenceSetting preferenceSetting, DataFetchCallback<List<RecommendedUser>> callback) {

        List<RecommendedUser> recommendedUsers = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            long childrenCount = snapshot.getChildrenCount();
                            for (DataSnapshot s: snapshot.getChildren()) {
                                RecommendedUser recommendedUser = s.getValue(RecommendedUser.class);

                                if (userId.equals(recommendedUser.getUserId())) {
                                    continue;
                                }

                                // System.out.println("get recommend by preference: " + recommendedUser);
                                // fetch the precise location of this recommended user
                                fetchUserLatestLocation(recommendedUser.getUserId(), location -> {
                                    if (location != null && location.length() > 0) {
                                        recommendedUser.setLocation(location);
                                    }


                                    updateRecommendedUserByConnection(userId, recommendedUser.getUserId(), recommendedUser, updatedUser -> {
                                        recommendedUsers.add(updatedUser);
                                        System.out.println("size: " + recommendedUsers.size());
                                        if (recommendedUsers.size() == childrenCount - 1) {
                                            // sort the recommends
                                            recommendedUsers.sort(new RecommendedUserComparator(preferenceSetting));
                                            System.out.println(recommendedUsers);
                                            if (recommendedUsers.size() > 5) {
                                                callback.onCallback(recommendedUsers.subList(0, 5));
                                            } else {
                                                callback.onCallback(recommendedUsers);
                                            }
                                        }
                                    });
                                });
                            }

                        } else {
                            callback.onCallback(recommendedUsers);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback(recommendedUsers);
                    }
                });

    }

    public static void getSingleRecommendedUserById(String senderId, String receiverId, DataFetchCallback<RecommendedUser> callback) {
        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                RecommendedUser recommendedUser = snapshot.getValue(RecommendedUser.class);
                                // fetch the connection
                                updateRecommendedUserByConnection(senderId, receiverId, recommendedUser, (updatedUser) -> {
                                    callback.onCallback(updatedUser);
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                callback.onCallback(null);
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


    public static void updateRecommendedUserByConnection(String senderId, String receiverId, RecommendedUser recommendedUser, DataFetchCallback<RecommendedUser> callback) {
        if (senderId == null || senderId.length() == 0 || receiverId == null || receiverId.length() == 0) {
            callback.onCallback(recommendedUser);
            return;
        }

        FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
//                .addValueEventListener(new ValueEventListener() {
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    String connectionId;
                    Connection connection;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (!snapshot.hasChild(senderId + receiverId) && !snapshot.hasChild(receiverId + senderId)) {
                                callback.onCallback(recommendedUser);
                                return;
                            }

                            if (snapshot.hasChild(senderId + receiverId)) {
                                connectionId = senderId + receiverId;
                            } else if (snapshot.hasChild(receiverId + senderId)) {
                                connectionId = receiverId + senderId;
                            }
                            connection = snapshot.child(connectionId).getValue(Connection.class);

                            if (connection == null) {
                                callback.onCallback(recommendedUser);
                                return;
                            }

                            try {
                                recommendedUser.setIsLiked(isReceiverBeingLiked(connection, receiverId));
                                recommendedUser.setConnectionId(connectionId);
                                recommendedUser.setConnectionPoint(connection.getConnectionPoint());
                                callback.onCallback(recommendedUser);
                            } catch (Exception e) {
                                callback.onCallback(recommendedUser);
                                return;
                            }
                        } else {
                            callback.onCallback(recommendedUser);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback(recommendedUser);
                    }
                });
    }

    public static boolean isReceiverBeingLiked(Connection connection, String receiverId) throws Exception{
        if (connection == null) {
            return false;
        }

        if (receiverId.equals(connection.getUser1().getUserId())) {
            return connection.getUser1().getIsLiked();
        } else if (receiverId.equals(connection.getUser2().getUserId())) {
            return connection.getUser2().getIsLiked();
        } else {
            throw new MetUException("Something went wrong");
        }


    }




    public static void fetchRecommendUserIdsFromDatabase(String userId, DataFetchCallback<List<String>> callback) {
        String date = getDateKey();
        List<String> ids = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference(Constants.RECOMMENDATIONS_STORE)
                .child(userId)
                .child(date)
                .orderByValue()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot id: snapshot.getChildren()) {
                                try {
                                    String idValue = id.getKey();
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
