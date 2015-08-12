package com.andreapivetta.blu.adapters.holders;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.VideoActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.ExtendedMediaEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;

public class VHItemVideo extends VHItem {

    private ImageView tweetVideoImageView;
    private ImageButton playVideoImageButton;

    public VHItemVideo(View container) {
        super(container);

        this.tweetVideoImageView = (ImageView) container.findViewById(R.id.tweetVideoImageView);
        this.playVideoImageButton = (ImageButton) container.findViewById(R.id.playVideoImageButton);
    }

    @Override
    public void setup(final Status status, final Context context, ArrayList<Long> favorites, ArrayList<Long> retweets, Twitter twitter) {
        super.setup(status, context, favorites, retweets, twitter);

        final ExtendedMediaEntity mediaEntity = status.getExtendedMediaEntities()[0];

        Glide.with(context)
                .load(mediaEntity.getMediaURL())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(tweetVideoImageView);

        playVideoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, VideoActivity.class);
                i.putExtra(VideoActivity.TAG_VIDEO, mediaEntity.getVideoVariants()[0].getUrl())
                        .putExtra(VideoActivity.TAG_TYPE, mediaEntity.getType());
                context.startActivity(i);
            }
        });
    }


}
