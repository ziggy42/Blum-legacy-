package com.andreapivetta.blu.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.receivers.AlarmReceiver;
import com.andreapivetta.blu.utilities.Common;

import java.util.Calendar;

public class BasicNotificationService extends IntentService {

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    public static void startService(Context context) {
        startService(context, Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_frequencies), "1200")));
    }

    public static void startService(Context context, int frequency) {
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ConnectionDetector detector = new ConnectionDetector(getApplicationContext());

        if (detector.isConnectingToInternet()) {
            if (sharedPreferences.getBoolean(getString(R.string.pref_key_db_populated), false)) {
                String pref = sharedPreferences.getString(getString(R.string.pref_key_fav_ret), Common.WIFI_ONLY);
                if (pref.equals(Common.WIFI_ONLY) && detector.isConnectingToWiFi())
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));
                else if (pref.equals(Common.ALWAYS))
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));

                pref = sharedPreferences.getString(getString(R.string.pref_key_followers), Common.WIFI_ONLY);
                if (pref.equals(Common.WIFI_ONLY) && detector.isConnectingToWiFi())
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckFollowersService.class));
                else if (pref.equals(Common.ALWAYS))
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckFollowersService.class));

                pref = sharedPreferences.getString(getString(R.string.pref_key_mentions), Common.ALWAYS);
                if (pref.equals(Common.WIFI_ONLY) && detector.isConnectingToWiFi())
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckMentionsService.class));
                else if (pref.equals(Common.ALWAYS))
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckMentionsService.class));

                pref = sharedPreferences.getString(getString(R.string.pref_key_dms), Common.ALWAYS);
                if (pref.equals(Common.WIFI_ONLY) && detector.isConnectingToWiFi())
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckMessageService.class));
                else if (pref.equals(Common.ALWAYS))
                    getApplicationContext()
                            .startService(new Intent(getApplicationContext(), CheckMessageService.class));

            } else {
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), PopulateDatabasesService.class));
            }
        }

    }

}
