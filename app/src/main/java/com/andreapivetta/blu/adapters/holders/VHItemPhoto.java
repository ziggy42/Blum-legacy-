package com.andreapivetta.blu.adapters.holders;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;

public class VHItemPhoto extends VHItem {

    private ImageView tweetPhotoImageView;

    public VHItemPhoto(View container) {
        super(container);

        this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
    }

    @Override
    public void setup(Status status, final Context context, ArrayList<Long> favorites,
                      ArrayList<Long> retweets, Twitter twitter) {
        super.setup(status, context, favorites, retweets, twitter);

        final MediaEntity mediaEntity = status.getMediaEntities()[0];
        if (mediaEntity.getType().equals("photo")) {
            Glide.with(context)
                    .load(mediaEntity.getMediaURL())
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .into(tweetPhotoImageView);

            tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ImageActivity.class);
                    i.putExtra(ImageActivity.TAG_IMAGES, new String[]{mediaEntity.getMediaURL()});
                    context.startActivity(i);
                }
            });
        }
    }
}
