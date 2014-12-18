package com.andreapivetta.blu.data;


import android.support.annotation.NonNull;

import java.util.Calendar;

public class Notification implements Comparable<Notification> {

    public final static String TYPE_FAVOURITE = "stellina";
    public final static String TYPE_RETWEET = "retweet";
    public final static String TYPE_MENTION = "mention";
    public final static String TYPE_FOLLOW = "newfollower";
    public final static String TYPE_RETWEET_MENTIONED = "retweetmentioned";

    public boolean read;
    public long tweetID, userID;
    public String user, type, status, profilePicURL;
    public int hh, mm, YY, MM, DD, notificationID;

    public Notification(boolean read, long tweet, String user, String type, String status, String profilePicURL, long userID) {
        this.read = read;
        this.tweetID = tweet;
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
        this.tweetID = tweet;
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

    @Override
    public int compareTo(@NonNull Notification another) {

        if (YY < another.YY) {
            return -1;
        } else {
            if (YY > another.YY) return 1;
            if (MM < another.MM) {
                return -1;
            } else {
                if (MM > another.MM) return 1;
                if (DD < another.DD) {
                    return -1;
                } else {
                    if (DD > another.DD) return 1;
                    if (hh < another.hh) {
                        return -1;
                    } else {
                        if (hh > another.hh) return 1;
                        if (mm < another.mm) {
                            return -1;
                        } else {
                            if (mm > another.mm) return 1;
                            return 0;
                        }
                    }
                }
            }
        }
    }
}
