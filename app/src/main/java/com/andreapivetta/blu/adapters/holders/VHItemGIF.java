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

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;

public class VHItemGIF extends VHItem {

    private ImageView tweetGIFImageView;
    private ImageButton playGIFImageButton;

    public VHItemGIF(View container) {
        super(container);

        this.tweetGIFImageView = (ImageView) container.findViewById(R.id.tweetGIFImageView);
        this.playGIFImageButton = (ImageButton) container.findViewById(R.id.playGIFImageButton);
    }

    @Override
    public void setup(final Status status, final Context context, ArrayList<Long> favorites, ArrayList<Long> retweets, Twitter twitter) {
        super.setup(status, context, favorites, retweets, twitter);

        final MediaEntity mediaEntity = status.getMediaEntities()[0];

        Glide.with(context)
                .load(mediaEntity.getMediaURL())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(tweetGIFImageView);

        playGIFImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, VideoActivity.class);
                i.putExtra(VideoActivity.TAG_VIDEO, status.getExtendedMediaEntities()[0].getVideoVariants()[0].getUrl())
                        .putExtra(VideoActivity.TAG_IS_GIF, true);
                context.startActivity(i);
            }
        });
    }


}
