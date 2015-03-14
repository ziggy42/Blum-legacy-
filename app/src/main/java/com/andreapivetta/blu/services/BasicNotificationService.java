package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;

import com.andreapivetta.blu.internet.ConnectionDetector;

public class BasicNotificationService extends IntentService {

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (new ConnectionDetector(getApplicationContext()).isConnectingToInternet()) {
            getApplicationContext()
                    .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));
            getApplicationContext()
                    .startService(new Intent(getApplicationContext(), CheckFollowersService.class));
            getApplicationContext()
                    .startService(new Intent(getApplicationContext(), CheckMentionsService.class));
        }

    }

}
