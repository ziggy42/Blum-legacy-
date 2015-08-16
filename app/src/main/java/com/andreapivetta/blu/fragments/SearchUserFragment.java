package com.andreapivetta.blu.fragments;

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
import com.andreapivetta.blu.adapters.decorators.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.UserListAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;


public class SearchUserFragment extends Fragment {

    private static final String TAG_QUERY = "query";

    private ArrayList<User> mDataSet = new ArrayList<>();
    private Twitter twitter;
    private UserListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private String query;
    private Paging paging = new Paging(1, 200);

    private ProgressBar loadingProgressBar;
    private TextView nothingToShowTextView;

    private boolean loading = true;

    public static SearchUserFragment newInstance(String query) {
        SearchUserFragment f = new SearchUserFragment();
        Bundle args = new Bundle();
        args.putString(TAG_QUERY, query);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        query = getArguments().getString(TAG_QUERY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        twitter = TwitterUtils.getTwitter(getActivity());

        ScaleInBottomAnimator scaleInBottomAnimator = new ScaleInBottomAnimator();
        scaleInBottomAnimator.setAddDuration(300);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notificationsRecyclerView);
        mRecyclerView.setItemAnimator(scaleInBottomAnimator);
        mRecyclerView.addItemDecoration(new SpaceTopItemDecoration(Common.dpToPx(getActivity(), 10)));
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new UserListAdapter(mDataSet, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (loading && ((mLinearLayoutManager.getChildCount() + (mLinearLayoutManager.findFirstVisibleItemPosition() + 1))
                        >= mLinearLayoutManager.getItemCount())) {
                    loading = false;
                    new LoadUsers().execute();
                }
            }
        });

        loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingProgressBar);
        nothingToShowTextView = (TextView) rootView.findViewById(R.id.nothingToShowTextView);

        new LoadUsers().execute();

        return rootView;
    }

    private class LoadUsers extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ResponseList<User> users = twitter.searchUsers(query, paging.getPage());
                for (twitter4j.User user : users)
                    if (mDataSet.contains(user))
                        return true;
                    else
                        mDataSet.add(user);

                paging.setPage(paging.getPage() + 1);

            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);

                if (mDataSet.size() == 0)
                    nothingToShowTextView.setVisibility(View.VISIBLE);

                loading = true;
            }
        }
    }
}
