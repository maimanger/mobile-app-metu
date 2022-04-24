package edu.neu.madcourse.metu;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Map;

import edu.neu.madcourse.metu.video.VideoActivity;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.RtmCallEventListener;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;

public class BaseCalleeActivity extends AppCompatActivity implements RtmClientListener, RtmCallEventListener {
    protected final String TAG = getClass().getSimpleName();
    protected Dialog callInvitationDialog;
    protected Vibrator callInvitationVibrator;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register activity RtmClient in App
        ((App) getApplication()).registerClientListener(this);
        ((App) getApplication()).registerCallEventListener(this);
    }

    @Override
    protected void onDestroy() {
        // Unregister activity RtmClient in App
        ((App) getApplication()).removeClientListener(this);
        ((App) getApplication()).removeCallEventListener(this);
        super.onDestroy();
    }

    protected void setupAcceptDialogBtn() {
        FloatingActionButton btnAccept = callInvitationDialog.findViewById(R.id.btn_callDialog_accept);
        btnAccept.setOnClickListener((View view) -> {
            dismissDialogAndVibrator();
            Intent intent = new Intent(this, VideoActivity.class);
            startActivity(intent);
        });
    }

    protected void setupRefuseDialogBtn() {
        FloatingActionButton btnRefuse = callInvitationDialog.findViewById(R.id.btn_callDialog_refuse);
        btnRefuse.setOnClickListener((View view) -> {
            dismissDialogAndVibrator();
            ((App) getApplication()).refuseCallInvitation();
        });
    }

    protected void showCallDialogAndVibrator(String callerName, String callerAvatarUrl) {
        runOnUiThread(() -> {
            callInvitationVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            callInvitationDialog = new Dialog(this, R.style.DialogStyle);
            callInvitationDialog.setContentView(R.layout.call_dialog);

            // Rounded dialog window
            callInvitationDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_shape_gradient);
            // Change the dialog position to the top of the window
            WindowManager.LayoutParams wlp = callInvitationDialog.getWindow().getAttributes();
            wlp.gravity = Gravity.TOP;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            callInvitationDialog.getWindow().setAttributes(wlp);

            setupAcceptDialogBtn();
            setupRefuseDialogBtn();

            // TODO: update callerAvatar based on image url
            ShapeableImageView callerAvatarImg = callInvitationDialog.findViewById(R.id.image_callDialog_callerAvatar);
            callerAvatarImg.setImageResource(R.drawable.user_avatar);
            TextView callerNameText = callInvitationDialog.findViewById(R.id.text_callDialog_callerName);
            callerNameText.setText(callerName);

            // Show dialog and start vibrating
            callInvitationDialog.show();
            callInvitationVibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{500, 300, 200, 200, 300, 1000}, 0));
        });
    }

    protected void dismissDialogAndVibrator() {
        if (callInvitationDialog != null & callInvitationVibrator != null) {
            runOnUiThread(() -> {
                callInvitationDialog.dismiss();
                callInvitationVibrator.cancel();
                callInvitationDialog = null;
                callInvitationVibrator = null;
            });
        }
    }



    @Override
    public void onLocalInvitationReceivedByPeer(LocalInvitation localInvitation) {

    }

    @Override
    public void onLocalInvitationAccepted(LocalInvitation localInvitation, String s) {

    }

    @Override
    public void onLocalInvitationRefused(LocalInvitation localInvitation, String s) {

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
        App app = (App) getApplication();
        app.setRemoteInvitation(remoteInvitation);
        // First check if App is in the background, if yes, send notification
        // Otherwise show incoming call dialog
        if (!app.sendCallNotification()) {
            String callerName = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_NAME);
            String callerAvatarUrl = Utils.getRemoteInvitationContent(remoteInvitation, Utils.CALLER_AVATAR);
            showCallDialogAndVibrator(callerName, callerAvatarUrl);
        }
    }

    @Override
    public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) {

    }

    @Override
    public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) {
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {
        Log.d(TAG, "onRemoteInvitationCanceled");
        // Remove previous call notification, if exits any
        int callNotifId = ((App) getApplication()).getCallNotificationId();
        if (callNotifId != -1) {
            NotificationManagerCompat.from(getApplicationContext()).cancel(
                    "VideoCall" + callNotifId, callNotifId);
        }
        // Dismiss dialog and vibrator, if Activity is in foreground
        dismissDialogAndVibrator();

        // Send missed call notification
        ((App) getApplication()).sendCanceledCallNotification(remoteInvitation);

        ((App) getApplication()).setRemoteInvitation(null);
    }

    @Override
    public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int i) {

    }

    @Override
    public void onConnectionStateChanged(int i, int i1) {

    }

    @Override
    public void onMessageReceived(RtmMessage rtmMessage, String s) {

    }

    @Override
    public void onImageMessageReceivedFromPeer(RtmImageMessage rtmImageMessage, String s) {

    }

    @Override
    public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {

    }

    @Override
    public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

    }

    @Override
    public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

    }

    @Override
    public void onTokenExpired() {

    }

    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

    }




}
