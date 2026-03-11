package com.appstronautstudios.repeatingalarmmanager.model;

import android.content.Context;

import com.appstronautstudios.repeatingalarmmanager.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class RepeatingAlarm {
    private int id; // ANDROID ALARM IDS ARE INTS
    private int hour;
    private int minute;
    private long interval;
    private String title;
    private String description;
    private String activityClass;
    private boolean active;
    private int smallIcon; // DO NOT USE! int resource ids are not consistent across updates
    private String smallIconName;

    public RepeatingAlarm(JSONObject object) {
        id = object.optInt(Constants.ALARM_ID);
        hour = object.optInt(Constants.ALARM_HOURS);
        minute = object.optInt(Constants.ALARM_MINUTES);
        interval = object.optLong(Constants.ALARM_INTERVAL);
        title = object.optString(Constants.ALARM_TITLE);
        description = object.optString(Constants.ALARM_DESCRIPTION);
        activityClass = object.optString(Constants.ALARM_CLICK_ACTIVITY);
        active = object.optBoolean(Constants.ALARM_ACTIVE);
        smallIcon = object.optInt(Constants.ALARM_SMALL_ICON);
        smallIconName = object.optString(Constants.ALARM_SMALL_ICON_NAME);
    }

    public RepeatingAlarm(int id, int hours, int minutes, long interval, String title, String description, String activity, boolean active, String iconName) {
        this.id = id;
        this.hour = hours;
        this.minute = minutes;
        this.interval = interval;
        this.title = title;
        this.description = description;
        this.activityClass = activity;
        this.active = active;
        this.smallIconName = iconName;
    }

    public JSONObject convertToJsonObject() {
        JSONObject alarmJson = new JSONObject();
        try {
            alarmJson.put(Constants.ALARM_ID, getId());
            alarmJson.put(Constants.ALARM_HOURS, getHour());
            alarmJson.put(Constants.ALARM_MINUTES, getMinute());
            alarmJson.put(Constants.ALARM_INTERVAL, getInterval());
            alarmJson.put(Constants.ALARM_TITLE, getTitle());
            alarmJson.put(Constants.ALARM_DESCRIPTION, getDescription());
            alarmJson.put(Constants.ALARM_CLICK_ACTIVITY, getActivityClass());
            alarmJson.put(Constants.ALARM_ACTIVE, isActive());
            alarmJson.put(Constants.ALARM_SMALL_ICON_NAME, getSmallIconName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alarmJson;
    }

    public int getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public long getInterval() {
        return interval;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getActivityClass() {
        return activityClass;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSmallIcon(Context context) {
        String name = getSmallIconName();
        int iconResId = 0;

        if (name != null && !name.isEmpty()) {
            iconResId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        }

        if (iconResId == 0) {
            // Fallback to app icon first, then system info icon
            iconResId = context.getApplicationInfo().icon;
            if (iconResId == 0) {
                iconResId = android.R.drawable.ic_dialog_info;
            }
        }
        return iconResId;
    }

    public String getSmallIconName() {
        return smallIconName;
    }

    public String getHumanReadableTime() {
        return String.format("%02d", getHour()) + ":" + String.format("%02d", getMinute());
    }

    public long getNextTriggerTimestamp() {
        // create alarm intent and schedule
        // calculate alarm time based on hour and minute
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, getHour());
        calendar.set(Calendar.MINUTE, getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // check if alarm trigger is in past (e.g. you set it to repeat every day at 12pm but it is
        // already 2pm). To prevent from firing immediately move forward intervals until in future
        while (Calendar.getInstance().after(calendar)) {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + getInterval());
        }

        return calendar.getTimeInMillis();
    }
}