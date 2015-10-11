package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.interf.SnackbarContainer;
import com.andreapivetta.blu.adapters.UserProfileAdapter;
import com.andreapivetta.blu.adapters.decorators.SpaceTopItemDecoration;
import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserActivity extends AppCompatActivity implements SnackbarContainer {

    public final static String TAG_USER = "user";
    public final static String TAG_ID = "id";

    private static final String TAG_DATA = "mDataSet";

    private Twitter twitter;
    private Paging paging = new Paging(1, 30);
    private int currentPage = 1;

    private Intent intent;
    private User user;
    private ArrayList<Object> mDataSet = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private UserProfileAdapter mAdapter;

    private ImageView profileBackgroundImageView;
    private FloatingActionButton tweetToUserButton;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RecyclerView mRecyclerView;

    private boolean isUp = true, loading = true, isBlocked = false;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        switch (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_themes), "B")) {
            case "B":
                setTheme(R.style.BlueUserTheme);
                break;
            case "P":
                setTheme(R.style.PinkUserTheme);
                break;
            case "G":
                setTheme(R.style.GreenUserTheme);
                break;
            case "D":
                setTheme(R.style.DarkUserTheme);
                break;
            default:
                setTheme(R.style.BlueUserTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

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

        twitter = TwitterUtils.getTwitter(this);
        profileBackgroundImageView = (ImageView) findViewById(R.id.profileBackgroundImageView);
        tweetToUserButton = (FloatingActionButton) findViewById(R.id.tweetToUserButton);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);

        if (savedInstanceState != null) {
            user = (User) savedInstanceState.getSerializable(TAG_USER);
            mDataSet = (ArrayList) savedInstanceState.getSerializable(TAG_DATA);
            setUpUI();
        } else {
            setUpUser();
        }
    }

    void setUpUser() {
        intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.containsKey(TAG_USER)) {
            user = (User) bundle.getSerializable(TAG_USER);
            mDataSet.add(user);
            setUpUI();

            new LoadTweets().execute();
        } else {
            new LoadUser().execute();
        }
    }

    void setUpUI() {
        mRecyclerView.addItemDecoration(new SpaceTopItemDecoration(Common.dpToPx(this, 10)));
        mAdapter = new UserProfileAdapter(mDataSet, this, twitter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (loading && ((mLinearLayoutManager.getChildCount() + (mLinearLayoutManager.findFirstVisibleItemPosition() + 1))
                        >= mLinearLayoutManager.getItemCount() - 10)) {
                    loading = false;
                    new LoadTweets().execute();
                }

                if (dy > 0) {
                    if (isUp)
                        tweetToUserButtonDown();
                } else {
                    if (!isUp)
                        tweetToUserButtonUp();
                }
            }
        });

        collapsingToolbarLayout.setTitle(user.getName());

        Glide.with(this)
                .load(user.getProfileBannerURL())
                .asBitmap()
                .into(profileBackgroundImageView);

        tweetToUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserActivity.this, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + user.getScreenName());
                startActivity(i);
            }
        });
    }

    void tweetToUserButtonDown() {
        if (!isBlocked) {
            tweetToUserButton.animate().translationY(
                    tweetToUserButton.getHeight() + (int) (getResources().getDimension(R.dimen.fabMargin))).start();
            isUp = false;
        }
    }

    void tweetToUserButtonUp() {
        if (!isBlocked) {
            tweetToUserButton.animate().translationY(0).start();
            isUp = true;
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TAG_USER, user);
        outState.putSerializable(TAG_DATA, mDataSet);
        super.onSaveInstanceState(outState);
    }

    private class LoadUser extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Uri uri = intent.getData();
                if (uri != null)
                    user = twitter.showUser(uri.toString().substring(29));
                else {
                    long id = intent.getLongExtra(TAG_ID, 0);
                    if (id != 0) {
                        user = twitter.showUser(id);
                        if (user.getId() == twitter.getId()) {
                            return true;
                        }
                    } else {
                        user = twitter.showUser(twitter.getId());
                        return true;
                    }
                }

            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean status) {
            if (status) {
                mDataSet.add(user);
                setUpUI();

                new LoadTweets().execute();
            } else {
                if (!new ConnectionDetector(UserActivity.this).isConnectingToInternet())
                    Toast.makeText(UserActivity.this,
                            getString(R.string.cant_find_user), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(UserActivity.this,
                            getString(R.string.reached_twitter_user_limit), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class LoadTweets extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                if (currentPage > 1)
                    paging.setMaxId(((twitter4j.Status) mDataSet.get(mDataSet.size() - 1)).getId() - 1);

                mDataSet.addAll(twitter.getUserTimeline(user.getId(), paging));
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                currentPage += 1;
                mAdapter.notifyDataSetChanged();
            }

            loading = true;
        }
    }
}
