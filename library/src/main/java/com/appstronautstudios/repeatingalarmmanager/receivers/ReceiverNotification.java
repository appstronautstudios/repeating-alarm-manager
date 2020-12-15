package com.appstronautstudios.repeatingalarmmanager.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;
import com.appstronautstudios.repeatingalarmmanager.model.RepeatingAlarm;
import com.appstronautstudios.repeatingalarmmanager.utils.Constants;

import androidx.core.app.NotificationCompat;

public class ReceiverNotification extends BroadcastReceiver {

    public static final String CHANNEL_ID = "repeating_alarm_manager";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // get id from intent
        Bundle extras = intent.getExtras();
        int alarmId = -1;
        if (extras != null) {
            alarmId = extras.getInt(Constants.ALARM_ID);
            Log.d(CHANNEL_ID, "alarmId:" + alarmId);
        }

        // did we catch a valid alarm
        if (alarmId >= 0) {
            RepeatingAlarm alarm = RepeatingAlarmManager.getInstance().getAlarm(context, alarmId);

            // Android 8.0 garbage
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                CharSequence channelName = "Daily reminders";
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.BLUE);
                if (mNotificationManager != null) {
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
            }

            // create intent that will kick user to main activity on notification click
            Intent activityIntent;
            try {
                activityIntent = new Intent(context, Class.forName(alarm.getActivityClass()));
                activityIntent.putExtra("notificationClicked", true);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);

                // build local notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setContentTitle(alarm.getTitle())
                        .setContentText(alarm.getDescription())
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(android.R.drawable.alert_light_frame)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                // notify
                if (mNotificationManager != null) {
                    mNotificationManager.notify(alarmId, notificationBuilder.build());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}