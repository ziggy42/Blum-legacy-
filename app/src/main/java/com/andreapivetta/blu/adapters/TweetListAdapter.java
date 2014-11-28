package com.andreapivetta.blu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;


public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder> {

    private ArrayList<Status> mDataSet;
    private Context context;

    public TweetListAdapter(ArrayList<Status> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
    }

    @Override
    public TweetListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tweet, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Status currentStatus = mDataSet.get(position);
        MediaEntity mediaEntityArray[];

        if (currentStatus.isRetweet()) {
            holder.retweetTextView.setVisibility(View.VISIBLE);
            holder.retweetTextView.setText(context.getString(R.string.retweeted_by) + " @" + currentStatus.getUser().getScreenName());
            currentStatus = currentStatus.getRetweetedStatus();

            mediaEntityArray = currentStatus.getMediaEntities();

        } else {
            holder.retweetTextView.setVisibility(View.GONE);
            mediaEntityArray = currentStatus.getMediaEntities();
        }

        holder.userNameTextView.setText(currentStatus.getUser().getName());
        holder.statusTextView.setText(currentStatus.getText());
        Linkify.addLinks(holder.statusTextView, Linkify.ALL);

        holder.timeTextView.setText(new SimpleDateFormat("hh:mm").format(currentStatus.getCreatedAt()));

        Picasso.with(context)
                .load(currentStatus.getUser().getBiggerProfileImageURL())
                .into(holder.userProfilePicImageView);

        if (mediaEntityArray.length > 0) {
            for (MediaEntity mediaEntity : mediaEntityArray) {
                if (mediaEntity.getType().equals("photo")) {
                    holder.tweetPhotoImageView.setVisibility(View.VISIBLE);
                    Picasso.with(context)
                            .load(mediaEntity.getMediaURL())
                            .into(holder.tweetPhotoImageView);
                    break;
                }
            }
        } else {
            holder.tweetPhotoImageView.setVisibility(View.GONE);
        }



    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userNameTextView, statusTextView, timeTextView, retweetTextView;
        public ImageView userProfilePicImageView, tweetPhotoImageView;

        public ViewHolder(View container) {
            super(container);

            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.retweetTextView = (TextView) container.findViewById(R.id.retweetTextView);
            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
        }
    }
}
