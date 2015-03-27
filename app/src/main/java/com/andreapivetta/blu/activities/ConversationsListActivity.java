package com.andreapivetta.blu.activities;

import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
    private RecyclerView mRecyclerView;

    private boolean isUp = true;

    private ProgressBar loadingProgressBar;
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

        newMessageImageButton = (ImageButton) findViewById(R.id.newMessageImageButton);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

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

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });

        new LoadConversations().execute(null, null, null);
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

    private class LoadConversations extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(ConversationsListActivity.this);
            dbm.open();

            for (Long id : dbm.getInterlocutors())
                mDataSet.add(dbm.getLastMessageForGivenUser(id));

            Collections.sort(mDataSet);

            dbm.close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                conversationListAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
            }
        }
    }
}
