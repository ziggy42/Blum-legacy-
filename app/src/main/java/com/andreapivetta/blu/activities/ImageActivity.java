package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.andreapivetta.blu.R;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends AppCompatActivity {

    public static final String TAG_IMAGE = "IMAGE";
    public static final String TAG_TITLE = "title";

    private PhotoViewAttacher attacher;
    private String imageURL, toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageURL = getIntent().getStringExtra(TAG_IMAGE);
        toolbarTitle = getIntent().getStringExtra(TAG_TITLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            if(toolbarTitle != null)
                toolbar.setTitle(toolbarTitle);

            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        final ImageView tweetImageView = (ImageView) findViewById(R.id.tweetImageView);
        Picasso.with(this)
                .load(imageURL)
                .into(tweetImageView);

        tweetImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (attacher == null)
                    attacher = new PhotoViewAttacher(tweetImageView);

                return true;
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.check_out_photo, imageURL))
                    .setType("text/plain");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
