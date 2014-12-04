package com.andreapivetta.blu.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.TweetListAdapter;
import com.andreapivetta.blu.adapters.UserListAdapter;
import com.andreapivetta.blu.twitter.FollowTwitterUser;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;


public class UserActivity extends ActionBarActivity {

    private final static String FOLLOWERS = "NJFHWI";
    private final static String FOLLOWING = "HIJHOJOJO";
    private final static int I_FOLLOW_HIM = 0;
    private final static int WE_FOLLOW_EACH_OTHER = 2;
    private final static int I_DONT_KNOW_WHO_YOU_ARE = -1;
    private int STATUS;

    private Twitter twitter;
    private User user;

    private ArrayList<User> followers = new ArrayList<>(), following = new ArrayList<>();
    private UserListAdapter mUsersAdapter;
    private LinearLayoutManager mDialogLinearLayoutManager;
    private boolean dialogLoading = true;
    private int dialogPastVisibleItems, dialogVisibleItemCount, dialogTotalItemCount;
    private long cursor = -1;

    private ImageView profileBackgroundImageView, profilePictureImageView;
    private TextView userNickTextView, userNameTextView, descriptionTextView, userLocationTextView,
            userWebsiteTextView, tweetAmountTextView, followingAmountTextView, followersAmountTextView;
    private ImageButton followImageButton;
    private TweetListAdapter mTweetsAdapter;
    private ArrayList<Status> userTweetList = new ArrayList<>();
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private LinearLayoutManager mLinearLayoutManager;
    private Paging paging = new Paging(1, 100);
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        twitter = TwitterUtils.getTwitter(UserActivity.this);
        profileBackgroundImageView = (ImageView) findViewById(R.id.profileBackgroundImageView);
        profilePictureImageView = (ImageView) findViewById(R.id.profilePictureImageView);
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        userNickTextView = (TextView) findViewById(R.id.userNickTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        userLocationTextView = (TextView) findViewById(R.id.userLocationTextView);
        userWebsiteTextView = (TextView) findViewById(R.id.userWebsiteTextView);
        tweetAmountTextView = (TextView) findViewById(R.id.tweetAmountTextView);
        followingAmountTextView = (TextView) findViewById(R.id.followingAmountTextView);
        followersAmountTextView = (TextView) findViewById(R.id.followersAmountTextView);
        followImageButton = (ImageButton) findViewById(R.id.followImageButton);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.userTweetsRecyclerView);
        mTweetsAdapter = new TweetListAdapter(userTweetList, this, twitter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mLinearLayoutManager.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition() + 1;

                if (loading) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        new GetTimeLine().execute(null, null, null);
                    }
                }
            }
        });

        new LoadUser().execute(getIntent().getLongExtra("ID", 0));
    }

    void setUpUI() {
        Picasso.with(UserActivity.this)
                .load(user.getProfileBannerURL())
                .placeholder(getResources().getDrawable(R.drawable.placeholder_banner))
                .into(profileBackgroundImageView, new Callback() {
                    @Override
                    @TargetApi(21)
                    public void onSuccess() {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            //Bitmap onePixelBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) profileBackgroundImageView.getDrawable()).getBitmap(), 1, 1, true);
                            //int pixel = onePixelBitmap.getPixel(0, 0);
                            //getWindow().setStatusBarColor(Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel)));

                            Palette.generateAsync(((BitmapDrawable) profileBackgroundImageView.getDrawable()).getBitmap(),
                                    new Palette.PaletteAsyncListener() {
                                        public void onGenerated(Palette palette) {
                                            int defaultColor = getResources().getColor(R.color.colorAccent);
                                            getWindow().setStatusBarColor(palette.getLightVibrantColor(defaultColor));
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        Picasso.with(UserActivity.this)
                .load(user.getOriginalProfileImageURL()).placeholder(R.drawable.placeholder)
                .into(profilePictureImageView);

        userNameTextView.setText(user.getName());
        userNickTextView.setText("@" + user.getScreenName());
        descriptionTextView.setText(user.getDescription());

        String location = user.getLocation();
        if (location.length() != 0)
            userLocationTextView.setText(location);
        else
            userLocationTextView.setVisibility(View.GONE);

        String website = user.getURLEntity().getDisplayURL();
        if (website.length() != 0)
            userWebsiteTextView.setText(website);
        else
            userWebsiteTextView.setVisibility(View.GONE);

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // TODO REMOVE AND CHANGE API MIN
            switch (STATUS) {
                case I_FOLLOW_HIM:
                    followImageButton.setBackground(getResources().getDrawable(R.drawable.circle_button_blue));
                    followImageButton.setImageResource(R.drawable.ic_person_add_white_24dp);
                    break;
                case WE_FOLLOW_EACH_OTHER:
                    followImageButton.setBackground(getResources().getDrawable(R.drawable.circle_button_green));
                    followImageButton.setImageResource(R.drawable.ic_person_add_white_24dp);
                    break;
                default:
                    break;
            }
        }


        followImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);

                switch (STATUS) {
                    case I_FOLLOW_HIM:
                        builder.setTitle(getString(R.string.you_are_following))
                                .setMessage(getString(R.string.stop_following) + " " + user.getName() + "?")
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new FollowTwitterUser(UserActivity.this, twitter, false).execute(user.getId());
                                    }
                                });
                        break;
                    case WE_FOLLOW_EACH_OTHER:
                        builder.setTitle(getString(R.string.you_and) + " " + user.getName() + " " + getString(R.string.are_following_ea))
                                .setMessage(getString(R.string.stop_following) + " " + user.getName() + "?")
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new FollowTwitterUser(UserActivity.this, twitter, false).execute(user.getId());
                                    }
                                });
                        break;
                    default:
                        builder.setTitle(getString(R.string.follow) + " " +  user.getName() + "?")
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new FollowTwitterUser(UserActivity.this, twitter, true).execute(user.getId());
                                    }
                                });
                        break;
                }

                builder.setNegativeButton(getString(R.string.cancel), null).create().show();
            }
        });

        String amount = user.getStatusesCount() + "";
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);

        SpannableStringBuilder sb = new SpannableStringBuilder(amount + "\n" + getString(R.string.tweets));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tweetAmountTextView.setText(sb);

        amount = user.getFriendsCount() + "";
        sb = new SpannableStringBuilder(amount + "\n" + getString(R.string.following));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        followingAmountTextView.setText(sb);

        amount = user.getFollowersCount() + "";
        sb = new SpannableStringBuilder(amount + "\n" + getString(R.string.followers));
        sb.setSpan(b, 0, amount.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        followersAmountTextView.setText(sb);

        tweetAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserActivity.this, UserTimeLineActivity.class);
                i.putExtra("ID", user.getId())
                        .putExtra("Twitter", twitter);
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
    }

    void createUsersDialog(final String mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        View dialogView = View.inflate(UserActivity.this, R.layout.dialog_users, null);
        RecyclerView mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.usersRecyclerView);

        if (mode.equals(FOLLOWERS)) mUsersAdapter = new UserListAdapter(followers, UserActivity.this, twitter);
        else mUsersAdapter = new UserListAdapter(following, UserActivity.this, twitter);

        mDialogLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mDialogLinearLayoutManager);
        mRecyclerView.setAdapter(mUsersAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
        getMenuInflater().inflate(R.menu.menu_user, menu);

        if (user != null) {
            ShareActionProvider mShareActionProvider = (ShareActionProvider)
                    MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
            mShareActionProvider.setShareIntent(getDefaultIntent());
        }

        return true;
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.check_out) + " " + user.getName() + " " + getString(R.string.on_twitter) + " http://twitter.com/" + user.getScreenName());
        intent.setType("text/plain");
        return intent;
    }

    private class LoadUser extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                user = twitter.showUser(params[0]);

                Relationship rel = twitter.showFriendship(twitter.getId(), user.getId());
                if(rel.isSourceFollowingTarget() && rel.isTargetFollowingSource()) {
                    STATUS = WE_FOLLOW_EACH_OTHER;
                } else if (rel.isSourceFollowingTarget()) {
                    STATUS = I_FOLLOW_HIM;
                } else {
                    STATUS = I_DONT_KNOW_WHO_YOU_ARE;
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
                invalidateOptionsMenu();
                new GetTimeLine().execute(null, null, null);
            } else {
                Toast.makeText(UserActivity.this, "Can't find this user", Toast.LENGTH_SHORT).show();
                finish();
            }
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
                mUsersAdapter.notifyDataSetChanged();
                dialogLoading = true;
            } else {
                Toast.makeText(UserActivity.this, "Can't load users", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetTimeLine extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... uris) {
            try {
                paging.setPage(currentPage);
                for (twitter4j.Status status : twitter.getUserTimeline(user.getScreenName(), paging))
                    userTweetList.add(status);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mTweetsAdapter.notifyDataSetChanged();
                currentPage += 1;
            }

            loading = true;
        }
    }
}
