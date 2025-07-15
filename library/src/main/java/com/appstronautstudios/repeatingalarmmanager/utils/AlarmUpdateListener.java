package com.appstronautstudios.repeatingalarmmanager.utils;

public interface AlarmUpdateListener {
    void success(long nextAlarmTimestamp);

    void failure(String errorMessage);
}
