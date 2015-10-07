package com.andreapivetta.blu.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.services.CheckFollowingService;
import com.andreapivetta.blu.services.StreamNotificationService;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (mPref.getBoolean(context.getString(R.string.pref_key_stream_service), false))
            context.startService(new Intent(context, StreamNotificationService.class));
        else {
            int frequency = Integer.parseInt(mPref.getString(context.getString(R.string.pref_key_frequencies), "1200"));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmReceiver.class), 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, frequency);
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency * 1000, pendingIntent);
        }

        CheckFollowingService.startService(context);
    }
}
