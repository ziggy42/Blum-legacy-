package com.andreapivetta.blu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.andreapivetta.blu.adapters.TweetListAdapter;
import com.andreapivetta.blu.twitter.TwitterKs;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity {

    private ImageButton newTweetImageButton;
    private SharedPreferences mSharedPreferences;
    private TweetListAdapter mTweetsAdapter;
    private ArrayList<Status> tweetList = new ArrayList<Status>();
    private LinearLayoutManager mLinearLayoutManager;
    Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mSharedPreferences = getSharedPreferences("MyPref", 0);

        if (!isTwitterLoggedInAlready()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TwitterKs.TWITTER_CONSUMER_KEY)
                    .setOAuthConsumerSecret(TwitterKs.TWITTER_CONSUMER_SECRET);

            // Access Token
            String access_token = mSharedPreferences.getString(
                    TwitterKs.PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = mSharedPreferences.getString(
                    TwitterKs.PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token,
                    access_token_secret);
            twitter = new TwitterFactory(builder.build())
                    .getInstance(accessToken);

            new GetTimeLine().execute(null, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mTweetsAdapter = new TweetListAdapter(tweetList, this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // TODO
            }
        });

        newTweetImageButton = (ImageButton) findViewById(R.id.newTweetImageButton);
        setOnClickListener();

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
    }

    public class GetTimeLine extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                for (twitter4j.Status status : twitter.getHomeTimeline())
                    tweetList.add(status);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mTweetsAdapter.notifyDataSetChanged();
            }
        }
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
}
