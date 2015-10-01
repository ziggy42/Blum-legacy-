package com.andreapivetta.blu.data;


public class UserFollowed {

    public long id;
    public String name;
    public String screenName;
    public String profilePicUrl;

    public UserFollowed(long id, String name, String screenName, String profilePicUrl) {
        this.id = id;
        this.name = name;
        this.screenName = screenName;
        this.profilePicUrl = profilePicUrl;
    }
}
