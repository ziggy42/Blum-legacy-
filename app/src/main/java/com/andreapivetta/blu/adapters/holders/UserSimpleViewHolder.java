package com.andreapivetta.blu.adapters.holders;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;

public class UserSimpleViewHolder extends RecyclerView.ViewHolder {

    public ImageView userProfilePicImageView;
    public TextView userNameTextView, screenNameTextView;
    public RelativeLayout container;

    public UserSimpleViewHolder(View container) {
        super(container);

        this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
        this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
        this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
        this.container = (RelativeLayout) container.findViewById(R.id.containerRelativeLayout);
    }
}
