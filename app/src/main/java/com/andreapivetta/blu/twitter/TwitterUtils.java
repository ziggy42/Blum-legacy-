package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.content.SharedPreferences;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public final class TwitterUtils {
    public static String TWITTER_CONSUMER_KEY = "bqC07DsGd4GxN7a5SzPYvAkOm";
    public static String TWITTER_CONSUMER_SECRET = "qJeeNMIuranov0m7hkGI6QvtcMx2BQYWqaYxwUegNf30bs1INO";
    public static final String CALLBACK_URL = "http://andreapivetta.altervista.org/";
    public static final boolean DUMMY_CALLBACK_URL = true;
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    public static final String PREF_KEY_PICTURE_URL = "picture_url";

    public static Twitter getTwitter(Context context) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences("MyPref", 0);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TwitterUtils.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(TwitterUtils.TWITTER_CONSUMER_SECRET);

        AccessToken accessToken = new AccessToken(mSharedPreferences.getString(TwitterUtils.PREF_KEY_OAUTH_TOKEN, ""),
                mSharedPreferences.getString(TwitterUtils.PREF_KEY_OAUTH_SECRET, ""));
        return new TwitterFactory(builder.build()).getInstance(accessToken);
    }

}
