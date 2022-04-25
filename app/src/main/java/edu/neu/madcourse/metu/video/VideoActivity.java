package edu.neu.madcourse.metu.video;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Callback;

import java.util.Random;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.service.UpdateVideoHistoryService;
import edu.neu.madcourse.metu.utils.Utils;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.RtmCallEventListener;

public class VideoActivity extends AppCompatActivity implements RtmCallEventListener {
    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private static final String TAG = "VideoActivity";

    private FloatingActionButton muteBtn;
    private FloatingActionButton unmuteBtn;
    private ShapeableImageView videoAvatar;
    private TextView videoNickName;
    private TextView videoFriendLevel;
    private FrameLayout filterFrame;
    private ProgressBar loadingVideoProgress;
    private TextView loadingMsg;

    private boolean isPeerJoined = false;
    private boolean isCaller = false;
    private Integer currentFilterIdx;

    // TODO: channelName should be the Connection Id,
    //  and connectionPoint, nickname, avatarUrl should be fetched from database
    private String connectionId;
    private int connectionPoint;
    private int connectionLevel;
    private String friendNickname;
    private String friendAvatarUrl;

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel to get the uid of the user.
        public void onUserJoined(int uid, int elapsed) {
            isPeerJoined = true;
            Log.d(TAG, "onUserJoined: currentFilterIdx = " + currentFilterIdx);

            // Set up filters
            int filterId = Utils.getCurrentFilter(connectionLevel, currentFilterIdx);
            runOnUiThread(() -> {
                setupRemoteVideo(uid);
                loadingVideoProgress.setVisibility(View.INVISIBLE);
                loadingMsg.setVisibility(View.INVISIBLE);
                if (filterId == -9999) {
                    // Transparent filter
                    filterFrame.setBackgroundColor(0x00000000);
                } else {
                    filterFrame.setBackgroundResource(filterId);
                }
                filterFrame.setVisibility(View.VISIBLE);
            });

            // Caller must update video chat history and connection point in Firebase
            if (isCaller) {
                updateChatHistoryAndConnectionPoint();
            }
        }

