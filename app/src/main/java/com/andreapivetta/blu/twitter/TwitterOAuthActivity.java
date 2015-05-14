package com.andreapivetta.blu.twitter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.andreapivetta.blu.R;

import twitter4j.auth.AccessToken;

public class TwitterOAuthActivity extends Activity implements TwitterOAuthView.Listener {

    private TwitterOAuthView view;
    private boolean oauthStarted;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new TwitterOAuthView(this);
        view.setDebugEnabled(true);

        setContentView(view);

        oauthStarted = false;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (oauthStarted)
            return;

        oauthStarted = true;

        view.start(TwitterUtils.TWITTER_CONSUMER_KEY, TwitterUtils.TWITTER_CONSUMER_SECRET,
                TwitterUtils.CALLBACK_URL, TwitterUtils.DUMMY_CALLBACK_URL, this);
    }


    public void onSuccess(TwitterOAuthView view, AccessToken accessToken) {
        PreferenceManager.getDefaultSharedPreferences(TwitterOAuthActivity.this).edit().
                putString(TwitterUtils.PREF_KEY_OAUTH_TOKEN, accessToken.getToken()).
                putString(TwitterUtils.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret()).
                putBoolean(getString(R.string.pref_key_login), true).
                putLong(getString(R.string.pref_key_logged_user), accessToken.getUserId()).
                apply();

        showMessage(getString(R.string.authorized_by, accessToken.getScreenName()));

        setResult(RESULT_OK, new Intent());
        finish();
    }

    public void onFailure(TwitterOAuthView view, TwitterOAuthView.Result result) {
        showMessage((getString(R.string.failed_due, result)));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
