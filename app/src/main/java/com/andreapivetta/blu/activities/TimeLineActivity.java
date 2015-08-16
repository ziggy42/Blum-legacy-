package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.decorators.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.utilities.Common;
import com.andreapivetta.blu.utilities.ThemeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public abstract class TimeLineActivity extends ThemedActivity implements SnackbarContainer {

    protected String TAG_TWEET_LIST = "TWEET_LIST";
    protected String TAG_CURRENT_PAGE = "CURRENTPAGE";

    protected Twitter twitter;
    protected Paging paging = new Paging(1, 50);
    protected int currentPage = 1;

    protected Toolbar toolbar;
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected FloatingActionButton newTweetFAB;
    protected ProgressBar loadingProgressBar;
    protected TweetsListAdapter mTweetsAdapter;
    protected ArrayList<Status> tweetList = new ArrayList<>(50);
    protected LinearLayoutManager mLinearLayoutManager;

    protected boolean isUp = true, loading = true, isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ScaleInBottomAnimator animator = new ScaleInBottomAnimator();
        animator.setAddDuration(300);

        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mRecyclerView.addItemDecoration(new SpaceTopItemDecoration(Common.dpToPx(this, 10)));
        mRecyclerView.setItemAnimator(animator);
        mTweetsAdapter = new TweetsListAdapter(tweetList, this, twitter, -1);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (loading && ((mLinearLayoutManager.getChildCount() + (mLinearLayoutManager.findFirstVisibleItemPosition() + 1))
                        >= mLinearLayoutManager.getItemCount() - 10)) {
                    loading = false;
                    new GetTimeLine().execute();
                }

                if (dy > 0) {
                    if (isUp)
                        newTweetDown();
                } else {
                    if (!isUp)
                        newTweetUp();
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(ThemeUtils.getColorPrimaryDark(TimeLineActivity.this),
                ThemeUtils.getColorPrimary(TimeLineActivity.this));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        new RefreshTimeLine().execute();
                    }
                });
            }
        });

        newTweetFAB = (FloatingActionButton) findViewById(R.id.newTweetFAB);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        if (tweetList.size() > 0)
            loadingProgressBar.setVisibility(View.GONE);

        this.newTweetFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TimeLineActivity.this, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, getInitialText());
                startActivity(i);
            }
        });

        this.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });
    }

    abstract String getInitialText();

    abstract List<Status> getCurrentTimeLine() throws TwitterException;

    abstract List<Status> getRefreshedTimeLine(Paging paging) throws TwitterException;

    void newTweetDown() {
        if (!isBlocked) {
            newTweetFAB.animate().translationY(newTweetFAB.getHeight() + (int) (getResources().getDimension(R.dimen.fabMargin))).start();
            isUp = false;
        }
    }

    void newTweetUp() {
        if (!isBlocked) {
            newTweetFAB.animate().translationY(0).start();
            isUp = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TAG_TWEET_LIST, tweetList);
        outState.putInt(TAG_CURRENT_PAGE, currentPage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(TimeLineActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void getTimeLineCallBack() {

    }

    public void showSnackBar(String content) {
        Snackbar.make(getWindow().getDecorView().findViewById(R.id.coordinatorLayout), content, Snackbar.LENGTH_SHORT).show();
        isUp = true;
        isBlocked = true;

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isBlocked = false;
            }
        }, 1800);
    }

    protected class GetTimeLine extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<twitter4j.Status> buffer = new ArrayList<>(50);

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                if (currentPage > 1)
                    paging.setMaxId(tweetList.get(tweetList.size() - 1).getId() - 1);

                buffer.addAll(getCurrentTimeLine());
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                currentPage += 1;
                loadingProgressBar.setVisibility(View.GONE);

                // needed for a smooth animation
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        for (twitter4j.Status tmp : buffer)
                            mTweetsAdapter.add(tmp);
                    }
                });

                getTimeLineCallBack();
            }

            loading = true;
        }
    }

    protected class RefreshTimeLine extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                Paging currentPaging = new Paging(1, 200);
                currentPaging.setSinceId(tweetList.get(0).getId());

                List<twitter4j.Status> newTweets = getRefreshedTimeLine(currentPaging);
                ListIterator<twitter4j.Status> it = newTweets.listIterator(newTweets.size());

                while (it.hasPrevious()) {
                    tweetList.add(0, it.previous());
                    tweetList.remove(tweetList.size() - 1);
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

            swipeRefreshLayout.setRefreshing(false);
        }
    }
}