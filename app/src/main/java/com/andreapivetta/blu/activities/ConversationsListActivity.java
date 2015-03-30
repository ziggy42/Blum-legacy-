package com.andreapivetta.blu.activities;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ConversationListAdapter;
import com.andreapivetta.blu.adapters.UserListMessageAdapter;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;
import java.util.Collections;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class ConversationsListActivity extends ActionBarActivity {

    private ArrayList<Message> mDataSet = new ArrayList<>();
    private ArrayList<User> followers = new ArrayList<>(), subset = new ArrayList<>();
    private ConversationListAdapter conversationListAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private DataUpdateReceiver dataUpdateReceiver;
    private UserListMessageAdapter mUsersSimpleAdapter;
    private ProgressBar loadingProgressBar;
    private Twitter t;


    private boolean isUp = true;

    private RecyclerView mRecyclerView;
    private ImageButton newMessageImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_list);

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

        t = TwitterUtils.getTwitter(ConversationsListActivity.this);

        DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(ConversationsListActivity.this);
        dbm.open();
        for (Long id : dbm.getInterlocutors())
            mDataSet.add(dbm.getLastMessageForGivenUser(id));
        dbm.close();
        Collections.sort(mDataSet);

        mRecyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);
        conversationListAdapter = new ConversationListAdapter(ConversationsListActivity.this, mDataSet);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(conversationListAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    if (isUp)
                        newMessageDown();
                } else {
                    if (!isUp)
                        newMessageUp();
                }
            }
        });

        newMessageImageButton = (ImageButton) findViewById(R.id.newMessageImageButton);
        newMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });
    }

    void showDialog() {
        mUsersSimpleAdapter = new UserListMessageAdapter(subset, ConversationsListActivity.this);
        new LoadFollowers().execute(null, null, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationsListActivity.this);
        View dialogView = View.inflate(ConversationsListActivity.this, R.layout.dialog_select_user, null);
        RecyclerView mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.usersRecyclerView);

        LinearLayoutManager mDialogLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mDialogLinearLayoutManager);
        mRecyclerView.setAdapter(mUsersSimpleAdapter);

        loadingProgressBar = (ProgressBar) dialogView.findViewById(R.id.loadingProgressBar);

        EditText searchEditText = (EditText) dialogView.findViewById(R.id.findUserEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String prefix = s.toString().toLowerCase();
                subset.clear();
                for (User u : followers)
                    if (u.getName().toLowerCase().startsWith(prefix))
                        subset.add(u);

                mUsersSimpleAdapter.notifyDataSetChanged();
            }
        });

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, null)
                .create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dataUpdateReceiver == null)
            dataUpdateReceiver = new DataUpdateReceiver();

        registerReceiver(dataUpdateReceiver, new IntentFilter(Message.NEW_MESSAGE_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (dataUpdateReceiver != null)
            unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mDataSet.clear();
        DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(ConversationsListActivity.this);
        dbm.open();
        for (Long id : dbm.getInterlocutors())
            mDataSet.add(dbm.getLastMessageForGivenUser(id));
        dbm.close();
        Collections.sort(mDataSet);
        conversationListAdapter.notifyDataSetChanged();
    }

    void newMessageDown() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newMessageImageButton.getLayoutParams();
        ValueAnimator downAnimator = ValueAnimator.ofInt(params.bottomMargin, -newMessageImageButton.getHeight());
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                newMessageImageButton.requestLayout();
            }
        });
        downAnimator.setDuration(200)
                .start();

        isUp = false;
    }

    void newMessageUp() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newMessageImageButton.getLayoutParams();
        ValueAnimator upAnimator = ValueAnimator.ofInt(params.bottomMargin, Common.dpToPx(this, 20));
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                newMessageImageButton.requestLayout();
            }
        });
        upAnimator.setDuration(200)
                .start();

        isUp = true;
    }

    public class DataUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Message.NEW_MESSAGE_INTENT)) {
                mDataSet.clear();
                DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(ConversationsListActivity.this);
                dbm.open();
                for (Long id : dbm.getInterlocutors())
                    mDataSet.add(dbm.getLastMessageForGivenUser(id));
                dbm.close();
                Collections.sort(mDataSet);
                conversationListAdapter.notifyDataSetChanged();
            }
        }
    }

    private class LoadFollowers extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                long cursor = -1;
                PagableResponseList<User> usersResponse = t.getFollowersList(t.getScreenName(), cursor, 200);
                followers.addAll(usersResponse);
                subset.addAll(usersResponse);

                while (usersResponse.hasNext()) {
                    cursor = usersResponse.getNextCursor();
                    usersResponse = t.getFollowersList(t.getScreenName(), cursor, 200);
                    followers.addAll(usersResponse);
                    subset.addAll(usersResponse);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean status) {
            if (status) {
                loadingProgressBar.setVisibility(View.GONE);
                mUsersSimpleAdapter.notifyDataSetChanged();
            }
        }
    }
}
