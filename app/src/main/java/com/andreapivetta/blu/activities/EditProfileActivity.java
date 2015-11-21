package com.andreapivetta.blu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class EditProfileActivity extends ThemedActivity {

    private static final String TAG_USER = "user";

    private Twitter twitter;
    private User user;
    private EditText screenNameEditText, descriptionEditText, locationEditText, websiteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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

        twitter = TwitterUtils.getTwitter(EditProfileActivity.this);
        screenNameEditText = (EditText) findViewById(R.id.screenNameEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        locationEditText = (EditText) findViewById(R.id.locationEditText);
        websiteEditText = (EditText) findViewById(R.id.websiteEditText);

        if (savedInstanceState != null) {
            user = (User) savedInstanceState.getSerializable(TAG_USER);
            setUpUI();
        } else {
            new LoadLoggedUser().execute();
        }

    }

    private void setUpUI() {
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        findViewById(R.id.editScrollView).setVisibility(View.VISIBLE);
        screenNameEditText.setText(user.getScreenName());
        descriptionEditText.setText(user.getDescription());
        locationEditText.setText(user.getLocation());
        websiteEditText.setText(user.getURLEntity().getDisplayURL());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            new UploadChanges().execute(screenNameEditText.getText().toString(), websiteEditText.getText().toString(),
                    locationEditText.getText().toString(), descriptionEditText.getText().toString());
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TAG_USER, user);
        super.onSaveInstanceState(outState);
    }

    private class LoadLoggedUser extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                user = twitter.showUser(twitter.getId());
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                setUpUI();
            }
        }
    }

    private class UploadChanges extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                twitter.updateProfile(params[0], params[1], params[2], params[3]);
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.action_not_performed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
