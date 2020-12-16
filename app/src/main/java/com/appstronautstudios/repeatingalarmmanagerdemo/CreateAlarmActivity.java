package com.appstronautstudios.repeatingalarmmanagerdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.appstronautstudios.library.SegmentedController;
import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;
import com.appstronautstudios.repeatingalarmmanager.utils.SuccessFailListener;
import com.appstronautstudios.repeatingalarmmanagerdemo.utils.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAlarmActivity extends AppCompatActivity {

    private long alarmDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    private long timeInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        alarmDate = new Date().getTime();

        // set up back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Create Notification");


        final TextView dateTV = findViewById(R.id.date_et);
        final TextView timeTV = findViewById(R.id.time_et);

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(alarmDate);

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE) + 5;

        updateDateTime();

        dateTV.setText(Utils.timestampToReadableDateString(alarmDate));
        dateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;
                                updateDateTime();
                                dateTV.setText(Utils.timestampToReadableDateString(alarmDate));
                            }
                        },
                        mYear,
                        mMonth,
                        mDay
                ).show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });
        timeTV.setText(Utils.timestampToReadableTimeString(alarmDate));
        timeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                                mHour = hourOfDay;
                                mMinute = minute;
                                updateDateTime();
                                timeTV.setText(Utils.timestampToReadableTimeString(alarmDate));
                            }
                        },
                        mHour,
                        mMinute,
                        false
                ).show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });

        SegmentedController timeIntervalSC = findViewById(R.id.time_interval_segment);
        timeIntervalSC.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.fifteen_mins:
                        timeInterval = TimeUnit.MINUTES.toMillis(15);
                        break;
                    case R.id.half_day:
                        timeInterval = TimeUnit.HOURS.toMillis(12);
                        break;
                    case R.id.day:
                        timeInterval = TimeUnit.HOURS.toMillis(24);
                        break;
                    case R.id.week:
                        timeInterval = TimeUnit.DAYS.toMillis(7);
                        break;
                }
            }
        });
        timeIntervalSC.check(R.id.fifteen_mins);
    }

    private void updateDateTime() {
        Calendar date = Calendar.getInstance();
        date.set(mYear, mMonth, mDay, mHour, mMinute);
        alarmDate = date.getTimeInMillis();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_alarm, menu);
        return true;
    }

    public void complete() {
        EditText alarmNameET = findViewById(R.id.alarm_name_et);
        EditText alarmDescET = findViewById(R.id.alarm_desc_et);
        // set up alarm
        // default alarm doesn't exist yet
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(alarmDate));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        RepeatingAlarmManager.getInstance().addAlarm(CreateAlarmActivity.this,
                RepeatingAlarmManager.getInstance().getUnusedAlarmId(CreateAlarmActivity.this),
                hour,
                minute,
                timeInterval,
                alarmNameET.getText().toString(),
                alarmDescET.getText().toString(),
                MainActivity.class,
                new SuccessFailListener() {
                    @Override
                    public void success(Object object) {
                        Log.d("ALRM", "success");
                    }

                    @Override
                    public void failure(Object object) {
                        Log.d("ALRM", "failure" + String.valueOf(object));
                    }
                });
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_save_log: {
                complete();
                return true;
            }
            case android.R.id.home:
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(menuItem);
        }
    }
}
