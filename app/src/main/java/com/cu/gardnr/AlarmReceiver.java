package com.cu.gardnr;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        String username = intent.getStringExtra("username");
        Integer reminder = intent.getIntExtra("reminder", 1);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_water)
                        .setContentText("Water your plants!");

        if (reminder == 1){
            mBuilder.setContentTitle("First Weekly Reminder");
        }
        else if (reminder == 2) {
            mBuilder.setContentTitle("Second Weekly Reminder");
        }
        else {
            mBuilder.setContentTitle("Third Weekly Reminder");
        }

        Intent resultIntent = new Intent(context, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
