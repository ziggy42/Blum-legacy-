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

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;

import java.util.ArrayList;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class SearchTweetsFragment extends Fragment {

    private ArrayList<Status> mDataSet = new ArrayList<>();
    private Twitter twitter;
    private TweetsListAdapter mAdapter;
    private String query;
    private ProgressBar loadingProgressBar;

    public static SearchTweetsFragment newInstance(String query) {
        SearchTweetsFragment f = new SearchTweetsFragment();
        Bundle args = new Bundle();
        args.putString("QUERY", query);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        query = getArguments().getString("QUERY");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        twitter = TwitterUtils.getTwitter(getActivity());
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notificationsRecyclerView);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new TweetsListAdapter(mDataSet, getActivity(), twitter, -1);
        mRecyclerView.setAdapter(mAdapter);

        loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingProgressBar);

        new LoadTweets().execute(null,null,null);

        return rootView;
    }

    private class LoadTweets extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                QueryResult result = twitter.search(new Query(query));
                for (twitter4j.Status tmpStatus : result.getTweets()) {
                        mDataSet.add(tmpStatus);
                }
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
            }
        }
    }
}
