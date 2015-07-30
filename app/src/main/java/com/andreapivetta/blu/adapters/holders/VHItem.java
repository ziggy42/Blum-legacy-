package com.andreapivetta.blu.adapters.holders;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.util.Linkify;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.NewTweetActivity;
import com.andreapivetta.blu.activities.NewTweetQuoteActivity;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

public class VHItem extends ViewHolder {

    protected FrameLayout cardView;
    protected ImageButton openTweetImageButton;

    public VHItem(View container) {
        super(container);

        this.cardView = (FrameLayout) container.findViewById(R.id.card_view);
        this.openTweetImageButton = (ImageButton) container.findViewById(R.id.openTweetImageButton);
    }

    @Override
    public void setup(Status status, final Context context, final ArrayList<Long> favorites,
                      final ArrayList<Long> retweets, final Twitter twitter) {
        final Status currentStatus;

        if (status.isRetweet()) {
            currentStatus = status.getRetweetedStatus();
            retweetTextView.setVisibility(View.VISIBLE);
            retweetTextView.setText(
                    context.getString(R.string.retweeted_by, status.getUser().getScreenName()));
        } else {
            currentStatus = status;
            retweetTextView.setVisibility(View.GONE);
        }

        final User currentUser = currentStatus.getUser();

        userNameTextView.setText(currentUser.getName());

        Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
        c2.setTime(currentStatus.getCreatedAt());

        long diff = c.getTimeInMillis() - c2.getTimeInMillis();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds > 60) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > 60) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        timeTextView.setText((new java.text.SimpleDateFormat("MMM dd", Locale.getDefault())).format(currentStatus.getCreatedAt()));
                    else
                        timeTextView.setText((new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault())).format(currentStatus.getCreatedAt()));
                } else
                    timeTextView.setText(context.getString(R.string.mini_hours, (int) hours));
            } else
                timeTextView.setText(context.getString(R.string.mini_minutes, (int) minutes));
        } else timeTextView.setText(context.getString(R.string.mini_seconds, (int) seconds));

        Glide.with(context)
                .load(currentUser.getBiggerProfileImageURL())
                .placeholder(R.drawable.placeholder)
                .into(userProfilePicImageView);

        if (currentStatus.isFavorited() || favorites.contains(currentStatus.getId()))
            favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
        else
            favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);

        if (currentStatus.isRetweeted() || retweets.contains(currentStatus.getId()))
            retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
        else
            retweetImageButton.setImageResource(R.drawable.ic_repeat_grey600_36dp);

        userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(UserProfileActivity.TAG_USER, currentUser);
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });

        favouriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStatus.isFavorited() || favorites.contains(currentStatus.getId())) {
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), -1L);
                    favorites.remove(currentStatus.getId());
                    favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);
                } else {
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), 1L);
                    favorites.add(currentStatus.getId());
                    favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
                }
            }
        });

        retweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.retweet_title))
                        .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new RetweetTweet(context, twitter).execute(currentStatus.getId());
                                retweets.add(currentStatus.getId());
                                retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).create().show();
            }
        });

        quoteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetQuoteActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(NewTweetQuoteActivity.PAR_CURRENT_STATUS, currentStatus);
                i.putExtra(NewTweetQuoteActivity.PAR_BUNDLE, b);
                context.startActivity(i);
            }
        });

        respondImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + currentUser.getScreenName())
                        .putExtra(NewTweetActivity.TAG_REPLY_ID, currentStatus.getId());
                context.startActivity(i);
            }
        });

        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" +
                                currentUser.getScreenName() + "/status/" + currentStatus.getId())
                        .setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_tweet)));
            }
        });

        statusTextView.setText(currentStatus.getText());
        interactionLinearLayout.setVisibility(View.GONE);
        Linkify.addLinks(statusTextView, Linkify.ALL);

        statusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionLinearLayout.setVisibility(
                        (interactionLinearLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionLinearLayout.setVisibility(
                        (interactionLinearLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
            }
        });

        openTweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TweetActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(TweetActivity.TAG_TWEET, currentStatus);
                i.putExtra(TweetActivity.TAG_STATUS_BUNDLE, b);
                context.startActivity(i);
            }
        });
    }
}
