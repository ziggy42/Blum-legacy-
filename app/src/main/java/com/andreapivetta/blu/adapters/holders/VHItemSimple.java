package com.andreapivetta.blu.adapters.holders;

import android.content.Context;
import android.text.util.Linkify;
import android.view.View;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;

public class VHItemSimple extends VHItem {

    public VHItemSimple(View container) {
        super(container);
    }

    @Override
    public void setup(Status status, Context context, ArrayList<Long> favorites, ArrayList<Long> retweets, Twitter twitter) {
        super.setup(status, context, favorites, retweets, twitter);
        statusTextView.setText(status.getText());
        Linkify.addLinks(statusTextView, Linkify.ALL);
    }
}
