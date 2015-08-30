package com.andreapivetta.blu.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.holders.BaseViewHolder;
import com.andreapivetta.blu.adapters.holders.UserHeaderViewHolder;
import com.andreapivetta.blu.adapters.holders.VHItem;
import com.andreapivetta.blu.adapters.holders.VHItemMultiplePhotos;
import com.andreapivetta.blu.adapters.holders.VHItemPhoto;
import com.andreapivetta.blu.adapters.holders.VHItemQuote;
import com.andreapivetta.blu.adapters.holders.VHItemVideo;

import java.util.ArrayList;

import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_ITEM_PHOTO = 2;
    private static final int TYPE_ITEM_QUOTE = 3;
    private static final int TYPE_ITEM_MULTIPLE_PHOTOS = 4;
    private static final int TYPE_ITEM_VIDEO = 5;

    private ArrayList<Object> mDataSet;
    private Context context;
    private Twitter twitter;

    private ArrayList<Long> favorites = new ArrayList<>();
    private ArrayList<Long> retweets = new ArrayList<>();

    public UserProfileAdapter(ArrayList<Object> mDataSet, Context context, Twitter twitter) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.twitter = twitter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                return new VHItem(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_basic, parent, false));
            case TYPE_ITEM_PHOTO:
                return new VHItemPhoto(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_photo, parent, false));
            case TYPE_ITEM_QUOTE:
                return new VHItemQuote(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_quote, parent, false));
            case TYPE_ITEM_MULTIPLE_PHOTOS:
                return new VHItemMultiplePhotos(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_multiplephotos, parent, false));
            case TYPE_ITEM_VIDEO:
                return new VHItemVideo(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_video, parent, false));
            case TYPE_HEADER:
                return new UserHeaderViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.user_header, parent, false));
            default:
                return new VHItem(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_basic, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0)
            ((UserHeaderViewHolder) holder).setup((User) mDataSet.get(position), context, twitter);
        else
            ((BaseViewHolder) holder).setup((Status) mDataSet.get(position), context, favorites, retweets, twitter);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0)
            return TYPE_HEADER;
        else {
            final Status status = (Status) mDataSet.get(position);
            final ExtendedMediaEntity mediaEntities[] = status.getExtendedMediaEntities();
            if (mediaEntities.length == 1) {
                if ("photo".equals(mediaEntities[0].getType()))
                    return TYPE_ITEM_PHOTO;

                return TYPE_ITEM_VIDEO;
            }

            if (mediaEntities.length > 1)
                return TYPE_ITEM_MULTIPLE_PHOTOS;

            if (status.getQuotedStatusId() > 0)
                return TYPE_ITEM_QUOTE;

            return TYPE_ITEM;
        }
    }
}
