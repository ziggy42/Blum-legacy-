package com.andreapivetta.blu.activities;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.andreapivetta.blu.utilities.Common;
import com.andreapivetta.blu.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.Twitter;

public class NewTweetActivity extends ThemedActivity {

    private static final int MAX_URL_LENGTH = 23; // it will change

    public static final String TAG_USER_PREFIX = "userPref";
    public static final String TAG_REPLY_ID = "replyId";

    private static final int REQUEST_GRAB_IMAGE = 3;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String FILES = "file";

    private ArrayList<File> imageFiles = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DeletableImageAdapter mAdapter;
    private Twitter twitter;
    private Intent intent;

    private EditText newTweetEditText;

    private int charsLeft;
    private File imageFile;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (savedInstanceState != null) {
            imageFiles = (ArrayList<File>) savedInstanceState.getSerializable(FILES);
        }

        twitter = TwitterUtils.getTwitter(NewTweetActivity.this);
        newTweetEditText = (EditText) findViewById(R.id.newTweetEditText);
        ImageButton takePhotoImageButton = (ImageButton) findViewById(R.id.takePhotoImageButton);
        ImageButton grabImageImageButton = (ImageButton) findViewById(R.id.grabImageImageButton);
        mRecyclerView = (RecyclerView) findViewById(R.id.photosRecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(NewTweetActivity.this, 2));
        mAdapter = new DeletableImageAdapter();
        mRecyclerView.setAdapter(mAdapter);

        intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                newTweetEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            } else if (type.startsWith("image/")) {
                Uri selectedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                imageFiles.add(new File(FileUtils.getPath(NewTweetActivity.this, selectedImageUri)));
                mRecyclerView.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageUris != null) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < imageUris.size() && i < 4; i++)
                        imageFiles.add(new File(FileUtils.getPath(NewTweetActivity.this, imageUris.get(i))));
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        if (intent.getStringExtra(TAG_USER_PREFIX) != null &&
                intent.getStringExtra(TAG_USER_PREFIX).length() > 0)
            newTweetEditText.setText(intent.getStringExtra(TAG_USER_PREFIX) + " ");

        newTweetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkLength(s.toString());
            }
        });

        newTweetEditText.setSelection(newTweetEditText.getText().length());
        checkLength(newTweetEditText.getText().toString());

        takePhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageFiles.size() < 4) {
                    if (ContextCompat.checkSelfPermission(NewTweetActivity.this, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        takePicture();
                    } else {
                        ActivityCompat.requestPermissions(NewTweetActivity.this,
                                new String[]{permission.WRITE_EXTERNAL_STORAGE}, REQUEST_TAKE_PHOTO);
                    }
                } else {
                    showTooMuchImagesToast();
                }
            }
        });

        grabImageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewTweetActivity.this, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    grabImage();
                else
                    ActivityCompat.requestPermissions(NewTweetActivity.this,
                            new String[]{permission.READ_EXTERNAL_STORAGE}, REQUEST_TAKE_PHOTO);
            }
        });
    }

    void takePicture() {
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

    void grabImage() {
        if (imageFiles.size() < 4) {
            Intent intent = new Intent()
                    .setType("image/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), REQUEST_GRAB_IMAGE);
        } else {
            showTooMuchImagesToast();
        }
    }

    void checkLength(String text) {
        int wordsLength = 0;
        int urls = (imageFiles.size() > 0) ? 1 : 0;

        for (String entry : text.split(" ")) {
            if (Patterns.WEB_URL.matcher(entry).matches() && entry.length() > MAX_URL_LENGTH)
                urls++;
            else
                wordsLength += entry.length();
        }

        int spaces = text.length() - text.replace(" ", "").length();
        charsLeft = (140 - urls * MAX_URL_LENGTH) - spaces - wordsLength;
        invalidateOptionsMenu();
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
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (imageReturnedIntent.getData() != null) {
                        imageFiles.add(new File(FileUtils.getPath(NewTweetActivity.this, imageReturnedIntent.getData())));
                        mAdapter.notifyItemInserted(imageFiles.size() - 1);
                    } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                            && imageReturnedIntent.getClipData() != null) {
                        ClipData mClipData = imageReturnedIntent.getClipData();
                        for (int i = 0; i < mClipData.getItemCount() && i < 4; i++)
                            imageFiles.add(new File(FileUtils.getPath(NewTweetActivity.this, mClipData.getItemAt(i).getUri())));
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(NewTweetActivity.this, getString(R.string.operation_not_supported), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    imageFiles.add(imageFile);
                    mAdapter.notifyItemInserted(imageFiles.size() - 1);
                }
                break;
        }

        checkLength(newTweetEditText.getText().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Snackbar.make(getWindow().getDecorView().findViewById(R.id.linearLayout), R.string.no_photos,
                        Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_GRAB_IMAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                grabImage();
            } else {
                Snackbar.make(getWindow().getDecorView().findViewById(R.id.linearLayout), R.string.no_grab_images,
                        Snackbar.LENGTH_LONG).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showTooMuchImagesToast() {
        Toast.makeText(NewTweetActivity.this, getString(R.string.too_many_images), Toast.LENGTH_SHORT).show();
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
            charsLeftTextView.setTextColor(getColor(R.color.red));

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
            } else if (charsLeft != 140) {
                if (imageFiles.size() > 0) {
                    new UpdateTwitterStatus(NewTweetActivity.this, twitter,
                            intent.getLongExtra(TAG_REPLY_ID, -1L), imageFiles)
                            .execute(newTweetEditText.getText().toString());
                } else {
                    new UpdateTwitterStatus(NewTweetActivity.this, twitter,
                            intent.getLongExtra(TAG_REPLY_ID, -1L))
                            .execute(newTweetEditText.getText().toString());
                }

                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(FILES, imageFiles);
        super.onSaveInstanceState(outState);
    }

    private class DeletableImageAdapter extends RecyclerView.Adapter<DeletableImageAdapter.VHItem> {

        @Override
        public VHItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_deletable, parent, false);

            return new VHItem(v);
        }

        @Override
        public void onBindViewHolder(VHItem holder, final int position) {
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(imageFiles.get(position).getAbsolutePath()),
                    Common.dpToPx(NewTweetActivity.this, 200), Common.dpToPx(NewTweetActivity.this, 200));
            holder.photoImageView.setImageBitmap(thumbImage);
        }

        @Override
        public int getItemCount() {
            return imageFiles.size();
        }

        public class VHItem extends RecyclerView.ViewHolder {
            public ImageView photoImageView;
            public ImageButton deleteButton;

            public VHItem(View container) {
                super(container);

                this.photoImageView = (ImageView) container.findViewById(R.id.tweetPhotoImageView);
                this.deleteButton = (ImageButton) container.findViewById(R.id.deleteButton);
                this.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageFiles.remove(getAdapterPosition());
                        DeletableImageAdapter.this.notifyItemRemoved(getAdapterPosition());
                        checkLength(newTweetEditText.getText().toString());
                    }
                });
            }
        }
    }
}
