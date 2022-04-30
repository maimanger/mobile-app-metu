package edu.neu.madcourse.metu.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.network.ApiClient;
import edu.neu.madcourse.metu.utils.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        User loginUser = ((App) getApplication()).getLoginUser();

        // not allowed to send the message notification
        if (loginUser == null || !loginUser.getAllowMessageNotif()) {
            return;
        }
        // check the type of notification
        super.onMessageReceived(message);

        // check the notification type
        String notificationType = message.getData().get(Constants.NOTIFICATION_TYPE);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, RecentConversationActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_new_msg_notification);

        if (notificationType.equals(Constants.NOTIFY_GET_A_LIKE)) {

        } else if (notificationType.equals(Constants.NOTIFY_NEW_MSG)) {
            // packing the ConnectionUser
            ConnectionUser sender = new ConnectionUser();
            sender.setUserId(message.getData().get(Constants.MSG_SENDER_USER_ID));
            sender.setNickname(message.getData().get(Constants.MSG_SENDER_NICKNAME));
            sender.setAvatarUri(message.getData().get(Constants.MSG_SENDER_AVATAR_URI));
            String connectionId = message.getData().get(Constants.CONNECTION_ID);

            builder.setContentTitle(sender.getNickname());

        }

        builder.setContentText(message.getData().get(Constants.MESSAGE_CONTENT));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getData().get(Constants.MESSAGE_CONTENT)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String description = "This notification is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());

    }

    public static void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                Log.d("FCM", error.getString("error"));
                                return;
                            }
                        }
                    } catch (Exception e) {
                        // todo: log it maybe
                        e.printStackTrace();
                    }
                    // showToast("Notification sent successfully");
                } else {
                    Log.d("FCM", "error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                // showToast(t.getMessage());
                Log.d("FCM", t.getMessage());
            }
        });
    }


}
