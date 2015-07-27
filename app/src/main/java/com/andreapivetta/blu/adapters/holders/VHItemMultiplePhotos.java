package com.andreapivetta.blu.adapters.holders;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ImagesAdapter;
import com.andreapivetta.blu.adapters.decorators.SpaceLeftItemDecoration;

import java.util.ArrayList;

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

        tweetPhotosRecyclerView.setAdapter(new ImagesAdapter(status.getExtendedMediaEntities(), context));
    }
}
