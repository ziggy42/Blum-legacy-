package com.andreapivetta.blu.services;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ConversationActivity;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class CheckMessageService extends IntentService {

    public CheckMessageService() {
        super("CheckMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(getApplicationContext());
        dbm.open();
        try {
            ArrayList<DirectMessage> messages = new ArrayList<>();
            for (DirectMessage message : twitter.getDirectMessages(new Paging(1, 200)))
                messages.add(message);

            for (DirectMessage message : twitter.getSentDirectMessages(new Paging(1, 200)))
                messages.add(message);

            ArrayList<DirectMessage> newMessages = dbm.check(messages);
            for (DirectMessage message : newMessages)
                pushMessage(message);

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        dbm.close();
    }

    private void pushMessage(DirectMessage dm) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        Intent resultIntent = new Intent(getApplicationContext(), ConversationActivity.class);
        resultIntent.putExtra("ID", dm.getSenderId());
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setDefaults(android.app.Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentTitle(getApplicationContext().getString(R.string.message_not_title, dm.getSender().getName()))
                .setContentText(dm.getText())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(dm.getText()))
                .setContentIntent(resultPendingIntent)
                .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(Common.getBitmapFromURL(dm.getSender().getProfileImageURL()))
                .setSmallIcon(R.drawable.ic_message_white_24dp)
                .setLights(Color.BLUE, 500, 1000)
                .setContentIntent(resultPendingIntent);

        if (getApplicationContext().getSharedPreferences(Common.PREF, 0)
                .getBoolean(Common.PREF_HEADS_UP_NOTIFICATIONS, true))
            mBuilder.setPriority(android.app.Notification.PRIORITY_HIGH);

        ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))
                .notify((int) dm.getId(), mBuilder.build());

        Intent i = new Intent();
        i.setAction(Message.NEW_MESSAGE_INTENT);
        getApplicationContext().sendBroadcast(i);
    }
}
