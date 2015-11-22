package com.andreapivetta.blu.adapters.holders;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.View;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ImagesAdapter;
import com.andreapivetta.blu.adapters.decorators.SpaceLeftItemDecoration;

import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;

public class VHItemMultiplePhotos extends VHItem {

    private RecyclerView tweetPhotosRecyclerView;

    public VHItemMultiplePhotos(View container) {
        super(container);

        this.tweetPhotosRecyclerView = (RecyclerView) container.findViewById(R.id.tweetPhotosRecyclerView);
        this.tweetPhotosRecyclerView.addItemDecoration(new SpaceLeftItemDecoration(5));
        this.tweetPhotosRecyclerView.setLayoutManager(
                new LinearLayoutManager(container.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.tweetPhotosRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void setup(Status status, Context context, ArrayList<Long> favorites,
                      ArrayList<Long> retweets, Twitter twitter) {
        super.setup(status, context, favorites, retweets, twitter);

        MediaEntity[] entities = status.getExtendedMediaEntities();
        String text = status.getText();
        for (int i = 0; i < entities.length; i++)
            text = text.replace(entities[i].getURL(), "");
        statusTextView.setText(text);
        Linkify.addLinks(statusTextView, Linkify.ALL);

        tweetPhotosRecyclerView.setAdapter(new ImagesAdapter(entities, context));
    }
}
