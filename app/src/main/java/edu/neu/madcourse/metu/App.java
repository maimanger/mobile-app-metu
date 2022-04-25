package edu.neu.madcourse.metu;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.utils.FCMTokenUtils;
import edu.neu.madcourse.metu.video.DismissCanceledCallNotifReceiver;
import edu.neu.madcourse.metu.video.RefuseVideoCallReceiver;
import edu.neu.madcourse.metu.video.VideoActivity;
import edu.neu.madcourse.metu.utils.Utils;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallEventListener;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    // Activity life cycle members
    private int foregroundActivityCount = 0;
    private int aliveActivityCount = 0;
    private boolean isActivityChangingConfigurations = false;
    private String currActivityName;



    // Call invitation function members
    private int callNotificationId = -1;
    private List<Integer> canceledCallNotificationIds = new ArrayList<>();
    private RemoteInvitation remoteInvitation;
    private LocalInvitation localInvitation;
    private RtmClient rtmClient;
    private RtmCallManager rtmCallManager;
    private AgoraEventListener agoraEventListener;

    private User loginUser;


    private String userId;
    private String userNickname;
    private String userAvatarUrl = "https://" + userNickname + ".png";

    private String fcmToken = "";

    /*private Map<String, Integer> peersOnlineStatus;*/

    class AgoraEventListener implements RtmClientListener, RtmCallEventListener {
        private Map<String, RtmClientListener> activityClientListeners;
        private Map<String, RtmCallEventListener> activityCallEventListeners;

        public AgoraEventListener() {
            activityClientListeners = new HashMap<>();
            activityCallEventListeners = new HashMap<>();
        }

        private void registerClientListener(RtmClientListener clientListener) {
            String activityName = ((Activity) clientListener).getClass().getSimpleName();
            Log.d("App", "registerClientListener: " + activityName);
            activityClientListeners.put(activityName, clientListener);
        }

        private void removeClientListener(RtmClientListener clientListener) {
            String activityName = ((Activity) clientListener).getClass().getSimpleName();
            activityClientListeners.remove(activityName, clientListener);
        }

        private void registerCallEventListener(RtmCallEventListener callEventListener) {
            String activityName = ((Activity) callEventListener).getClass().getSimpleName();
            Log.d("App", "registerCallEventListener: " + activityName);
            activityCallEventListeners.put(activityName, callEventListener);
        }

        private void removeCallEventListener(RtmCallEventListener callEventListener) {
            String activityName = ((Activity) callEventListener).getClass().getSimpleName();
            activityCallEventListeners.remove(activityName, callEventListener);
        }

        @Override
        public void onConnectionStateChanged(int i, int i1) { }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, String s) { }

        @Override
        public void onImageMessageReceivedFromPeer(RtmImageMessage rtmImageMessage, String s) { }

        @Override
        public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) { }

        @Override
        public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) { }

        @Override
        public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) { }

        @Override
        public void onTokenExpired() { }

        @Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
            // Update local subscribed peers status
            /*new Thread(() -> {
                for (Map.Entry<String, Integer> peerStatus : map.entrySet()) {
                    peersOnlineStatus.put(peerStatus.getKey(), peerStatus.getValue());
                }
            }).start();*/

            // Trigger current activity listener's method to update corresponding UI
            Log.d("App", "onPeersOnlineStatusChanged: " + currActivityName);
            RtmClientListener activityListener = activityClientListeners.get(currActivityName);
            if (activityListener != null) {
                activityListener.onPeersOnlineStatusChanged(map);
            }
        }

        @Override
        public void onLocalInvitationReceivedByPeer(LocalInvitation localInvitation) {
            RtmCallEventListener activityListener = activityCallEventListeners.get(currActivityName);
            if (activityListener != null) {
                activityListener.onLocalInvitationReceivedByPeer(localInvitation);
            }
        }

        @Override
        public void onLocalInvitationAccepted(LocalInvitation localInvitation, String s) {
            RtmCallEventListener activityListener = activityCallEventListeners.get(currActivityName);
            if (activityListener != null) {
                activityListener.onLocalInvitationAccepted(localInvitation, s);
            }
        }

        @Override
        public void onLocalInvitationRefused(LocalInvitation localInvitation, String s) {
            RtmCallEventListener activityListener = activityCallEventListeners.get(currActivityName);
            if (activityListener != null) {
                activityListener.onLocalInvitationRefused(localInvitation, s);
            }
        }

        @Override
        public void onLocalInvitationCanceled(LocalInvitation localInvitation) { }

        @Override
        public void onLocalInvitationFailure(LocalInvitation localInvitation, int i) { }

        @Override
        public void onRemoteInvitationReceived(RemoteInvitation remoteInvitation) {
            RtmCallEventListener activityListener = activityCallEventListeners.get(currActivityName);
            if (activityListener != null) {
                activityListener.onRemoteInvitationReceived(remoteInvitation);
            }
        }

        @Override
        public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) { }

        @Override
        public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) { }

        @Override
        public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {
            RtmCallEventListener activityListener = activityCallEventListeners.get(currActivityName);
            if (activityListener != null) {
                activityListener.onRemoteInvitationCanceled(remoteInvitation);
            }
        }

        @Override
        public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int i) { }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        agoraEventListener = new AgoraEventListener();
        //peersOnlineStatus = new HashMap<>();

        try {
            rtmClient = RtmClient.createInstance(
                    getBaseContext(), getString(R.string.agora_app_id), agoraEventListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert rtmClient != null;
        rtmCallManager = rtmClient.getRtmCallManager();
        rtmCallManager.setEventListener(agoraEventListener);
    }


    public void registerClientListener(RtmClientListener clientListener) {
        agoraEventListener.registerClientListener(clientListener);
    }


    public void removeClientListener(RtmClientListener clientListener) {
        agoraEventListener.removeClientListener(clientListener);
    }

    public void registerCallEventListener(RtmCallEventListener callEventListener) {
        agoraEventListener.registerCallEventListener(callEventListener);
    }

    public void removeCallEventListener(RtmCallEventListener callEventListener) {
        agoraEventListener.removeCallEventListener(callEventListener);
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        aliveActivityCount++;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++foregroundActivityCount == 1 && !isActivityChangingConfigurations) {
            // App enters foreground -> stop the service
            // set the current user to be available
            // todo: check if logged in
            if (userId != null && userId.length() > 0) {
                FCMTokenUtils.setStatusActive(userId);
                // reset the token
                fcmToken = "";
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currActivityName = activity.getClass().getSimpleName();
        Log.d("App", "onActivityResumed: " + currActivityName);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) { }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // Orientation change will also trigger activity onStop
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--foregroundActivityCount == 0 && !isActivityChangingConfigurations) {
            // set the current user to be inactive
            if (userId != null && userId.length() > 0) {
                FCMTokenUtils.setStatusInactive(userId);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) { }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--aliveActivityCount == 0 && !isActivityChangingConfigurations) {
            rtmClient.logout(new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("App", "onSuccess: Logout RTM");
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) { }
            });

            // remove the FCM token
            if (userId != null && userId.length() > 0) {
                FCMTokenUtils.removeFCMToken(userId);
                fcmToken = "";
            }
        }
    }







    // TODO: Check LoginUser's notification allowance
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void sendCanceledCallNotification(RemoteInvitation remoteInvitation) {
        String callerName = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_NAME);
        String callerAvatarUrl = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_AVATAR);

        Bitmap bitmap = Utils.getBitmapFromUri(callerAvatarUrl);
        if (bitmap == null) {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.user_avatar, null);
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        }

        // Register the new channel with the system
        NotificationChannel channel = new NotificationChannel(
                "CanceledVideoCallNotification", "CanceledVideoCallNotification",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Video chat invitation canceled");
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        // Create notification
        int notifId = (int)(new Timestamp(System.currentTimeMillis())).getTime();

        Intent dismissIntent = new Intent(this, DismissCanceledCallNotifReceiver.class);
        dismissIntent.putExtra("NOTIFICATION_ID", notifId);
        PendingIntent pendingDismissIntent =PendingIntent.getBroadcast(
                this, notifId, dismissIntent, 0);

        // TODO: Go to caller Profile (REMEMBER: App.removeCanceledCallNotificationId(notifId))
        Intent contentIntent = new Intent(this, UserProfileActivity.class);
        //contentIntent.putExtra("NOTIFICATION_ID", notifId);
        PendingIntent pendingContentIntent =PendingIntent.getActivity(
                this, notifId * 2, contentIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                "CanceledVideoCallNotification")
                .setContentTitle("Missed video call")
                .setContentText("You have a missed video invitation")
                .setSmallIcon(R.drawable.ic_launcher_metu_foreground)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setGroup("CanceledVideoCallNotification")
                .setGroupSummary(canceledCallNotificationIds.isEmpty())
                .setContentIntent(pendingContentIntent)
                .addAction(R.drawable.ic_launcher_metu_foreground, "Dismiss", pendingDismissIntent);

        Notification notif = builder.build();
        NotificationManagerCompat.from(this).notify("CanceledVideoCall" + notifId, notifId, notif);
        canceledCallNotificationIds.add(notifId);
    }


    // TODO: Check LoginUser's notification allowance
    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean sendCallNotification() {
        if (foregroundActivityCount == 0) {
            String callerName = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_NAME);
            String callerAvatarUrl = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_AVATAR);
            Bitmap bitmap = Utils.getBitmapFromUri(callerAvatarUrl);
            if (bitmap == null) {
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.user_avatar, null);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
            }

            // Register the new channel with the system
            NotificationChannel channel = new NotificationChannel(
                    "VideoCallNotification", "VideoCallNotification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setDescription("Video chat invitation");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            // Create notification
            int notifId = (int)(new Timestamp(System.currentTimeMillis())).getTime();

            Intent refuseIntent = new Intent(this, RefuseVideoCallReceiver.class);
            refuseIntent.putExtra("NOTIFICATION_ID", notifId);
            PendingIntent pendingRefuseIntent =PendingIntent.getBroadcast(
                    this, notifId, refuseIntent, 0);

            Intent acceptIntent = new Intent(this, VideoActivity.class);
            acceptIntent.putExtra("NOTIFICATION_ID", notifId);
            PendingIntent pendingAcceptIntent =PendingIntent.getActivity(
                    this, notifId * 2, acceptIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    this,
                    "VideoCallNotification")
                    .setContentTitle(callerName)
                    .setContentText("is inviting you for a video chat")
                    .setSmallIcon(R.drawable.ic_launcher_metu_foreground)
                    .setLargeIcon(bitmap)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                    .addAction(R.drawable.ic_launcher_metu_foreground, "Refuse", pendingRefuseIntent)
                    .addAction(R.drawable.ic_launcher_metu_foreground, "Accept", pendingAcceptIntent);

            Notification notif = builder.build();
            Log.d("App", "sent notification: " + channel.toString());
            NotificationManagerCompat.from(this).notify("VideoCall" + notifId, notifId, notif);
            // Mark the callNotificationId
            callNotificationId = notifId;
            return true;
        }
        return false;
    }


    public void sendCallInvitation(String calleeId, int connectionPoint, String connectionId) {
        if (loginUser != null) {
            localInvitation = rtmCallManager.createLocalInvitation(calleeId);

            localInvitation.setContent(Utils.createCallInvitationContent(
                    loginUser.getNickname(), loginUser.getAvatarUri(),
                    connectionPoint, connectionId));

            rtmCallManager.sendLocalInvitation(localInvitation, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("App", "onSuccess: SendLocalInvitation");
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                }
            });
        }
    }


    public void acceptCallInvitation() {
        if (remoteInvitation != null) {
            rtmCallManager.acceptRemoteInvitation(remoteInvitation, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("App", "acceptRemoteInvitation onSuccess");
                    remoteInvitation = null;
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                }
            });
        }
    }

    public void refuseCallInvitation() {
        if (remoteInvitation != null) {
            rtmCallManager.refuseRemoteInvitation(remoteInvitation, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("App", "onSuccess: refuse RemoteInvitation");
                    remoteInvitation = null;
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                }
            });
        }
    }

    public void cancelCallInvitation() {
        if (localInvitation != null) {
            rtmCallManager.cancelLocalInvitation(localInvitation, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("App", "onSuccess: cancel localInvitation");
                    localInvitation = null;
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) { }
            });
        }
    }

    public void rtmLogin(String userId) {
        rtmClient.login(null, userId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("App", "onSuccess: RTM login " + userId);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
            }
        });
    }


    public void rtmSubscribePeer(Set<String> peersId) {
        rtmClient.subscribePeersOnlineStatus(peersId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("App", "onSuccess: subscribe peersOnlineStatus");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    public void queryPeerOnlineStatus(Set<String> peersId, ResultCallback<Map<String, Boolean>> callback) {
        rtmClient.queryPeersOnlineStatus(peersId, callback);
    }


    public RemoteInvitation getRemoteInvitation() {
        return remoteInvitation;
    }


    public void setRemoteInvitation(RemoteInvitation remoteInvitation) {
        this.remoteInvitation = remoteInvitation;
    }

    public LocalInvitation getLocalInvitation() {
        return localInvitation;
    }

    public void setLocalInvitation(LocalInvitation localInvitation) {
        this.localInvitation = localInvitation;
    }

    public RtmClient getRtmClient() {
        return rtmClient;
    }

    public RtmCallManager getRtmCallManager() {
        return rtmCallManager;
    }

/*    public boolean getPeerOnlineStatus(String peerId) {
        return peersOnlineStatus != null ? peersOnlineStatus.get(peerId) == 0 : false;
    }*/




    public int getCallNotificationId() {
        return callNotificationId;
    }

    public void setCallNotificationId(int callNotificationId) {
        this.callNotificationId = callNotificationId;
    }

    public void removeCanceledCallNotificationId(int canceledCallNotificationId) {
        canceledCallNotificationIds.remove((Integer)canceledCallNotificationId);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(User loginUser) {
        this.loginUser = loginUser;
    }

    public String getFcmToken() {
        return this.fcmToken;
    }

    public void setFcmToken(String s) {
        this.fcmToken = s;
    }
}