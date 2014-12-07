package com.andreapivetta.blu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.TweetsListHeaderAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import twitter4j.MediaEntity;
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

    private ImageView userProfilePicImageView, tweetPhotoImageView;
    private TextView userNameTextView, screenNameTextView, timeTextView, statusTextView,
            retweetTextView, retweetsStatsTextView, favouritesStatsTextView;
    private ImageButton favouriteImageButton, retweetImageButton, respondImageButton, shareImageButton;

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

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mTweetsAdapter = new TweetsListHeaderAdapter(mDataSet, this, twitter);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);

        /*this.userProfilePicImageView = (ImageView) findViewById(R.id.userProfilePicImageView);
        this.userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        this.screenNameTextView = (TextView) findViewById(R.id.screenNameTextView);
        this.timeTextView = (TextView) findViewById(R.id.timeTextView);
        this.statusTextView = (TextView) findViewById(R.id.statusTextView);
        this.retweetTextView = (TextView) findViewById(R.id.retweetTextView);
        this.retweetsStatsTextView = (TextView) findViewById(R.id.retweetsStatsTextView);
        this.favouritesStatsTextView = (TextView) findViewById(R.id.favouritesStatsTextView);
        this.tweetPhotoImageView = (ImageView) findViewById(R.id.tweetPhotoImageView);
        this.favouriteImageButton = (ImageButton) findViewById(R.id.favouriteImageButton);
        this.retweetImageButton = (ImageButton) findViewById(R.id.retweetImageButton);
        this.respondImageButton = (ImageButton)  findViewById(R.id.respondImageButton);
        this.shareImageButton = (ImageButton) findViewById(R.id.shareImageButton);*/

        new LoadStatus().execute(null, null, null);
    }

    void setUpUI() {
        if (status.isRetweet()) {
            retweetTextView.setVisibility(View.VISIBLE);
            retweetTextView.setText(getString(R.string.retweeted_by) + " @" + status.getUser().getScreenName());
            status = status.getRetweetedStatus();
        }

        Picasso.with(TweetActivity.this)
                .load(this.status.getUser().getProfileImageURL())
                .placeholder(getResources().getDrawable(R.drawable.placeholder))
                .into(userProfilePicImageView);

        this.userNameTextView.setText(this.status.getUser().getName());
        this.screenNameTextView.setText("@" + this.status.getUser().getScreenName());
        this.timeTextView.setText(new SimpleDateFormat("hh:mm").format(status.getCreatedAt()));
        this.statusTextView.setText(status.getText());

        String amount = status.getFavoriteCount() + "";
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

        SpannableStringBuilder sb = new SpannableStringBuilder(amount + " " + getString(R.string.favourites));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        favouritesStatsTextView.setText(sb);

        amount = status.getRetweetCount() + "";
        b = new StyleSpan(android.graphics.Typeface.BOLD);

        sb = new SpannableStringBuilder(amount + " " + getString(R.string.retweets));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        retweetsStatsTextView.setText(sb);

        MediaEntity[] mediaEntityArray = status.getMediaEntities();
        if (mediaEntityArray.length > 0) {
            for (MediaEntity mediaEntity : mediaEntityArray) {
                if (mediaEntity.getType().equals("photo")) {
                    tweetPhotoImageView.setVisibility(View.VISIBLE);
                    Picasso.with(TweetActivity.this)
                            .load(mediaEntity.getMediaURL())
                            .placeholder(getResources().getDrawable(R.drawable.placeholder))
                            .into(tweetPhotoImageView);
                    break;
                }
            }
        }

        new LoadResponses().execute(null, null, null);

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

                Log.i("STATUS_LOADED", status.toString());
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                //setUpUI();
                mDataSet.add(status);
                new LoadResponses().execute(null, null, null);
            }
        }
    }

    private class LoadResponses extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Query query = new Query("to:" + status.getUser().getScreenName());
                QueryResult result = twitter.search(query);
                for (twitter4j.Status tmpStatus : result.getTweets()) {
                    if (status.getId() == tmpStatus.getInReplyToStatusId()) {
                        mDataSet.add(tmpStatus);
                        Log.i("REPLY", tmpStatus.getText());
                    }
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
