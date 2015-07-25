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
    public long messageID;
    public long senderID;
    public long recipientID;
    public long otherID;
    public long timeStamp;
    public String messageText;
    public String otherUserName;
    public String otherUserProfilePicUrl;
    public boolean isRead;

    public Message(long messageID, long senderID, long recipientID, long otherID, String otherName,
                   String messageText, String otherUserProfilePicUrl, boolean isRead, long timeStamp) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.recipientID = recipientID;
        this.otherID = otherID;
        this.otherUserName = otherName;
        this.messageText = messageText;
        this.otherUserProfilePicUrl = otherUserProfilePicUrl;
        this.isRead = isRead;
        this.timeStamp = timeStamp;
    }

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
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_mute_notifications), false)) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            Intent resultIntent = new Intent(context, ConversationActivity.class);
            resultIntent.putExtra(ConversationActivity.TAG_ID, dm.getSenderId());
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
    }

    @Override
    public int compareTo(@NonNull Message another) {
        return (another.timeStamp - this.timeStamp > 0) ? 1 : -1;
    }
}
