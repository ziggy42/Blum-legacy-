package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.utilities.Common;

public class BasicNotificationService extends IntentService {

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i("NotificationService", "Checking...");
        if (new ConnectionDetector(getApplicationContext()).isConnectingToInternet()) {
            if (getApplicationContext().getSharedPreferences(Common.PREF, 0)
                    .getBoolean(Common.PREF_DATABASE_POPULATED, false)) {
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckFollowersService.class));
                getApplicationContext()
                        .startService(new Intent(getApplicationContext(), CheckMentionsService.class));
            } else {
                getApplicationContext()
                        .startActivity(new Intent(getApplicationContext(), PopulateDatabasesService.class));
            }
        }

    }

}
