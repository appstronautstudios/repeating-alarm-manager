package com.appstronautstudios.repeatingalarmmanager.managers;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.appstronautstudios.repeatingalarmmanager.model.RepeatingAlarm;
import com.appstronautstudios.repeatingalarmmanager.receivers.ReceiverNotification;
import com.appstronautstudios.repeatingalarmmanager.utils.Constants;
import com.appstronautstudios.repeatingalarmmanager.utils.SuccessFailListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * https://github.com/Ajeet-Meena/SimpleAlarmManager-Android
 * https://github.com/alberto234/schedule-alarm-manager
 * https://github.com/zubairehman/AlarmManager
 */
public class RepeatingAlarmManager {

    public static final int ALARM_DEFAULT_ID = 1;

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

    /**
     * add alarm to managed set and schedule it
     *
     * @param context     - context
     * @param hour        - hour of date to set alarm (0-24)
     * @param minute      - minute of hour to set alarm (0-60)
     * @param interval    - MS interval to repeat alarm over
     * @param title       - title for notification
     * @param description - detail text for notification
     * @param activity    - activity class to be opened on notification click
     * @param listener    - success/fail listener for add operation. Timestamp of next trigger on success, message on failure
     */
    public void addAlarm(Context context, int hour, int minute, long interval, String title, String description, Class activity, SuccessFailListener listener) {
        addAlarm(
                context,
                hour,
                minute,
                interval,
                title,
                description,
                true,
                activity,
                listener);
    }

    /**
     * add alarm to managed set and schedule it
     *
     * @param context     - context
     * @param hour        - hour of date to set alarm (0-24)
     * @param minute      - minute of hour to set alarm (0-60)
     * @param interval    - MS interval to repeat alarm over
     * @param title       - title for notification
     * @param description - detail text for notification
     * @param activity    - activity class to be opened on notification click
     * @param listener    - success/fail listener for add operation. Timestamp of next trigger on success, message on failure
     */
    public void addAlarm(Context context, int hour, int minute, long interval, String title, String description, boolean startingState, Class activity, SuccessFailListener listener) {
        addAlarm(
                context,
                getUnusedAlarmId(context),
                hour,
                minute,
                interval,
                title,
                description,
                startingState,
                activity,
                listener);
    }

    /**
     * add alarm to managed set and schedule it
     *
     * @param context     - context
     * @param id          - alarm id. Will override if already exists
     * @param hour        - hour of date to set alarm (0-24)
     * @param minute      - minute of hour to set alarm (0-60)
     * @param interval    - MS interval to repeat alarm over
     * @param title       - title for notification
     * @param description - detail text for notification
     * @param activity    - activity class to be opened on notification click
     * @param listener    - success/fail listener for add operation. Timestamp of next trigger on success, message on failure
     */
    public void addAlarm(Context context, int id, int hour, int minute, long interval, String title, String description, boolean startingState, Class activity, SuccessFailListener listener) {
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

        // exists in our list. Remove it
        if (usedIds.contains(id)) {
            removeAlarm(context, id);
        }

        // create alarm object and add it to prefs
        RepeatingAlarm addedAlarm = new RepeatingAlarm(id, hour, minute, interval, title, description, activity.getName(), startingState);
        addAlarmPref(context, addedAlarm);

        // if its supposed to start active attempt to schedule
        if (startingState) {
            scheduleRepeatingAlarmWithPermission(context, addedAlarm);
        }

        if (listener != null) {
            listener.success(addedAlarm.getNextTriggerTimestamp());
        }
    }

    private void updateAlarm(Context context, RepeatingAlarm alarm) {
        ArrayList<RepeatingAlarm> allAlarms = getAllAlarms(context);
        ArrayList<RepeatingAlarm> updatedAlarms = new ArrayList<>();
        for (RepeatingAlarm repeatingAlarm : allAlarms) {
            if (repeatingAlarm.getId() != alarm.getId()) {
                updatedAlarms.add(repeatingAlarm);
            } else {
                // alarm exists in our set. Replace it with incoming alarm
                updatedAlarms.add(alarm);
            }
        }

        setAlarmsPref(context, updatedAlarms);
    }

