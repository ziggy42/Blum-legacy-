package com.andreapivetta.blu.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
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

    private static final int REQUEST_GRAB_IMAGE = 3;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private ImageView uploadedImageView;
    private EditText newTweetEditText;

    private File imageFile;
    private Twitter twitter;
    private Intent intent;

    private int charsLeft;

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
        newTweetEditText = (EditText) findViewById(R.id.newTweetEditText);
        uploadedImageView = (ImageView) findViewById(R.id.uploadedImageView);
        ImageButton takePhotoImageButton = (ImageButton) findViewById(R.id.takePhotoImageButton);
        ImageButton grabImageImageButton = (ImageButton) findViewById(R.id.grabImageImageButton);

        intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                newTweetEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            } else if (type.startsWith("image/")) {
                Uri selectedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                imageFile = new File(FileUtils.getPath(NewTweetActivity.this, selectedImageUri));

                uploadedImageView.setVisibility(View.VISIBLE);
                Picasso.with(NewTweetActivity.this)
                        .load(selectedImageUri)
                        .into(uploadedImageView);
            }
        } /*else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {

            }
        }*/

        if (intent.getStringExtra("USER_PREFIX") != null &&
                intent.getStringExtra("USER_PREFIX").length() > 0)
            newTweetEditText.setText(intent.getStringExtra("USER_PREFIX") + " ");

        newTweetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charsLeft = (140 - s.length());
                invalidateOptionsMenu();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        newTweetEditText.setSelection(newTweetEditText.getText().length());

        charsLeft = 140 - newTweetEditText.getText().length();
        invalidateOptionsMenu();

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
        @SuppressLint("SimpleDateFormat") String imageFileName =
                "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

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
                            .load(Uri.parse("file:" + imageFile.getAbsolutePath()))
                            .into(uploadedImageView);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_new_tweet, menu);

        MenuItem item = menu.findItem(R.id.action_chars_left);
        MenuItemCompat.setActionView(item, R.layout.menu_chars_left);
        View view = MenuItemCompat.getActionView(item);
        TextView charsLeftTextView = (TextView) view.findViewById(R.id.charsLeftTextView);
        charsLeftTextView.setText(String.valueOf(charsLeft));

        if (charsLeft < 0)
            charsLeftTextView.setTextColor(getResources().getColor(R.color.red));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            if (charsLeft < 0) {
                (new AlertDialog.Builder(NewTweetActivity.this)).setTitle(R.string.too_many_characters)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
            } else {
                if (uploadedImageView.getVisibility() == View.VISIBLE)
                    new UpdateTwitterStatus(NewTweetActivity.this, twitter, imageFile,
                            intent.getLongExtra("REPLY_ID", (long) -1))
                            .execute(newTweetEditText.getText().toString());
                else
                    new UpdateTwitterStatus(NewTweetActivity.this, twitter,
                            intent.getLongExtra("REPLY_ID", (long) -1))
                            .execute(newTweetEditText.getText().toString());

                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
