package com.tpb.hn.item;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 25/10/16.
 */

public class ItemViewActivity extends AppCompatActivity  implements HNLoader.HNItemLoadDone, FragmentPagerAdapter.Fullscreen {
    private static final String TAG = ItemViewActivity.class.getSimpleName();
    private Tracker mTracker;

    @BindView(R.id.item_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.item_viewpager)
    LockableViewPager mStoryPager;

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

    @BindView(R.id.item_fab)
    FloatingActionButton mFab;

    @OnClick(R.id.item_back_button)
    public void onClick() {
        finish();
    }

    private FragmentPagerAdapter mAdapter;

    private int originalFlags;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = ((Analytics) getApplication()).getDefaultTracker();

        final SharedPrefsController prefs = SharedPrefsController.getInstance(this);
        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_item_view);
        ButterKnife.bind(this);
        setSupportActionBar(mStoryToolbar);

        final Intent launchIntent = getIntent();
        if(Intent.ACTION_VIEW.equals(launchIntent.getAction())) {
            AdBlocker.init(this);
            final String data = launchIntent.getDataString();
            new HNLoader(this, this).loadItem(APIPaths.parseUrl(data));
        } else {
            if(launchIntent.getParcelableExtra("item") != null) {
                final Item item = launchIntent.getParcelableExtra("item");
                setupFragments(prefs.getPageTypes(), item);
                setTitle(item);
            } else {
                Toast.makeText(this, R.string.error_no_item, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        originalFlags = getWindow().getDecorView().getSystemUiVisibility();
        //TODO- Fullscreen mode
        //TODO- Load Items in background while user is looking at current item
    }

    public void showFab() {
        mFab.show();
    }

    public void hideFab() {
        mFab.hide();
    }

    public void setUpFab(int resId, View.OnClickListener listener) {
        mFab.setImageDrawable(getDrawable(resId));
        mFab.setOnClickListener(listener);
    }

    public void setFabDrawable(int resId) {
        mFab.setImageDrawable(getDrawable(resId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setupFragments(FragmentPagerAdapter.PageType[] possiblePages, Item item) {
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), mStoryPager, possiblePages, item);
        mStoryPager.setAdapter(mAdapter);
        mStoryPager.setOffscreenPageLimit(mAdapter.getCount());
        mStoryTabs.setupWithViewPager(mStoryPager);
    }

    private void setTitle(Item item) {
        mTitle.setText(item.getTitle());
        mUrl.setText(item.getFormattedURL());
        mStats.setText(item.getFormattedInfo());
        mAuthor.setText(String.format(getResources().getString(R.string.text_item_by), item.getBy()));

        mTracker.send(new HitBuilders.EventBuilder()
            .setCategory(Analytics.CATEGORY_ITEM)
            .setAction(Analytics.KEY_OPEN_ITEM)
            .setLabel(item.toString())
            .build());

    }

    @Override
    public void openFullScreen() {
        mStoryAppbar.setExpanded(false, true);
        getWindow().getDecorView().setSystemUiVisibility(originalFlags |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        mStoryPager.setSwipeEnabled(false);
    }

    @Override
    public void closeFullScreen() {
        mStoryAppbar.setExpanded(true, true);
        getWindow().getDecorView().setSystemUiVisibility(originalFlags);
        mStoryPager.setSwipeEnabled(true);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.slide_down);
    }

    @Override
    public void itemLoaded(Item item, boolean success, int code) {
        setupFragments(SharedPrefsController.getInstance(this).getPageTypes(), item);
        setTitle(item);
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success, int code) {

    }
}
