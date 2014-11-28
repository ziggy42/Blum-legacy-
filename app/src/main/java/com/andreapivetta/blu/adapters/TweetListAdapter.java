package com.andreapivetta.blu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import twitter4j.Status;


public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder> {

    private ArrayList<Status> mDataSet;
    private Context context;

    public TweetListAdapter(ArrayList<Status> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userNameTextView, statusTextView, timeTextView;
        public ImageView userProfilePicImageView;

        public ViewHolder(View container) {
            super(container);

            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
        }
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
        if (currentStatus.isRetweet()) {
            // TODO show that this is a retweet
            currentStatus = currentStatus.getRetweetedStatus();
        }

        holder.userNameTextView.setText(currentStatus.getUser().getName());
        holder.statusTextView.setText(currentStatus.getText());
        holder.timeTextView.setText(new SimpleDateFormat("hh:mm").format(currentStatus.getCreatedAt()));

        Picasso.with(context)
                .load(mDataSet.get(position).getUser().getBiggerProfileImageURL())
                .into(holder.userProfilePicImageView);

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
