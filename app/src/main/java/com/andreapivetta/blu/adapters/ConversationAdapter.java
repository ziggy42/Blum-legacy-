package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private static final int TYPE_ME = 0;
    private static final int TYPE_OTHER = 1;

    private ArrayList<Message> mDataSet;
    private long loggedUserID;

    private Context context;

    public ConversationAdapter(ArrayList<Message> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.loggedUserID = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_key_logged_user), 0L);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder((viewType == TYPE_ME) ?
                LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right, parent, false) :
                LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return (mDataSet.get(position).getSenderID() == loggedUserID) ? TYPE_ME : TYPE_OTHER;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message currentMessage = mDataSet.get(position);

        holder.messageTextView.setText(currentMessage.getMessageText());
        Linkify.addLinks(holder.messageTextView, Linkify.ALL);

        Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
        c2.setTimeInMillis(currentMessage.getTimeStamp());

        long diff = c.getTimeInMillis() - c2.getTimeInMillis();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds > 60) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > 60) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        holder.timeTextView.setText(
                                (new java.text.SimpleDateFormat("MMM dd", Locale.getDefault())).format(c2.getTime()));
                    else
                        holder.timeTextView.setText(
                                (new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault())).format(c2.getTime()));
                } else
                    holder.timeTextView.setText(context.getString(R.string.mini_hours, (int) hours));
            } else
                holder.timeTextView.setText(context.getString(R.string.mini_minutes, (int) minutes));
        } else holder.timeTextView.setText(context.getString(R.string.mini_seconds, (int) seconds));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView messageTextView, timeTextView;

        public ViewHolder(View container) {
            super(container);

            this.messageTextView = (TextView) container.findViewById(R.id.messageTextView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
        }
    }
}
