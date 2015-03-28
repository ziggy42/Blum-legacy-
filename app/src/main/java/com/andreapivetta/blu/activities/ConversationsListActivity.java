package com.andreapivetta.blu.activities;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ConversationListAdapter;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;
import java.util.Collections;

public class ConversationsListActivity extends ActionBarActivity {

    private ArrayList<Message> mDataSet = new ArrayList<>();
    private ConversationListAdapter conversationListAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private DataUpdateReceiver dataUpdateReceiver;

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

            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });

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
}
