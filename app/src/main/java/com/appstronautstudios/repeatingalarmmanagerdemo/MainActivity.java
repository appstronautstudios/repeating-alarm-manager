package com.appstronautstudios.repeatingalarmmanagerdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;
import com.appstronautstudios.repeatingalarmmanager.model.RepeatingAlarm;
import com.appstronautstudios.repeatingalarmmanager.utils.AlarmUpdateListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // set up back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Notification Manager");

        View view = findViewById(R.id.reset_all);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepeatingAlarmManager.getInstance().resetAllAlarms(MainActivity.this);
                Toast.makeText(MainActivity.this, "All alarms re-scheduled", Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton btn = findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateAlarmActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ListView listView = findViewById(R.id.alarm_list);
        listView.setAdapter(new AlarmAdapter(MainActivity.this, RepeatingAlarmManager.getInstance().getAllAlarms(MainActivity.this)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class AlarmAdapter extends ArrayAdapter<RepeatingAlarm> {
        private ArrayList<RepeatingAlarm> alarms;

        AlarmAdapter(Context context, ArrayList<RepeatingAlarm> items) {
            super(context, 0, items);

            alarms = items;
        }

        @Override
        public int getCount() {
            return alarms.size();
        }

        @Override
        public RepeatingAlarm getItem(int i) {
            return alarms.get(i);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_alarm, parent, false);
            }

            final RepeatingAlarm alarm = getItem(position);

            TextView alarmTitleTV = convertView.findViewById(R.id.alarm_title);
            TextView alarmDescriptionTV = convertView.findViewById(R.id.alarm_description);
            TextView startTimeTV = convertView.findViewById(R.id.start_time);
            TextView intervalTV = convertView.findViewById(R.id.interval);
            CheckBox enableDisableCB = convertView.findViewById(R.id.enable_disable);
            View removeBTN = convertView.findViewById(R.id.alarm_remove);

            alarmTitleTV.setText(alarm.getTitle());
            alarmDescriptionTV.setText(alarm.getDescription());
            startTimeTV.setText(alarm.getHumanReadableTime());
            if (TimeUnit.MILLISECONDS.toMinutes(alarm.getInterval()) < 60) {
                intervalTV.setText(TimeUnit.MILLISECONDS.toMinutes(alarm.getInterval()) + "m");
            } else if (TimeUnit.MILLISECONDS.toHours(alarm.getInterval()) <= 24) {
                intervalTV.setText(TimeUnit.MILLISECONDS.toHours(alarm.getInterval()) + "h");
            } else {
                intervalTV.setText(TimeUnit.MILLISECONDS.toDays(alarm.getInterval()) + "d");
            }

            enableDisableCB.setOnCheckedChangeListener(null);
            enableDisableCB.setChecked(alarm.isActive());
            enableDisableCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        RepeatingAlarmManager.getInstance().activateAlarm(MainActivity.this, alarm.getId(), new AlarmUpdateListener() {
                            @Override
                            public void success(long nextAlarmTimestamp) {
                                long millisUntil = nextAlarmTimestamp - System.currentTimeMillis();
                                long minutes = (millisUntil / (1000 * 60)) % 60;
                                long hours = (millisUntil / (1000 * 60 * 60));
                                Toast.makeText(MainActivity.this, "Next alarm in: " + hours + " hours and " + minutes + " minutes", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void failure(String errorMessage) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Error")
                                        .setMessage(errorMessage)
                                        .setPositiveButton("Ok", null)
                                        .setNegativeButton("Settings", (dialogInterface, i) -> {
                                            RepeatingAlarmManager.openNotificationSettings(MainActivity.this);
                                        })
                                        .show();
                            }
                        });
                    } else {
                        RepeatingAlarmManager.getInstance().deactivateAlarm(MainActivity.this, alarm.getId(), new AlarmUpdateListener() {
                            @Override
                            public void success(long nextAlarmTimestamp) {
                                Toast.makeText(MainActivity.this, "Alarm deactivated", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void failure(String errorMessage) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Error")
                                        .setMessage(errorMessage)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
                    }
                    refreshData();
                }
            });

            removeBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RepeatingAlarmManager.getInstance().removeAlarm(MainActivity.this, alarm.getId());
                    refreshData();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "alarm id:" + alarm.getId(), Toast.LENGTH_LONG).show();
                }
            });

            return convertView;
        }

        void refreshData() {
            this.alarms = RepeatingAlarmManager.getInstance().getAllAlarms(getContext());
            notifyDataSetChanged();
        }
    }
}
