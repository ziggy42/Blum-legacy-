package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;

public class BasicNotificationService extends IntentService {

    public BasicNotificationService() {
        super("AlternateNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        getApplicationContext()
                .startService(new Intent(getApplicationContext(), CheckInteractionsService.class));
        getApplicationContext()
                .startService(new Intent(getApplicationContext(), CheckFollowersService.class));
        getApplicationContext()
                .startService(new Intent(getApplicationContext(), CheckMentionsService.class));

    }

}
