package com.andreapivetta.blu.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ConversationListAdapter;
import com.andreapivetta.blu.adapters.UserListMessageAdapter;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.data.UserFollowed;

import java.util.ArrayList;

public class ConversationsListActivity extends ThemedActivity {

    private ArrayList<Message> mDataSet = new ArrayList<>();
    private ArrayList<UserFollowed> followers = new ArrayList<>(), subset = new ArrayList<>();
    private ConversationListAdapter conversationListAdapter;
    private DataUpdateReceiver dataUpdateReceiver;
    private UserListMessageAdapter mUsersSimpleAdapter;
    private SharedPreferences mSharedPreferences;

    private boolean isUp = true;

    private FloatingActionButton newMessageFAB;
    private DatabaseManager databaseManager = DatabaseManager.getInstance(ConversationsListActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_list);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!mSharedPreferences.getBoolean(getString(R.string.pref_key_db_populated), false))
            showComeHereLaterDialog();

        mDataSet.addAll(databaseManager.getLastMessages());

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);
        conversationListAdapter = new ConversationListAdapter(ConversationsListActivity.this, mDataSet);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(conversationListAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        newMessageFAB = (FloatingActionButton) findViewById(R.id.newMessageFAB);
        newMessageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSharedPreferences.getBoolean(getString(R.string.pref_key_following_populated), false))
                    showChooseUserDialog();
                else
                    showChooseUserDialogLimited();
            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void showChooseUserDialogLimited() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationsListActivity.this);
        View dialogView = View.inflate(ConversationsListActivity.this, R.layout.dialog_select_user_limited, null);

        final EditText searchEditText = (EditText) dialogView.findViewById(R.id.userEditText);
        builder.setTitle(getString(R.string.insert_user_screenname))
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(ConversationsListActivity.this, ConversationActivity.class);
                        i.putExtra(ConversationActivity.TAG_ID, -1l);
                        i.putExtra(ConversationActivity.TAG_SCREEN_NAME, searchEditText.getText().toString());
                        startActivity(i);
                    }
                })
                .create().show();
    }

    void showChooseUserDialog() {
        if (followers.size() == 0)
            followers.addAll(databaseManager.getFollowed());
        subset.clear();
        subset.addAll(followers);

        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationsListActivity.this);
        View dialogView = View.inflate(ConversationsListActivity.this, R.layout.dialog_select_user, null);

        RecyclerView mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.usersRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersSimpleAdapter = new UserListMessageAdapter(subset, ConversationsListActivity.this);
        mRecyclerView.setAdapter(mUsersSimpleAdapter);

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
                for (int i = 0; i < followers.size(); i++)
                    if (followers.get(i).name.toLowerCase().startsWith(prefix))
                        subset.add(followers.get(i));

                mUsersSimpleAdapter.notifyDataSetChanged();
            }
        });

        builder.setView(dialogView)
                .setPositiveButton(R.string.cancel, null)
                .create().show();
    }

    void showComeHereLaterDialog() {
        new AlertDialog.Builder(ConversationsListActivity.this)
                .setCancelable(false)
                .setTitle(getString(R.string.ops))
                .setMessage(R.string.come_here_later_message)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConversationsListActivity.this.finish();
                    }
                })
                .create()
                .show();
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
        mDataSet.addAll(databaseManager.getLastMessages());
        conversationListAdapter.notifyDataSetChanged();
    }

    void newMessageDown() {
        if (mSharedPreferences.getBoolean(getString(R.string.pref_key_hide_fab), true))
            newMessageFAB.hide();
        isUp = false;
    }

    void newMessageUp() {
        if (mSharedPreferences.getBoolean(getString(R.string.pref_key_hide_fab), true))
            newMessageFAB.show();
        isUp = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conversations_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_mark_as_read) {
            databaseManager.markAllDirectMessagesAsRead();

            mDataSet.clear();
            mDataSet.addAll(databaseManager.getLastMessages());
            conversationListAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    public class DataUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Message.NEW_MESSAGE_INTENT)) {
                mDataSet.clear();
                mDataSet.addAll(databaseManager.getLastMessages());
                conversationListAdapter.notifyDataSetChanged();
            }
        }
    }

}
