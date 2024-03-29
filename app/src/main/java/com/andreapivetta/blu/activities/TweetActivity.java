package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.interf.SnackbarContainer;
import com.andreapivetta.blu.adapters.decorators.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetActivity extends ThemedActivity implements SnackbarContainer {

    private static final String TAG_TWEET_LIST = "tweetlist";
    private static final String TAG_CURRENT_TWEET = "header";
    public static final String TAG_STATUS_BUNDLE = "status";
    public static final String TAG_TWEET = "tweet";
    public static final String TAG_TWEET_ID = "id";

    private boolean isUp = true, isBlocked = false;
    private Twitter twitter;
    private Status status;
    private ArrayList<Status> mDataSet = new ArrayList<>();
    private TweetsListAdapter mTweetsAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FloatingActionButton replyFAB;
    private ProgressBar loadingProgressBar;

    private int currentIndex = 0;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getBundleExtra(TAG_STATUS_BUNDLE);
            if (bundle != null)
                this.status = (Status) bundle.getSerializable(TAG_TWEET);
        } else {
            mDataSet = (ArrayList<Status>) savedInstanceState.getSerializable(TAG_TWEET_LIST);
            this.currentIndex = savedInstanceState.getInt(TAG_CURRENT_TWEET);
            this.status = mDataSet.get(currentIndex);
            loadingProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        twitter = TwitterUtils.getTwitter(TweetActivity.this);
        mRecyclerView.addItemDecoration(new SpaceTopItemDecoration(Common.dpToPx(this, 10)));
        mTweetsAdapter = new TweetsListAdapter(mDataSet, this, twitter, currentIndex);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    if (isUp)
                        newTweetDown();
                } else {
                    if (!isUp)
                        newTweetUp();
                }
            }
        });

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
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
                }
            });
        }

        replyFAB = (FloatingActionButton) findViewById(R.id.replyFAB);
        replyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TweetActivity.this, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + status.getUser().getScreenName())
                        .putExtra(NewTweetActivity.TAG_REPLY_ID, status.getId());
                startActivity(i);
            }
        });

        if (mDataSet.isEmpty())
            new LoadConversationAsyncTask().execute();

    }

    private void newTweetDown() {
        if (!isBlocked) {
            replyFAB.animate().translationY(replyFAB.getHeight() + (int) (getResources().getDimension(R.dimen.fabMargin)))
                    .start();
            isUp = false;
        }
    }

    private void newTweetUp() {
        if (!isBlocked) {
            replyFAB.animate().translationY(0).start();
            isUp = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TAG_TWEET_LIST, mDataSet);
        outState.putInt(TAG_CURRENT_TWEET, currentIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(TweetActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSnackBar(String content) {
        Snackbar.make(getWindow().getDecorView().findViewById(R.id.coordinatorLayout), content, Snackbar.LENGTH_SHORT)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        isBlocked = false;
                    }
                }).show();
        isUp = true;
        isBlocked = true;
    }

    private class LoadConversationAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (status == null)
                    status = twitter.showStatus(getIntent().getLongExtra(TAG_TWEET_ID, 0));

                twitter4j.Status current = status;
                long id;
                while ((id = current.getInReplyToStatusId()) != -1) {
                    current = twitter.showStatus(id);
                    mDataSet.add(0, current);
                }

                currentIndex = mDataSet.size();
                mDataSet.add(status);

                QueryResult result = twitter.search(new Query("to:" + status.getUser().getScreenName()));
                for (twitter4j.Status tmpStatus : result.getTweets())
                    if (status.getId() == tmpStatus.getInReplyToStatusId())
                        mDataSet.add((tmpStatus));

            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mTweetsAdapter.setHeaderPosition(currentIndex);
                loadingProgressBar.setVisibility(View.GONE);
                mTweetsAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(currentIndex);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
