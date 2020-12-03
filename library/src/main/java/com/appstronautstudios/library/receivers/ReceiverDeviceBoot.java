package com.appstronautstudios.library.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.appstronautstudios.library.managers.RepeatingAlarmManager;

/**
 * @author Nilanchala
 * <p/>
 * Broadcast reciever, starts when the device gets starts.
 * Seems you have to reset your alarm every time you restart the phone...
 */
public class ReceiverDeviceBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            RepeatingAlarmManager.getInstance().resetAllAlarms(context);
        }
    }
}