package com.andreapivetta.blu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.andreapivetta.blu.services.AlternateNotificationService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, AlternateNotificationService.class));
    }
}
