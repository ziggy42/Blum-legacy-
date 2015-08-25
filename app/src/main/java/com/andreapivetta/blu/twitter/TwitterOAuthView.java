package com.andreapivetta.blu.twitter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterOAuthView extends WebView {

    private TwitterOAuthTask twitterOAuthTask;

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

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings settings = getSettings();

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);

        setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
    }

    public void start(String consumerKey, String consumerSecret, String callbackUrl, boolean dummyCallbackUrl,
                      Listener listener) {

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

        if (!task.isCancelled())
            task.cancel(true);

        synchronized (task) {
            task.notify();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
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

        @Override
        protected void onPreExecute() {
            TwitterOAuthView.this.setWebViewClient(new LocalWebViewClient());
        }

        @Override
        protected Result doInBackground(Object... args) {
            if (isCancelled()) {
                return Result.CANCELLATION;
            }

            String consumerKey = (String) args[0];
            String consumerSecret = (String) args[1];
            callbackUrl = (String) args[2];
            dummyCallbackUrl = (Boolean) args[3];
            listener = (Listener) args[4];

            twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(consumerKey, consumerSecret);

            requestToken = getRequestToken();
            if (requestToken == null) {
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

            if (isCancelled()) {
                return Result.CANCELLATION;
            }

            accessToken = getAccessToken();
            if (accessToken == null) {
                return Result.ACCESS_TOKEN_ERROR;
            }

            return Result.SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (isCancelled())
                return;

            TwitterOAuthView.this.loadUrl(requestToken.getAuthorizationURL());
        }

        @Override
        protected void onPostExecute(Result result) {

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
            listener.onSuccess(TwitterOAuthView.this, accessToken);
        }

        private void fireOnFailure(Result result) {
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
                return twitter.getOAuthRequestToken();
            } catch (TwitterException e) {
                e.printStackTrace();
                return null;
            }
        }

        private void authorize() {
            publishProgress();
        }

        private boolean waitForAuthorization() {
            while (!authorizationDone) {
                if (isCancelled())
                    return true;

                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            return false;
        }

        private void notifyAuthorization() {
            authorizationDone = true;

            synchronized (this) {
                this.notify();
            }
        }

        private AccessToken getAccessToken() {
            try {
                return twitter.getOAuthAccessToken(requestToken, verifier);
            } catch (TwitterException e) {
                e.printStackTrace();
                return null;
            }
        }

        private class LocalWebViewClient extends WebViewClient {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                notifyAuthorization();
            }

            @Override
            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith(callbackUrl))
                    return false;

                verifier = Uri.parse(url).getQueryParameter("oauth_verifier");

                notifyAuthorization();

                return dummyCallbackUrl;
            }
        }
    }
}