package com.andreapivetta.blu.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.ImagesAdapter;
import com.andreapivetta.blu.adapters.decorators.SpaceLeftItemDecoration;
import com.andreapivetta.blu.adapters.UserListSimpleAdapter;
import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.twitter.FavoriteTweet;
import com.andreapivetta.blu.twitter.FollowTwitterUser;
import com.andreapivetta.blu.twitter.RetweetTweet;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import twitter4j.MediaEntity;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserProfileActivity extends ThemedActivity implements SnackbarContainer {

    public final static String TAG_USER = "user";
    public final static String TAG_TYPE = "type";
    public final static String TAG_ARRAY = "array";
    public final static String TAG_ID = "id";

    private final static String FOLLOWERS = "followers";
    private final static String FOLLOWING = "following";

    private TYPE type;
    private Twitter twitter;
    private User user;
    private ArrayList<User> followers = new ArrayList<>(), following = new ArrayList<>();
    private Status statuses[];
    private UserListSimpleAdapter mUsersSimpleAdapter;
    private boolean dialogLoading = true;
    private int dialogPastVisibleItems, dialogVisibleItemCount, dialogTotalItemCount;
    private long cursor = -1;
    private Toolbar toolbar;
    private ScrollView profileScrollView;
    private ImageView profileBackgroundImageView, profilePictureImageView;
    private TextView userNickTextView, userNameTextView, descriptionTextView, userLocationTextView, userWebsiteTextView,
            tweetAmountTextView, followingAmountTextView, followersAmountTextView, isHeFollowingTextView;
    private Button tweetButton, followButton;
    private ViewStub[] stubs = new ViewStub[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        twitter = TwitterUtils.getTwitter(UserProfileActivity.this);
        profileScrollView = (ScrollView) findViewById(R.id.profileScrollView);
        profileBackgroundImageView = (ImageView) findViewById(R.id.profileBackgroundImageView);
        profilePictureImageView = (ImageView) findViewById(R.id.userProfilePicImageView);
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        userNickTextView = (TextView) findViewById(R.id.userNickTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        userLocationTextView = (TextView) findViewById(R.id.userLocationTextView);
        userWebsiteTextView = (TextView) findViewById(R.id.userWebsiteTextView);
        tweetAmountTextView = (TextView) findViewById(R.id.tweetAmountTextView);
        followingAmountTextView = (TextView) findViewById(R.id.followingAmountTextView);
        followersAmountTextView = (TextView) findViewById(R.id.followersAmountTextView);
        isHeFollowingTextView = (TextView) findViewById(R.id.isHeFollowingTextView);
        tweetButton = (Button) findViewById(R.id.tweetButton);
        followButton = (Button) findViewById(R.id.followButton);
        stubs[0] = (ViewStub) findViewById(R.id.firstViewStub);
        stubs[1] = (ViewStub) findViewById(R.id.secondViewStub);
        stubs[2] = (ViewStub) findViewById(R.id.thirdViewStub);

        if (savedInstanceState != null) {
            user = (User) savedInstanceState.getSerializable(TAG_USER);
            type = (TYPE) savedInstanceState.getSerializable(TAG_TYPE);
            statuses = (Status[]) savedInstanceState.getSerializable(TAG_ARRAY);

            if (statuses != null && type != null && user != null) {
                setUpUI();
                setUpRelationControls();
                setUpTweets();
                invalidateOptionsMenu();
            } else {
                setUpUser();
            }
        } else {
            setUpUser();
        }

    }

    void setUpUser() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.containsKey(TAG_USER)) {
            user = (User) bundle.getSerializable(TAG_USER);
            setUpUI();
            new LoadRelationship().execute(null, null, null);
        } else {
            Uri uri = intent.getData();
            if (uri != null)
                new LoadUserByName().execute(uri.toString().substring(29));
            else
                new LoadUser().execute(intent.getLongExtra(TAG_ID, 0));
        }
    }

    void setUpUI() {

        final int height = Common.dpToPx(UserProfileActivity.this, 200);
        final double da = 1.0 / height;

        profileScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int y = profileScrollView.getScrollY();
                profileBackgroundImageView.setY(-y / 2);

                if (toolbar.getVisibility() == View.VISIBLE) {
                    if (height <= y) {
                        toolbar.setVisibility(View.GONE);
                    } else {
                        toolbar.setAlpha((float) (1 - da * y));
                    }
                } else if (toolbar.getVisibility() == View.GONE && height >= y) {
                    toolbar.setVisibility(View.VISIBLE);
                }
            }
        });

        Picasso.with(this)
                .load(user.getProfileBannerURL())
                .placeholder(R.drawable.placeholder)
                .into(profileBackgroundImageView, new Callback() {
                    @Override
                    @TargetApi(21)
                    public void onSuccess() {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            new Palette.Builder(((BitmapDrawable) profileBackgroundImageView.getDrawable()).getBitmap())
                                    .generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            Palette.Swatch swatch = palette.getLightVibrantSwatch();
                                            if (swatch != null) {
                                                getWindow().setStatusBarColor(swatch.getRgb());
                                            } else {
                                                Bitmap onePixelBitmap = Bitmap.createScaledBitmap(
                                                        ((BitmapDrawable) profileBackgroundImageView
                                                                .getDrawable()).getBitmap(), 1, 1, true);
                                                int pixel = onePixelBitmap.getPixel(0, 0);
                                                getWindow().setStatusBarColor(
                                                        Color.rgb(Color.red(pixel),
                                                                Color.green(pixel), Color.blue(pixel)));
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        Picasso.with(this)
                .load(user.getOriginalProfileImageURL())
                .placeholder(R.drawable.placeholder)
                .into(profilePictureImageView);

        userNameTextView.setText(user.getName());
        userNickTextView.setText("@" + user.getScreenName());
        descriptionTextView.setText(user.getDescription());
        Linkify.addLinks(descriptionTextView, Linkify.ALL);

        String location = user.getLocation();
        if (location.length() != 0) userLocationTextView.setText(location);
        else userLocationTextView.setVisibility(View.GONE);

        String website = user.getURLEntity().getDisplayURL();
        if (website.length() != 0) userWebsiteTextView.setText(website);
        else userWebsiteTextView.setVisibility(View.GONE);

        String amount = user.getStatusesCount() + "";
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

        SpannableStringBuilder sb = new SpannableStringBuilder(getString(R.string.amount_tweets, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tweetAmountTextView.setText(sb);

        amount = user.getFriendsCount() + "";
        sb = new SpannableStringBuilder(getString(R.string.amount_following, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        followingAmountTextView.setText(sb);

        amount = user.getFollowersCount() + "";
        sb = new SpannableStringBuilder(getString(R.string.amount_followers, amount));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        followersAmountTextView.setText(sb);

        tweetAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, UserTimeLineActivity.class);
                i.putExtra(UserTimeLineActivity.TAG_USER_ID, user.getId());
                startActivity(i);
            }
        });

        followingAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUsersDialog(FOLLOWING);
                new LoadFollowersOrFollowing().execute(FOLLOWING, null, null);
            }
        });

        followersAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUsersDialog(FOLLOWERS);
                new LoadFollowersOrFollowing().execute(FOLLOWERS, null, null);
            }
        });

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("NewApi")
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, ImageActivity.class);
                i.putExtra(ImageActivity.TAG_TITLE, user.getName());
                i.putExtra(ImageActivity.TAG_IMAGES, new String[]{user.getOriginalProfileImageURL()});
                startActivity(i);
            }
        });
    }

    void setUpRelationControls() {
        switch (type) {
            case I_FOLLOW_HIM:
                followButton.setText(getString(R.string.unfollow));
                isHeFollowingTextView.setText(getString(R.string.you_are_following, user.getName()));
                break;
            case HE_FOLLOWS_ME:
                followButton.setText(getString(R.string.follow_user));
                isHeFollowingTextView.setText(getString(R.string.you_are_followed, user.getName()));
                break;
            case WE_FOLLOW_EACH_OTHER:
                followButton.setText(getString(R.string.unfollow));
                isHeFollowingTextView.setText(getString(R.string.are_following_ea, user.getName()));
                break;
            case I_DONT_KNOW_WHO_YOU_ARE:
                followButton.setText(getString(R.string.follow_user));
                isHeFollowingTextView.setText(getString(R.string.you_are_not_following, user.getName()));
                break;
            case THIS_IS_ME:
                findViewById(R.id.followTweetLinearLayout).setVisibility(View.GONE);
                isHeFollowingTextView.setText(getString(R.string.you_are_awesome));
                break;
        }

        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, NewTweetActivity.class);
                i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + user.getScreenName());
                startActivity(i);
            }
        });

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFollowDialog();
            }
        });
    }

    void setUpTweets() {
        for (int i = 0; i < statuses.length; i++) {
            final int index = i;

            TWEET_TYPE tweetType = TWEET_TYPE.TYPE_ITEM;
            if (statuses[i].getExtendedMediaEntities().length == 1)
                tweetType = TWEET_TYPE.TYPE_ITEM_PHOTO;
            else if (statuses[i].getExtendedMediaEntities().length > 1)
                tweetType = TWEET_TYPE.TYPE_ITEM_MULTIPLEPHOTOS;
            else if (statuses[i].getQuotedStatusId() > 0)
                tweetType = TWEET_TYPE.TYPE_ITEM_QUOTE;

            switch (tweetType) {
                case TYPE_ITEM:
                    stubs[i].setLayoutResource(R.layout.tweet_basic);
                    break;
                case TYPE_ITEM_PHOTO:
                    stubs[i].setLayoutResource(R.layout.tweet_photo);
                    break;
                case TYPE_ITEM_QUOTE:
                    stubs[i].setLayoutResource(R.layout.tweet_quote);
                    break;
                case TYPE_ITEM_MULTIPLEPHOTOS:
                    stubs[i].setLayoutResource(R.layout.tweet_multiplephotos);
                    break;
            }

            View tweetView = stubs[i].inflate();
            TextView retweetTextView = (TextView) tweetView.findViewById(R.id.retweetTextView);
            TextView userNameTextView = (TextView) tweetView.findViewById(R.id.userNameTextView);
            TextView statusTextView = (TextView) tweetView.findViewById(R.id.statusTextView);
            TextView timeTextView = (TextView) tweetView.findViewById(R.id.timeTextView);
            ImageView userProfilePicImageView = (ImageView) tweetView.findViewById(R.id.userProfilePicImageView);
            final ImageButton favouriteImageButton = (ImageButton) tweetView.findViewById(R.id.favouriteImageButton);
            final ImageButton retweetImageButton = (ImageButton) tweetView.findViewById(R.id.retweetImageButton);
            ImageButton quoteImageButton = (ImageButton) tweetView.findViewById(R.id.quoteImageButton);
            ImageButton respondImageButton = (ImageButton) tweetView.findViewById(R.id.respondImageButton);
            ImageButton shareImageButton = (ImageButton) tweetView.findViewById(R.id.shareImageButton);
            ImageButton openTweetImageButton = (ImageButton) tweetView.findViewById(R.id.openTweetImageButton);
            final LinearLayout interLinearLayout = (LinearLayout) tweetView.findViewById(R.id.interactionLinearLayout);
            FrameLayout cardView = (FrameLayout) tweetView.findViewById(R.id.card_view);

            if (statuses[i].isRetweet()) {
                statuses[i] = statuses[i].getRetweetedStatus();
                retweetTextView.setVisibility(View.VISIBLE);
                retweetTextView.setText(
                        getString(R.string.retweeted_by, statuses[i].getUser().getScreenName()));
            } else retweetTextView.setVisibility(View.GONE);

            userNameTextView.setText(statuses[i].getUser().getName());

            Date d = statuses[i].getCreatedAt();
            Calendar c = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c2.setTime(d);

            long diff = c.getTimeInMillis() - c2.getTimeInMillis();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            if (seconds > 60) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                if (minutes > 60) {
                    long hours = TimeUnit.MILLISECONDS.toHours(diff);
                    if (hours > 24) {
                        if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                            timeTextView.setText(
                                    (new java.text.SimpleDateFormat("MMM dd", Locale.getDefault())).format(d));
                        else
                            timeTextView.setText(
                                    (new java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault())).format(d));
                    } else timeTextView.setText(getString(R.string.mini_hours, (int) hours));
                } else timeTextView.setText(getString(R.string.mini_minutes, (int) minutes));
            } else timeTextView.setText(getString(R.string.mini_seconds, (int) seconds));

            Picasso.with(this)
                    .load(statuses[i].getUser().getBiggerProfileImageURL())
                    .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.placeholder, null))
                    .into(userProfilePicImageView);

            favouriteImageButton.setImageResource((statuses[i].isFavorited()) ?
                    R.drawable.ic_star_outline_black_36dp : R.drawable.ic_star_grey600_36dp);

            retweetImageButton.setImageResource((statuses[i].isRetweeted()) ?
                    R.drawable.ic_repeat_black_36dp : R.drawable.ic_repeat_grey600_36dp);

            favouriteImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (statuses[index].isFavorited()) {
                        new FavoriteTweet(UserProfileActivity.this, twitter).execute(statuses[index].getId(), -1L);
                        favouriteImageButton.setImageResource(R.drawable.ic_star_grey600_36dp);
                    } else {
                        new FavoriteTweet(UserProfileActivity.this, twitter).execute(statuses[index].getId(), 1L);
                        favouriteImageButton.setImageResource(R.drawable.ic_star_outline_black_36dp);
                    }
                }
            });

            userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(UserProfileActivity.this, UserProfileActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserProfileActivity.TAG_USER, statuses[index].getUser());
                    i.putExtras(bundle);

                    startActivity(i);
                }
            });

            retweetImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                    builder.setTitle(getString(R.string.retweet_title))
                            .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new RetweetTweet(UserProfileActivity.this, twitter).execute(statuses[index].getId());
                                    retweetImageButton.setImageResource(R.drawable.ic_repeat_black_36dp);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null).create().show();
                }
            });

            quoteImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(UserProfileActivity.this, NewTweetQuoteActivity.class);

                    Bundle b = new Bundle();
                    b.putSerializable(NewTweetQuoteActivity.PAR_CURRENT_STATUS, statuses[index]);
                    i.putExtra(NewTweetQuoteActivity.PAR_BUNDLE, b);

                    startActivity(i);
                }
            });

            respondImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(UserProfileActivity.this, NewTweetActivity.class);
                    i.putExtra(NewTweetActivity.TAG_USER_PREFIX, "@" + statuses[index].getUser().getScreenName())
                            .putExtra(NewTweetActivity.TAG_REPLY_ID, statuses[index].getId());
                    startActivity(i);
                }
            });

            shareImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" +
                                    statuses[index].getUser().getScreenName() + "/status/" + statuses[index].getId())
                            .setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_tweet)));
                }
            });

            statusTextView.setText(statuses[i].getText());
            interLinearLayout.setVisibility(View.GONE);
            Linkify.addLinks(statusTextView, Linkify.ALL);

            statusTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interLinearLayout.setVisibility(
                            (interLinearLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                }
            });

            if (tweetType == TWEET_TYPE.TYPE_ITEM_PHOTO) {
                ImageView tweetPhotoImageView = (ImageView) tweetView.findViewById(R.id.tweetPhotoImageView);
                final MediaEntity mediaEntity = statuses[i].getMediaEntities()[0];
                if (mediaEntity.getType().equals("photo")) {
                    Picasso.with(this)
                            .load(mediaEntity.getMediaURL())
                            .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.placeholder, null))
                            .fit()
                            .centerCrop()
                            .into(tweetPhotoImageView);

                    tweetPhotoImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(UserProfileActivity.this, ImageActivity.class);
                            i.putExtra(ImageActivity.TAG_IMAGES, new String[]{mediaEntity.getMediaURL()});
                            startActivity(i);
                        }
                    });
                }
            } else if (tweetType == TWEET_TYPE.TYPE_ITEM_MULTIPLEPHOTOS) {
                RecyclerView mRecyclerView = (RecyclerView) tweetView.findViewById(R.id.tweetPhotosRecyclerView);
                mRecyclerView.addItemDecoration(new SpaceLeftItemDecoration(5));
                mRecyclerView.setAdapter(new ImagesAdapter(statuses[i].getExtendedMediaEntities(), UserProfileActivity.this));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));
            } else if (tweetType == TWEET_TYPE.TYPE_ITEM_QUOTE) {
                final Status quotedStatus = statuses[i].getQuotedStatus();

                ImageView photoImageView = ((ImageView) tweetView.findViewById(R.id.photoImageView));
                ((TextView) tweetView.findViewById(R.id.quotedUserNameTextView)).setText(quotedStatus.getUser().getName());
                ((TextView) tweetView.findViewById(R.id.quotedStatusTextView)).setText(quotedStatus.getText());

                if (quotedStatus.getMediaEntities().length > 0) {
                    photoImageView.setVisibility(View.VISIBLE);
                    Picasso.with(UserProfileActivity.this)
                            .load(quotedStatus.getMediaEntities()[0].getMediaURL())
                            .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.placeholder, null))
                            .into(photoImageView);
                } else
                    photoImageView.setVisibility(View.GONE);

                tweetView.findViewById(R.id.quotedStatus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(UserProfileActivity.this, TweetActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable(TweetActivity.TAG_TWEET, quotedStatus);
                        i.putExtra(TweetActivity.TAG_STATUS_BUNDLE, b);
                        startActivity(i);
                    }
                });

            }

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interLinearLayout.setVisibility(
                            (interLinearLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                }
            });

            openTweetImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(UserProfileActivity.this, TweetActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(TweetActivity.TAG_TWEET, statuses[index]);
                    i.putExtra(TweetActivity.TAG_STATUS_BUNDLE, b);
                    startActivity(i);
                }
            });

        }
    }

    void displayFollowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);

        switch (type) {
            case I_FOLLOW_HIM:
                builder.setTitle(getString(R.string.you_are_following, user.getName()))
                        .setMessage(getString(R.string.stop_following, user.getName()))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FollowTwitterUser(UserProfileActivity.this, twitter, false)
                                        .execute(user.getId());
                                followButton.setText(getString(R.string.follow, user.getScreenName()));
                                isHeFollowingTextView.setText(
                                        getString(R.string.you_are_not_following, user.getName()));
                                type = TYPE.I_DONT_KNOW_WHO_YOU_ARE;
                            }
                        });
                break;
            case WE_FOLLOW_EACH_OTHER:
                builder.setTitle(getString(R.string.are_following_ea, user.getName()))
                        .setMessage(getString(R.string.stop_following, user.getName()))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FollowTwitterUser(UserProfileActivity.this, twitter, false)
                                        .execute(user.getId());
                                followButton.setText(getString(R.string.follow, user.getScreenName()));
                                isHeFollowingTextView.setText(
                                        getString(R.string.you_are_followed, user.getName()));
                                type = TYPE.HE_FOLLOWS_ME;
                            }
                        });
                break;
            default:
                builder.setTitle(getString(R.string.follow, user.getName()))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FollowTwitterUser(UserProfileActivity.this, twitter, true)
                                        .execute(user.getId());
                                followButton.setText(getString(R.string.unfollow));
                                isHeFollowingTextView.setText(
                                        getString(R.string.you_are_following, user.getName()));
                                type = (type.equals(TYPE.HE_FOLLOWS_ME)) ?
                                        TYPE.WE_FOLLOW_EACH_OTHER : TYPE.I_FOLLOW_HIM;
                            }
                        });
                break;
        }

        builder.setNegativeButton(getString(R.string.cancel), null).create().show();
    }

    void createUsersDialog(final String mode) {
        mUsersSimpleAdapter = new UserListSimpleAdapter(
                (mode.equals(FOLLOWERS)) ? followers : following, UserProfileActivity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        View dialogView = View.inflate(UserProfileActivity.this, R.layout.dialog_users, null);
        RecyclerView mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.usersRecyclerView);

        final LinearLayoutManager mDialogLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mDialogLinearLayoutManager);
        mRecyclerView.setAdapter(mUsersSimpleAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                dialogVisibleItemCount = mDialogLinearLayoutManager.getChildCount();
                dialogTotalItemCount = mDialogLinearLayoutManager.getItemCount();
                dialogPastVisibleItems = mDialogLinearLayoutManager.findFirstVisibleItemPosition() + 1;

                if (dialogLoading) {
                    if ((dialogVisibleItemCount + dialogPastVisibleItems) >= dialogTotalItemCount) {
                        dialogLoading = false;
                        new LoadFollowersOrFollowing().execute(mode, null, null);
                    }
                }
            }
        });

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, null)
                .create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.check_out, user.getName(), user.getScreenName()))
                    .setType("text/plain");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TAG_USER, user);
        outState.putSerializable(TAG_TYPE, type);
        outState.putSerializable(TAG_ARRAY, statuses);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showSnackBar(String content) {
        Snackbar.make(profileScrollView, content, Snackbar.LENGTH_SHORT).show();
    }

    private enum TWEET_TYPE {TYPE_ITEM, TYPE_ITEM_PHOTO, TYPE_ITEM_MULTIPLEPHOTOS, TYPE_ITEM_QUOTE}

    private enum TYPE {
        I_FOLLOW_HIM, HE_FOLLOWS_ME, WE_FOLLOW_EACH_OTHER, I_DONT_KNOW_WHO_YOU_ARE, THIS_IS_ME
    }

    private class LoadUser extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                if (params[0] != 0) {
                    user = twitter.showUser(params[0]);

                    if (user.getId() == twitter.getId()) {
                        type = TYPE.THIS_IS_ME;
                        return true;
                    }

                    Relationship rel = twitter.showFriendship(twitter.getId(), user.getId());
                    if (rel.isSourceFollowingTarget() && rel.isTargetFollowingSource()) {
                        type = TYPE.WE_FOLLOW_EACH_OTHER;
                    } else if (rel.isTargetFollowingSource()) {
                        type = TYPE.HE_FOLLOWS_ME;
                    } else if (rel.isSourceFollowingTarget()) {
                        type = TYPE.I_FOLLOW_HIM;
                    } else {
                        type = TYPE.I_DONT_KNOW_WHO_YOU_ARE;
                    }
                } else {
                    user = twitter.showUser(twitter.getId());
                    type = TYPE.THIS_IS_ME;
                }

            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean status) {
            if (status) {
                setUpUI();
                setUpRelationControls();
                invalidateOptionsMenu();
                new LoadTweets().execute(null, null, null);
            } else {
                if (!new ConnectionDetector(UserProfileActivity.this).isConnectingToInternet())
                    Toast.makeText(UserProfileActivity.this,
                            getString(R.string.cant_find_user), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(UserProfileActivity.this,
                            getString(R.string.reached_twitter_user_limit), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class LoadUserByName extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                user = twitter.showUser(params[0]);

                Relationship rel = twitter.showFriendship(twitter.getId(), user.getId());
                if (rel.isSourceFollowingTarget() && rel.isTargetFollowingSource()) {
                    type = TYPE.WE_FOLLOW_EACH_OTHER;
                } else if (rel.isTargetFollowingSource()) {
                    type = TYPE.HE_FOLLOWS_ME;
                } else if (rel.isSourceFollowingTarget()) {
                    type = TYPE.I_FOLLOW_HIM;
                } else {
                    type = TYPE.I_DONT_KNOW_WHO_YOU_ARE;
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (status) {
                setUpUI();
                setUpRelationControls();
                invalidateOptionsMenu();
                new LoadTweets().execute(null, null, null);
            } else {
                if (!new ConnectionDetector(UserProfileActivity.this).isConnectingToInternet())
                    Toast.makeText(UserProfileActivity.this,
                            getString(R.string.cant_find_user), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(UserProfileActivity.this,
                            getString(R.string.reached_twitter_user_limit), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class LoadRelationship extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (user.getId() == twitter.getId()) {
                    type = TYPE.THIS_IS_ME;
                } else {
                    Relationship rel = twitter.showFriendship(twitter.getId(), user.getId());
                    if (rel.isSourceFollowingTarget() && rel.isTargetFollowingSource()) {
                        type = TYPE.WE_FOLLOW_EACH_OTHER;
                    } else if (rel.isTargetFollowingSource()) {
                        type = TYPE.HE_FOLLOWS_ME;
                    } else if (rel.isSourceFollowingTarget()) {
                        type = TYPE.I_FOLLOW_HIM;
                    } else {
                        type = TYPE.I_DONT_KNOW_WHO_YOU_ARE;
                    }
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setUpRelationControls();
            invalidateOptionsMenu();
            new LoadTweets().execute(null, null, null);
        }
    }

    private class LoadFollowersOrFollowing extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                PagableResponseList<User> usersResponse;

                if (params[0].equals(FOLLOWERS)) {
                    usersResponse = twitter.getFollowersList(user.getScreenName(), cursor);
                    followers.addAll(usersResponse);
                } else {
                    usersResponse = twitter.getFriendsList(user.getScreenName(), cursor);
                    following.addAll(usersResponse);
                }

                cursor = usersResponse.getNextCursor();
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean status) {
            if (status) {
                mUsersSimpleAdapter.notifyDataSetChanged();
                dialogLoading = true;
            } else {
                Toast.makeText(UserProfileActivity.this, getString(R.string.cant_load_user),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadTweets extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {

            try {
                ResponseList<twitter4j.Status> mList =
                        twitter.getUserTimeline(user.getScreenName(), new Paging(1, 3));
                statuses = new twitter4j.Status[mList.size()];
                for (int i = 0; i < mList.size(); i++)
                    statuses[i] = mList.get(i);
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean status) {
            if (status) setUpTweets();
        }
    }
}
