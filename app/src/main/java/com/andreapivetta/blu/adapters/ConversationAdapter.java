package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.Message;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private static final int TYPE_ME = 0;
    private static final int TYPE_OTHER = 1;

    private ArrayList<Message> mDataSet;
    private long loggedUserID;

    public ConversationAdapter(ArrayList<Message> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.loggedUserID = context.getSharedPreferences(Common.PREF, 0).getLong(Common.PREF_LOGGED_USER, 0L);
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
        holder.messageTextView.setText(mDataSet.get(position).getMessageText());
        Linkify.addLinks(holder.messageTextView, Linkify.ALL);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView messageTextView;

        public ViewHolder(View container) {
            super(container);

            this.messageTextView = (TextView) container.findViewById(R.id.messageTextView);
        }
    }
}
