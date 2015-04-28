package com.andreapivetta.blu.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetActivity extends AppCompatActivity {

    private static final String TWEETS_LIST_TAG = "tweetlist";
    private static final String CURRENT_TWEET_TAG = "header";

    private boolean isUp = true;
    private Twitter twitter;
    private Status status;
    private ArrayList<Status> mDataSet = new ArrayList<>();
    private TweetsListAdapter mTweetsAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageButton replyImageButton;
    private ProgressBar loadingProgressBar;

    private int currentIndex = 0;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getBundleExtra("STATUS");
            if (bundle != null)
                this.status = (Status) bundle.getSerializable("TWEET");
        } else {
            mDataSet = (ArrayList<Status>) savedInstanceState.getSerializable(TWEETS_LIST_TAG);
            this.currentIndex = savedInstanceState.getInt(CURRENT_TWEET_TAG);
            this.status = mDataSet.get(currentIndex);
            loadingProgressBar.setVisibility(View.GONE);
        }

        this.twitter = TwitterUtils.getTwitter(TweetActivity.this);
        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);

        /*SharedPreferences mSharedPreferences = getSharedPreferences(Common.PREF, 0);
        if (mSharedPreferences.getBoolean(Common.PREF_ANIMATIONS, true)) {
            mRecyclerView.setItemAnimator(new ScaleInBottomAnimator());
            mRecyclerView.getItemAnimator().setAddDuration(300);
        }*/

        mRecyclerView.setVerticalScrollBarEnabled(false);
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

        replyImageButton = (ImageButton) findViewById(R.id.replyImageButton);
        replyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reply(status);
            }
        });

        if (mDataSet.isEmpty())
            new LoadConversation().execute(null, null, null);

    }

    void reply(Status status) {
        Intent i = new Intent(TweetActivity.this, NewTweetActivity.class);
        i.putExtra("USER_PREFIX", "@" + status.getUser().getScreenName())
                .putExtra("REPLY_ID", status.getId());
        startActivity(i);
    }

    void newTweetDown() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) replyImageButton.getLayoutParams();
        ValueAnimator downAnimator = ValueAnimator.ofInt(params.bottomMargin, -replyImageButton.getHeight());
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                replyImageButton.requestLayout();
            }
        });
        downAnimator.setDuration(200);
        downAnimator.start();

        isUp = false;
    }

    void newTweetUp() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) replyImageButton.getLayoutParams();
        ValueAnimator upAnimator = ValueAnimator.ofInt(params.bottomMargin, Common.dpToPx(this, 20));
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                replyImageButton.requestLayout();
            }
        });
        upAnimator.setDuration(200);
        upAnimator.start();

        isUp = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TWEETS_LIST_TAG, mDataSet);
        outState.putInt(CURRENT_TWEET_TAG, currentIndex);
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

    private class LoadConversation extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<twitter4j.Status> buffer = new ArrayList<>();

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (status == null)
                    status = twitter.showStatus(getIntent().getLongExtra("STATUS_ID", 0));

                twitter4j.Status current = status;
                long id;
                while ((id = current.getInReplyToStatusId()) != -1) {
                    current = twitter.showStatus(id);
                    buffer.add(0, current);
                }

                currentIndex = buffer.size();
                buffer.add(status);

                QueryResult result = twitter.search(new Query("to:" + status.getUser().getScreenName()));
                for (twitter4j.Status tmpStatus : result.getTweets())
                    if (status.getId() == tmpStatus.getInReplyToStatusId())
                        buffer.add(tmpStatus);

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
                for (int i = 0; i < buffer.size(); i++)
                    mTweetsAdapter.add(buffer.get(i));
                mRecyclerView.scrollToPosition(currentIndex);
            }
        }
    }
}
