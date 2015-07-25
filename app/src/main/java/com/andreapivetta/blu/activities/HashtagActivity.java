package com.andreapivetta.blu.activities;


import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

public class HashtagActivity extends TimeLineActivity {

    private String hashtag;
    private Query mQuery;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {

        twitter = TwitterUtils.getTwitter(HashtagActivity.this);
        hashtag = getIntent().getData().toString().substring(32);
        mQuery = new Query("#" + hashtag);
        if (savedInstanceState != null)
            tweetList = (ArrayList<Status>) savedInstanceState.getSerializable(TAG_TWEET_LIST);
        else
            new GetTimeLine().execute(null, null, null);

        super.onCreate(savedInstanceState);

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setTitle("#" + hashtag);
    }

    @Override
    String getInitialText() {
        return "#" + hashtag;
    }

    @Override
    List<Status> getCurrentTimeLine() throws TwitterException {
        try {
            QueryResult result = twitter.search(mQuery);
            mQuery = result.nextQuery();
            return result.getTweets();
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }
    }

    @Override
    List<Status> getRefreshedTimeLine(Paging paging) throws TwitterException {
        return getCurrentTimeLine();
    }
}
