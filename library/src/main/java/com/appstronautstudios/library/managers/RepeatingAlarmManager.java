package com.appstronautstudios.library.managers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.appstronautstudios.library.model.RepeatingAlarm;
import com.appstronautstudios.library.receivers.ReceiverDeviceBoot;
import com.appstronautstudios.library.receivers.ReceiverNotification;
import com.appstronautstudios.library.utils.Constants;
import com.appstronautstudios.library.utils.SuccessFailListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RepeatingAlarmManager {

    private static final RepeatingAlarmManager INSTANCE = new RepeatingAlarmManager();
    private static final int ALARM_CAP = 100;

    private RepeatingAlarmManager() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static RepeatingAlarmManager getInstance() {
        return INSTANCE;
    }

    public void addAlarm(Context context, int id, int hour, int minute, long interval, String title, String description, SuccessFailListener listener) {
        // get alarms and check which ids are in use
        Set<Integer> usedIds = new HashSet<>();
        ArrayList<RepeatingAlarm> alarms = getAllAlarms(context);
        for (RepeatingAlarm alarm : alarms) {
            usedIds.add(alarm.getId());
        }

        // check if we're over the cap
        if (usedIds.size() >= ALARM_CAP) {
            if (listener != null) {
                listener.failure("Too many alarms. Please remove one and try again");
            }
            return;
        }

        // check if id is valid
        if (id <= 0 || id > ALARM_CAP) {
            if (listener != null) {
                listener.failure("Alarm id must be between 1-100");
            }
            return;
        }

        // check if id already used
        if (usedIds.contains(id)) {
            if (listener != null) {
                listener.failure("Alarm already exists with this id");
            }
            return;
        }


        // create alarm object, schedule alarm and add it to prefs
        RepeatingAlarm addedAlarm = new RepeatingAlarm(id, hour, minute, interval, title, description, true);
        scheduleRepeatingAlarm(context, addedAlarm);
        addAlarmPref(context, addedAlarm);

        if (listener != null) {
            listener.success(null);
        }
    }

    /**
     * Cancel chosen alarm and remove it from the managed set
     *
     * @param context - context
     * @param alarm   - alarm to cancel and remove
     */
    public void removeAlarm(Context context, RepeatingAlarm alarm) {
        cancelAlarm(context, alarm);
        ArrayList<RepeatingAlarm> allAlarms = getAllAlarms(context);
        ArrayList<RepeatingAlarm> updatedAlarms = new ArrayList<>();
        for (RepeatingAlarm repeatingAlarm : allAlarms) {
            if (repeatingAlarm.getId() != alarm.getId()) {
                updatedAlarms.add(repeatingAlarm);
            }
        }

        setAlarmsPref(context, updatedAlarms);
    }

    public void enableAlarm(Context context, RepeatingAlarm alarm) {
        // todo implement
        alarm.setActive(true);
        scheduleRepeatingAlarm(context, alarm);
    }

    public void disableAlarm(Context context, RepeatingAlarm alarm) {
        // todo implement
        alarm.setActive(false);
        cancelAlarm(context, alarm);
    }

    private void scheduleRepeatingAlarm(Context context, RepeatingAlarm alarm) {
        // calculate alarm time based on hour and minute
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // create alarm intent and schedule (will be delayed during "doze periods")
        Intent intent = new Intent(context, ReceiverNotification.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }

        // enable boot receiver
        ComponentName receiver = new ComponentName(context, ReceiverDeviceBoot.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void cancelAlarm(Context context, RepeatingAlarm alarm) {
        Intent intent = new Intent(context, ReceiverNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarm.getId(), intent, PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


    public ArrayList<RepeatingAlarm> getAllAlarms(Context context) {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
        String alarmsPref = preferenceManager.getString(Constants.PREF_KEY_ALARMS, "");

        // convert string json to ArrayList of alarms and return
        ArrayList<RepeatingAlarm> repeatingAlarms = new ArrayList<>();
        try {
            JSONArray alarmsJson = new JSONArray(alarmsPref);
            for (int i = 0; i < alarmsJson.length(); i++) {
                JSONObject alarmJson = alarmsJson.getJSONObject(i);
                repeatingAlarms.add(new RepeatingAlarm(alarmJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return repeatingAlarms;
    }

    public RepeatingAlarm getAlarm(Context context, int id) {
        ArrayList<RepeatingAlarm> allAlarms = getAllAlarms(context);
        for (RepeatingAlarm alarm : allAlarms) {
            if (alarm.getId() == id) {
                return alarm;
            }
        }
        return null;
    }

    private void setAlarmsPref(Context context, ArrayList<RepeatingAlarm> alarms) {
        // convert alarm objects to json string
        JSONArray jsonArray = new JSONArray();
        for (RepeatingAlarm alarm : alarms) {
            jsonArray.put(alarm.convertToJsonObject());
        }

        // convert JSONArray object to string and insert into prefs
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferenceManager.edit();
        editor.putString(Constants.PREF_KEY_ALARMS, jsonArray.toString());
        editor.apply();
    }

    private void addAlarmPref(Context context, RepeatingAlarm alarm) {
        ArrayList<RepeatingAlarm> repeatingAlarms = getAllAlarms(context);
        repeatingAlarms.add(alarm);
        setAlarmsPref(context, repeatingAlarms);
    }

    public void resetAllAlarms(Context context) {
        ArrayList<RepeatingAlarm> repeatingAlarms = getAllAlarms(context);
        for (RepeatingAlarm alarm : repeatingAlarms) {
            cancelAlarm(context, alarm);
            if (alarm.isActive()) {
                scheduleRepeatingAlarm(context, alarm);
            }
        }
    }

    public int getUnusedAlarmId(Context context) {
        Set<Integer> usedIds = new HashSet<>();
        ArrayList<RepeatingAlarm> alarms = getAllAlarms(context);
        for (RepeatingAlarm alarm : alarms) {
            usedIds.add(alarm.getId());
        }

        // check if we're over the cap
        if (usedIds.size() >= ALARM_CAP) {
            return -1;
        }

        // create unique id from possible pool
        int uniqueId = 1;
        while (usedIds.contains(uniqueId)) {
            uniqueId = new Random().nextInt(ALARM_CAP) + 1;
        }

        return uniqueId;
    }
}
