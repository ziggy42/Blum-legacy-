package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitter4j.User;

public class UserListMessageAdapter extends RecyclerView.Adapter<UserListMessageAdapter.ViewHolder> {

    private ArrayList<User> mDataSet;
    private Context context;

    public UserListMessageAdapter(ArrayList<User> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
    }

    @Override
    public UserListMessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_simple, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserListMessageAdapter.ViewHolder holder, int position) {
        final User user = mDataSet.get(position);

        Picasso.with(context)
                .load(user.getOriginalProfileImageURL())
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.userProfilePicImageView);

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable(UserProfileActivity.TAG_USER, user);
                i.putExtras(bundle);

                context.startActivity(i);
            }
        });

        holder.userNameTextView.setText(user.getName());
        holder.screenNameTextView.setText("@" + user.getScreenName());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ConversationActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable(UserProfileActivity.TAG_USER, user);
                i.putExtras(bundle);

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
        public FrameLayout container;

        public ViewHolder(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
            this.container = (FrameLayout) container.findViewById(R.id.containerRelativeLayout);
        }
    }
}
