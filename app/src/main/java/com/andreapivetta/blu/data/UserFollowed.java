package com.andreapivetta.blu.data;


public class UserFollowed {

    public long userId;
    public String userName;
    public String screenName;
    public String profilePicUrl;

    public UserFollowed(long userId, String userName, String profilePicUrl, String screenName) {
        this.userId = userId;
        this.userName = userName;
        this.profilePicUrl = profilePicUrl;
        this.screenName = screenName;
    }
}
