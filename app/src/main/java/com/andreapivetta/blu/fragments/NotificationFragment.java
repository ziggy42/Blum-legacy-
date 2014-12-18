package com.andreapivetta.blu.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.NotificationAdapter;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;
import java.util.Collections;

import twitter4j.Twitter;

public class NotificationFragment extends Fragment {

    private ArrayList<Notification> notificationList = new ArrayList<>();
    private NotificationsDatabaseManager databaseManager;

    public static NotificationFragment newInstance(int mode) {
        NotificationFragment f = new NotificationFragment();
        Bundle args = new Bundle();
        args.putInt("KIND", mode);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseManager = new NotificationsDatabaseManager(getActivity());
        databaseManager.open();
        if (getArguments().getInt("KIND") == 0) {
            notificationList = databaseManager.getAllUnreadNotifications();
        } else {
            notificationList = databaseManager.getAllReadNotifications();
        }
        databaseManager.close();

        Collections.sort(notificationList, Collections.reverseOrder());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        Twitter twitter = TwitterUtils.getTwitter(getActivity());
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notificationsRecyclerView);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        NotificationAdapter mNotificationAdapter = new NotificationAdapter(notificationList, getActivity(), twitter);
        mRecyclerView.setAdapter(mNotificationAdapter);

        if (notificationList.size() == 0)
            rootView.findViewById(R.id.nothingToShowTextView).setVisibility(View.VISIBLE);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        databaseManager.open();
        for(Notification n : notificationList)
            databaseManager.setRead(n);
        databaseManager.close();
    }
}