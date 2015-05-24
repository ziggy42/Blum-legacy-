package com.andreapivetta.blu.services;

import android.app.IntentService;
import android.content.Intent;

import com.andreapivetta.blu.data.MentionsDatabaseManager;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class CheckMentionsService extends IntentService {

    public CheckMentionsService() {
        super("CheckMentionsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.i("MentionsService", "MentionsService START");

        Twitter twitter = TwitterUtils.getTwitter(getApplicationContext());
        MentionsDatabaseManager dbm = new MentionsDatabaseManager(getApplicationContext());
        try {
            List<Status> mentions = twitter.getMentionsTimeline(new Paging(1, 200));
            if (mentions.size() > 0) {
                ArrayList<ArrayList<Long>> triples = new ArrayList<>();
                for (Status mention : mentions) {
                    ArrayList<Long> tmp = new ArrayList<>();
                    tmp.add(mention.getId());
                    tmp.add(mention.getUser().getId());
                    tmp.add(mention.getCreatedAt().getTime());
                    triples.add(tmp);
                }

                dbm.open();
                ArrayList<ArrayList<Long>> newMentions = dbm.check(triples);
                for (ArrayList<Long> triple : newMentions)
                    Notification.pushNotification(
                            triple.get(0), triple.get(1), Notification.TYPE_MENTION, getApplicationContext());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        dbm.close();

        //Log.i("MentionsService", "MentionsService STOP");
    }

}
