package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.UserActivity;
import com.andreapivetta.blu.data.Notification;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitter4j.Twitter;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private ArrayList<Notification> mDataSet;
    private Context context;
    private Twitter twitter;

    public NotificationAdapter(ArrayList<Notification> mDataSet, Context context, Twitter twitter) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.twitter = twitter;
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Notification notification = mDataSet.get(position);

        Picasso.with(context)
                .load(notification.profilePicURL)
                .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                .into(holder.userProfilePicImageView);

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserActivity.class);
                i.putExtra("ID", notification.userID)
                        .putExtra("Twitter", twitter);
                context.startActivity(i);
            }
        });

        String screenName = notification.user;
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

        if(notification.type.equals(Notification.TYPE_FAVOURITE)) {
            SpannableStringBuilder sb = new SpannableStringBuilder(screenName + " " + context.getString(R.string.favourite_not));
            sb.setSpan(b, 0, screenName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.notificationExplanationTextView.setText(sb);

            holder.statusTextView.setText(notification.status);
        } else if(notification.type.equals(Notification.TYPE_FOLLOW)) {

        } else if(notification.type.equals(Notification.TYPE_MENTION)) {

        } else if(notification.type.equals(Notification.TYPE_RETWEET)) {

        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userProfilePicImageView;
        public TextView notificationExplanationTextView, statusTextView, timeTextView;

        public ViewHolder(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.notificationExplanationTextView = (TextView) container.findViewById(R.id.notificationExplanationTextView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
        }
    }
}
