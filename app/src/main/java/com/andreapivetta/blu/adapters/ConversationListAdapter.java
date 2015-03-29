package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ConversationActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.utilities.Common;
import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Message> mDataSet;
    private long loggedUserID;

    public ConversationListAdapter(Context context, ArrayList<Message> mDataSet) {
        this.context = context;
        this.mDataSet = mDataSet;
        this.loggedUserID = context.getSharedPreferences(Common.PREF, 0).getLong(Common.PREF_LOGGED_USER, 0L);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversation, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Message current = mDataSet.get(position);

        Picasso.with(context)
                .load(current.getOtherUserProfilePicUrl())
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.userProfilePicImageView);

        if (!current.isRead())
            holder.messageTextView.setTypeface(null, Typeface.BOLD_ITALIC);
        else
            holder.messageTextView.setTypeface(null, Typeface.NORMAL);


        holder.userNameTextView.setText(current.getOtherUserName());
        holder.messageTextView.setText((loggedUserID == current.getSenderID()) ?
                context.getString(R.string.you_message, current.getMessageText()) : current.getMessageText());

        Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
        c2.setTimeInMillis(current.getTimeStamp());

        long diff = c.getTimeInMillis() - c2.getTimeInMillis();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds > 60) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > 60) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        holder.timeTextView.setText((new java.text.SimpleDateFormat("MMM dd", Locale.getDefault()))
                                .format(c2.getTimeInMillis()));
                    else
                        holder.timeTextView.setText(
                                (new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault()))
                                        .format(c2.getTimeInMillis()));
                } else
                    holder.timeTextView.setText(context.getString(R.string.mini_hours, (int) hours));
            } else
                holder.timeTextView.setText(context.getString(R.string.mini_minutes, (int) minutes));
        } else holder.timeTextView.setText(context.getString(R.string.mini_seconds, (int) seconds));

        final long otherUserID = (loggedUserID == current.getSenderID()) ?
                current.getRecipientID() : current.getSenderID();

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                i.putExtra("ID", otherUserID);
                context.startActivity(i);
            }
        });

        holder.conversationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ConversationActivity.class);
                i.putExtra("ID", otherUserID);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userProfilePicImageView;
        public TextView userNameTextView, messageTextView, timeTextView;
        public FrameLayout conversationContainer;

        public ViewHolder(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.messageTextView = (TextView) container.findViewById(R.id.messageTextView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.conversationContainer = (FrameLayout) container.findViewById(R.id.conversationContainer);
        }
    }
}
