package com.andreapivetta.blu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.holders.VHHeader;
import com.andreapivetta.blu.adapters.holders.VHItem;
import com.andreapivetta.blu.adapters.holders.VHItemGIF;
import com.andreapivetta.blu.adapters.holders.VHItemMultiplePhotos;
import com.andreapivetta.blu.adapters.holders.VHItemPhoto;
import com.andreapivetta.blu.adapters.holders.VHItemQuote;
import com.andreapivetta.blu.adapters.holders.BaseViewHolder;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;

public class TweetsListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_ITEM_PHOTO = 2;
    private static final int TYPE_ITEM_QUOTE = 3;
    private static final int TYPE_ITEM_MULTIPLE_PHOTOS = 4;
    private static final int TYPE_ITEM_GIF = 5;

    private ArrayList<Status> mDataSet;
    private Context context;
    private Twitter twitter;
    private int headerPosition;

    private ArrayList<Long> favorites = new ArrayList<>();
    private ArrayList<Long> retweets = new ArrayList<>();

    public TweetsListAdapter(ArrayList<Status> mDataSet, Context context, Twitter twitter, int headerPosition) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.twitter = twitter;
        this.headerPosition = headerPosition;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            case TYPE_ITEM_GIF:
                return new VHItemGIF(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_gif, parent, false));
            case TYPE_HEADER:
                return new VHHeader(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_expanded, parent, false));
            default:
                return new VHItem(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_basic, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.setup(mDataSet.get(position), context, favorites, retweets, twitter);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (isPositionHeader(position))
            return TYPE_HEADER;

        Status status = mDataSet.get(position);
        if (status.getMediaEntities().length > 0) {
            if (status.getExtendedMediaEntities()[0].getVideoVariants().length > 0)
                return TYPE_ITEM_GIF;

            if (status.getExtendedMediaEntities().length == 1)
                return TYPE_ITEM_PHOTO;

            return TYPE_ITEM_MULTIPLE_PHOTOS;
        }

        if (mDataSet.get(position).getQuotedStatusId() > 0)
            return TYPE_ITEM_QUOTE;

        return TYPE_ITEM;
    }

    public void setHeaderPosition(int position) {
        this.headerPosition = position;
    }

    private boolean isPositionHeader(int position) {
        return position == headerPosition;
    }

    public void add(Status status) {
        mDataSet.add(status);
        notifyItemInserted(mDataSet.size() - 1);
    }

}