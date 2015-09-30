package com.andreapivetta.blu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.andreapivetta.blu.services.CheckFollowingService;

public class FollowingAlarmReceiver extends BroadcastReceiver {
    public FollowingAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CheckFollowingService.class));
    }
}
