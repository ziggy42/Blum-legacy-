package com.andreapivetta.blu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ConversationAdapter;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.twitter.SendDirectMessage;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;
import java.util.Calendar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class ConversationActivity extends ActionBarActivity {

    private User currentUser;
    private Twitter twitter;
    private long userID;
    private ArrayList<Message> messages = new ArrayList<>();

    private ProgressBar loadingProgressBar;
    private Toolbar toolbar;
    private EditText messageEditText;

    private ConversationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        twitter = TwitterUtils.getTwitter(ConversationActivity.this);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        ImageButton sendMessageImageButton = (ImageButton) findViewById(R.id.sendMessageImageButton);

        userID = getIntent().getLongExtra("ID", 0L);

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);
        mAdapter = new ConversationAdapter(messages, ConversationActivity.this);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        sendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString();
                if (message.length() > 0) {
                    new SendDirectMessage(ConversationActivity.this, twitter, userID, messages.get(0).getOtherUserName(),
                            messages.get(0).getOtherUserProfilePicUrl()).execute(message, null, null);

                    messages.add(
                            new Message(0L, getSharedPreferences(Common.PREF, 0).getLong(Common.PREF_LOGGED_USER, 0L),
                                    0L, message, Calendar.getInstance().getTime().getTime(), "", "")); // MOLTO PERICOLOSO

                    mAdapter.notifyDataSetChanged();

                    messageEditText.setText("");
                    mRecyclerView.scrollToPosition(messages.size() - 1);
                }
            }
        });

        if (userID == 0)
            finish();
        else
            new Loader().execute(null, null, null);
    }

    private class Loader extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                currentUser = twitter.showUser(userID);
                DirectMessagesDatabaseManager dbm = new DirectMessagesDatabaseManager(ConversationActivity.this);
                dbm.open();

                for (Message message : dbm.getConversation(userID))
                    messages.add(message);

                dbm.close();
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                loadingProgressBar.setVisibility(View.GONE);
                toolbar.setTitle(currentUser.getName());
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
