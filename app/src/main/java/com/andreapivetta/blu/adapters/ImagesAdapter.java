package com.andreapivetta.blu.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.squareup.picasso.Picasso;

import twitter4j.MediaEntity;


public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.VHItem> {

    private MediaEntity mediaEntities[];
    private Context context;

    public ImagesAdapter(MediaEntity mediaEntities[], Context context) {
        this.mediaEntities = mediaEntities;
        this.context = context;
    }

    @Override
    public VHItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo, parent, false);

        return new VHItem(v);
    }

    @Override
    public void onBindViewHolder(VHItem holder, final int position) {
        Picasso.with(context)
                .load(mediaEntities[position].getMediaURL())
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.tweetPhotoImageView);

        holder.tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ImageActivity.class);
                i.putExtra("IMAGE", mediaEntities[position].getMediaURL());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaEntities.length;
    }

    class VHItem extends RecyclerView.ViewHolder {
        public ImageView tweetPhotoImageView;

        public VHItem(View container) {
            super(container);

            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
        }
    }
}
