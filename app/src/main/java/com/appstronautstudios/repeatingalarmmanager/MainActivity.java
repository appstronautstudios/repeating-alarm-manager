package com.appstronautstudios.repeatingalarmmanager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appstronautstudios.library.managers.RepeatingAlarmManager;
import com.appstronautstudios.library.model.RepeatingAlarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView alarmsList = findViewById(R.id.alarms);

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        RepeatingAlarmManager.getInstance().addAlarm(MainActivity.this,
                10,
                hour,
                minute,
                TimeUnit.MINUTES.toMillis(5),
                "test 1",
                "abcdefghijklmnop",
                MainActivity.class.toString(),
                null);
        RepeatingAlarmManager.getInstance().addAlarm(MainActivity.this,
                11,
                hour,
                minute + 5,
                TimeUnit.MINUTES.toMillis(5),
                "test 2",
                "abcdefghijklmnop",
                MainActivity.class.toString(),
                null);
        RepeatingAlarmManager.getInstance().addAlarm(MainActivity.this,
                12,
                hour,
                minute + 10,
                TimeUnit.MINUTES.toMillis(5),
                "test 3",
                "abcdefghijklmnop",
                MainActivity.class.toString(),
                null);
        RepeatingAlarmManager.getInstance().addAlarm(MainActivity.this,
                13,
                hour,
                minute + 15,
                TimeUnit.MINUTES.toMillis(5),
                "test 4",
                "abcdefghijklmnop",
                MainActivity.class.toString(),
                null);

        alarmsList.setAdapter(new CustomAdapter(RepeatingAlarmManager.getInstance().getAllAlarms(MainActivity.this)));
    }

    public class CustomAdapter extends BaseAdapter {

        private ArrayList<RepeatingAlarm> alarms;

        CustomAdapter(ArrayList<RepeatingAlarm> alarms) {
            this.alarms = alarms;
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
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_alarm, viewGroup, false);
            }

            TextView titleTV = view.findViewById(R.id.title);
            TextView descriptionTV = view.findViewById(R.id.description);
            TextView detailsTV = view.findViewById(R.id.details);
            View cancelBTN = view.findViewById(R.id.cancel);

            final RepeatingAlarm alarm = getItem(i);
            titleTV.setText(alarm.getTitle());
            descriptionTV.setText(alarm.getDescription());
            detailsTV.setText("Repeats every " + TimeUnit.MILLISECONDS.toMinutes(alarm.getInterval()) + "m at " + alarm.getHumanReadableTime());

            cancelBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RepeatingAlarmManager.getInstance().removeAlarm(MainActivity.this, alarm);
                    alarms.remove(alarm);
                    notifyDataSetChanged();
                }
            });

            return view;
        }
    }
}
