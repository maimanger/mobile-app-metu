package edu.neu.madcourse.metu.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import edu.neu.madcourse.metu.App;

public class DismissCanceledCallNotifReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notifId = intent.getIntExtra("NOTIFICATION_ID", 0);
        NotificationManagerCompat.from(context.getApplicationContext())
                .cancel("CanceledVideoCall" + notifId, notifId);
        ((App)context.getApplicationContext()).removeCanceledCallNotificationId(notifId);
    }
}
