package com.andreapivetta.blu.activities;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.fragments.SearchTweetsFragment;
import com.andreapivetta.blu.fragments.SearchUserFragment;
import com.andreapivetta.blu.fragments.tabs.SlidingTabLayout;

public class SearchActivity extends ThemedActivity {

    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        query = getIntent().getStringExtra(SearchManager.QUERY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(query);
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        SearchFragmentPagerAdapter myFragmentPagerAdapter = new SearchFragmentPagerAdapter();
        viewPager.setAdapter(myFragmentPagerAdapter);
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);

    }

    public class SearchFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;

        public SearchFragmentPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return SearchTweetsFragment.newInstance(query);
                case 1:
                    return SearchUserFragment.newInstance(query);
                default:
                    return SearchTweetsFragment.newInstance(query);
            }
        }

        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.search_tabs_names)[position];
        }

    }
}
