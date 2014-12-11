package com.andreapivetta.blu.data;


import java.util.Calendar;

public class Notification {

    public final static String TYPE_FAVOURITE = "stellina";
    public final static String TYPE_RETWEET = "retweet";
    public final static String TYPE_MENTION = "mention";
    public final static String TYPE_FOLLOW = "newfollower";

    public boolean read;
    public long tweet, userID;
    public String user, type, status, profilePicURL;
    public int hh, mm, YY, MM, DD, notificationID;

    public Notification(boolean read, long tweet, String user, String type, String status, String profilePicURL, long userID) {
        this.read = read;
        this.tweet = tweet;
        this.user = user;
        this.type = type;
        this.status = status;
        this.profilePicURL = profilePicURL;
        this.userID = userID;

        Calendar c = Calendar.getInstance();
        this.YY = c.get(Calendar.YEAR);
        this.MM = c.get(Calendar.MONTH);
        this.DD = c.get(Calendar.DAY_OF_MONTH);
        this.hh = c.get(Calendar.HOUR_OF_DAY);
        this.mm = c.get(Calendar.MINUTE);
    }

    public Notification(boolean read, long tweet, String user, String type, String status, String profilePicURL,
                        int hh, int mm, int YY, int MM, int DD, long userID, int notificationID) {
        this.read = read;
        this.tweet = tweet;
        this.user = user;
        this.type = type;
        this.YY = YY;
        this.MM = MM;
        this.DD = DD;
        this.hh = hh;
        this.mm = mm;
        this.status = status;
        this.profilePicURL = profilePicURL;
        this.userID = userID;
        this.notificationID = notificationID;
    }
}
