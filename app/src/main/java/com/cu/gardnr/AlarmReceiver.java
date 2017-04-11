package com.cu.gardnr;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.GregorianCalendar;

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

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intentAlarm1 = new Intent(context, AlarmReceiver.class);
        Intent intentAlarm2 = new Intent(context, AlarmReceiver.class);
        Intent intentAlarm3 = new Intent(context, AlarmReceiver.class);

        intentAlarm1.putExtra("reminder", 1);
        intentAlarm1.putExtra("username", username);
        intentAlarm2.putExtra("reminder", 2);
        intentAlarm2.putExtra("username", username);
        intentAlarm3.putExtra("reminder", 3);
        intentAlarm3.putExtra("username", username);
        Long time = new GregorianCalendar().getTimeInMillis() +(7 * 24 * 60 * 60 * 1000);

        if (reminder == 1){
            mBuilder.setContentTitle("First Weekly Reminder");
            if (Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm1, PendingIntent.FLAG_CANCEL_CURRENT));
            }
        }
        else if (reminder == 2) {
            mBuilder.setContentTitle("Second Weekly Reminder");
            if (Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 2, intentAlarm2, PendingIntent.FLAG_CANCEL_CURRENT));
            }
        }
        else {
            mBuilder.setContentTitle("Third Weekly Reminder");
            if (Build.VERSION.SDK_INT >= 19){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 3, intentAlarm3, PendingIntent.FLAG_CANCEL_CURRENT));
            }
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
