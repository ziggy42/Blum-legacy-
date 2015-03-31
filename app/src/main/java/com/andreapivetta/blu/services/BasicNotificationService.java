package com.andreapivetta.blu.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.receivers.AlarmReceiver;
import com.andreapivetta.blu.utilities.Common;

import java.util.Calendar;

public class BasicNotificationService extends IntentService {

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    public static void startService(Context context) {
        int frequency = context.getSharedPreferences(Common.PREF, 0).getInt(Common.PREF_FREQ, 1200);
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

    @Override
    protected void onHandleIntent(Intent intent) {

        if (new ConnectionDetector(getApplicationContext()).isConnectingToInternet()) {
            if (getApplicationContext().getSharedPreferences(Common.PREF, 0)
                    .getBoolean(Common.PREF_DATABASE_POPULATED, false)) {
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckFollowersService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckMentionsService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckMessageService.class));
            } else {
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), PopulateDatabasesService.class));
            }
        }

    }

}
