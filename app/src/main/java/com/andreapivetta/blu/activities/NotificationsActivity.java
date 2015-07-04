package com.andreapivetta.blu.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.fragments.NotificationFragment;

public class NotificationsActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
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
        MyFragmentPagerAdapter myFragmentPagerAdapter = new MyFragmentPagerAdapter();
        viewPager.setAdapter(myFragmentPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(NotificationsActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_clear_db) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NotificationsActivity.this);
            builder.setTitle(getString(R.string.clear_database))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NotificationsDatabaseManager.getInstance(NotificationsActivity.this).clearDatabase();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;

        public MyFragmentPagerAdapter() {
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
                    return NotificationFragment.newInstance(0);
                case 1:
                    return NotificationFragment.newInstance(1);
                default:
                    return NotificationFragment.newInstance(0);
            }
        }

        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.notifications_tabs_names)[position];
        }

    }
}