        // remote user has left channel, finish the VideoActivity
        @Override
        public void onUserOffline(int uid, int reason) {
            finish();
        }
    };

    private void updateChatHistoryAndConnectionPoint() {
        Intent startServiceIntent = new Intent(this, UpdateVideoHistoryService.class);
        startServiceIntent.putExtra("CONNECTION_ID", connectionId);
        startServiceIntent.putExtra("INCREMENT_POINT", 2);
        startService(startServiceIntent);
    }


    private void initRtcEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
        setupRtcProfile();
        setupLocalVideo();
    }

    private void joinRtcChannel() {
        mRtcEngine.joinChannel(null, connectionId, "", 0);
    }


    private void setupRtcProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        mRtcEngine.setVideoEncoderConfiguration(
                new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_840x480,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        mRtcEngine.enableVideo();
    }

    private void setupLocalVideo() {
        runOnUiThread(() -> {
            FrameLayout videoContainer = findViewById(R.id.local_video_view_container);
            videoContainer.setClipToOutline(true);

            // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
            SurfaceView localVideoSurface = RtcEngine.CreateRendererView(getBaseContext());
            localVideoSurface.setBackgroundResource(R.drawable.rounded_shape_transparent);
            localVideoSurface.setZOrderMediaOverlay(true);
            localVideoSurface.setClipToOutline(true);
            videoContainer.addView(localVideoSurface);
            localVideoSurface.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            // Pass the SurfaceView object to Agora so that it renders the local video.
            // leave the uid parameter blank so that the SDK can handle creating a dynamic ID for each user
            mRtcEngine.setupLocalVideo(new VideoCanvas(localVideoSurface, VideoCanvas.RENDER_MODE_FILL, 0));
        });

    }

    private void setupRemoteVideo(int uid) {
        runOnUiThread(() -> {
            FrameLayout container = findViewById(R.id.remote_video_view_container);
            SurfaceView remoteVideoView = RtcEngine.CreateRendererView(getBaseContext());
            container.addView(remoteVideoView);
            mRtcEngine.setupRemoteVideo(new VideoCanvas(remoteVideoView, VideoCanvas.RENDER_MODE_FIT, uid));
            //If the video degrades, the engine reverts to audio only
            mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
        });
    }


    /*******************************Dealing with Permissions**************************************/
    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Sorry, MetU needs your permissions to continue video chat.");
                // if user is a callee, cancel the notification and refuse the call invitation
                calleeRoleInit(false);
                finish();
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            new Thread(() -> {
                initRtcEngine();
                calleeRoleInit(true);
                callerRoleInit();
                initFriendUI();
                createRandomFilterIdx();
                joinRtcChannel();
            }).start();
        }
    }
    /*******************************End Dealing with Permissions**********************************/

    // Run on UI thread to make a toast
    private void showLongToast(final String msg) {
        this.runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        });
    }


    public void onClickLeaveChannel(View view) {
        // Cancel LocalInvitation
        ((App) getApplication()).cancelCallInvitation();
        finish();
    }

    public void onClickMute(View view) {
        mRtcEngine.muteLocalAudioStream(true);
        muteBtn.setVisibility(View.INVISIBLE);
        unmuteBtn.setVisibility(View.VISIBLE);
    }

    public void onClickUnmute(View view) {
        mRtcEngine.muteLocalAudioStream(false);
        unmuteBtn.setVisibility(View.INVISIBLE);
        muteBtn.setVisibility(View.VISIBLE);
    }

    public void onClickChangeFilter(View view) {
        currentFilterIdx = (currentFilterIdx + 1) % (Utils.getFiltersSize(connectionLevel));
        int filterId = Utils.getCurrentFilter(connectionLevel, currentFilterIdx);
        if (filterId != -9999) {
            filterFrame.setBackgroundResource(filterId);
        } else {
            filterFrame.setBackgroundColor(0x00000000);
        }
    }

