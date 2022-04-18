package edu.neu.madcourse.metu.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Random;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.utils.Utils;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class VideoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private FloatingActionButton leaveBtn;
    private FloatingActionButton muteBtn;
    private FloatingActionButton unmuteBtn;
    private ShapeableImageView videoAvatar;
    private TextView videoNickName;
    private TextView videoFriendLevel;
    private FrameLayout filterFrame;
    private ProgressBar loadingVideoProgress;

    private Integer currentFilterIdx;

    // TODO: channelName should be the Connection Id
    // TODO: connectionPoint, nickname, avatarUrl should be fetched from database
    private String channelName = "first_demo";
    private int connectionPoint;
    private String friendNickname;
    private String friendAvatarUrl;
    private int friendLevel;

    private RtcEngine mRtcEngine;


    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel to get the uid of the user.
        public void onUserJoined(int uid, int elapsed) {
            // TODO: Update Video history in Firebase

            int filterId = Utils.getCurrentFilter(currentFilterIdx);
            runOnUiThread(() -> {
                setupRemoteVideo(uid);
                loadingVideoProgress.setVisibility(View.INVISIBLE);
                if (filterId == -9999) {
                    filterFrame.setBackgroundColor(0x00000000);
                } else {
                    filterFrame.setBackgroundResource(filterId);
                }
                filterFrame.setVisibility(View.VISIBLE);
            });
        }

        // remote user has left channel, remove the remote video view and finish the VideoActivity
        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                removeVideo(R.id.remote_video_view_container);// not necessary
                finish();
            });
        }
    };

    private void initRtcEngineAndJoin() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
        setupRtcProfile();
        setupLocalVideo();
        mRtcEngine.joinChannel(null, channelName, "", 0);
    }


    private void setupRtcProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        mRtcEngine.setVideoEncoderConfiguration(
                new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_840x480,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine.enableVideo();
    }

    private void setupLocalVideo() {
        FrameLayout videoContainer = findViewById(R.id.local_video_view_container);
        videoContainer.setClipToOutline(true);

        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        SurfaceView localVideoSurface = RtcEngine.CreateRendererView(getBaseContext());
        localVideoSurface.setZOrderMediaOverlay(true);
        videoContainer.addView(localVideoSurface);
        localVideoSurface.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Pass the SurfaceView object to Agora so that it renders the local video.
        // leave the uid parameter blank so that the SDK can handle creating a dynamic ID for each user
        mRtcEngine.setupLocalVideo(new VideoCanvas(localVideoSurface, VideoCanvas.RENDER_MODE_FILL, 0));
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView remoteVideoView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(remoteVideoView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(remoteVideoView, VideoCanvas.RENDER_MODE_FIT, uid));
        //If the video degrades, the engine reverts to audio only
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
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
            if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions to start a video chat!");
                finish();
                return;
            }
            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initRtcEngineAndJoin();
        }
    }

    // Run on UI thread to make a toast
    private void showLongToast(final String msg) {
        this.runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        });
    }
    /*******************************End Dealing with Permissions**********************************/


    public void onClickLeaveChannel(View view) {
        mRtcEngine.leaveChannel();
        removeVideo(R.id.remote_video_view_container); // not necessary
        removeVideo(R.id.local_video_view_container); // not necessary
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
        currentFilterIdx = (currentFilterIdx + 1) % (Utils.getFiltersSize(friendLevel));
        int filterId = Utils.getCurrentFilter(currentFilterIdx);
        if (filterId != -9999) {
            filterFrame.setBackgroundResource(filterId);
        } else {
            filterFrame.setBackgroundColor(0x00000000);
        }

    }

    private void removeVideo(int containerID) {
        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        muteBtn = findViewById(R.id.fab_mute);
        unmuteBtn = findViewById(R.id.fab_unmute);
        videoAvatar = findViewById(R.id.image_video_avatar);
        filterFrame = findViewById(R.id.frame_filter);
        videoFriendLevel = findViewById(R.id.text_video_friendLevel);
        loadingVideoProgress = findViewById(R.id.progressBar_loadingVideo);
        videoNickName = findViewById(R.id.text_video_nickName);
        // Check permissions and init RtcEngine
        if (checkPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initRtcEngineAndJoin();
        }
        new Thread(this::fetchingData).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }

    private void fetchingData() {
        // TODO: create friend's nickname, avatar, and friendLevel after fetching
        friendNickname = "Monica Galler";
        connectionPoint = 45;
        friendLevel = Utils.calculateFriendLevel(connectionPoint);
        // Create random filter based on friendLevel
        if (friendLevel > 2) {
            currentFilterIdx = Utils.getFiltersSize(friendLevel) - 1;
        } else {
            currentFilterIdx = new Random().nextInt(Utils.getFiltersSize(friendLevel));
        }

        if (friendNickname != null && connectionPoint >= 0 && friendLevel >= 0) {
            runOnUiThread(() -> {
                videoNickName.setText(friendNickname);
                videoAvatar.setImageResource(R.drawable.user_avatar);
                videoFriendLevel.setText(Integer.toString(friendLevel));
            });
        }



    }

}