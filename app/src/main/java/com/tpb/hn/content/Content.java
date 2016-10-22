package com.tpb.hn.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.DividerItemDecoration;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.storage.SharedPrefsController;
import com.tpb.hn.story.StoryAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity implements HNLoader.HNItemLoadDone, ContentAdapter.ContentOpener {
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

    @BindView(R.id.content_recycler)
    RecyclerView mRecycler;

    private ContentAdapter mAdapter;


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
        mStoryPager.setOffscreenPageLimit(mStoryAdapter.getCount());

        ((TabLayout) ButterKnife.findById(this, R.id.story_tabs)).setupWithViewPager(mStoryPager);

        mAdapter = new ContentAdapter(this);

        mNavSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        ));

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(mAdapter);
        mRecycler.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(android.R.drawable.divider_horizontal_dim_dark)));

        mAdapter.loadItems(SharedPrefsController.getInstance(this).getDefaultPage());
    }



    @Override
    public void itemLoaded(Item item, boolean success) {
        mPanelController.setTitle(item);
        mStoryAdapter.loadStory(item);
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {
        mPanelController.setTitle(items.get(0));
        mStoryAdapter.loadStory(items.get(0));
    }

    @Override
    public void openItem(Item item) {
        mPanelController.setTitle(item);
        mStoryAdapter.loadStory(item);
    }

    @Override
    public void openUser(Item item) {

    }

    @Override
    public void openPage(Item item, StoryAdapter.PageType type) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPanelController.onResume();
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
