package com.andreapivetta.blu.adapters.holders;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.andreapivetta.blu.activities.NewTweetActivity;
import com.andreapivetta.blu.activities.NewTweetQuoteActivity;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserProfileActivity;
import com.andreapivetta.blu.adapters.ImagesAdapter;
import com.andreapivetta.blu.adapters.decorators.SpaceLeftItemDecoration;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.andreapivetta.blu.utilities.CircleTransform;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

public class VHHeader extends ViewHolder {

    public TextView screenNameTextView, retweetsStatsTextView, favouritesStatsTextView;
    public ImageView tweetPhotoImageView;
    public ViewStub quotedTweetViewStub;
    public RecyclerView tweetPhotosRecyclerView;

    private View tweetView;

    public VHHeader(View container) {
        super(container);

        this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
        this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
        this.retweetsStatsTextView = (TextView) container.findViewById(R.id.retweetsStatsTextView);
        this.favouritesStatsTextView = (TextView) container.findViewById(R.id.favouritesStatsTextView);
        this.quotedTweetViewStub = (ViewStub) container.findViewById(R.id.quotedViewStub);
        this.tweetPhotosRecyclerView = (RecyclerView) container.findViewById(R.id.tweetPhotosRecyclerView);
    }

    @Override
    @SuppressLint("NewApi")
    public void setup(Status status, final Context context, final ArrayList<Long> favorites,
                      final ArrayList<Long> retweets, final Twitter twitter) {
        final Status currentStatus;

        if (status.isRetweet()) {
            currentStatus = status.getRetweetedStatus();
            retweetTextView.setVisibility(View.VISIBLE);
            retweetTextView.setText(
                    context.getString(R.string.retweeted_by, status.getUser().getScreenName()));
        } else {
            currentStatus = status;
            retweetTextView.setVisibility(View.GONE);
        }

        final User currentUser = currentStatus.getUser();

        userNameTextView.setText(currentUser.getName());

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
                        timeTextView.setText((new java.text.SimpleDateFormat("MMM dd", Locale.getDefault())).format(d));
                    else
                        timeTextView.setText((new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault())).format(d));
                } else
                    timeTextView.setText(context.getString(R.string.mini_hours, (int) hours));
            } else
                timeTextView.setText(context.getString(R.string.mini_minutes, (int) minutes));
        } else timeTextView.setText(context.getString(R.string.mini_seconds, (int) seconds));

        Glide.with(context)
                .load(currentUser.getBiggerProfileImageURL())
                .placeholder(R.drawable.placeholder_circular)
                .transform(new CircleTransform(context))
                .into(userProfilePicImageView);

        if (currentStatus.isFavorited() || favorites.contains(currentStatus.getId()))
            favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
        else
            favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);

        if (currentStatus.isRetweeted() || retweets.contains(currentStatus.getId()))
            retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
        else
            retweetImageButton.setImageResource(R.drawable.ic_repeat_grey600_36dp);

        userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(UserProfileActivity.TAG_USER, currentUser);
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });

        favouriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStatus.isFavorited() || favorites.contains(currentStatus.getId())) {
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), -1L);
                    favorites.remove(currentStatus.getId());
                    favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);
                } else {
                    new FavoriteTweet(context, twitter).execute(currentStatus.getId(), 1L);
                    favorites.add(currentStatus.getId());
                    favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
                }
            }
        });

        retweetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.retweet_title))
                        .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new RetweetTweet(context, twitter).execute(currentStatus.getId());
                                retweets.add(currentStatus.getId());
                                retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).create().show();
            }
        });

        quoteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetQuoteActivity.class);

                Bundle b = new Bundle();
                b.putSerializable(NewTweetQuoteActivity.PAR_CURRENT_STATUS, currentStatus);
                i.putExtra(NewTweetQuoteActivity.PAR_BUNDLE, b);

                context.startActivity(i);
            }
        });

        respondImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + currentUser.getScreenName())
                        .putExtra(NewTweetActivity.TAG_REPLY_ID, currentStatus.getId());
                context.startActivity(i);
            }
        });

        shareImageButton.setOnClickListener(new View.OnClickListener() {
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
                    if (word.charAt(0) == '@' || (word.charAt(0) == '.' && word.charAt(1) == '@')) {
                        int index = word.indexOf('@');
                        int i;
                        for (i = index + 1; i < word.length(); i++)
                            if ("|/()=?'^[],;.:-\"\\".indexOf(word.charAt(i)) >= 0) {
                                endString = word.substring(i);
                                break;
                            }

                        word = word.substring(index, i);
                        iHateHtml.append(((index == 0) ? "" : "."))
                                .append("<a href=\"com.andreapivetta.blu.user://")
                                .append(word.substring(1))
                                .append("\">")
                                .append(word)
                                .append("</a>")
                                .append(endString);
                    } else if (word.charAt(0) == '#') {
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

        statusTextView.setText(Html.fromHtml(iHateHtml.toString()));
        statusTextView.setMovementMethod(LinkMovementMethod.getInstance());

        screenNameTextView.setText("@" + currentUser.getScreenName());

        String amount = currentStatus.getFavoriteCount() + "";
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

        SpannableStringBuilder sb = new SpannableStringBuilder(context.getString(R.string.favourites, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        favouritesStatsTextView.setText(sb);

        amount = currentStatus.getRetweetCount() + "";
        b = new StyleSpan(android.graphics.Typeface.BOLD);

        sb = new SpannableStringBuilder(context.getString(R.string.retweets, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        retweetsStatsTextView.setText(sb);

        MediaEntity mediaEntityArray[] = currentStatus.getExtendedMediaEntities();
        if (mediaEntityArray.length == 1) {
            final MediaEntity mediaEntity = mediaEntityArray[0];
            if (mediaEntity.getType().equals("photo")) {
                tweetPhotoImageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(mediaEntity.getMediaURL())
                        .asBitmap()
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.placeholder)
                        .into(tweetPhotoImageView);

                tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, ImageActivity.class);
                        i.putExtra(ImageActivity.TAG_IMAGES, new String[]{mediaEntity.getMediaURL()});
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            ActivityOptions options = ActivityOptions
                                    .makeSceneTransitionAnimation((Activity) context, tweetPhotoImageView,
                                            context.getString(R.string.image_transition));
                            context.startActivity(i, options.toBundle());
                        } else {
                            context.startActivity(i);
                        }
                    }
                });
            }
        } else if (mediaEntityArray.length > 1) {
            tweetPhotosRecyclerView.setVisibility(View.VISIBLE);
            tweetPhotosRecyclerView.addItemDecoration(new SpaceLeftItemDecoration(5));
            tweetPhotosRecyclerView.setAdapter(new ImagesAdapter(currentStatus.getExtendedMediaEntities(), context));
            tweetPhotosRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

        if (currentStatus.getQuotedStatusId() > 0) {
            if (tweetView == null)
                tweetView = quotedTweetViewStub.inflate();

            final Status quotedStatus = currentStatus.getQuotedStatus();

            ImageView photoImageView = (ImageView) tweetView.findViewById(R.id.photoImageView);
            ((TextView) tweetView.findViewById(R.id.quotedUserNameTextView)).setText(quotedStatus.getUser().getName());
            ((TextView) tweetView.findViewById(R.id.quotedStatusTextView)).setText(quotedStatus.getText());

            if (quotedStatus.getMediaEntities().length > 0) {
                photoImageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(quotedStatus.getMediaEntities()[0].getMediaURL())
                        .placeholder(R.drawable.placeholder)
                        .into(photoImageView);
            } else
                photoImageView.setVisibility(View.GONE);

            tweetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, TweetActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(TweetActivity.TAG_TWEET, quotedStatus);
                    i.putExtra(TweetActivity.TAG_STATUS_BUNDLE, b);
                    context.startActivity(i);
                }
            });


        }
    }
}