/*    private void removeVideo(int containerID) {
        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        ((App) getApplication()).registerCallEventListener(this);

        muteBtn = findViewById(R.id.fab_mute);
        unmuteBtn = findViewById(R.id.fab_unmute);
        videoAvatar = findViewById(R.id.image_video_avatar);
        videoNickName = findViewById(R.id.text_video_nickName);
        filterFrame = findViewById(R.id.frame_filter);
        videoFriendLevel = findViewById(R.id.text_video_friendLevel);
        loadingVideoProgress = findViewById(R.id.progressBar_loadingVideo);
        loadingMsg = findViewById(R.id.text_loadingMsg);

        // Check permissions and init RtcEngine
        if (checkPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            new Thread(() -> {
                initRtcEngine();
                calleeRoleInit(true);
                callerRoleInit();
                initFriendUI();
                createRandomFilterIdx();
                joinRtcChannel();
            }).start();
        }
    }


    private void createRandomFilterIdx() {
        if (connectionLevel > 2) {
            currentFilterIdx = Utils.getFiltersSize(connectionLevel) - 1;
        } else {
            currentFilterIdx = new Random().nextInt(Utils.getFiltersSize(connectionLevel));
        }
    }


    private void calleeRoleInit(boolean isPermitted) {
        // If VideoActivity is started from notification, dismiss the notification
        if (getIntent().hasExtra("NOTIFICATION_ID")) {
            new Thread(() -> {
                int notifId = getIntent().getIntExtra("NOTIFICATION_ID", 0);
                NotificationManagerCompat.from(this).cancel("VideoCall" + notifId, notifId);
                ((App) getApplication()).setCallNotificationId(-1);
            }).start();
        }

        RemoteInvitation remoteInvitation = ((App) getApplication()).getRemoteInvitation();
        if (remoteInvitation != null) {
            friendNickname = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_NAME);
            friendAvatarUrl = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_AVATAR);
            connectionPoint = Integer.parseInt(Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALL_CONNECTION_POINT));
            connectionId = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALL_CONNECTION_ID);
            connectionLevel = Utils.calculateFriendLevel(connectionPoint);

            if (isPermitted) {
                ((App) getApplication()).acceptCallInvitation();
            } else {
                ((App) getApplication()).refuseCallInvitation();
            }
            Log.d(TAG, "calleeRoleInit: connectionPoint = " + connectionPoint);
        }
    }

    private void callerRoleInit() {
        if (getIntent().hasExtra("CALLEE_ID")) {
            isCaller = true;
            String calleeId = getIntent().getStringExtra("CALLEE_ID");
            friendNickname = getIntent().getStringExtra("CALLEE_NAME");
            friendAvatarUrl = getIntent().getStringExtra("CALLEE_AVATAR");
            connectionPoint = getIntent().getIntExtra("CONNECTION_POINT", 1);
            connectionId = getIntent().getStringExtra("CONNECTION_ID");
            connectionLevel = Utils.calculateFriendLevel(connectionPoint);
            ((App) getApplication()).sendCallInvitation(calleeId, connectionPoint, connectionId);
            Log.d(TAG, "callerRoleInit: connectionPoint = " + connectionPoint);
        }
    }

    private void initFriendUI() {
        runOnUiThread(() -> {
            videoNickName.setText(friendNickname);
            videoFriendLevel.setText(Integer.toString(Utils.calculateFriendLevel(connectionPoint)));
            // TODO: VideoAvatar should be fetched from Firebase
            Utils.loadImgUri(friendAvatarUrl, videoAvatar, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(Exception e) {
                    videoAvatar.setImageResource(R.drawable.user_avatar);
                }
            });
            //videoAvatar.setImageResource(R.drawable.user_avatar);
        });
    }

    @Override
    protected void onDestroy() {
        ((App) getApplication()).removeCallEventListener(this);
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto cancel call invitation after 45s, if no one answered
        new Thread(() -> {
            long start = System.currentTimeMillis();
            long wait = start;
            while (wait - start <= 45000 && !isPeerJoined) {
                wait = System.currentTimeMillis();
            }

            if (!isPeerJoined) {
                runOnUiThread(() -> {
                    loadingMsg.setText("No answer...");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "No answer to video call");
                ((App) getApplication()).cancelCallInvitation();
                finish();
            }
        }).start();
    }

    @Override
    public void onLocalInvitationReceivedByPeer(LocalInvitation localInvitation) {
    }

    @Override
    public void onLocalInvitationAccepted(LocalInvitation localInvitation, String s) {
        ((App) getApplication()).setLocalInvitation(null);
    }

    @Override
    public void onLocalInvitationRefused(LocalInvitation localInvitation, String s) {
        Log.d(TAG, "onLocalInvitationRefused");
        runOnUiThread(() -> {
            loadingMsg.setText("Line is busy...");
        });
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ((App) getApplication()).setLocalInvitation(null);
        finish();
    }

    @Override
    public void onLocalInvitationCanceled(LocalInvitation localInvitation) {

    }

    @Override
    public void onLocalInvitationFailure(LocalInvitation localInvitation, int i) {

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRemoteInvitationReceived(RemoteInvitation remoteInvitation) {
        // Currently in a video chat, refuse the remote invitation automatically
        ((App) getApplication()).getRtmCallManager().refuseRemoteInvitation(remoteInvitation, null);
        // Send notification to remind of missed call
        ((App) getApplication()).sendCanceledCallNotification(remoteInvitation);
    }

    @Override
    public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) {

    }

    @Override
    public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) {

    }

    @Override
    public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {
        ((App) getApplication()).setRemoteInvitation(null);
        finish();
    }

    @Override
    public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int i) {

    }
}