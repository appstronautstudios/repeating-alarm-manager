package com.appstronautstudios.repeatingalarmmanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;

public class ReceiverDeviceBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                case Intent.ACTION_BOOT_COMPLETED:
                    Log.d("RepAM", action);
                    RepeatingAlarmManager.getInstance().resetAllAlarms(context);
                    break;
                default:
                    break;
            }
        }
    }
}