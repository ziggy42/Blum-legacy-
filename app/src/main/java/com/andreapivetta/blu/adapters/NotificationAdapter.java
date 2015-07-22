package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.data.Notification;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private ArrayList<Notification> mDataSet;
    private Context context;
    private StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

    public NotificationAdapter(ArrayList<Notification> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
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
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.userProfilePicImageView);

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                i.putExtra(UserProfileActivity.TAG_ID, notification.userID);
                context.startActivity(i);
            }
        });

        Calendar c = Calendar.getInstance(), current = Calendar.getInstance();
        current.setTimeInMillis(notification.timestamp);
        if (c.get(Calendar.YEAR) == current.get(Calendar.YEAR) && c.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                && c.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)) {

            holder.timeTextView.setText(
                    android.text.format.DateFormat.getTimeFormat(context).format(current.getTime()));
        } else {
            holder.timeTextView.setText(
                    android.text.format.DateFormat.getDateFormat(context).format(current.getTime()));
        }

        String screenName = notification.user;

        if (notification.type.equals(Notification.TYPE_FAVOURITE)
                || notification.type.equals(Notification.TYPE_RETWEET)
                || notification.type.equals(Notification.TYPE_MENTION)
                || notification.type.equals(Notification.TYPE_RETWEET_MENTIONED)) {
            SpannableStringBuilder sb;
            switch (notification.type) {
                case Notification.TYPE_FAVOURITE:
                    sb = new SpannableStringBuilder(context.getString(R.string.fav_not_title, screenName));
                    break;
                case Notification.TYPE_RETWEET:
                    sb = new SpannableStringBuilder(context.getString(R.string.retw_not_title, screenName));
                    break;
                case Notification.TYPE_MENTION:
                    sb = new SpannableStringBuilder(context.getString(R.string.mentioned_not, screenName));
                    break;
                default:
                    sb = new SpannableStringBuilder(context.getString(R.string.retw_ment_not_title, screenName));
                    break;
            }

            sb.setSpan(b, 0, screenName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.notificationExplainedTextView.setText(sb);

            holder.statusTextView.setText(notification.status);

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, TweetActivity.class);
                    i.putExtra("STATUS_ID", notification.tweetID);
                    context.startActivity(i);
                }
            });
        } else if (notification.type.equals(Notification.TYPE_FOLLOW)) {
            SpannableStringBuilder sb = new SpannableStringBuilder(screenName);
            sb.setSpan(b, 0, screenName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.notificationExplainedTextView.setText(sb);
            holder.statusTextView.setText(screenName + " " +
                    context.getString(R.string.is_following_not));

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, UserProfileActivity.class);
                    i.putExtra(UserProfileActivity.TAG_ID, notification.userID);
                    context.startActivity(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userProfilePicImageView;
        public TextView notificationExplainedTextView, statusTextView, timeTextView;
        public FrameLayout cardView;

        public ViewHolder(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.notificationExplainedTextView = (TextView) container.findViewById(R.id.notificationExplanationTextView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.cardView = (FrameLayout) container.findViewById(R.id.card_view);
        }
    }
}
