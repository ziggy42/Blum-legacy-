package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;


public class HomeActivity extends TimeLineActivity {

    private static final int REQUEST_LOGIN = 0;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        mSharedPreferences = getSharedPreferences("MyPref", 0);

        if (!isTwitterLoggedInAlready()) {
            startActivityForResult(new Intent(HomeActivity.this, LoginActivity.class), REQUEST_LOGIN);
        } else {
            twitter = TwitterUtils.getTwitter(HomeActivity.this);
            new GetTimeLine().execute(null, null, null);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    String getInitialText() {
        return "";
    }

    @Override
    List<Status> getCurrentTimeLine() throws TwitterException {
        return twitter.getHomeTimeline(paging);
    }

    @Override
    List<Status> getRefreshedTimeLine(Paging paging) throws TwitterException {
        return twitter.getHomeTimeline(paging);
    }

    private boolean isTwitterLoggedInAlready() {
        return mSharedPreferences.getBoolean(TwitterUtils.PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("exit")) {
            setIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            if (("exit").equalsIgnoreCase(getIntent().getStringExtra(("exit")))) {
                onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == REQUEST_LOGIN) {
            twitter = TwitterUtils.getTwitter(HomeActivity.this);
            new GetTimeLine().execute(null, null, null);
        }
    }
}
