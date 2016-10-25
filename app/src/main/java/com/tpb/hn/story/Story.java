package com.tpb.hn.story;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 25/10/16.
 */

public class Story extends AppCompatActivity {

    @BindView(R.id.item_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.item_viewpager)
    ViewPager mStoryPager;

    @BindView(R.id.item_appbar)
    AppBarLayout mStoryAppbar;

    @BindView(R.id.item_tabs)
    TabLayout mStoryTabs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);
        ButterKnife.bind(this);
        setSupportActionBar(mStoryToolbar);

        StoryAdapter mAdapter = new StoryAdapter(getSupportFragmentManager(),
                new StoryAdapter.PageType[] {StoryAdapter.PageType.BROWSER, StoryAdapter.PageType.COMMENTS, StoryAdapter.PageType.READABILITY, StoryAdapter.PageType.SKIMMER}, mStoryAppbar);

        mStoryPager.setAdapter(mAdapter);
        mStoryTabs.setupWithViewPager(mStoryPager);

        final Item item = new Item();
        item.setId(1);
        item.setBy("someone");
        item.setScore(100);
        item.setUrl("http://www.bbc.co.uk/news/uk-england-37667371");
        item.setType(ItemType.STORY);
        mAdapter.loadStory(item);

    }
}
