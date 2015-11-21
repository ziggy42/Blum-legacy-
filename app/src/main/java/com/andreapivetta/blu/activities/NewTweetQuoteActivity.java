package com.andreapivetta.blu.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.decorators.SpaceLeftMarginItemDecoration;
import com.andreapivetta.blu.adapters.holders.UserSuggestedViewHolder;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.UserFollowed;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.andreapivetta.blu.utilities.CircleTransform;
import com.andreapivetta.blu.utilities.Common;
import com.andreapivetta.blu.views.EditTextCursorWatcher;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;

public class NewTweetQuoteActivity extends ThemedActivity {

    public static final String PAR_CURRENT_STATUS = "currentStatus";
    public static final String PAR_BUNDLE = "bundle";
    private static final int MAX_URL_LENGTH = 23;
    private Twitter twitter;

    private EditTextCursorWatcher newTweetEditText;

    private RecyclerView followedRecyclerView;
    private UserFollowedAdapter followedAdapter;
    private ArrayList<UserFollowed> followers = new ArrayList<>(), subset = new ArrayList<>();

    private boolean suggestionsOn = false;
    private int lastAtIndex = -1;
    private int charsAfterAt = 0;
    private int charsLeft = 140 - MAX_URL_LENGTH;
    private Status currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet_quote);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        twitter = TwitterUtils.getTwitter(NewTweetQuoteActivity.this);
        currentStatus = (Status) getIntent().getBundleExtra(PAR_BUNDLE).getSerializable(PAR_CURRENT_STATUS);

        ((TextView) findViewById(R.id.userNameTextView)).setText(currentStatus.getUser().getName());
        TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusTextView.setText(currentStatus.getText());
        Linkify.addLinks(statusTextView, Linkify.ALL);

        FrameLayout containerCardView = (FrameLayout) findViewById(R.id.containerCardView);
        containerCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NewTweetQuoteActivity.this, TweetActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(TweetActivity.TAG_TWEET, currentStatus);
                i.putExtra(TweetActivity.TAG_STATUS_BUNDLE, b);
                NewTweetQuoteActivity.this.startActivity(i);
            }
        });

        followedRecyclerView = (RecyclerView) findViewById(R.id.followedRecyclerView);
        followedRecyclerView.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(
                NewTweetQuoteActivity.this, LinearLayoutManager.HORIZONTAL, false));
        followedRecyclerView.setHasFixedSize(true);
        followedRecyclerView.addItemDecoration(new SpaceLeftMarginItemDecoration(Common.dpToPx(this, 6)));
        followedAdapter = new UserFollowedAdapter();
        followedRecyclerView.setAdapter(followedAdapter);

        newTweetEditText = (EditTextCursorWatcher) findViewById(R.id.newTweetEditText);
        newTweetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.length() > 0) {
                    if (text.charAt(start - 1 + count) == '@') {
                        charsAfterAt = 0;
                        lastAtIndex = start - 1 + count;
                        showSuggestions();
                    } else if (Character.isSpaceChar(text.charAt(start - 1 + count))) {
                        lastAtIndex = -1;
                        charsAfterAt = 0;
                        hideSuggestions();
                    } else if (suggestionsOn) {
                        charsAfterAt += (count == 0) ? -1 : 1;
                        String prefix = text.subSequence(lastAtIndex + 1, lastAtIndex + charsAfterAt + 1).toString().toLowerCase();
                        subset.clear();
                        for (int i = 0; i < followers.size(); i++)
                            if (followers.get(i).screenName.toLowerCase().startsWith(prefix))
                                subset.add(followers.get(i));
                        followedAdapter.notifyDataSetChanged();
                    }
                } else if (suggestionsOn) {
                    hideSuggestions();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkLength(s.toString());
            }
        });

        newTweetEditText.addCursorWatcher(new EditTextCursorWatcher.CursorWatcher() {
            @Override
            public void onCursorPositionChanged(int currentStartPosition, int currentEndPosition) {
                hideSuggestions();
            }
        });

        if (currentStatus.getMediaEntities().length > 0) {
            ImageView photoImageView = (ImageView) findViewById(R.id.photoImageView);
            photoImageView.setVisibility(View.VISIBLE);

            Glide.with(NewTweetQuoteActivity.this)
                    .load(currentStatus.getMediaEntities()[0].getMediaURL())
                    .placeholder(R.drawable.placeholder)
                    .into(photoImageView);
        }
    }

    private void showSuggestions() {
        suggestionsOn = true;
        followedRecyclerView.setVisibility(View.VISIBLE);
        if (followers.size() == 0)
            followers.addAll(DatabaseManager.getInstance(NewTweetQuoteActivity.this).getFollowed());
        subset.clear();
        subset.addAll(followers);
        followedAdapter.notifyDataSetChanged();
    }

    private void hideSuggestions() {
        suggestionsOn = false;
        subset.clear();
        followedAdapter.notifyDataSetChanged();
        followedRecyclerView.setVisibility(View.GONE);
    }

    private void checkLength(String text) {
        int wordsLength = 0;
        int urls = 1;
        for (String entry : text.split(" ")) {
            if (Patterns.WEB_URL.matcher(entry).matches() && entry.length() > MAX_URL_LENGTH)
                urls++;
            else
                wordsLength += entry.length();
        }

        int spaces = text.length() - text.replace(" ", "").length();
        charsLeft = (140 - urls * MAX_URL_LENGTH) - spaces - wordsLength;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_new_tweet, menu);

        MenuItem item = menu.findItem(R.id.action_chars_left);
        MenuItemCompat.setActionView(item, R.layout.menu_chars_left);
        View view = MenuItemCompat.getActionView(item);
        TextView charsLeftTextView = (TextView) view.findViewById(R.id.charsLeftTextView);
        charsLeftTextView.setText(String.valueOf(charsLeft));

        if (charsLeft < 0)
            charsLeftTextView.setTextColor(ContextCompat.getColor(NewTweetQuoteActivity.this, R.color.red));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            if (charsLeft < 0) {
                (new AlertDialog.Builder(NewTweetQuoteActivity.this)).setTitle(R.string.too_many_characters)
                        .setPositiveButton(R.string.ok, null).create().show();
            } else {
                new UpdateTwitterStatus(NewTweetQuoteActivity.this, twitter, currentStatus.getUser().getId())
                        .execute(newTweetEditText.getText().toString() +
                                " https://twitter.com/" + currentStatus.getUser().getScreenName() +
                                "/status/" + currentStatus.getId());
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class UserFollowedAdapter extends RecyclerView.Adapter<UserSuggestedViewHolder> {

        @Override
        public UserSuggestedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_simple_card, parent, false);

            return new UserSuggestedViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UserSuggestedViewHolder holder, int position) {
            final UserFollowed user = subset.get(position);

            Glide.with(NewTweetQuoteActivity.this)
                    .load(user.profilePicUrl)
                    .placeholder(R.drawable.placeholder_circular)
                    .transform(new CircleTransform(NewTweetQuoteActivity.this))
                    .into(holder.userProfilePicImageView);

            holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(NewTweetQuoteActivity.this, UserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserActivity.TAG_ID, user.id);
                    i.putExtras(bundle);
                    NewTweetQuoteActivity.this.startActivity(i);
                }
            });

            holder.userNameTextView.setText(user.name);
            holder.screenNameTextView.setText("@" + user.screenName);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    String text = newTweetEditText.getText().toString();
                    int selectionIndex = lastAtIndex + user.screenName.length() + 1;

                    newTweetEditText.setText(text.substring(0, lastAtIndex + 1) + user.screenName +
                            text.substring(lastAtIndex + charsAfterAt + 1, text.length()));
                    newTweetEditText.setSelection(selectionIndex);
                    followedRecyclerView.setVisibility(View.GONE);
                    subset.clear();
                }
            });
        }

        @Override
        public int getItemCount() {
            return subset.size();
        }
    }
}
