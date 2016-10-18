package com.tpb.hn.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;
import com.tpb.hn.network.APIPaths;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity {
    private static final String TAG = Content.class.toString();

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingLayout;

    @BindView(R.id.content_toolbar)
    Toolbar mContentToolbar;

    @BindView(R.id.story_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.story_pager)
    ViewPager mStoryPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        setSupportActionBar(mContentToolbar);

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get(APIPaths.getNewStoriesPath())
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: String request listener " + response);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

        mStoryPager.setAdapter(new Adapter(getSupportFragmentManager()));

        ((TabLayout) findViewById(R.id.story_tabs)).setupWithViewPager(mStoryPager);
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
