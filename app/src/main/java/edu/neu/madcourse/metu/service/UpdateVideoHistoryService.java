package edu.neu.madcourse.metu.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class UpdateVideoHistoryService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String connectionId = intent.getStringExtra("CONNECTION_ID");
        int incrementPoint = intent.getIntExtra("INCREMENT_POINT", 0);
        FirebaseService.getInstance().updateVideoHistory(
                connectionId,
                (Long newCount) -> {
                    if (newCount < 3) {
                        FirebaseService.getInstance().addConnectionPoint(incrementPoint, connectionId);
                    }
                });
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
