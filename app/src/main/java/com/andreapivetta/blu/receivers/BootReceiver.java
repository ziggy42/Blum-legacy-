package com.andreapivetta.blu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.services.BasicNotificationService;
import com.andreapivetta.blu.services.CheckFollowingService;
import com.andreapivetta.blu.services.StreamNotificationService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (mPref.getBoolean(context.getString(R.string.pref_key_stream_service), false))
            context.startService(new Intent(context, StreamNotificationService.class));
        else
            BasicNotificationService.startService(context);

        CheckFollowingService.startService(context);
    }
}
