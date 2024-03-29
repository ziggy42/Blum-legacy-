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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.andreapivetta.blu.activities.NewTweetActivity;
import com.andreapivetta.blu.activities.NewTweetQuoteActivity;
import com.andreapivetta.blu.activities.TweetActivity;
import com.andreapivetta.blu.activities.UserActivity;
import com.andreapivetta.blu.activities.VideoActivity;
import com.andreapivetta.blu.adapters.ImagesAdapter;
import com.andreapivetta.blu.adapters.decorators.SpaceLeftItemDecoration;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

public class VHHeader extends BaseViewHolder {

    private TextView screenNameTextView, retweetsStatsTextView, favouritesStatsTextView;
    private ImageView tweetPhotoImageView, tweetVideoImageView;
    private ViewStub quotedTweetViewStub;
    private RecyclerView tweetPhotosRecyclerView;
    private FrameLayout videoCover;
    private ImageButton playVideoImageButton;

    private View tweetView;

    public VHHeader(View container) {
        super(container);

        this.tweetPhotoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
        this.screenNameTextView = (TextView) container.findViewById(R.id.screenNameTextView);
        this.retweetsStatsTextView = (TextView) container.findViewById(R.id.retweetsStatsTextView);
        this.favouritesStatsTextView = (TextView) container.findViewById(R.id.favouritesStatsTextView);
        this.quotedTweetViewStub = (ViewStub) container.findViewById(R.id.quotedViewStub);
        this.tweetPhotosRecyclerView = (RecyclerView) container.findViewById(R.id.tweetPhotosRecyclerView);
        this.tweetVideoImageView = (ImageView) container.findViewById(R.id.tweetVideoImageView);
        this.videoCover = (FrameLayout) container.findViewById(R.id.videoCover);
        this.playVideoImageButton = (ImageButton) container.findViewById(R.id.playVideoImageButton);
    }

    @Override
    @SuppressLint("NewApi")
    public void setup(final Status status, final Context context, final ArrayList<Long> favorites,
                      final ArrayList<Long> retweets, final Twitter twitter) {

        final User currentUser = status.getUser();

        userNameTextView.setText(currentUser.getName());

        Date d = status.getCreatedAt();
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
                .placeholder(R.drawable.placeholder)
                .into(userProfilePicImageView);

        if (status.isFavorited() || favorites.contains(status.getId()))
            favouriteImageButton.setImageResource(R.drawable.ic_favorite_red_a700_36dp);
        else
            favouriteImageButton.setImageResource(R.drawable.ic_favorite_grey_600_36dp);

        if (status.isRetweeted() || retweets.contains(status.getId()))
            retweetImageButton.setImageResource(R.drawable.ic_repeat_green_a700_36dp);
        else
            retweetImageButton.setImageResource(R.drawable.ic_repeat_grey600_36dp);

        userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(UserActivity.TAG_USER, currentUser);
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });

        favouriteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.isFavorited() || favorites.contains(status.getId())) {
                    new FavoriteTweet(context, twitter).execute(status.getId(), -1L);
                    favorites.remove(status.getId());
                    favouriteImageButton.setImageResource(R.drawable.ic_favorite_grey_600_36dp);
                } else {
                    new FavoriteTweet(context, twitter).execute(status.getId(), 1L);
                    favorites.add(status.getId());
                    favouriteImageButton.setImageResource(R.drawable.ic_favorite_red_a700_36dp);
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
                                new RetweetTweet(context, twitter).execute(status.getId());
                                retweets.add(status.getId());
                                retweetImageButton.setImageResource(R.drawable.ic_repeat_green_a700_36dp);
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
                b.putSerializable(NewTweetQuoteActivity.PAR_CURRENT_STATUS, status);
                i.putExtra(NewTweetQuoteActivity.PAR_BUNDLE, b);

                context.startActivity(i);
            }
        });

        respondImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + currentUser.getScreenName())
                        .putExtra(NewTweetActivity.TAG_REPLY_ID, status.getId());
                context.startActivity(i);
            }
        });

        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" +
                                currentUser.getScreenName() + "/status/" + status.getId())
                        .setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_tweet)));
            }
        });

        String text = status.getText();
        ExtendedMediaEntity mediaEntityArray[] = status.getExtendedMediaEntities();
        for (int i = 0; i < mediaEntityArray.length; i++)
            text = text.replace(mediaEntityArray[i].getURL(), "");

        StringBuilder iHateHtml = new StringBuilder();
        String endString = "";
        for (String line : text.split("\\r?\\n")) {
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

        String amount = status.getFavoriteCount() + "";
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

        SpannableStringBuilder sb = new SpannableStringBuilder(context.getString(R.string.likes, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        favouritesStatsTextView.setText(sb);

        amount = status.getRetweetCount() + "";
        b = new StyleSpan(android.graphics.Typeface.BOLD);

        sb = new SpannableStringBuilder(context.getString(R.string.retweets, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        retweetsStatsTextView.setText(sb);

        if (mediaEntityArray.length == 1) {
            final ExtendedMediaEntity mediaEntity = mediaEntityArray[0];
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
            } else {
                videoCover.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(mediaEntity.getMediaURL())
                        .placeholder(R.drawable.placeholder)
                        .centerCrop()
                        .into(tweetVideoImageView);

                playVideoImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, VideoActivity.class);
                        i.putExtra(VideoActivity.TAG_VIDEO, mediaEntity.getVideoVariants()[0].getUrl())
                                .putExtra(VideoActivity.TAG_TYPE, mediaEntity.getType());
                        context.startActivity(i);
                    }
                });
            }
        } else if (mediaEntityArray.length > 1) {
            tweetPhotosRecyclerView.setVisibility(View.VISIBLE);
            tweetPhotosRecyclerView.addItemDecoration(new SpaceLeftItemDecoration(5));
            tweetPhotosRecyclerView.setAdapter(new ImagesAdapter(status.getExtendedMediaEntities(), context));
            tweetPhotosRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

        if (status.getQuotedStatusId() > 0) {
            if (tweetView == null)
                tweetView = quotedTweetViewStub.inflate();

            final Status quotedStatus = status.getQuotedStatus();

            ImageView photoImageView = (ImageView) tweetView.findViewById(R.id.photoImageView);
            ((TextView) tweetView.findViewById(R.id.quotedUserNameTextView)).setText(quotedStatus.getUser().getName());

            if (quotedStatus.getMediaEntities().length > 0) {
                photoImageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(quotedStatus.getMediaEntities()[0].getMediaURL())
                        .placeholder(R.drawable.placeholder)
                        .into(photoImageView);

                ((TextView) tweetView.findViewById(R.id.quotedStatusTextView)).setText(
                        quotedStatus.getText().replace(quotedStatus.getMediaEntities()[0].getURL(), ""));
            } else {
                photoImageView.setVisibility(View.GONE);
                ((TextView) tweetView.findViewById(R.id.quotedStatusTextView)).setText(quotedStatus.getText());
            }

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
