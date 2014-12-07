package com.andreapivetta.blu.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserActivity;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;


public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder> {

    private ArrayList<Status> mDataSet;
    private Context context;
    private Twitter twitter;

    public TweetListAdapter(ArrayList<Status> mDataSet, Context context, Twitter twitter) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.twitter = twitter;
    }

    @Override
    public TweetListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tweet, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Status currentStatus;
        MediaEntity mediaEntityArray[];
        holder.interactionLinearLayout.setVisibility(View.GONE);

        if (mDataSet.get(position).isRetweet()) {
            currentStatus = mDataSet.get(position).getRetweetedStatus();
            holder.retweetTextView.setVisibility(View.VISIBLE);
            holder.retweetTextView.setText(context.getString(R.string.retweeted_by) + " @" + mDataSet.get(position).getUser().getScreenName());
        } else {
            currentStatus = mDataSet.get(position);
            holder.retweetTextView.setVisibility(View.GONE);
        }

        mediaEntityArray = currentStatus.getMediaEntities();

        holder.userNameTextView.setText(currentStatus.getUser().getName());
        holder.statusTextView.setText(currentStatus.getText());
        Linkify.addLinks(holder.statusTextView, Linkify.ALL);

        holder.timeTextView.setText(new SimpleDateFormat("hh:mm").format(currentStatus.getCreatedAt()));

        Picasso.with(context)
                .load(currentStatus.getUser().getBiggerProfileImageURL())
                .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                .into(holder.userProfilePicImageView);

        if (mediaEntityArray.length > 0) {
            for (MediaEntity mediaEntity : mediaEntityArray) {
                if (mediaEntity.getType().equals("photo")) {
                    holder.tweetPhotoImageView.setVisibility(View.VISIBLE);
                    Picasso.with(context)
                            .load(mediaEntity.getMediaURL())
                            .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                            .into(holder.tweetPhotoImageView);
                    break;
                }
            }
        } else {
            holder.tweetPhotoImageView.setVisibility(View.GONE);
        }

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserActivity.class);
                i.putExtra("ID", currentStatus.getUser().getId())
                        .putExtra("Twitter", twitter);
                context.startActivity(i);
            }
        });

        holder.statusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.interactionLinearLayout.getVisibility() == View.VISIBLE)
                    holder.interactionLinearLayout.setVisibility(View.GONE);
                else
                    holder.interactionLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.interactionLinearLayout.getVisibility() == View.VISIBLE)
                    holder.interactionLinearLayout.setVisibility(View.GONE);
                else
                    holder.interactionLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        holder.favouriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long type;
                if (currentStatus.isFavorited()) {
                    type = -1;
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), type);
                } else {
                    type = 1;
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), type);
                }
            }
        });

        holder.retweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.retweet_title))
                        .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new RetweetTweet(context, twitter).execute(currentStatus.getId());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).create().show();
            }
        });

        holder.respondImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://twitter.com/" + currentStatus.getUser().getScreenName()
                        + "/status/" + currentStatus.getId();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, url)
                        .setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_tweet)));
            }
        });

        holder.openTweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TweetActivity.class);
                i.putExtra("STATUS", currentStatus.getId());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userNameTextView, statusTextView, timeTextView, retweetTextView;
        public ImageView userProfilePicImageView, tweetPhotoImageView;
        public LinearLayout interactionLinearLayout;
        public FrameLayout cardView;
        public ImageButton favouriteImageButton, retweetImageButton, respondImageButton, shareImageButton, openTweetImageButton;

        public ViewHolder(View container) {
            super(container);

            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.retweetTextView = (TextView) container.findViewById(R.id.retweetTextView);
            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);

            this.interactionLinearLayout = (LinearLayout) container.findViewById(R.id.interactionLinearLayout);
            this.cardView = (FrameLayout) container.findViewById(R.id.card_view);

            this.favouriteImageButton = (ImageButton) container.findViewById(R.id.favouriteImageButton);
            this.retweetImageButton = (ImageButton) container.findViewById(R.id.retweetImageButton);
            this.respondImageButton = (ImageButton) container.findViewById(R.id.respondImageButton);
            this.shareImageButton = (ImageButton) container.findViewById(R.id.shareImageButton);
            this.openTweetImageButton = (ImageButton) container.findViewById(R.id.openTweetImageButton);
        }
    }
}
