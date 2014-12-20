package com.andreapivetta.blu.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.andreapivetta.blu.utilities.FileUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.Twitter;

public class NewTweetActivity extends ActionBarActivity {

    protected static final int REQUEST_GRAB_IMAGE = 3;
    protected static final int REQUEST_TAKE_PHOTO = 1;

    private TextView charsLeftTextView;
    private ImageView uploadedImageView;
    private EditText newTweetEditText;

    private String mCurrentPhotoPath, userPrefix;
    private File imageFile;
    private Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);

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

        twitter = TwitterUtils.getTwitter(NewTweetActivity.this);
        userPrefix = getIntent().getStringExtra("USER_PREFIX");

        newTweetEditText = (EditText) findViewById(R.id.newTweetEditText);
        charsLeftTextView = (TextView) findViewById(R.id.charsLeftTextView);
        uploadedImageView = (ImageView) findViewById(R.id.uploadedImageView);
        ImageButton takePhotoImageButton = (ImageButton) findViewById(R.id.takePhotoImageButton);
        ImageButton grabImageImageButton = (ImageButton) findViewById(R.id.grabimageImageButton);

        newTweetEditText.setText(userPrefix);

        newTweetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int i = (140 - s.length());
                charsLeftTextView.setText(i + "");
                if (i < 0)
                    charsLeftTextView.setTextColor(getResources().getColor(R.color.red));
                else
                    charsLeftTextView.setTextColor(getResources().getColor(R.color.grey));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        takePhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        grabImageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GRAB_IMAGE);

            }
        });

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

                    uploadedImageView.setVisibility(View.VISIBLE);
                    Picasso.with(NewTweetActivity.this)
                            .load(imageReturnedIntent.getData())
                            .into(uploadedImageView);

                    imageFile = new File(FileUtils.getPath(NewTweetActivity.this, imageReturnedIntent.getData()));
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
        getMenuInflater().inflate(R.menu.menu_new_tweet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            if (Integer.parseInt(charsLeftTextView.getText().toString()) < 0) {
                (new AlertDialog.Builder(NewTweetActivity.this)).setTitle(R.string.too_many_characters)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
            } else {
                if (uploadedImageView.getVisibility() == View.VISIBLE)
                    new UpdateTwitterStatus(NewTweetActivity.this, twitter, imageFile)
                            .execute(newTweetEditText.getText().toString());
                else
                    new UpdateTwitterStatus(NewTweetActivity.this, twitter)
                            .execute(newTweetEditText.getText().toString());

                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
