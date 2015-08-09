package com.andreapivetta.blu.adapters.holders;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.ImageActivity;
import com.andreapivetta.blu.adapters.UserListSimpleAdapter;
import com.andreapivetta.blu.twitter.FollowTwitterUser;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserHeaderViewHolder extends RecyclerView.ViewHolder {

    private final static int FOLLOWERS = 0;
    private final static int FOLLOWING = 1;

    private ArrayList<User> followers = new ArrayList<>(), following = new ArrayList<>();
    private UserListSimpleAdapter mUsersSimpleAdapter;
    private long cursor = -1;
    private boolean dialogLoading = true;

    private static final int FOLLOW = 0;
    private static final int NOT_FOLLOW = 1;
    private int type = -1;

    private ImageView profilePictureImageView;
    private Button shareUserButton, followUserButton;
    private TextView userNameTextView, userNickTextView, descriptionTextView, userLocationTextView,
            userWebsiteTextView, followingStatsTextView, followersStatsTextView;

    public UserHeaderViewHolder(View container) {
        super(container);

        this.userNameTextView = (TextView) container.findViewById(R.id.userNameTextView);
        this.userNickTextView = (TextView) container.findViewById(R.id.userNickTextView);
        this.descriptionTextView = (TextView) container.findViewById(R.id.descriptionTextView);
        this.userLocationTextView = (TextView) container.findViewById(R.id.userLocationTextView);
        this.userWebsiteTextView = (TextView) container.findViewById(R.id.userWebsiteTextView);
        this.profilePictureImageView = (ImageView) container.findViewById(R.id.userProfilePicImageView);
        this.shareUserButton = (Button) container.findViewById(R.id.shareUserButton);
        this.followUserButton = (Button) container.findViewById(R.id.followUserButton);
        this.followingStatsTextView = (TextView) container.findViewById(R.id.followingStatsTextView);
        this.followersStatsTextView = (TextView) container.findViewById(R.id.followersStatsTextView);
    }

    public void setup(final User user, final Context context, final Twitter twitter) {

        if (type < 0)
            new LoadRelationship(twitter, user, context).execute(null, null, null);

        Glide.with(context)
                .load(user.getOriginalProfileImageURL())
                .placeholder(R.drawable.placeholder)
                .into(profilePictureImageView);

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ImageActivity.class);
                i.putExtra(ImageActivity.TAG_IMAGES, new String[]{user.getOriginalProfileImageURL()});
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) context, profilePictureImageView,
                                    context.getString(R.string.image_transition));
                    context.startActivity(i, options.toBundle());
                } else {
                    context.startActivity(i);
                }
            }
        });

        userNameTextView.setText(user.getName());
        if (user.isVerified())
            userNameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user_light_blue_500_18dp, 0);

        userNickTextView.setText("@" + user.getScreenName());
        descriptionTextView.setText(user.getDescription());
        Linkify.addLinks(descriptionTextView, Linkify.ALL);

        String location = user.getLocation();
        if (location.length() != 0) userLocationTextView.setText(location);
        else userLocationTextView.setVisibility(View.GONE);

        String website = user.getURLEntity().getDisplayURL();
        if (website.length() != 0) userWebsiteTextView.setText(website);
        else userWebsiteTextView.setVisibility(View.GONE);

        followingStatsTextView.setText(Html.fromHtml(context.getString(R.string.amount_following, getCount(user.getFriendsCount()))));
        followersStatsTextView.setText(Html.fromHtml(context.getString(R.string.amount_followers, getCount(user.getFollowersCount()))));

        followingStatsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUsersDialog(FOLLOWING, context, twitter, user);
                new LoadFollowersOrFollowing(twitter, user).execute(FOLLOWING, null, null);
            }
        });

        followersStatsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUsersDialog(FOLLOWERS, context, twitter, user);
                new LoadFollowersOrFollowing(twitter, user).execute(FOLLOWERS, null, null);
            }
        });

        shareUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.check_out, user.getName(), user.getScreenName()))
                        .setType("text/plain");
                context.startActivity(intent);
            }
        });

        if (type < 0)
            followUserButton.setEnabled(false);
        else
            setUpFollowButton(user, context, twitter);
    }

    void setUpFollowButton(final User user, final Context context, final Twitter twitter) {
        followUserButton.setEnabled(true);
        followUserButton.setText((type == FOLLOW) ? context.getString(R.string.unfollow) :
                context.getString(R.string.follow_simple));
        followUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (type == FOLLOW) {
                    builder.setTitle(context.getString(R.string.you_are_following, user.getName()))
                            .setMessage(context.getString(R.string.stop_following, user.getName()))
                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new FollowTwitterUser(context, twitter, false)
                                            .execute(user.getId());
                                    type = NOT_FOLLOW;
                                    followUserButton.setText(context.getString(R.string.follow_simple));
                                }
                            });
                } else {
                    builder.setTitle(context.getString(R.string.follow, user.getName()))
                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new FollowTwitterUser(context, twitter, false)
                                            .execute(user.getId());
                                    type = FOLLOW;
                                    followUserButton.setText(context.getString(R.string.unfollow));
                                }
                            });
                }
                builder.create().show();
            }
        });
    }

    private String getCount(int amount) {
        if (amount < 1000) return String.valueOf(amount);

        return amount / 1000 + "k";
    }

    void createUsersDialog(final int mode, Context context, final Twitter twitter, final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (mode == FOLLOWERS) {
            mUsersSimpleAdapter = new UserListSimpleAdapter(followers, context);
            builder.setTitle(context.getString(R.string.followers));
        } else {
            mUsersSimpleAdapter = new UserListSimpleAdapter(following, context);
            builder.setTitle(context.getString(R.string.following));
        }

        View dialogView = View.inflate(context, R.layout.dialog_users, null);
        RecyclerView mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.usersRecyclerView);

        final LinearLayoutManager mDialogLinearLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mDialogLinearLayoutManager);
        mRecyclerView.setAdapter(mUsersSimpleAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dialogLoading) {
                    if ((mDialogLinearLayoutManager.getChildCount() + (mDialogLinearLayoutManager.findFirstVisibleItemPosition() + 1))
                            >= mDialogLinearLayoutManager.getItemCount() - 5) {
                        dialogLoading = false;
                        new LoadFollowersOrFollowing(twitter, user).execute(mode, null, null);
                    }
                }
            }
        });

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, null)
                .create().show();
    }

    private class LoadRelationship extends AsyncTask<Void, Void, Boolean> {

        private Twitter twitter;
        private User user;
        private Context context;

        public LoadRelationship(Twitter twitter, User user, Context context) {
            this.twitter = twitter;
            this.user = user;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (twitter.getId() == user.getId())
                    return false;

                Relationship rel = twitter.showFriendship(twitter.getId(), user.getId());
                type = rel.isSourceFollowingTarget() ? FOLLOW : NOT_FOLLOW;
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setUpFollowButton(user, context, twitter);
            } else {
                followUserButton.setVisibility(View.GONE);
            }
        }
    }

    private class LoadFollowersOrFollowing extends AsyncTask<Integer, Void, Boolean> {

        private Twitter twitter;
        private User user;

        public LoadFollowersOrFollowing(Twitter twitter, User user) {
            this.twitter = twitter;
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                PagableResponseList<User> usersResponse;

                if (params[0] == FOLLOWERS) {
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
            }
        }
    }
}
