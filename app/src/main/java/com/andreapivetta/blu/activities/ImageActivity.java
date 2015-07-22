package com.andreapivetta.blu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andreapivetta.blu.R;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends AppCompatActivity {

    public static final String TAG_IMAGES = "images";
    public static final String TAG_TITLE = "title";
    public static final String TAG_CURRENT_ITEM = "current_item";

    private List<String> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        images = Arrays.asList(getIntent().getStringArrayExtra(TAG_IMAGES));
        int currentItem = getIntent().getIntExtra(TAG_CURRENT_ITEM, 0);
        String toolbarTitle = getIntent().getStringExtra(TAG_TITLE);
        if (images.size() > 1)
            toolbarTitle = getString(R.string.m_of_n, currentItem + 1, images.size());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            if (toolbarTitle != null)
                toolbar.setTitle(toolbarTitle);

            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.photosViewPager);
        ImageFragmentPagerAdapter myFragmentPagerAdapter = new ImageFragmentPagerAdapter();
        viewPager.setAdapter(myFragmentPagerAdapter);
        viewPager.setCurrentItem(currentItem);
        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();

                if (position < -1) {
                    view.setAlpha(0);
                } else if (position <= 0) {
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else if (position <= 1) {
                    view.setAlpha(1 - position);
                    view.setTranslationX(pageWidth * -position);

                    float scaleFactor = 0.75f
                            + (1 - 0.75f) * (1 - Math.abs(position));
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);
                } else {
                    view.setAlpha(0);
                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (images.size() > 1)
                    getSupportActionBar().setTitle(getString(R.string.m_of_n, position + 1, images.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class ImageFragmentPagerAdapter extends FragmentPagerAdapter {

        public ImageFragmentPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(images.get(position));
        }

    }


    public static class ImageFragment extends Fragment {

        private static final String TAG_IMAGE = "image";

        private String imageURL;
        private PhotoViewAttacher attacher;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            imageURL = getArguments().getString(TAG_IMAGE);
        }

        public static ImageFragment newInstance(String imageURL) {
            ImageFragment imageFragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putString(TAG_IMAGE, imageURL);
            imageFragment.setArguments(args);
            return imageFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_image, container, false);

            final ImageView tweetImageView = (ImageView) rootView.findViewById(R.id.tweetImageView);
            Picasso.with(getActivity())
                    .load(imageURL)
                    .into(tweetImageView);

            tweetImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (attacher == null)
                        attacher = new PhotoViewAttacher(tweetImageView);

                    return true;
                }
            });

            return rootView;
        }

        @Override
        public void onDestroyView() {
            attacher = null;
            super.onDestroyView();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_share, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.action_share) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.check_out_photo, imageURL))
                        .setType("text/plain");
                startActivity(intent);
            }

            return super.onOptionsItemSelected(item);
        }
    }
}
