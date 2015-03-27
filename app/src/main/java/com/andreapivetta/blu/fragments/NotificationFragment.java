package com.andreapivetta.blu.fragments;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.NotificationAdapter;
import com.andreapivetta.blu.data.Notification;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;
import java.util.Collections;

import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;


public class NotificationFragment extends Fragment {

    private ArrayList<Notification> notificationList = new ArrayList<>();
    private NotificationsDatabaseManager databaseManager;
    private NotificationAdapter mAdapter;

    private ProgressBar loadingProgressBar;
    private TextView nothingToShowTextView;
    private RecyclerView mRecyclerView;

    private int kind;

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

        loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingProgressBar);
        nothingToShowTextView = (TextView) rootView.findViewById(R.id.nothingToShowTextView);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notificationsRecyclerView);

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Common.PREF, 0);
        if (mSharedPreferences.getBoolean(Common.PREF_ANIMATIONS, true)) {
            mRecyclerView.setItemAnimator(new ScaleInBottomAnimator());
            mRecyclerView.getItemAnimator().setAddDuration(300);
        }

        mRecyclerView.addItemDecoration(new SpaceTopItemDecoration(Common.dpToPx(getActivity(), 10)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new NotificationAdapter(notificationList, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        new LoadNotifications().execute(null, null, null);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (kind == 0) {
            databaseManager.open();
            for (Notification n : notificationList)
                databaseManager.setRead(n);
            databaseManager.close();
        }
    }

    private class LoadNotifications extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<Notification> buffer = new ArrayList<>();

        @Override
        protected Boolean doInBackground(Void... params) {

            databaseManager = new NotificationsDatabaseManager(getActivity());
            databaseManager.open();
            buffer = (kind == 0) ? databaseManager.getAllUnreadNotifications() :
                    databaseManager.getAllReadNotifications();
            databaseManager.close();

            Collections.sort(buffer, Collections.reverseOrder());

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                loadingProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);

                if (buffer.size() == 0)
                    nothingToShowTextView.setVisibility(View.VISIBLE);
                else
                    for (int i = 0; i < buffer.size(); i++) {
                        notificationList.add(buffer.get(i));
                        mAdapter.notifyItemChanged(i);
                    }
            }
        }
    }
}
