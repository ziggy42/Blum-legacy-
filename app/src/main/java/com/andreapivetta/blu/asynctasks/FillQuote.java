package com.andreapivetta.blu.asynctasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.TweetActivity;
import com.squareup.picasso.Picasso;

import twitter4j.Twitter;
import twitter4j.TwitterException;


public class FillQuote extends AsyncTask<Void, Void, Boolean> {
    private TextView quotedUserNameTextView, quotedStatusTextView;
    private LinearLayout quotedStatusLinearLayout;
    private ImageView photoImageView;

    private Twitter twitter;
    private Context context;
    private twitter4j.Status status;
    private long statusID;

    public FillQuote(TextView quotedUserNameTextView, TextView quotedStatusTextView, ImageView photoImageView,
                     LinearLayout quotedStatusLinearLayout, String status, Twitter twitter, Context context) {
        this.twitter = twitter;
        this.context = context;
        this.quotedStatusTextView = quotedStatusTextView;
        this.quotedUserNameTextView = quotedUserNameTextView;
        this.photoImageView = photoImageView;
        this.quotedStatusLinearLayout = quotedStatusLinearLayout;
        this.statusID = Long.parseLong(status.substring(status.lastIndexOf('/') + 1));
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            status = twitter.showStatus(statusID);
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException ne) {
            ne.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            this.quotedUserNameTextView.setText(status.getUser().getName());
            this.quotedStatusTextView.setText(status.getText());

            if (status.getMediaEntities().length > 0) {
                photoImageView.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(status.getMediaEntities()[0].getMediaURL())
                        .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.placeholder, null))
                        .into(photoImageView);
            } else
                photoImageView.setVisibility(View.GONE);

            // TODO workaround
            if (this.quotedStatusLinearLayout != null)
                this.quotedStatusLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, TweetActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable("TWEET", status);
                        i.putExtra("STATUS", b);
                        context.startActivity(i);
                    }
                });
            else
                this.quotedStatusTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, TweetActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable("TWEET", status);
                        i.putExtra("STATUS", b);
                        context.startActivity(i);
                    }
                });
        }
    }
}