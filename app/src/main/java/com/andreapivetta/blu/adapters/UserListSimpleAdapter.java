package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ConversationActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitter4j.User;

public class UserListSimpleAdapter extends RecyclerView.Adapter<UserListSimpleAdapter.ViewHolder> {

    private ArrayList<User> mDataSet;
    private Context context;

    public UserListSimpleAdapter(ArrayList<User> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
    }


    @Override
    public UserListSimpleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_simple, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserListSimpleAdapter.ViewHolder holder, int position) {
        final User user = mDataSet.get(position);

        Picasso.with(context)
                .load(user.getOriginalProfileImageURL())
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.userProfilePicImageView);

        holder.userNameTextView.setText(user.getName());
        holder.screenNameTextView.setText("@" + user.getScreenName());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ConversationActivity.class);
                i.putExtra("ID", user.getId());
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
        public TextView userNameTextView, screenNameTextView;
        public RelativeLayout container;

        public ViewHolder(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
            this.container = (RelativeLayout) container.findViewById(R.id.containerRelativeLayout);
        }
    }
}
