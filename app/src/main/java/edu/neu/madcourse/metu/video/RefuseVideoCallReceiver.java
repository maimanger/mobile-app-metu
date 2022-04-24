package edu.neu.madcourse.metu.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import edu.neu.madcourse.metu.App;

public class RefuseVideoCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notifId = intent.getIntExtra("NOTIFICATION_ID", 0);
        Context appContext = context.getApplicationContext();
        NotificationManagerCompat.from(appContext).cancel(
                "VideoCall" + notifId, notifId);
        ((App)appContext).refuseCallInvitation();
        ((App)appContext).setCallNotificationId(-1);

    }
}
