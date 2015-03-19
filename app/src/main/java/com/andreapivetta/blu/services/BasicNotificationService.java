package com.andreapivetta.blu.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.receivers.AlarmReceiver;
import com.andreapivetta.blu.utilities.Common;

import java.util.Calendar;

public class BasicNotificationService extends IntentService {

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (new ConnectionDetector(getApplicationContext()).isConnectingToInternet()) {
            if (getApplicationContext().getSharedPreferences(Common.PREF, 0)
                    .getBoolean(Common.PREF_DATABASE_POPULATED, false)) {
                Log.i("NotificationService", "Checking...");
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckFollowersService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckMentionsService.class));
            } else {
                Log.i("NotificationService", "Populating...");
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), PopulateDatabasesService.class));
            }
        }

    }

    public static void startService(Context context) {
        int frequency = context.getSharedPreferences(Common.PREF, 0).getInt(Common.PREF_FREQ, 300);
        AlarmManager a = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, frequency);
        a.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                frequency * 1000,
                PendingIntent.getBroadcast(
                        context, 0, new Intent(context, AlarmReceiver.class), 0));
    }

    public static void stopService(Context context) {
        ((AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE))
                .cancel(PendingIntent.getBroadcast(context, 0,
                        new Intent(context, AlarmReceiver.class), 0));
    }

}
