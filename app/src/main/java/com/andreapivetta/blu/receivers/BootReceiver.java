package com.andreapivetta.blu.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.andreapivetta.blu.services.StreamNotificationService;
import com.andreapivetta.blu.utilities.Common;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mPref = context.getSharedPreferences(Common.PREF, 0);
        if (mPref.getBoolean(Common.PREF_STREAM_ON, false))
            context.startService(new Intent(context, StreamNotificationService.class));
        else {
            int frequency = mPref.getInt(Common.PREF_FREQ, 1200);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmReceiver.class), 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, frequency);
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency * 1000, pendingIntent);
        }
    }
}
