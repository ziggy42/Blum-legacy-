package com.andreapivetta.blu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.TweetsListHeaderAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetActivity extends ActionBarActivity {

    private Twitter twitter;
    private Status status;
    private ArrayList<Status> mDataSet = new ArrayList<>();
    private TweetsListHeaderAdapter mTweetsAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        this.twitter = TwitterUtils.getTwitter(TweetActivity.this);

        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mTweetsAdapter = new TweetsListHeaderAdapter(mDataSet, this, twitter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });

        new LoadStatus().execute(null, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tweet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadStatus extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                status = twitter.showStatus(getIntent().getLongExtra("STATUS", (long) 0));
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mDataSet.add(status);
                new LoadResponses().execute(null, null, null);
            }
        }
    }

    private class LoadResponses extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                QueryResult result = twitter.search(new Query("to:" + status.getUser().getScreenName()));
                for (twitter4j.Status tmpStatus : result.getTweets()) {
                    if (status.getId() == tmpStatus.getInReplyToStatusId())
                        mDataSet.add(tmpStatus);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mTweetsAdapter.notifyDataSetChanged();
            }
        }
    }

}
