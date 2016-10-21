package com.tpb.hn.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.story.StoryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity {
    private static final String TAG = Content.class.getSimpleName();

    private PanelController mPanelController;
    private StoryAdapter mStoryAdapter;

    @BindView(R.id.content_toolbar)
    Toolbar mContentToolbar;

    @BindView(R.id.story_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.story_pager)
    ViewPager mStoryPager;

    @BindView(R.id.story_appbar)
    AppBarLayout mStoryAppbar;

    @BindView(R.id.story_panel)
    RelativeLayout mStoryPanel;

    @BindView(R.id.nav_spinner)
    Spinner mNavSpinner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        setSupportActionBar(mContentToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //TODO- Async do checks and start loading content
        mPanelController = new PanelController(
                (SlidingUpPanelLayout) ButterKnife.findById(this, R.id.sliding_layout));


        mStoryAdapter = new StoryAdapter(getSupportFragmentManager(),
                new StoryAdapter.PageType[] {StoryAdapter.PageType.BROWSER, StoryAdapter.PageType.COMMENTS, StoryAdapter.PageType.READABILITY, StoryAdapter.PageType.SKIMMER});


        mStoryPager.setAdapter(mStoryAdapter);
        mStoryPager.setOffscreenPageLimit(Integer.MAX_VALUE);
        mStoryPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int pos = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((StoryAdapter.FragmentCycle) mStoryAdapter.getItem(position)).onResumeFragment();
                ((StoryAdapter.FragmentCycle) mStoryAdapter.getItem(pos)).onPauseFragment();
                pos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ((TabLayout) ButterKnife.findById(this, R.id.story_tabs)).setupWithViewPager(mStoryPager);




        mNavSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        ));

        final Item test = new Item();
        test.setUrl("http://www.bbc.co.uk/news/uk-37725327");
        mStoryAdapter.loadStory(test);
    }



    @Override
    public void onBackPressed() {
        //TODO- Allow the fragments to override back press
        if(mPanelController.isExpanded()) {
            mPanelController.collapse();
        } else {
            super.onBackPressed();
        }
    }

}
