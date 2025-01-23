package com.appstronautstudios.repeatingalarmmanager.model;

import com.appstronautstudios.repeatingalarmmanager.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
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

    public RepeatingAlarm(JSONObject object) {
        id = object.optInt(Constants.ALARM_ID);
        hour = object.optInt(Constants.ALARM_HOURS);
        minute = object.optInt(Constants.ALARM_MINUTES);
        interval = object.optLong(Constants.ALARM_INTERVAL);
        title = object.optString(Constants.ALARM_TITLE);
        description = object.optString(Constants.ALARM_DESCRIPTION);
        activityClass = object.optString(Constants.ALARM_CLICK_ACTIVITY);
        active = object.optBoolean(Constants.ALARM_ACTIVE);
    }

    public RepeatingAlarm(int id, int hours, int minutes, long interval, String title, String description, String activity, boolean active) {
        this.id = id;
        this.hour = hours;
        this.minute = minutes;
        this.interval = interval;
        this.title = title;
        this.description = description;
        this.activityClass = activity;
        this.active = active;
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
