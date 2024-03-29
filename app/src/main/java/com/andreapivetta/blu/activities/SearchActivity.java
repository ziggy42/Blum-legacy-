package com.andreapivetta.blu.activities;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.activities.interf.SnackbarContainer;
import com.andreapivetta.blu.fragments.SearchTweetsFragment;
import com.andreapivetta.blu.fragments.SearchUserFragment;

public class SearchActivity extends ThemedActivity implements SnackbarContainer {

    private String query;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        query = getIntent().getStringExtra(SearchManager.QUERY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(query);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new SearchFragmentPagerAdapter());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void showSnackBar(String content) {
        Snackbar.make(viewPager, content, Snackbar.LENGTH_SHORT).show();
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
