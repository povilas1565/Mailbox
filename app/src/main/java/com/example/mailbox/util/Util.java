package com.example.mailbox.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import com.example.mailbox.R;
import com.example.mailbox.alarm.AlarmReceiver;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

public class Util {

    public static void setAlarm(Context context){

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60*1000,pendingIntent);
        }

    }

    public static String formatStringDate(String rawDateString) {
        String[] parts = rawDateString.split("T");
        String[] date = parts[0].split("-");
        String[] time = parts[1].split(":");

        Calendar calendarGmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        calendarGmt.set(year, month, day, hour, minute);
        long gmtTime = calendarGmt.getTime().getTime();

        Calendar calendarDefaultTimezone = Calendar.getInstance(TimeZone.getDefault());
        calendarDefaultTimezone.setTimeInMillis(gmtTime);

        year = calendarDefaultTimezone.get(Calendar.YEAR);
        month = calendarDefaultTimezone.get(Calendar.MONTH);
        day = calendarDefaultTimezone.get(Calendar.DAY_OF_MONTH);
        hour = calendarDefaultTimezone.get(Calendar.HOUR_OF_DAY);
        minute = calendarDefaultTimezone.get(Calendar.MINUTE);

        return String.format(Locale.getDefault(),"%04d-%02d-%02d %02d:%02d", year, month, day, hour, minute);
    }

    public static void buttonEffect(View button, Context context){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(context.getResources().getColor(R.color.blue_dark, context.getTheme()), PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
