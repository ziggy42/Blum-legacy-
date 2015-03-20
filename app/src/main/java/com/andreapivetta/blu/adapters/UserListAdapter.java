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
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private ArrayList<User> mDataSet;
    private Context context;
    private Twitter twitter;

    public UserListAdapter(ArrayList<User> mDataSet, Context context, Twitter twitter) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.twitter = twitter;
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final User user = mDataSet.get(position);

        Picasso.with(context)
                .load(user.getOriginalProfileImageURL())
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.userProfilePicImageView);

        holder.userNameTextView.setText(user.getName());
        holder.screenNameTextView.setText("@" + user.getScreenName());
        holder.descriptionTextView.setText(user.getDescription());
        holder.containerRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                i.putExtra("ID", user.getId())
                        .putExtra("Twitter", twitter);
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
        public TextView userNameTextView, screenNameTextView, descriptionTextView;
        public RelativeLayout containerRelativeLayout;

        public ViewHolder(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.descriptionTextView = (TextView) container.findViewById(R.id.descriptionTextView);
            this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
            this.containerRelativeLayout = (RelativeLayout) container.findViewById(R.id.containerRelativeLayout);
        }
    }
}
