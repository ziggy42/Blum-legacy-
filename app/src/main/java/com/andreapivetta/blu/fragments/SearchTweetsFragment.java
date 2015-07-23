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
import com.andreapivetta.blu.adapters.SpaceTopItemDecoration;
import com.andreapivetta.blu.adapters.TweetsListAdapter;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class SearchTweetsFragment extends Fragment {

    private ArrayList<Status> mDataSet = new ArrayList<>();
    private Twitter twitter;
    private TweetsListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar loadingProgressBar;
    private TextView nothingToShowTextView;
    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private Query mQuery;

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

        mQuery = new Query(getArguments().getString("QUERY"));
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
        mAdapter = new TweetsListAdapter(mDataSet, getActivity(), twitter, -1);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mLinearLayoutManager.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition() + 1;

                if (loading) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = false;
                        new TweetsLoaderAsyncTask().execute(null, null, null);
                    }
                }
            }
        });

        loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingProgressBar);
        nothingToShowTextView = (TextView) rootView.findViewById(R.id.nothingToShowTextView);

        new TweetsLoaderAsyncTask().execute(null, null, null);

        return rootView;
    }

    private class TweetsLoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                QueryResult result = twitter.search(mQuery);
                mDataSet.addAll(result.getTweets());

                if (!result.hasNext()) {
                    loading = false;
                } else {
                    loading = true;
                    mQuery = result.nextQuery();
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                loadingProgressBar.setVisibility(View.GONE);

                if (mDataSet.size() == 0)
                    nothingToShowTextView.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
