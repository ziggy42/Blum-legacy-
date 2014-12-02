package com.andreapivetta.blu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.blu.adapters.UserListAdapter;
import com.andreapivetta.blu.twitter.FollowTwitterUser;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.Relationship;
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
    private LinearLayoutManager mLinearLayoutManager;
    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private long cursor = -1;

    private ImageView profileBackgroundImageView, profilePictureImageView;
    private TextView userNickTextView, userNameTextView, descriptionTextView, userLocationTextView,
            userWebsiteTextView, tweetAmountTextView, followingAmountTextView, followersAmountTextView;
    private ImageButton followImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        new LoadUser().execute(getIntent().getLongExtra("ID", 0));

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
    }

    void setUpUI() {
        Picasso.with(UserActivity.this)
                .load(user.getProfileBannerURL())
                .placeholder(getResources().getDrawable(R.drawable.placeholder_banner))
                .into(profileBackgroundImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap onePixelBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) profileBackgroundImageView.getDrawable()).getBitmap(), 1, 1, true);
                        int pixel = onePixelBitmap.getPixel(0, 0);

                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            getWindow().setStatusBarColor(Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel)));
                    }

                    @Override
                    public void onError() {

                    }
                });

        Picasso.with(UserActivity.this)
                .load(user.getOriginalProfileImageURL())
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

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mUsersAdapter);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                loading = true;
            } else {
                Toast.makeText(UserActivity.this, "Can't load users", Toast.LENGTH_SHORT).show();
            }
        }
    }
}