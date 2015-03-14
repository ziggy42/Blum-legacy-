package com.andreapivetta.blu.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public abstract class TimeLineActivity extends ActionBarActivity {

    protected String TWEETS_LIST_TAG = "TWEET_LIST";

    protected Twitter twitter;
    protected Paging paging = new Paging(1, 200);
    protected int currentPage = 1;

    protected Toolbar toolbar;
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected ImageButton newTweetImageButton;
    protected ProgressBar loadingProgressBar;
    protected TweetsListAdapter mTweetsAdapter;
    protected ArrayList<Status> tweetList = new ArrayList<>();
    protected LinearLayoutManager mLinearLayoutManager;

    protected boolean isUp = true, loading = true;
    protected int pastVisibleItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(Common.dpToPx(this, 10)));

        SharedPreferences mSharedPreferences = getSharedPreferences(Common.PREF, 0);
        if (mSharedPreferences.getBoolean(Common.PREF_ANIMATIONS, true)) {
            mRecyclerView.setItemAnimator(new ScaleInBottomAnimator());
            mRecyclerView.getItemAnimator().setAddDuration(300);
        }

        mTweetsAdapter = new TweetsListAdapter(tweetList, this, twitter, -1);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mLinearLayoutManager.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition() + 1;

                if (loading && ((visibleItemCount + pastVisibleItems) >= totalItemCount)) {
                    loading = false;
                    new GetTimeLine().execute(null, null, null);
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
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new RefreshTimeLine().execute(null, null, null);
                    }
                }, 5000);
            }
        });

        newTweetImageButton = (ImageButton) findViewById(R.id.newTweetImageButton);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        if (tweetList.size() > 0)
            loadingProgressBar.setVisibility(View.GONE);

        this.newTweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TimeLineActivity.this, NewTweetActivity.class);
                i.putExtra("USER_PREFIX", getInitialText());
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
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newTweetImageButton.getLayoutParams();
        ValueAnimator downAnimator = ValueAnimator.ofInt(params.bottomMargin, -newTweetImageButton.getHeight());
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                newTweetImageButton.requestLayout();
            }
        });
        downAnimator.setDuration(200);
        downAnimator.start();

        isUp = false;
    }

    void newTweetUp() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newTweetImageButton.getLayoutParams();
        ValueAnimator upAnimator = ValueAnimator.ofInt(params.bottomMargin, Common.dpToPx(this, 20));
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                newTweetImageButton.requestLayout();
            }
        });
        upAnimator.setDuration(200);
        upAnimator.start();

        isUp = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TWEETS_LIST_TAG, tweetList);
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

    protected class GetTimeLine extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<twitter4j.Status> buffer = new ArrayList<>();

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                paging.setPage(currentPage);
                for (twitter4j.Status status : getCurrentTimeLine())
                    buffer.add(status);
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

                while (it.hasPrevious())
                    tweetList.add(0, it.previous());

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