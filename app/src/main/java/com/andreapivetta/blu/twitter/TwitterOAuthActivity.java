package com.andreapivetta.blu.twitter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.andreapivetta.blu.twitter.TwitterKs.TWITTER_CONSUMER_KEY;
import static com.andreapivetta.blu.twitter.TwitterKs.TWITTER_CONSUMER_SECRET;

public class TwitterOAuthActivity extends Activity implements TwitterOAuthView.Listener {

    private TwitterOAuthView view;
    private boolean oauthStarted;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an instance of TwitterOAuthView.
        view = new TwitterOAuthView(this);
        view.setDebugEnabled(true);

        setContentView(view);

        oauthStarted = false;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (oauthStarted) {
            return;
        }

        oauthStarted = true;

        // Start Twitter OAuth process. Its result will be notified via
        // TwitterOAuthView.Listener interface.
        view.start(TwitterKs.TWITTER_CONSUMER_KEY, TwitterKs.TWITTER_CONSUMER_SECRET, TwitterKs.CALLBACK_URL, TwitterKs.DUMMY_CALLBACK_URL, this);
    }


    public void onSuccess(TwitterOAuthView view, AccessToken accessToken) {
        // The application has been authorized and an access token
        // has been obtained successfully. Save the access token
        // for later use.

        getSharedPreferences("MyPref", 0).edit().
                putString(TwitterKs.PREF_KEY_OAUTH_TOKEN, accessToken.getToken()).
                putString(TwitterKs.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret()).
                putBoolean(TwitterKs.PREF_KEY_TWITTER_LOGIN, true).
                apply();

        new GetProfilePictureURL().execute(accessToken.getToken(), accessToken.getTokenSecret());

        showMessage("Authorized by " + accessToken.getScreenName());

        setResult(RESULT_OK, new Intent());
        finish();
    }

    public void onFailure(TwitterOAuthView view, TwitterOAuthView.Result result) {
        // Failed to get an access token.
        showMessage("Failed due to " + result);
    }

    private void showMessage(String message) {
        // Show a popup message.
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private class GetProfilePictureURL extends AsyncTask<String, Integer, Void> {

        protected Void doInBackground(String... strings) {
            String access_token = strings[0];
            String access_token_secret = strings[1];

            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

            AccessToken accessToken = new AccessToken(access_token,
                    access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build())
                    .getInstance(accessToken);

            try {
                User user = twitter.showUser(accessToken.getUserId());
                String urldisplay = user.getOriginalProfileImageURL();

                getSharedPreferences("MyPref", 0).edit().putString(TwitterKs.PREF_KEY_PICTURE_URL, urldisplay).apply();

            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }

            return null;
        }
    }

}
