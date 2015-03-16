package com.andreapivetta.blu.twitter;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class TwitterOAuthView extends WebView {

    private static final String TAG = "TwitterOAuthView";
    private static final boolean DEBUG = false;
    private boolean isDebugEnabled = DEBUG;
    private TwitterOAuthTask twitterOAuthTask;
    private boolean cancelOnDetachedFromWindow = true;

    public TwitterOAuthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public TwitterOAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TwitterOAuthView(Context context) {
        super(context);

        init();
    }

    private void init() {
        WebSettings settings = getSettings();

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);

        setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
    }

    public void start(String consumerKey, String consumerSecret,
                      String callbackUrl, boolean dummyCallbackUrl,
                      Listener listener) {

        if (consumerKey == null || consumerSecret == null || callbackUrl == null || listener == null) {
            throw new IllegalArgumentException();
        }

        TwitterOAuthTask oldTask;
        TwitterOAuthTask newTask;

        synchronized (this) {
            oldTask = twitterOAuthTask;
            newTask = new TwitterOAuthTask();
            twitterOAuthTask = newTask;
        }

        cancelTask(oldTask);

        newTask.execute(consumerKey, consumerSecret, callbackUrl, dummyCallbackUrl, listener);
    }

    public void cancel() {
        TwitterOAuthTask task;

        synchronized (this) {
            task = twitterOAuthTask;
            twitterOAuthTask = null;
        }

        cancelTask(task);
    }

    private void cancelTask(TwitterOAuthTask task) {
        if (task == null)
            return;

        if (!task.isCancelled()) {
            if (isDebugEnabled())
                Log.d(TAG, "Cancelling a task.");

            task.cancel(true);
        }

        synchronized (task) {
            if (isDebugEnabled())
                Log.d(TAG, "Notifying a task of cancellation.");

            task.notify();
        }
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public void setDebugEnabled(boolean enabled) {
        isDebugEnabled = enabled;
    }

    public boolean isCancelOnDetachedFromWindow() {
        return cancelOnDetachedFromWindow;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (isCancelOnDetachedFromWindow()) {
            cancel();
        }
    }


    public enum Result {
        SUCCESS,
        CANCELLATION,
        REQUEST_TOKEN_ERROR,
        AUTHORIZATION_ERROR,
        ACCESS_TOKEN_ERROR
    }

    public interface Listener {

        void onSuccess(TwitterOAuthView view, AccessToken accessToken);

        void onFailure(TwitterOAuthView view, Result result);
    }

    private class TwitterOAuthTask extends AsyncTask<Object, Void, Result> {
        private String callbackUrl;
        private boolean dummyCallbackUrl;
        private Listener listener;
        private Twitter twitter;
        private RequestToken requestToken;
        private volatile boolean authorizationDone;
        private volatile String verifier;
        private AccessToken accessToken;

        private boolean checkCancellation(String context) {
            if (!isCancelled()) {
                return false;
            }

            if (isDebugEnabled()) {
                Log.d(TAG, "Cancellation was detected in the context of " + context);
            }

            return true;
        }


        @Override
        protected void onPreExecute() {
            TwitterOAuthView.this.setWebViewClient(new LocalWebViewClient());
        }


        @Override
        protected Result doInBackground(Object... args) {
            if (checkCancellation("doInBackground() [on entry]")) {
                return Result.CANCELLATION;
            }

            String consumerKey = (String) args[0];
            String consumerSecret = (String) args[1];
            callbackUrl = (String) args[2];
            dummyCallbackUrl = (Boolean) args[3];
            listener = (Listener) args[4];

            if (isDebugEnabled()) {
                debugDoInBackground(args);
            }

            twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(consumerKey, consumerSecret);

            requestToken = getRequestToken();
            if (requestToken == null) {
                // Failed to get a request token.
                return Result.REQUEST_TOKEN_ERROR;
            }

            authorize();

            boolean cancelled = waitForAuthorization();
            if (cancelled) {
                return Result.CANCELLATION;
            }

            if (verifier == null) {
                return Result.AUTHORIZATION_ERROR;
            }

            if (checkCancellation("doInBackground() [before getAccessToken()]")) {
                return Result.CANCELLATION;
            }

            accessToken = getAccessToken();
            if (accessToken == null) {
                return Result.ACCESS_TOKEN_ERROR;
            }

            return Result.SUCCESS;
        }


        private void debugDoInBackground(Object... args) {
            Log.d(TAG, "CONSUMER KEY = " + args[0]);
            Log.d(TAG, "CONSUMER SECRET = " + args[1]);
            Log.d(TAG, "CALLBACK URL = " + args[2]);
            Log.d(TAG, "DUMMY CALLBACK URL = " + args[3]);

            System.setProperty("twitter4j.debug", "true");
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            if (checkCancellation("onProgressUpdate()")) {
                return;
            }

            String url = requestToken.getAuthorizationURL();

            if (isDebugEnabled()) {
                Log.d(TAG, "Loading the authorization URL: " + url);
            }

            TwitterOAuthView.this.loadUrl(url);
        }


        @Override
        protected void onPostExecute(Result result) {
            if (isDebugEnabled()) {
                Log.d(TAG, "onPostExecute: result = " + result);
            }

            if (result == null) {
                result = Result.CANCELLATION;
            }

            if (result == Result.SUCCESS) {
                fireOnSuccess();
            } else {
                fireOnFailure(result);
            }

            clearTaskReference();
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();

            fireOnFailure(Result.CANCELLATION);

            clearTaskReference();
        }


        private void fireOnSuccess() {
            if (isDebugEnabled()) {
                Log.d(TAG, "Calling Listener.onSuccess");
            }

            listener.onSuccess(TwitterOAuthView.this, accessToken);
        }


        private void fireOnFailure(Result result) {
            if (isDebugEnabled()) {
                Log.d(TAG, "Calling Listener.onFailure, result = " + result);
            }

            listener.onFailure(TwitterOAuthView.this, result);
        }


        private void clearTaskReference() {
            synchronized (TwitterOAuthView.this) {
                if (TwitterOAuthView.this.twitterOAuthTask == this) {
                    TwitterOAuthView.this.twitterOAuthTask = null;
                }
            }
        }


        private RequestToken getRequestToken() {
            try {
                RequestToken token = twitter.getOAuthRequestToken();

                if (isDebugEnabled()) {
                    Log.d(TAG, "Got a request token.");
                }

                return token;
            } catch (TwitterException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to get a request token.", e);

                return null;
            }
        }


        private void authorize() {
            publishProgress();
        }


        private boolean waitForAuthorization() {
            while (!authorizationDone) {
                if (checkCancellation("waitForAuthorization()")) {
                    return true;
                }

                synchronized (this) {
                    try {
                        if (isDebugEnabled()) {
                            Log.d(TAG, "Waiting for the authorization step to be done.");
                        }

                        this.wait();
                    } catch (InterruptedException e) {
                        if (isDebugEnabled()) {
                            Log.d(TAG, "Interrupted while waiting for the authorization step to be done.");
                        }
                    }
                }
            }

            if (isDebugEnabled()) {
                Log.d(TAG, "Finished waiting for the authorization step to be done.");
            }

            return false;
        }


        private void notifyAuthorization() {
            authorizationDone = true;

            synchronized (this) {
                if (isDebugEnabled()) {
                    Log.d(TAG, "Notifying that the authorization step was done.");
                }

                this.notify();
            }
        }

        private AccessToken getAccessToken() {
            try {
                AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);

                if (isDebugEnabled()) {
                    Log.d(TAG, "Got an access token for " + token.getScreenName());
                }

                return token;
            } catch (TwitterException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to get an access token.", e);

                return null;
            }
        }

        private class LocalWebViewClient extends WebViewClient {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "onReceivedError: [" + errorCode + "] " + description);

                notifyAuthorization();
            }


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith(callbackUrl)) {
                    return false;
                }

                if (isDebugEnabled()) {
                    Log.d(TAG, "Detected the callback URL: " + url);
                }

                Uri uri = Uri.parse(url);

                verifier = uri.getQueryParameter("oauth_verifier");

                if (isDebugEnabled()) {
                    Log.d(TAG, "oauth_verifier = " + verifier);
                }

                notifyAuthorization();

                return dummyCallbackUrl;
            }
        }
    }
}