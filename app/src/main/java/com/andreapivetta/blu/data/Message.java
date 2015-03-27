package com.andreapivetta.blu.data;

import android.support.annotation.NonNull;

public class Message implements Comparable<Message> {

    private long messageID;
    private long senderID;
    private long recipientID;
    private long timeStamp;
    private String messageText;
    private String otherUserName;
    private String otherUserProfilePicUrl;

    public Message(long messageID, long senderID, long recipientID, String messageText, long timeStamp,
                   String otherUserName, String otherUserProfilePicUrl) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.recipientID = recipientID;
        this.messageText = messageText;
        this.timeStamp = timeStamp;
        this.otherUserName = otherUserName;
        this.otherUserProfilePicUrl = otherUserProfilePicUrl;
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