    public ArrayList<RepeatingAlarm> getAllAlarms(Context context) {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
        String alarmsPref = preferenceManager.getString(Constants.PREF_KEY_ALARMS, "[]");

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

    /**
     * Cancel chosen alarm and remove it from the managed set
     *
     * @param context - context
     * @param id      - id of alarm to cancel and remove
     */
    public void removeAlarm(Context context, int id) {
        cancelAlarm(context, id);
        ArrayList<RepeatingAlarm> allAlarms = getAllAlarms(context);
        ArrayList<RepeatingAlarm> updatedAlarms = new ArrayList<>();
        for (RepeatingAlarm repeatingAlarm : allAlarms) {
            if (repeatingAlarm.getId() != id) {
                updatedAlarms.add(repeatingAlarm);
            }
        }

        setAlarmsPref(context, updatedAlarms);
    }

    /**
     * Cancel all managed alarms and remove them from the managed set
     *
     * @param context - context
     */
    public void removeAllAlarms(Context context) {
        ArrayList<RepeatingAlarm> allAlarms = getAllAlarms(context);
        for (RepeatingAlarm repeatingAlarm : allAlarms) {
            cancelAlarm(context, repeatingAlarm.getId());
        }

        setAlarmsPref(context, null);
    }

    /**
     * set managed alarm activate state if it exists.
     *
     * @param context  - context
     * @param id       - id of alarm to active
     * @param listener - success/fail listener for activate operation. Timestamp of next trigger on success, message on failure
     */
    public void setAlarmActive(Context context, int id, boolean active, SuccessFailListener listener) {
        if (active) {
            activateAlarm(context, id, listener);
        } else {
            deactivateAlarm(context, id, listener);
        }
    }

    /**
     * activate managed alarm if it exists.
     *
     * @param context  - context
     * @param id       - id of alarm to active
     * @param listener - success/fail listener for activate operation. Timestamp of next trigger on success, message on failure
     */
    public void activateAlarm(Context context, int id, SuccessFailListener listener) {
        RepeatingAlarm alarm = getAlarm(context, id);
        if (alarm != null) {
            alarm.setActive(true);
            scheduleRepeatingAlarmWithPermission(context, alarm);
            updateAlarm(context, alarm);
            if (listener != null) listener.success(alarm.getNextTriggerTimestamp());
        } else {
            if (listener != null) listener.failure("Failed to find alarm with ID: " + id);
        }
    }

    /**
     * deactivate managed alarm if it exists.
     *
     * @param context  - context
     * @param id       - id of alarm to deactivate
     * @param listener - success/fail listener for activate operation. Timestamp of next trigger on success, message on failure
     */
    public void deactivateAlarm(Context context, int id, SuccessFailListener listener) {
        RepeatingAlarm alarm = getAlarm(context, id);
        if (alarm != null) {
            alarm.setActive(false);
            cancelAlarm(context, id);
            updateAlarm(context, alarm);
            if (listener != null) listener.success(alarm.getNextTriggerTimestamp());
        } else {
            if (listener != null) listener.failure("Failed to find alarm with ID: " + id);
        }
    }

    /**
     * schedule alarm. Will NOT update pref storage
     *
     * @param context - context
     * @param alarm   - alarm to schedule
     */
    private void scheduleRepeatingAlarmWithPermission(final Context context, final RepeatingAlarm alarm) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(context)
                    .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            scheduleRepeatingAlarm(context, alarm);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if (response.isPermanentlyDenied()) {
                                new AlertDialog.Builder(context)
                                        .setTitle("Permission required")
                                        .setMessage("Notification permissions are required to schedule an alarm.")
                                        .create();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } else {
            scheduleRepeatingAlarm(context, alarm);
        }
    }

    /**
     * schedule alarm. Will NOT update pref storage
     *
     * @param context - context
     * @param alarm   - alarm to schedule
     */
    private void scheduleRepeatingAlarm(final Context context, final RepeatingAlarm alarm) {
        // create alarm intent and schedule (will be delayed during "doze periods")
        Intent intent = new Intent(context, ReceiverNotification.class);
        intent.putExtra(Constants.ALARM_ID, alarm.getId());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarm.getId(), intent, 0 | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long nextAlarmTimestamp = alarm.getNextTriggerTimestamp();
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTimestamp, alarmIntent);
            Log.d(Constants.LOG_KEY, "alarm: \'" + alarm.getTitle() + "\' scheduled starting at: " + new Date(nextAlarmTimestamp) + " and repeating every: " + TimeUnit.MILLISECONDS.toMinutes(alarm.getInterval()) + "m");
        }
    }

    /**
     * cancel alarm. Will NOT update pref storage
     *
     * @param context - context
     * @param id      - alarm to schedule
     */
    private void cancelAlarm(Context context, int id) {
        Intent intent = new Intent(context, ReceiverNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void setAlarmsPref(Context context, ArrayList<RepeatingAlarm> alarms) {
        if (alarms == null) {
            alarms = new ArrayList<>();
        }

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
            cancelAlarm(context, alarm.getId());
            if (alarm.isActive()) {
                scheduleRepeatingAlarmWithPermission(context, alarm);
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
