package com.andreapivetta.blu.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.UserFollowed;
import com.andreapivetta.blu.services.BasicNotificationService;
import com.andreapivetta.blu.services.CheckFollowingService;
import com.andreapivetta.blu.services.PopulateDatabasesService;
import com.andreapivetta.blu.services.StreamNotificationService;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;


public class HomeActivity extends TimeLineActivity {

    private static final int REQUEST_LOGIN = 0;
    private static final String UPCOMING_TWEET_COUNT_TAG = "UPCOMING_TWEET_COUNT";
    private static final String UPCOMING_TWEETS_LIST_TAG = "UPCOMING_TWEET_LIST";

    private SharedPreferences mSharedPreferences;
    private DataUpdateReceiver dataUpdateReceiver;
    private int mNotificationsCount = 0, newTweetsCount = 0, mMessageCount = 0;
    private ArrayList<Status> upComingTweets = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!isTwitterLoggedInAlready()) {
            startActivityForResult(new Intent(HomeActivity.this, LoginActivity.class), REQUEST_LOGIN);
        } else {
            twitter = TwitterUtils.getTwitter(HomeActivity.this);

            if (savedInstanceState != null) {
                tweetList = (ArrayList<Status>) savedInstanceState.getSerializable(TAG_TWEET_LIST);
                upComingTweets = (ArrayList<Status>) savedInstanceState.getSerializable(UPCOMING_TWEETS_LIST_TAG);
                currentPage = savedInstanceState.getInt(TAG_CURRENT_PAGE);
                newTweetsCount = savedInstanceState.getInt(UPCOMING_TWEET_COUNT_TAG);
            } else {
                new GetTimeLine().execute();
            }

            if (mSharedPreferences.getBoolean(getString(R.string.pref_key_stream_service), false))
                startService(new Intent(HomeActivity.this, StreamNotificationService.class));

            if (mSharedPreferences.getBoolean(getString(R.string.pref_key_db_populated), false) &&
                    !mSharedPreferences.getBoolean("Following", false)) {
                showFollowingLoadingDialog();
            }
        }

        super.onCreate(savedInstanceState);

        this.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Status status : upComingTweets)
                    tweetList.add(0, status);

                mTweetsAdapter.notifyDataSetChanged();
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
                getSupportActionBar().setTitle(getString(R.string.app_name));
                newTweetsCount = 0;
                upComingTweets.clear();
            }
        });

        if (mSharedPreferences.getBoolean(getString(R.string.pref_key_stream_service), false)) {
            this.swipeRefreshLayout.setEnabled(false);

            if (newTweetsCount > 0)
                getSupportActionBar().setTitle(
                        getResources().getQuantityString(R.plurals.new_tweets, newTweetsCount, newTweetsCount));
        }
    }

    @Override
    String getInitialText() {
        return "";
    }

    @Override
    List<Status> getCurrentTimeLine() throws TwitterException {
        return twitter.getHomeTimeline(paging);
    }

    @Override
    List<Status> getRefreshedTimeLine(Paging paging) throws TwitterException {
        return twitter.getHomeTimeline(paging);
    }

    private boolean isTwitterLoggedInAlready() {
        return mSharedPreferences.getBoolean(getString(R.string.pref_key_login), false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("exit"))
            setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && ("exit").equalsIgnoreCase(getIntent().getStringExtra(("exit"))))
            onBackPressed();

        if (dataUpdateReceiver == null)
            dataUpdateReceiver = new DataUpdateReceiver();

        registerReceiver(dataUpdateReceiver, new IntentFilter(StreamNotificationService.NEW_TWEETS_INTENT));
        registerReceiver(dataUpdateReceiver, new IntentFilter(Notification.NEW_NOTIFICATION_INTENT));
        registerReceiver(dataUpdateReceiver, new IntentFilter(Message.NEW_MESSAGE_INTENT));

        DatabaseManager databaseManager = DatabaseManager.getInstance(HomeActivity.this);
        mNotificationsCount = databaseManager.getCountUnreadNotifications();

        if (mSharedPreferences.getBoolean(getString(R.string.pref_key_db_populated), false)) {
            mMessageCount = databaseManager.getCountUnreadDirectMessages();
        }

        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dataUpdateReceiver != null)
            unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                twitter = TwitterUtils.getTwitter(HomeActivity.this);
                new GetTimeLine().execute();

                if (mSharedPreferences.getBoolean(getString(R.string.pref_key_stream_service), false)) {
                    startService(new Intent(HomeActivity.this, StreamNotificationService.class));
                } else {
                    startService(new Intent(HomeActivity.this, PopulateDatabasesService.class));
                    BasicNotificationService.startService(HomeActivity.this);
                    CheckFollowingService.startService(HomeActivity.this);
                }
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(UPCOMING_TWEETS_LIST_TAG, upComingTweets);
        outState.putInt(UPCOMING_TWEET_COUNT_TAG, newTweetsCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem item = menu.findItem(R.id.action_notifications);
        MenuItemCompat.setActionView(item, R.layout.menu_notification_button);
        View view = MenuItemCompat.getActionView(item);
        ImageButton notificationImageButton = (ImageButton) view.findViewById(R.id.notificationImageButton);
        notificationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationsCount = 0;
                invalidateOptionsMenu();
                startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
            }
        });

        if (mNotificationsCount > 0) {
            TextView notificationsCountTextView = (TextView) view.findViewById(R.id.notificationCountTextView);
            notificationsCountTextView.setVisibility(View.VISIBLE);
            notificationsCountTextView.setText(String.valueOf(mNotificationsCount));
        }

        item = menu.findItem(R.id.action_messages);
        MenuItemCompat.setActionView(item, R.layout.menu_messages_button);
        view = MenuItemCompat.getActionView(item);
        ImageButton messagesImageButton = (ImageButton) view.findViewById(R.id.messagesImageButton);
        messagesImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ConversationsListActivity.class));
            }
        });

        if (mMessageCount > 0) {
            TextView messagesCountTextView = (TextView) view.findViewById(R.id.messagesCountTextView);
            messagesCountTextView.setVisibility(View.VISIBLE);
            messagesCountTextView.setText(String.valueOf(mMessageCount));
        }

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent i = new Intent(HomeActivity.this, SearchActivity.class);
                i.putExtra(SearchManager.QUERY, searchView.getQuery().toString());
                startActivity(i);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            onSearchRequested();
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(HomeActivity.this, UserActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void getTimeLineCallBack() {
        for (Status tmp : upComingTweets)
            if (tweetList.contains(tmp))
                upComingTweets.remove(tmp);

        if (upComingTweets.size() > 0)
            getSupportActionBar().setTitle(getResources().getQuantityString(
                    R.plurals.new_tweets, newTweetsCount, newTweetsCount));
    }

    public class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(StreamNotificationService.NEW_TWEETS_INTENT)) {
                Status newStatus = (Status) intent.getSerializableExtra(StreamNotificationService.TAG_PARCEL_STATUS);
                upComingTweets.add(newStatus);
                newTweetsCount++;
                if (loading)
                    getSupportActionBar().setTitle(getResources().getQuantityString(
                            R.plurals.new_tweets, newTweetsCount, newTweetsCount));
            } else if (intent.getAction().equals(Notification.NEW_NOTIFICATION_INTENT)) {
                mNotificationsCount++;
                invalidateOptionsMenu();
            } else if (intent.getAction().equals(Message.NEW_MESSAGE_INTENT)) {
                mMessageCount++;
                invalidateOptionsMenu();
            }
        }
    }

    private void showFollowingLoadingDialog() {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(HomeActivity.this, getString(R.string.wait_a_minute),
                getString(R.string.i_need_to_download), true);
        ringProgressDialog.setCancelable(true);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    long cursor = -1;
                    PagableResponseList<User> followingList;
                    DatabaseManager databaseManager = DatabaseManager.getInstance(HomeActivity.this);
                    do {
                        followingList = twitter.getFriendsList(twitter.getId(), cursor, 200);
                        for (User user : followingList)
                            databaseManager.insertFollowed(new UserFollowed(user.getId(), user.getName(), user.getScreenName(),
                                    user.getBiggerProfileImageURL()));
                    } while ((cursor = followingList.getNextCursor()) != 0);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mSharedPreferences.edit().putBoolean("Following", true).apply();
                ringProgressDialog.dismiss();
                CheckFollowingService.startService(HomeActivity.this);
            }

        }.execute();

    }
}