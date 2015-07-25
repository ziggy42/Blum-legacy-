package com.andreapivetta.blu.activities;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserTimeLineActivity extends TimeLineActivity {

    private static final String TAG_USER = "user";
    public  static final String TAG_USER_ID = "id";

    private User user;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        twitter = TwitterUtils.getTwitter(UserTimeLineActivity.this);

        if (savedInstanceState != null) {
            user = (User) savedInstanceState.getSerializable(TAG_USER);
            tweetList = (ArrayList<Status>) savedInstanceState.getSerializable(TAG_TWEET_LIST);
            currentPage = savedInstanceState.getInt(TAG_CURRENT_PAGE);
        } else {
            new LoadUser().execute(getIntent().getLongExtra(TAG_USER_ID, 0));
        }

        super.onCreate(savedInstanceState);

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ActionBar supportActionBar = getSupportActionBar();
        if ((user != null) && (supportActionBar != null))
            supportActionBar.setTitle(user.getName());
    }

    @Override
    String getInitialText() {
        return "@" + user.getScreenName();
    }

    @Override
    List<Status> getCurrentTimeLine() throws TwitterException {
        return twitter.getUserTimeline(user.getScreenName(), paging);
    }

    @Override
    List<Status> getRefreshedTimeLine(Paging paging) throws TwitterException {
        return twitter.getUserTimeline(user.getScreenName(), paging);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TAG_USER, user);
        super.onSaveInstanceState(outState);
    }

    private class LoadUser extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                user = twitter.showUser(params[0]);
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean status) {
            if (status) {
                toolbar.setTitle(user.getName());
                invalidateOptionsMenu();
                new GetTimeLine().execute(null, null, null);
            } else {
                Toast.makeText(UserTimeLineActivity.this, getString(R.string.cant_find_user),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
