package com.tpb.hn.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;
import com.tpb.hn.story.StoryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity {
    private static final String TAG = Content.class.getSimpleName();

    private PanelController mPanelController;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        setSupportActionBar(mContentToolbar);

        //TODO- Async do checks and start loading content


        mStoryPager.setAdapter(new StoryAdapter(getSupportFragmentManager(), new StoryAdapter.PageType[] {StoryAdapter.PageType.BROWSER, StoryAdapter.PageType.COMMENTS, StoryAdapter.PageType.READABILITY, StoryAdapter.PageType.SKIMMER}));
        mStoryPager.setOffscreenPageLimit(Integer.MAX_VALUE);
        ((TabLayout) ButterKnife.findById(this, R.id.story_tabs)).setupWithViewPager(mStoryPager);
        mPanelController = new PanelController(
                (SlidingUpPanelLayout) ButterKnife.findById(this, R.id.sliding_layout));
    }



    @Override
    public void onBackPressed() {
        if(mPanelController.isExpanded()) {
            mPanelController.collapse();
        } else {
            super.onBackPressed();
        }
    }

}
