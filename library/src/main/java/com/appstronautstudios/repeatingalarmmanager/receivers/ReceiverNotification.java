package com.appstronautstudios.repeatingalarmmanager.receivers;

import static com.appstronautstudios.repeatingalarmmanager.utils.Constants.NOTIFICATION_IS_DEFAULT;
import static com.appstronautstudios.repeatingalarmmanager.utils.Constants.NOTIFICATION_TRACKER_ACTION;
import static com.appstronautstudios.repeatingalarmmanager.utils.Constants.NOTIFICATION_TRACKER_PERMISSION;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;
import com.appstronautstudios.repeatingalarmmanager.model.RepeatingAlarm;
import com.appstronautstudios.repeatingalarmmanager.utils.Constants;

public class ReceiverNotification extends BroadcastReceiver {

    public static final String CHANNEL_ID = "repeating_alarm_manager";
    public static final String GROUP = "generic_group";

    @Override
    public void onReceive(Context context, Intent intent) {
        // get id from intent
        Bundle extras = intent.getExtras();
        int alarmId = -1;
        if (extras != null) {
            alarmId = extras.getInt(Constants.ALARM_ID);
            Log.d(Constants.LOG_KEY, "setting up notification for alarm: " + alarmId);
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
                NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel);
            }

            // create intent that will kick user to main activity on notification click
            Intent activityIntent;
            try {
                activityIntent = new Intent(context, Class.forName(alarm.getActivityClass()));
                activityIntent.putExtra("notificationClicked", alarmId);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                // build local notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(alarm.getTitle())
                        .setContentText(alarm.getDescription())
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(context.getApplicationInfo().icon)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setGroup(GROUP)
                        .setAutoCancel(true);

                // notify
                NotificationManagerCompat.from(context).notify(alarmId, notificationBuilder.build());

                // broadcast for tracking
                Intent broadcastIntent = new Intent(NOTIFICATION_TRACKER_ACTION);
                broadcastIntent.putExtra(NOTIFICATION_IS_DEFAULT, alarmId == RepeatingAlarmManager.ALARM_DEFAULT_ID);
                context.sendBroadcast(broadcastIntent, NOTIFICATION_TRACKER_PERMISSION);
            } catch (ClassNotFoundException | SecurityException e) {
                e.printStackTrace();
            }

            // now that we're using idle proof alarms they cannot repeat on their own. This means
            // every time we catch an alarm in this receiver we also have to reschedule it. To
            // keep things simple we're going to reschedule all alarms
            RepeatingAlarmManager.getInstance().resetAllAlarms(context);
        }
    }
}