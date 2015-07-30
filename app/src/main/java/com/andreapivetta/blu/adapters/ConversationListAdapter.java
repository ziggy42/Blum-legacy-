package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ConversationActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.utilities.CircleTransform;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Message> mDataSet;
    private long loggedUserID;

    private java.text.SimpleDateFormat shortDateFormat = new java.text.SimpleDateFormat("MMM dd", Locale.getDefault());
    private java.text.SimpleDateFormat longDateFormat = new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault());

    public ConversationListAdapter(Context context, ArrayList<Message> mDataSet) {
        this.context = context;
        this.mDataSet = mDataSet;
        this.loggedUserID = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_key_logged_user), 0L);
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

        Glide.with(context)
                .load(current.otherUserProfilePicUrl)
                .placeholder(R.drawable.placeholder_circular)
                .transform(new CircleTransform(context))
                .into(holder.userProfilePicImageView);

        holder.messageTextView.setTypeface(null, (current.isRead) ? Typeface.NORMAL : Typeface.BOLD_ITALIC);
        holder.userNameTextView.setText(current.otherUserName);
        holder.messageTextView.setText((loggedUserID == current.senderID) ?
                context.getString(R.string.you_message, current.messageText) : current.messageText);

        Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
        c2.setTimeInMillis(current.timeStamp);

        long diff = c.getTimeInMillis() - c2.getTimeInMillis();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds > 60) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > 60) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        holder.timeTextView.setText(shortDateFormat.format(c2.getTimeInMillis()));
                    else
                        holder.timeTextView.setText(longDateFormat.format(c2.getTimeInMillis()));
                } else
                    holder.timeTextView.setText(context.getString(R.string.mini_hours, (int) hours));
            } else
                holder.timeTextView.setText(context.getString(R.string.mini_minutes, (int) minutes));
        } else holder.timeTextView.setText(context.getString(R.string.mini_seconds, (int) seconds));

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                i.putExtra(UserProfileActivity.TAG_ID, current.otherID);
                context.startActivity(i);
            }
        });

        holder.conversationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ConversationActivity.class);
                i.putExtra(ConversationActivity.TAG_ID, current.otherID);
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
