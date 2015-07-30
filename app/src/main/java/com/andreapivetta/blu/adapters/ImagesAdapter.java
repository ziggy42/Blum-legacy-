package com.andreapivetta.blu.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.bumptech.glide.Glide;

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
        Glide.with(context)
                .load(mediaEntities[position].getMediaURL())
                .placeholder(R.drawable.placeholder)
                .into(holder.tweetPhotoImageView);

        holder.tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String images[] = new String[mediaEntities.length];
                for (int i = 0; i < mediaEntities.length; i++)
                    images[i] = mediaEntities[i].getMediaURL();

                Intent i = new Intent(context, ImageActivity.class);
                i.putExtra(ImageActivity.TAG_IMAGES, images);
                i.putExtra(ImageActivity.TAG_CURRENT_ITEM, position);
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
