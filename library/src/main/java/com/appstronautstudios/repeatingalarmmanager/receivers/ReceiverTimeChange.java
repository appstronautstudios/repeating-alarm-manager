package com.appstronautstudios.repeatingalarmmanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;
import com.appstronautstudios.repeatingalarmmanager.utils.Constants;

public class ReceiverTimeChange extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Log.d(Constants.LOG_KEY, action);
            switch (action) {
                case Intent.ACTION_TIME_CHANGED:
                case Intent.ACTION_TIMEZONE_CHANGED:
                case Intent.ACTION_TIME_TICK:
                    RepeatingAlarmManager.getInstance().resetAllAlarms(context);
                    break;
                default:
                    break;
            }
        }
    }
}
