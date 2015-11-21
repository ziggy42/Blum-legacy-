package com.andreapivetta.blu.adapters.holders;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;

public class UserSuggestedViewHolder extends RecyclerView.ViewHolder {

    public ImageView userProfilePicImageView;
    public TextView userNameTextView, screenNameTextView;
    public FrameLayout container;

    public UserSuggestedViewHolder(View container) {
        super(container);

        this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
        this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
        this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
        this.container = (FrameLayout) container.findViewById(R.id.containerCardView);
    }
}
