package com.tpb.hn.content;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity {
    private static final String TAG = Content.class.getCanonicalName();

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingLayout;

    private float mLastPanelOffset = 0.0f;

    @BindView(R.id.content_toolbar)
    Toolbar mContentToolbar;

    @BindView(R.id.story_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.story_pager)
    ViewPager mStoryPager;

    @BindView(R.id.story_appbar)
    AppBarLayout mStoryAppbar;

    @BindView(R.id.story_panel)
    FrameLayout mStoryPanel;

    @BindView(R.id.item_large_title)
    LinearLayout mLargeTitleLayout;

    @BindView(R.id.item_detail_layout)
    RelativeLayout mDetailLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        setSupportActionBar(mContentToolbar);

        //Never do this
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        //TODO- Async do checks and start loading content
//        AndroidNetworking.initialize(getApplicationContext());
//        AndroidNetworking.get(APIPaths.getMaxItemPath())
//                .setTag("test")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.i(TAG, "onResponse: String request listener " + response);
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//
//                    }
//                });
//
        mStoryPager.setAdapter(new Adapter(getSupportFragmentManager()));
//
        ((TabLayout) findViewById(R.id.story_tabs)).setupWithViewPager(mStoryPager);
//
//
//        HtmlFetcher fetcher = new HtmlFetcher();
//        //Probaly don't do this. Pull down full page and then pass the HTML to the extractor
//        try {
//            JResult res = fetcher.fetchAndExtract("http://www.xda-developers.com/a-look-at-what-has-changed-from-the-snapdragon-820-to-the-snapdragon-821-in-the-google-pixel-phones/",
//                    10000, true);
//            Log.i(TAG, "onCreate: " + res.getText());
//        } catch(Exception e) {
//            Log.e(TAG, "onCreate: Fetcher", e);
//        }

        //getSupportActionBar().hide();
        //mStoryPanel.setVisibility(View.INVISIBLE);

        //TODO- Move this into some sort of sliding panel controller
        mSlidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(slideOffset > mLastPanelOffset) {
                    mLargeTitleLayout.setAlpha(slideOffset);
                    mDetailLayout.setAlpha(1 - slideOffset);
                } else {
                    mLargeTitleLayout.setAlpha(slideOffset);
                    mDetailLayout.setAlpha(1 - slideOffset);

                }
                mLastPanelOffset = slideOffset;
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mSlidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    private class Adapter extends FragmentPagerAdapter {

        public Adapter(android.support.v4.app.FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return new Fragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return  "Title";
        }
    }

}
