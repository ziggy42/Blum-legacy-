package com.andreapivetta.blu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.andreapivetta.blu.services.StreamNotificationService;
import com.andreapivetta.blu.utilities.Common;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.getSharedPreferences("MyPRef", 0).getBoolean(Common.PREF_STREAM_ON, false))
            context.startService(new Intent(context, StreamNotificationService.class));
    }
}
