package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.squareup.picasso.Picasso;

import twitter4j.Status;
import twitter4j.Twitter;

public class NewTweetQuoteActivity extends ThemedActivity {

    public static final String PAR_CURRENT_STATUS = "currentStatus";
    public static final String PAR_BUNDLE = "bundle";
    private static final int MAX_URL_LENGTH = 23;
    private Twitter twitter;

    private EditText newTweetEditText;

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

        newTweetEditText = (EditText) findViewById(R.id.newTweetEditText);
        newTweetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkLength(s.toString());
            }
        });

        if (currentStatus.getMediaEntities().length > 0) {
            ImageView photoImageView = (ImageView) findViewById(R.id.photoImageView);
            photoImageView.setVisibility(View.VISIBLE);

            Picasso.with(NewTweetQuoteActivity.this)
                    .load(currentStatus.getMediaEntities()[0].getMediaURL())
                    .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.placeholder, null))
                    .into(photoImageView);
        }
    }

    void checkLength(String text) {
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
            charsLeftTextView.setTextColor(getResources().getColor(R.color.red));

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
}
