package com.andreapivetta.blu;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.andreapivetta.blu.adapters.TweetListAdapter;
import com.andreapivetta.blu.twitter.TwitterKs;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton newTweetImageButton;
    private SharedPreferences mSharedPreferences;
    private TweetListAdapter mTweetsAdapter;
    private ArrayList<Status> tweetList = new ArrayList<>();
    Twitter twitter;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean isUp = true, loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;

    Paging paging = new Paging(1, 200);
    int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mSharedPreferences = getSharedPreferences("MyPref", 0);

        if (!isTwitterLoggedInAlready())
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TwitterKs.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(TwitterKs.TWITTER_CONSUMER_SECRET);

        AccessToken accessToken = new AccessToken(mSharedPreferences.getString(TwitterKs.PREF_KEY_OAUTH_TOKEN, ""),
                mSharedPreferences.getString(TwitterKs.PREF_KEY_OAUTH_SECRET, ""));
        twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

        new GetTimeLine().execute(null, null, null);

        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mTweetsAdapter = new TweetListAdapter(tweetList, this);
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

                if (loading) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        new GetTimeLine().execute(null, null, null);
                    }
                }

                if(dy > 0) {
                    if(isUp) {
                        newTweetDown();
                    }
                } else {
                    if(!isUp) {
                        newTweetUp();
                    }
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        new RefreshTimeLine().execute(null, null, null);
                    }
                }, 5000);
            }
        });

        newTweetImageButton = (ImageButton) findViewById(R.id.newTweetImageButton);
        setOnClickListener();
    }

    void newTweetDown() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newTweetImageButton.getLayoutParams();
        ValueAnimator downAnimator = ValueAnimator.ofInt(params.bottomMargin, -120);
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
        ValueAnimator upAnimator = ValueAnimator.ofInt(params.bottomMargin, 20);
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

    private boolean isTwitterLoggedInAlready() {
        return mSharedPreferences.getBoolean(TwitterKs.PREF_KEY_TWITTER_LOGIN, false);
    }

    private void setOnClickListener() {
        this.newTweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class GetTimeLine extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                paging.setPage(currentPage);
                for (twitter4j.Status status : twitter.getHomeTimeline(paging))
                    tweetList.add(status);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mTweetsAdapter.notifyDataSetChanged();
                currentPage += 1;
            }

            loading = true;
        }
    }

    public class RefreshTimeLine extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                Paging currentPaging = new Paging();
                currentPaging.setSinceId(tweetList.get(0).getId());
                List<twitter4j.Status> newTweets = twitter.getHomeTimeline(currentPaging);
                ListIterator<twitter4j.Status> it = newTweets.listIterator(newTweets.size());

                while(it.hasPrevious())
                    tweetList.add(0, it.previous());

            } catch (TwitterException e) {
                e.printStackTrace();
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
