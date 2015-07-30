package com.andreapivetta.blu.adapters.holders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;

public class VHItemQuote extends VHItem {

    private TextView quotedUserNameTextView, quotedStatusTextView;
    private ImageView photoImageView;
    private LinearLayout quotedStatusLinearLayout;

    public VHItemQuote(View container) {
        super(container);

        this.quotedUserNameTextView = (TextView) container.findViewById(R.id.quotedUserNameTextView);
        this.quotedStatusTextView = (TextView) container.findViewById(R.id.quotedStatusTextView);
        this.photoImageView = (ImageView) container.findViewById(R.id.photoImageView);
        this.quotedStatusLinearLayout = (LinearLayout) container.findViewById(R.id.quotedStatus);
    }

    @Override
    public void setup(Status status, final Context context, ArrayList<Long> favorites,
                      ArrayList<Long> retweets, Twitter twitter) {
        super.setup(status, context, favorites, retweets, twitter);

        final Status quotedStatus = status.getQuotedStatus();

        quotedUserNameTextView.setText(quotedStatus.getUser().getName());
        quotedStatusTextView.setText(quotedStatus.getText());

        if (quotedStatus.getMediaEntities().length > 0) {
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(quotedStatus.getMediaEntities()[0].getMediaURL())
                    .placeholder(R.drawable.placeholder)
                    .into(photoImageView);
        } else
            photoImageView.setVisibility(View.GONE);

        quotedStatusLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TweetActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(TweetActivity.TAG_TWEET, quotedStatus);
                i.putExtra(TweetActivity.TAG_STATUS_BUNDLE, b);
                context.startActivity(i);
            }
        });
    }
}
