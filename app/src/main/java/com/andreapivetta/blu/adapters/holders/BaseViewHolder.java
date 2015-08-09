package com.andreapivetta.blu.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    protected LinearLayout interactionLinearLayout;
    protected TextView userNameTextView, statusTextView, timeTextView, retweetTextView;
    protected ImageView userProfilePicImageView;
    protected ImageButton favouriteImageButton, retweetImageButton, respondImageButton,
            shareImageButton, quoteImageButton;

    public BaseViewHolder(View container) {
        super(container);

        this.interactionLinearLayout = (LinearLayout) container.findViewById(R.id.interactionLinearLayout);
        this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
        this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
        this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
        this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
        this.retweetTextView = (TextView) container.findViewById(R.id.retweetTextView);
        this.favouriteImageButton = (ImageButton) container.findViewById(R.id.favouriteImageButton);
        this.retweetImageButton = (ImageButton) container.findViewById(R.id.retweetImageButton);
        this.respondImageButton = (ImageButton) container.findViewById(R.id.respondImageButton);
        this.shareImageButton = (ImageButton) container.findViewById(R.id.shareImageButton);
        this.quoteImageButton = (ImageButton) container.findViewById(R.id.quoteImageButton);
    }

    public abstract void setup(Status status, final Context context, final ArrayList<Long> favorites,
                               ArrayList<Long> retweets, Twitter twitter);

}
