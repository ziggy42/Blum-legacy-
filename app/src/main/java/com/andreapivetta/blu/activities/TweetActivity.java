package com.andreapivetta.blu.activities;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetActivity extends ActionBarActivity {

    protected static final int REQUEST_GRAB_IMAGE = 3;
    protected static final int REQUEST_TAKE_PHOTO = 1;
    protected boolean isUp = true;
    private Twitter twitter;
    private Status status;
    private ArrayList<Status> mDataSet = new ArrayList<>();
    private TweetsListAdapter mTweetsAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageButton replyImageButton;
    private String mCurrentPhotoPath;
    private File imageFile;
    private ImageView uploadedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

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

        this.twitter = TwitterUtils.getTwitter(TweetActivity.this);

        mRecyclerView = (RecyclerView) findViewById(R.id.tweetsRecyclerView);
        mTweetsAdapter = new TweetsListAdapter(mDataSet, this, twitter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    if (isUp)
                        newTweetDown();
                } else {
                    if (!isUp)
                        newTweetUp();
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
            }
        });

        replyImageButton = (ImageButton) findViewById(R.id.replyImageButton);
        replyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TweetActivity.this, NewTweetActivity.class);
                i.putExtra("USER_PREFIX", "@" + status.getUser().getScreenName());
                startActivity(i);
            }
        });

        new LoadStatus().execute(null, null, null);
    }

    void newTweetDown() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) replyImageButton.getLayoutParams();
        ValueAnimator downAnimator = ValueAnimator.ofInt(params.bottomMargin, -replyImageButton.getHeight());
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                replyImageButton.requestLayout();
            }
        });
        downAnimator.setDuration(200);
        downAnimator.start();

        isUp = false;
    }

    void newTweetUp() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) replyImageButton.getLayoutParams();
        ValueAnimator upAnimator = ValueAnimator.ofInt(params.bottomMargin, dpToPx(20));
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                replyImageButton.requestLayout();
            }
        });
        upAnimator.setDuration(200);
        upAnimator.start();

        isUp = true;
    }

    int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQUEST_GRAB_IMAGE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = imageReturnedIntent.getData();
                        imageFile = new File(getRealPathFromURI(selectedImage));
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        uploadedImageView.setVisibility(View.VISIBLE);
                        uploadedImageView.setImageBitmap(BitmapFactory.decodeStream(imageStream));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    uploadedImageView.setVisibility(View.VISIBLE);

                    Picasso.with(this)
                            .load(Uri.parse(mCurrentPhotoPath))
                            .into(uploadedImageView);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(TweetActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadStatus extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                status = twitter.showStatus(getIntent().getLongExtra("STATUS", (long) 0));
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mDataSet.add(status);
                new LoadResponses().execute(null, null, null);
            }
        }
    }

    private class LoadResponses extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                QueryResult result = twitter.search(new Query("to:" + status.getUser().getScreenName()));
                for (twitter4j.Status tmpStatus : result.getTweets()) {
                    if (status.getId() == tmpStatus.getInReplyToStatusId())
                        mDataSet.add(tmpStatus);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mTweetsAdapter.notifyDataSetChanged();
            }
        }
    }

}
