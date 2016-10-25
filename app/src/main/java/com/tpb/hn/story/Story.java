package com.tpb.hn.story;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 25/10/16.
 */

public class Story extends AppCompatActivity  implements HNLoader.HNItemLoadDone {
    private static final String TAG = Story.class.getSimpleName();

    @BindView(R.id.item_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.item_viewpager)
    ViewPager mStoryPager;

    @BindView(R.id.item_appbar)
    AppBarLayout mStoryAppbar;

    @BindView(R.id.item_tabs)
    TabLayout mStoryTabs;

    @BindView(R.id.item_title)
    TextView mTitle;

    @BindView(R.id.item_url)
    TextView mUrl;

    @BindView(R.id.item_stats)
    TextView mStats;

    @BindView(R.id.item_author)
    TextView mAuthor;

    @OnClick(R.id.story_back_button)
    public void onClick() {
        finish();
    }

    private StoryAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPrefsController prefs = SharedPrefsController.getInstance(this);
        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_story_view);
        ButterKnife.bind(this);
        setSupportActionBar(mStoryToolbar);

        mAdapter = new StoryAdapter(getSupportFragmentManager(), prefs.getPageTypes());

        mStoryPager.setAdapter(mAdapter);
        mStoryPager.setOffscreenPageLimit(mAdapter.getCount());
        mStoryTabs.setupWithViewPager(mStoryPager);

        final Intent launchIntent = getIntent();
        if(Intent.ACTION_VIEW.equals(launchIntent.getAction())) {
            final String data = launchIntent.getDataString();
            new HNLoader(this).loadItem(APIPaths.parseUrl(data));
        } else {
            if(launchIntent.getParcelableExtra("item") != null) {
                final Item item = launchIntent.getParcelableExtra("item");
                mAdapter.loadStory(item);
                setTitle(item);
            } else {
                Toast.makeText(this, R.string.error_no_item, Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    private void setTitle(Item item) {
        mTitle.setText(item.getTitle());
        mUrl.setText(item.getFormattedURL());
        mStats.setText(item.getFormattedInfo());
        mAuthor.setText(String.format(getResources().getString(R.string.text_item_by), item.getBy()));
    }

    @Override
    public void itemLoaded(Item item, boolean success) {
        mAdapter.loadStory(item);
        setTitle(item);
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {

    }
}
