package com.andreapivetta.blu.activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.services.BasicNotificationService;
import com.andreapivetta.blu.services.CheckFollowingService;
import com.andreapivetta.blu.services.PopulateDatabasesService;
import com.andreapivetta.blu.services.StreamNotificationService;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;


public class HomeActivity extends TimeLineActivity {

    private static final int REQUEST_LOGIN = 0;
    private static final String UPCOMING_TWEET_COUNT_TAG = "UPCOMING_TWEET_COUNT";
    private static final String UPCOMING_TWEETS_LIST_TAG = "UPCOMING_TWEET_LIST";

    private SharedPreferences mSharedPreferences;
    private DataUpdateReceiver dataUpdateReceiver;
    private int mNotificationsCount = 0, newTweetsCount = 0, mMessageCount = 0;
    private ArrayList<Status> upComingTweets = new ArrayList<>();

    private BillingProcessor billingProcessor;

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
            swipeRefreshLayout.setEnabled(false);

            if (newTweetsCount > 0)
                getSupportActionBar().setTitle(getResources().getQuantityString(R.plurals.new_tweets, newTweetsCount, newTweetsCount));
        }

        if (!mSharedPreferences.getBoolean(getString(R.string.pref_ads_removed), false)) {
            AdView mAdView = (AdView) findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("BF5C84C7EE32D459CE186190E0A187C7") // Nexus6p emulator
                    .addTestDevice("D92FD0DC69AECDDD9D41C63DEF1D68C4") // Nexus4
                    .addTestDevice("E2FC22DC6449FC5B5658CD110B58923A") // LG
                    .build();
            mAdView.loadAd(adRequest);
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

        if (billingProcessor != null)
            billingProcessor.release();
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

        if (mSharedPreferences.getBoolean(getString(R.string.pref_ads_removed), false))
            getMenuInflater().inflate(R.menu.menu_home, menu);
        else
            getMenuInflater().inflate(R.menu.menu_home_ads, menu);

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
        } else if (item.getItemId() == R.id.action_remove_ads) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            final View dialogView = View.inflate(HomeActivity.this, R.layout.dialog_remove_ads, null);

            final ProgressBar loadingProgressBar = (ProgressBar) dialogView.findViewById(R.id.loadingProgressBar);
            final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.iapRadioGroup);

            ((RadioButton) dialogView.findViewById(R.id.tweet_radioButton)).setText(getString(R.string.tweet));
            builder.setTitle(getString(R.string.iap_pref_title))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (radioGroup.getCheckedRadioButtonId()) {
                                case R.id.iap_099_radioButton:
                                    billingProcessor.purchase(HomeActivity.this, "remove_ads_099");
                                    break;
                                case R.id.iap_199_radioButton:
                                    billingProcessor.purchase(HomeActivity.this, "remove_ads_199");
                                    break;
                                case R.id.iap_499_radioButton:
                                    billingProcessor.purchase(HomeActivity.this, "remove_ads_049");
                                    break;
                                case R.id.iap_999_radioButton:
                                    billingProcessor.purchase(HomeActivity.this, "remove_ads_999");
                                    break;
                                case R.id.tweet_radioButton:
                                    new AlertDialog.Builder(HomeActivity.this).setTitle(getString(R.string.pay_with_tweet_title))
                                            .setMessage(getString(R.string.pay_with_tweet_status))
                                            .setPositiveButton(getString(R.string.tweet), new DialogInterface.OnClickListener() {
                                                @SuppressLint("CommitPrefEdits")
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new UpdateTwitterStatus(HomeActivity.this, twitter, -1l)
                                                            .execute(getString(R.string.pay_with_tweet_status));
                                                    mSharedPreferences.edit().putBoolean(getString(R.string.pref_ads_removed), true).commit();
                                                    Toast.makeText(HomeActivity.this, R.string.completed_purchase, Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    startActivity(getIntent());
                                                }
                                            }).setNegativeButton(getString(R.string.keep_ads), null)
                                            .create().show();
                                    break;
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setView(dialogView)
                    .create().show();

            billingProcessor = new BillingProcessor(HomeActivity.this, null, new BillingProcessor.IBillingHandler() {
                @Override
                public void onProductPurchased(String s, TransactionDetails transactionDetails) {
                    mSharedPreferences.edit().putBoolean(getString(R.string.pref_ads_removed), true).commit();
                    Toast.makeText(HomeActivity.this, R.string.completed_purchase, Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }

                @Override
                public void onPurchaseHistoryRestored() {

                }

                @Override
                public void onBillingError(int i, Throwable throwable) {
                    Toast.makeText(HomeActivity.this, R.string.failed_purchase, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBillingInitialized() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (setUpDialog(dialogView)) {
                                loadingProgressBar.setVisibility(View.GONE);
                                radioGroup.setVisibility(View.VISIBLE);
                            } else {
                                finish();
                                startActivity(getIntent());
                            }
                        }
                    }, 2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean setUpDialog(View dialogView) {
        if (billingProcessor.isPurchased("remove_ads_099") || billingProcessor.isPurchased("remove_ads_199") ||
                billingProcessor.isPurchased("remove_ads_049") || billingProcessor.isPurchased("remove_ads_999")) {
            mSharedPreferences.edit().putBoolean(getString(R.string.pref_ads_removed), true).apply();
            return false;
        }

        String[] ids = new String[]{"remove_ads_099", "remove_ads_199", "remove_ads_049", "remove_ads_999"};
        List<SkuDetails> details = billingProcessor.getPurchaseListingDetails(new ArrayList<>(Arrays.asList(ids)));
        for (SkuDetails detail : details) {
            if (ids[0].equals(detail.productId)) {
                ((RadioButton) dialogView.findViewById(R.id.iap_099_radioButton)).setText(detail.priceText);
            } else if (ids[1].equals(detail.productId)) {
                ((RadioButton) dialogView.findViewById(R.id.iap_199_radioButton)).setText(detail.priceText);
            } else if (ids[2].equals(detail.productId)) {
                ((RadioButton) dialogView.findViewById(R.id.iap_499_radioButton)).setText(detail.priceText);
            } else {
                ((RadioButton) dialogView.findViewById(R.id.iap_999_radioButton)).setText(detail.priceText);
            }
        }

        return true;
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
}