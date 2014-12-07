package com.andreapivetta.blu.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserActivity;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;

public class TweetsListHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<Status> mDataSet;
    private Context context;
    private Twitter twitter;

    public TweetsListHeaderAdapter(ArrayList<Status> mDataSet, Context context, Twitter twitter) {
        this.mDataSet = mDataSet;
        this.context = context;
        this.twitter = twitter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return new VHItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet, parent, false));
        } else {
            return new VHHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_expanded, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Status currentStatus;
        MediaEntity mediaEntityArray[];

        if (holder instanceof VHItem) {
            ((VHItem) holder).interactionLinearLayout.setVisibility(View.GONE);

            if (mDataSet.get(position).isRetweet()) {
                currentStatus = mDataSet.get(position).getRetweetedStatus();
                ((VHItem) holder).retweetTextView.setVisibility(View.VISIBLE);
                ((VHItem) holder).retweetTextView.setText(context.getString(R.string.retweeted_by) + " @" + mDataSet.get(position).getUser().getScreenName());
            } else {
                currentStatus = mDataSet.get(position);
                ((VHItem) holder).retweetTextView.setVisibility(View.GONE);
            }

            mediaEntityArray = currentStatus.getMediaEntities();

            ((VHItem) holder).userNameTextView.setText(currentStatus.getUser().getName());
            ((VHItem) holder).statusTextView.setText(currentStatus.getText());
            Linkify.addLinks(((VHItem) holder).statusTextView, Linkify.ALL);

            ((VHItem) holder).timeTextView.setText(new SimpleDateFormat("hh:mm").format(currentStatus.getCreatedAt()));

            Picasso.with(context)
                    .load(currentStatus.getUser().getBiggerProfileImageURL())
                    .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                    .into(((VHItem) holder).userProfilePicImageView);

            if (mediaEntityArray.length > 0) {
                for (MediaEntity mediaEntity : mediaEntityArray) {
                    if (mediaEntity.getType().equals("photo")) {
                        ((VHItem) holder).tweetPhotoImageView.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(mediaEntity.getMediaURL())
                                .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                                .into(((VHItem) holder).tweetPhotoImageView);
                        break;
                    }
                }
            } else {
                ((VHItem) holder).tweetPhotoImageView.setVisibility(View.GONE);
            }

            ((VHItem) holder).userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, UserActivity.class);
                    i.putExtra("ID", currentStatus.getUser().getId())
                            .putExtra("Twitter", twitter);
                    context.startActivity(i);
                }
            });

            ((VHItem) holder).statusTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((VHItem) holder).interactionLinearLayout.getVisibility() == View.VISIBLE)
                        ((VHItem) holder).interactionLinearLayout.setVisibility(View.GONE);
                    else
                        ((VHItem) holder).interactionLinearLayout.setVisibility(View.VISIBLE);
                }
            });

            ((VHItem) holder).cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((VHItem) holder).interactionLinearLayout.getVisibility() == View.VISIBLE)
                        ((VHItem) holder).interactionLinearLayout.setVisibility(View.GONE);
                    else
                        ((VHItem) holder).interactionLinearLayout.setVisibility(View.VISIBLE);
                }
            });

            ((VHItem) holder).favouriteImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long type;
                    if (currentStatus.isFavorited()) {
                        type = -1;
                        new FavoriteTweet(context, twitter).execute(currentStatus.getId(), type);
                    } else {
                        type = 1;
                        new FavoriteTweet(context, twitter).execute(currentStatus.getId(), type);
                    }
                }
            });

            ((VHItem) holder).retweetImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.retweet_title))
                            .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new RetweetTweet(context, twitter).execute(currentStatus.getId());
                                }
                            })
                            .setNegativeButton(R.string.cancel, null).create().show();
                }
            });

            ((VHItem) holder).respondImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            ((VHItem) holder).shareImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://twitter.com/" + currentStatus.getUser().getScreenName()
                            + "/status/" + currentStatus.getId();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, url)
                            .setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_tweet)));
                }
            });

            ((VHItem) holder).openTweetImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, TweetActivity.class);
                    i.putExtra("STATUS", currentStatus.getId());
                    context.startActivity(i);
                }
            });
        } else if (holder instanceof VHHeader) {
            if (mDataSet.get(position).isRetweet()) {
                currentStatus = mDataSet.get(position).getRetweetedStatus();
                ((VHHeader) holder).retweetTextView.setVisibility(View.VISIBLE);
                ((VHHeader) holder).retweetTextView.setText(context.getString(R.string.retweeted_by) + " @" + currentStatus.getUser().getScreenName());
            } else {
                currentStatus = mDataSet.get(position);
            }

            Picasso.with(context)
                    .load(currentStatus.getUser().getProfileImageURL())
                    .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                    .into(((VHHeader) holder).userProfilePicImageView);

            ((VHHeader) holder).userNameTextView.setText(currentStatus.getUser().getName());
            ((VHHeader) holder).screenNameTextView.setText("@" + currentStatus.getUser().getScreenName());
            ((VHHeader) holder).timeTextView.setText(new SimpleDateFormat("hh:mm").format(currentStatus.getCreatedAt()));
            ((VHHeader) holder).statusTextView.setText(currentStatus.getText());

            String amount = currentStatus.getFavoriteCount() + "";
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

            SpannableStringBuilder sb = new SpannableStringBuilder(amount + " " + context.getString(R.string.favourites));
            sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ((VHHeader) holder).favouritesStatsTextView.setText(sb);

            amount = currentStatus.getRetweetCount() + "";
            b = new StyleSpan(android.graphics.Typeface.BOLD);

            sb = new SpannableStringBuilder(amount + " " + context.getString(R.string.retweets));
            sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ((VHHeader) holder).retweetsStatsTextView.setText(sb);

            mediaEntityArray = currentStatus.getMediaEntities();
            if (mediaEntityArray.length > 0) {
                for (MediaEntity mediaEntity : mediaEntityArray) {
                    if (mediaEntity.getType().equals("photo")) {
                        ((VHHeader) holder).tweetPhotoImageView.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(mediaEntity.getMediaURL())
                                .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                                .into(((VHHeader) holder).tweetPhotoImageView);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    class VHItem extends RecyclerView.ViewHolder {
        public TextView userNameTextView, statusTextView, timeTextView, retweetTextView;
        public ImageView userProfilePicImageView, tweetPhotoImageView;
        public LinearLayout interactionLinearLayout;
        public FrameLayout cardView;
        public ImageButton favouriteImageButton, retweetImageButton, respondImageButton, shareImageButton, openTweetImageButton;

        public VHItem(View container) {
            super(container);

            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.retweetTextView = (TextView) container.findViewById(R.id.retweetTextView);
            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);

            this.interactionLinearLayout = (LinearLayout) container.findViewById(R.id.interactionLinearLayout);
            this.cardView = (FrameLayout) container.findViewById(R.id.card_view);

            this.favouriteImageButton = (ImageButton) container.findViewById(R.id.favouriteImageButton);
            this.retweetImageButton = (ImageButton) container.findViewById(R.id.retweetImageButton);
            this.respondImageButton = (ImageButton) container.findViewById(R.id.respondImageButton);
            this.shareImageButton = (ImageButton) container.findViewById(R.id.shareImageButton);
            this.openTweetImageButton = (ImageButton) container.findViewById(R.id.openTweetImageButton);
        }
    }

    class VHHeader extends RecyclerView.ViewHolder {
        public ImageView userProfilePicImageView, tweetPhotoImageView;
        public TextView userNameTextView, screenNameTextView, timeTextView, statusTextView,
                retweetTextView, retweetsStatsTextView, favouritesStatsTextView;
        public ImageButton favouriteImageButton, retweetImageButton, respondImageButton, shareImageButton;

        public VHHeader(View container) {
            super(container);

            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.retweetTextView = (TextView) container.findViewById(R.id.retweetTextView);
            this.retweetsStatsTextView = (TextView) container.findViewById(R.id.retweetsStatsTextView);
            this.favouritesStatsTextView = (TextView) container.findViewById(R.id.favouritesStatsTextView);
            this.favouriteImageButton = (ImageButton) container.findViewById(R.id.favouriteImageButton);
            this.retweetImageButton = (ImageButton) container.findViewById(R.id.retweetImageButton);
            this.respondImageButton = (ImageButton) container.findViewById(R.id.respondImageButton);
            this.shareImageButton = (ImageButton) container.findViewById(R.id.shareImageButton);
        }
    }
}
