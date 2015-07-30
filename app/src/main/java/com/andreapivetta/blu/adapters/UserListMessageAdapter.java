package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ConversationActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.adapters.holders.UserSimpleViewHolder;
import com.andreapivetta.blu.utilities.CircleTransform;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.User;

public class UserListMessageAdapter extends RecyclerView.Adapter<UserSimpleViewHolder> {

    private ArrayList<User> mDataSet;
    private Context context;

    public UserListMessageAdapter(ArrayList<User> mDataSet, Context context) {
        this.mDataSet = mDataSet;
        this.context = context;
    }

    @Override
    public UserSimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_simple, parent, false);

        return new UserSimpleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserSimpleViewHolder holder, int position) {
        final User user = mDataSet.get(position);

        Glide.with(context)
                .load(user.getProfileImageURL())
                .placeholder(R.drawable.placeholder_circular)
                .transform(new CircleTransform(context))
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

}
