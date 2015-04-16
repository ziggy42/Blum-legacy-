package com.andreapivetta.blu.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.andreapivetta.blu.activities.NewTweetActivity;
import com.andreapivetta.blu.activities.NewTweetQuoteActivity;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;

public class TweetsListAdapter extends RecyclerView.Adapter<TweetsListAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_ITEM_PHOTO = 2;
    private static final int TYPE_ITEM_QUOTE = 3;

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM)
            return new VHItem(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_basic, parent, false));
        else if (viewType == TYPE_ITEM_PHOTO)
            return new VHItemPhoto(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_photo, parent, false));
        else if (viewType == TYPE_ITEM_QUOTE)
            return new VHItemQuote(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_quote, parent, false));
        else
            return new VHHeader(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_expanded, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Status currentStatus;
        final int TYPE = getItemViewType(position);

        if (mDataSet.get(position).isRetweet()) {
            currentStatus = mDataSet.get(position).getRetweetedStatus();
            holder.retweetTextView.setVisibility(View.VISIBLE);
            holder.retweetTextView.setText(
                    context.getString(R.string.retweeted_by, mDataSet.get(position).getUser().getScreenName()));
        } else {
            currentStatus = mDataSet.get(position);
            holder.retweetTextView.setVisibility(View.GONE);
        }

        final User currentUser = currentStatus.getUser();

        holder.userNameTextView.setText(currentUser.getName());

        Date d = currentStatus.getCreatedAt();
        Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
        c2.setTime(d);

        long diff = c.getTimeInMillis() - c2.getTimeInMillis();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds > 60) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > 60) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        holder.timeTextView.setText(
                                (new java.text.SimpleDateFormat("MMM dd", Locale.getDefault())).format(d));
                    else
                        holder.timeTextView.setText(
                                (new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault())).format(d));
                } else
                    holder.timeTextView.setText(context.getString(R.string.mini_hours, (int) hours));
            } else
                holder.timeTextView.setText(context.getString(R.string.mini_minutes, (int) minutes));
        } else holder.timeTextView.setText(context.getString(R.string.mini_seconds, (int) seconds));

        Picasso.with(context)
                .load(currentUser.getBiggerProfileImageURL())
                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                .into(holder.userProfilePicImageView);

        if (currentStatus.isFavorited() || favorites.contains(currentStatus.getId()))
            holder.favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
        else
            holder.favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);

        if (currentStatus.isRetweeted() || retweets.contains(currentStatus.getId()))
            holder.retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
        else
            holder.retweetImageButton.setImageResource(R.drawable.ic_repeat_grey600_36dp);

        holder.userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                i.putExtra("ID", currentUser.getId());
                context.startActivity(i);
            }
        });

        holder.favouriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStatus.isFavorited() || favorites.contains(currentStatus.getId())) {
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), -1L);
                    favorites.remove(currentStatus.getId());
                    holder.favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);
                } else {
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), 1L);
                    favorites.add(currentStatus.getId());
                    holder.favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
                }
            }
        });

        holder.retweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.retweet_title))
                        .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new RetweetTweet(context, twitter).execute(currentStatus.getId());
                                retweets.add(currentStatus.getId());
                                holder.retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).create().show();
            }
        });

        holder.quoteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetQuoteActivity.class);

                Bundle b = new Bundle();
                b.putSerializable(NewTweetQuoteActivity.PAR_CURRENT_STATUS, currentStatus);
                i.putExtra(NewTweetQuoteActivity.PAR_BUNDLE, b);

                context.startActivity(i);
            }
        });

        holder.respondImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetActivity.class);
                i.putExtra("USER_PREFIX", "@" + currentUser.getScreenName())
                        .putExtra("REPLY_ID", currentStatus.getId());
                context.startActivity(i);
            }
        });

        holder.shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" +
                                currentUser.getScreenName() + "/status/" + currentStatus.getId())
                        .setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_tweet)));
            }
        });

        if (TYPE == TYPE_HEADER) {
            StringBuilder iHateHtml = new StringBuilder();
            String endString = "";
            for (String line : currentStatus.getText().split("\\r?\\n")) {
                if (iHateHtml.length() > 0) iHateHtml.append("<br/>");
                for (String word : line.split(" ")) {
                    if (Patterns.WEB_URL.matcher(word).matches()) {
                        iHateHtml.append("<a href=\"")
                                .append(word)
                                .append("\">")
                                .append(word)
                                .append("</a>");
                    } else if (word.length() > 1) {
                        if (word.substring(0, 1).equals("@")) {
                            for (int i = 1; i < word.length(); i++)
                                if ("|/()=?'^[],;.:-\"\\".indexOf(word.charAt(i)) >= 0) {
                                    endString = word.substring(i);
                                    word = word.substring(0, i);
                                    break;
                                }

                            iHateHtml.append("<a href=\"com.andreapivetta.blu.user://")
                                    .append(word.substring(1))
                                    .append("\">")
                                    .append(word)
                                    .append("</a>")
                                    .append(endString);
                        } else if (word.substring(0, 1).equals("#")) {
                            for (int i = 1; i < word.length(); i++)
                                if ("|/()=?'^[],;.:-\"\\".indexOf(word.charAt(i)) >= 0) {
                                    endString = word.substring(i);
                                    word = word.substring(0, i);
                                    break;
                                }

                            iHateHtml.append("<a href=\"com.andreapivetta.blu.hashtag://")
                                    .append(word.substring(1))
                                    .append("\">")
                                    .append(word)
                                    .append("</a>")
                                    .append(endString);
                        } else {
                            iHateHtml.append(word);
                        }
                    } else {
                        iHateHtml.append(word);
                    }
                    iHateHtml.append(" ");
                }
            }

            holder.statusTextView.setText(Html.fromHtml(iHateHtml.toString()));
            holder.statusTextView.setMovementMethod(LinkMovementMethod.getInstance());

            ((VHHeader) holder).screenNameTextView.setText("@" + currentUser.getScreenName());

            String amount = currentStatus.getFavoriteCount() + "";
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

            SpannableStringBuilder sb = new SpannableStringBuilder(context.getString(R.string.favourites, amount));
            sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ((VHHeader) holder).favouritesStatsTextView.setText(sb);

            amount = currentStatus.getRetweetCount() + "";
            b = new StyleSpan(android.graphics.Typeface.BOLD);

            sb = new SpannableStringBuilder(context.getString(R.string.retweets, amount));
            sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ((VHHeader) holder).retweetsStatsTextView.setText(sb);

            MediaEntity mediaEntityArray[] = currentStatus.getMediaEntities();
            if (mediaEntityArray.length > 0) {
                for (final MediaEntity mediaEntity : mediaEntityArray) {
                    if (mediaEntity.getType().equals("photo")) {
                        ((VHHeader) holder).tweetPhotoImageView.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(mediaEntity.getMediaURL())
                                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                                .into(((VHHeader) holder).tweetPhotoImageView);

                        ((VHHeader) holder).tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(context, ImageActivity.class);
                                i.putExtra("IMAGE", mediaEntity.getMediaURL());
                                context.startActivity(i);
                            }
                        });

                        break;
                    }
                }
            }
        } else {
            holder.statusTextView.setText(currentStatus.getText());
            holder.interactionLinearLayout.setVisibility(View.GONE);
            Linkify.addLinks(holder.statusTextView, Linkify.ALL);

            holder.statusTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.interactionLinearLayout.setVisibility(
                            (holder.interactionLinearLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                }
            });

            if (TYPE == TYPE_ITEM_PHOTO) {
                for (final MediaEntity mediaEntity : currentStatus.getMediaEntities()) {
                    if (mediaEntity.getType().equals("photo")) {
                        Picasso.with(context)
                                .load(mediaEntity.getMediaURL())
                                .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                                .fit()
                                .centerCrop()
                                .into(((VHItemPhoto) holder).tweetPhotoImageView);

                        ((VHItemPhoto) holder).tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(context, ImageActivity.class);
                                i.putExtra("IMAGE", mediaEntity.getMediaURL());
                                context.startActivity(i);
                            }
                        });

                        break;
                    }
                }
            } else if (TYPE == TYPE_ITEM_QUOTE) {
                String quotedStatusURL = "";
                for (URLEntity entity : currentStatus.getURLEntities())
                    if (entity.getExpandedURL().matches("(^https://twitter.com/)(.*)(/status/)(.*)")) {
                        quotedStatusURL = entity.getExpandedURL();
                        break;
                    }
                ((VHItemQuote) holder).photoImageView.setVisibility(View.GONE);
                new FillQuote(((VHItemQuote) holder).quotedUserNameTextView, ((VHItemQuote) holder).quotedStatusTextView,
                        ((VHItemQuote) holder).photoImageView, ((VHItemQuote) holder).quotedStatusLinearLayout,
                        quotedStatusURL).execute();
            }

            ((VHItem) holder).cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.interactionLinearLayout.setVisibility(
                            (holder.interactionLinearLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                }
            });

            ((VHItem) holder).openTweetImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, TweetActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("TWEET", currentStatus);
                    i.putExtra("STATUS", b);
                    context.startActivity(i);
                }
            });
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

        if (mDataSet.get(position).getMediaEntities().length > 0)
            return TYPE_ITEM_PHOTO;

        for (URLEntity entity : mDataSet.get(position).getURLEntities())
            if (entity.getExpandedURL().matches("(^https://twitter.com/)(.*)(/status/)(.*)"))
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

    class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout interactionLinearLayout;
        public TextView userNameTextView, statusTextView, timeTextView, retweetTextView;
        public ImageView userProfilePicImageView;
        public ImageButton favouriteImageButton, retweetImageButton, respondImageButton,
                shareImageButton, quoteImageButton;

        public ViewHolder(View container) {
            super(container);

            this.interactionLinearLayout = (LinearLayout) container.findViewById(R.id.interactionLinearLayout);
            this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
            this.statusTextView = (TextView) container.findViewById(R.id.statusTextView);
            this.userProfilePicImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
            this.timeTextView = (TextView) container.findViewById(R.id.timeTextView);
            this.retweetTextView = (TextView) container.findViewById(R.id.retweetTextView);
            this.favouriteImageButton = (ImageButton) container.findViewById(R.id.favouriteImageButton);
            this.retweetImageButton = (ImageButton) container.findViewById(R.id.retweetImageButton);
            this.respondImageButton = (ImageButton) container.findViewById(R.id.respondImageButton);
            this.shareImageButton = (ImageButton) container.findViewById(R.id.shareImageButton);
            this.quoteImageButton = (ImageButton) container.findViewById(R.id.quoteImageButton);
        }
    }

    class VHItem extends ViewHolder {
        public FrameLayout cardView;
        public ImageButton openTweetImageButton;

        public VHItem(View container) {
            super(container);

            this.cardView = (FrameLayout) container.findViewById(R.id.card_view);
            this.openTweetImageButton = (ImageButton) container.findViewById(R.id.openTweetImageButton);
        }
    }

    class VHItemPhoto extends VHItem {
        public ImageView tweetPhotoImageView; // TODO e se ho più foto?

        public VHItemPhoto(View container) {
            super(container);

            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
        }
    }

    class VHItemQuote extends VHItem {
        public TextView quotedUserNameTextView, quotedStatusTextView;
        public ImageView photoImageView;
        public LinearLayout quotedStatusLinearLayout;

        public VHItemQuote(View container) {
            super(container);

            this.quotedUserNameTextView = (TextView) container.findViewById(R.id.quotedUserNameTextView);
            this.quotedStatusTextView = (TextView) container.findViewById(R.id.quotedStatusTextView);
            this.photoImageView = (ImageView) container.findViewById(R.id.photoImageView);
            this.quotedStatusLinearLayout = (LinearLayout) container.findViewById(R.id.quotedStatusLinearLayout);
        }

    }

    class VHHeader extends ViewHolder {
        public TextView screenNameTextView, retweetsStatsTextView, favouritesStatsTextView;
        public ImageView tweetPhotoImageView; // TODO anche qui diversi header

        public VHHeader(View container) {
            super(container);

            this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
            this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
            this.retweetsStatsTextView = (TextView) container.findViewById(R.id.retweetsStatsTextView);
            this.favouritesStatsTextView = (TextView) container.findViewById(R.id.favouritesStatsTextView);
        }
    }

    class FillQuote extends AsyncTask<Void, Void, Boolean> {
        private TextView quotedUserNameTextView, quotedStatusTextView;
        private LinearLayout quotedStatusLinearLayout;
        private ImageView photoImageView;

        private twitter4j.Status status;
        private long statusID;

        public FillQuote(TextView quotedUserNameTextView, TextView quotedStatusTextView,
                         ImageView photoImageView, LinearLayout quotedStatusLinearLayout, String status) {
            this.quotedStatusTextView = quotedStatusTextView;
            this.quotedUserNameTextView = quotedUserNameTextView;
            this.photoImageView = photoImageView;
            this.quotedStatusLinearLayout = quotedStatusLinearLayout;
            this.statusID = Long.parseLong(status.substring(status.lastIndexOf('/') + 1));
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                status = twitter.showStatus(statusID);
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                this.quotedUserNameTextView.setText(status.getUser().getName());
                this.quotedStatusTextView.setText(status.getText());

                if (status.getMediaEntities().length > 0) {
                    photoImageView.setVisibility(View.VISIBLE);
                    Picasso.with(context)
                            .load(status.getMediaEntities()[0].getMediaURL())
                            .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                            .into(photoImageView);
                } else
                    photoImageView.setVisibility(View.GONE);

                this.quotedStatusLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, TweetActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable("TWEET", status);
                        i.putExtra("STATUS", b);
                        context.startActivity(i);
                    }
                });
            }
        }
    }
}