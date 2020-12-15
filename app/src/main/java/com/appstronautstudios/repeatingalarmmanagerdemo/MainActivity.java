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

import com.appstronautstudios.repeatingalarmmanager.managers.RepeatingAlarmManager;
import com.appstronautstudios.repeatingalarmmanager.model.RepeatingAlarm;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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

        @Override
        public long getItemId(int i) {
            return i;
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

            enableDisableCB.setChecked(alarm.isActive());
            enableDisableCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        RepeatingAlarmManager.getInstance().activateAlarm(MainActivity.this, alarm.getId());
                    } else {
                        RepeatingAlarmManager.getInstance().deactivateAlarm(MainActivity.this, alarm.getId());
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

            return convertView;
        }

        void refreshData() {
            this.alarms = RepeatingAlarmManager.getInstance().getAllAlarms(getContext());
            notifyDataSetChanged();
        }
    }
}
