package com.andreapivetta.blu.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ConversationActivity;
import com.andreapivetta.blu.utilities.Common;
import com.andreapivetta.blu.utilities.ThemeUtils;

import twitter4j.DirectMessage;

public class Message implements Comparable<Message> {

    public final static String NEW_MESSAGE_INTENT = "com.andreapivetta.blu.NEW_MESSAGE_INTENT";
    private long messageID;
    private long senderID;
    private long recipientID;
    private long timeStamp;
    private String messageText;
    private String otherUserName;
    private String otherUserProfilePicUrl;
    private boolean isRead;

    public Message(long messageID, long senderID, long recipientID, String messageText, long timeStamp,
                   String otherUserName, String otherUserProfilePicUrl, boolean isRead) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.recipientID = recipientID;
        this.messageText = messageText;
        this.timeStamp = timeStamp;
        this.otherUserName = otherUserName;
        this.otherUserProfilePicUrl = otherUserProfilePicUrl;
        this.isRead = isRead;
    }

    public static void pushMessage(DirectMessage dm, Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Intent resultIntent = new Intent(context, ConversationActivity.class);
        resultIntent.putExtra("ID", dm.getSenderId());
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setDefaults(android.app.Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.message_not_title, dm.getSender().getName()))
                .setContentText(dm.getText())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(dm.getText()))
                .setContentIntent(resultPendingIntent)
                .setColor(ThemeUtils.getResourceColorPrimary(context))
                .setLargeIcon(Common.getBitmapFromURL(dm.getSender().getProfileImageURL()))
                .setSmallIcon(R.drawable.ic_message_white_24dp)
                .setLights(Color.BLUE, 500, 1000)
                .setContentIntent(resultPendingIntent);

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_heads_up_notifications), true)
                && (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1))
            mBuilder.setPriority(android.app.Notification.PRIORITY_HIGH);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify((int) dm.getId(), mBuilder.build());

        Intent i = new Intent();
        i.setAction(Message.NEW_MESSAGE_INTENT);
        context.sendBroadcast(i);
    }

    public boolean isRead() {
        return isRead;
    }

    public long getMessageID() {
        return messageID;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getOtherUserProfilePicUrl() {
        return otherUserProfilePicUrl;
    }

    public long getSenderID() {
        return senderID;
    }

    public long getRecipientID() {
        return recipientID;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public int compareTo(@NonNull Message another) {
        return (another.getTimeStamp() - this.timeStamp > 0) ? 1 : -1;
    }
}
