package com.andreapivetta.blu.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.NotificationAdapter;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;


public class NotificationFragment extends Fragment {

    private ArrayList<Notification> notificationList = new ArrayList<>();

    private int kind;

    private DatabaseManager databaseManager = DatabaseManager.getInstance(getActivity());

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

        kind = getArguments().getInt("KIND");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationList.addAll((kind == 0) ? databaseManager.getAllUnreadNotifications() :
                databaseManager.getAllReadNotifications());

        if (notificationList.size() == 0)
            rootView.findViewById(R.id.nothingToShowTextView).setVisibility(View.VISIBLE);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notificationsRecyclerView);
        mRecyclerView.addItemDecoration(new SpaceTopItemDecoration(Common.dpToPx(getActivity(), 10)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        NotificationAdapter mAdapter = new NotificationAdapter(notificationList, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onPause() {
        if (kind == 0)
            databaseManager.setAllAsRead();

        super.onDestroy();
    }
}
