package com.tpb.hn.item;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.content.ContentActivity;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.views.LockableViewPager;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.loaders.HNItemLoader;
import com.tpb.hn.storage.SharedPrefsController;
import com.tpb.hn.user.UserViewActivity;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 25/10/16.
 */

public class ItemViewActivity extends AppCompatActivity implements HNItemLoader.HNItemLoadDone, FragmentPagerAdapter.Fullscreen, CommentAdapter.UserOpener {
    private static final String TAG = ItemViewActivity.class.getSimpleName();
    private Tracker mTracker;

    @BindView(R.id.item_toolbar) Toolbar mStoryToolbar;
    @BindView(R.id.item_viewpager) LockableViewPager mStoryPager;
    @BindView(R.id.item_appbar) AppBarLayout mStoryAppbar;
    @BindView(R.id.item_tabs) TabLayout mStoryTabs;
    @BindView(R.id.item_title) TextView mTitle;
    @BindView(R.id.item_url) TextView mUrl;
    @BindView(R.id.item_stats) TextView mStats;
    @BindView(R.id.item_author) TextView mAuthor;
    @BindView(R.id.item_fab) FloatingActionButton mFab;
    @BindView(R.id.item_back_button) ImageButton mBackButton;

    @OnClick(R.id.item_back_button)
    public void onClick() {
        onBackPressed();
    }

    @OnClick(R.id.item_author)
    void onAuthorClick() {
        if(mRootItem != null) {
            mLaunchItem = mRootItem;
            openUser(mLaunchItem);
        }
    }

    public static Item mLaunchItem;
    private Item mRootItem;

    private FragmentPagerAdapter mAdapter;

    private int originalFlags;
    private boolean mShouldShowFab = false;

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
            new HNItemLoader(this, this).loadItem(APIPaths.parseItemUrl(data));
        } else {
            if(ContentActivity.mLaunchItem != null) {
                mLaunchItem = ContentActivity.mLaunchItem;
                ContentActivity.mLaunchItem = null;
            } else if(UserViewActivity.mLaunchItem.getType() == ItemType.COMMENT) {
                loadCommentParent(UserViewActivity.mLaunchItem);
                Toast.makeText(getApplicationContext(), R.string.text_traversing_comments, Toast.LENGTH_LONG).show();
            } else {
                mLaunchItem = UserViewActivity.mLaunchItem;
            }
            if(mLaunchItem != null) {
                mRootItem = mLaunchItem;
                setupFragments(prefs.getPageTypes(), mLaunchItem);
                setTitle(mLaunchItem);
                if(launchIntent.getSerializableExtra("type") != null) {
                    final FragmentPagerAdapter.PageType type = (FragmentPagerAdapter.PageType) launchIntent.getSerializableExtra("type");
                    final int index = mAdapter.indexOf(type);
                    mStoryPager.post(new Runnable() {
                        @Override
                        public void run() {
                            if(index != -1) mStoryPager.setCurrentItem(index);
                        }
                    });

                }
            }
        }

        originalFlags = getWindow().getDecorView().getSystemUiVisibility();
    }

    @Override
    public void openUser(Item item) {
        Log.i(TAG, "openUser: " + item.toString());
        mLaunchItem = item;
        startActivity(new Intent(ItemViewActivity.this, UserViewActivity.class),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        Pair.create((View) mBackButton, "button"),
                        Pair.create((View) mStoryAppbar, "appbar")).toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
    }

    public void showFab() {
        if(mShouldShowFab) mFab.show();
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
        mFab.postDelayed(new Runnable() {
            @Override
            public void run() {
                mShouldShowFab = true;
                showFab();
            }
        }, 300);
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        if(mAdapter.onBackPressed()) {
            mStoryPager.setVisibility(View.INVISIBLE);
            mTitle.setVisibility(View.INVISIBLE);
            mBackButton.setVisibility(View.INVISIBLE);
            hideFab();
            super.onBackPressed();
        }
    }

    private void setupFragments(FragmentPagerAdapter.PageType[] possiblePages, Item item) {
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), mStoryPager, possiblePages, item);
        mStoryPager.setAdapter(mAdapter);
        mStoryPager.setOffscreenPageLimit(mAdapter.getCount());
        mStoryTabs.setupWithViewPager(mStoryPager);
    }

    private void loadCommentParent(Item item) {
        new HNItemLoader(this, this).loadItem(item.getParent());
    }

    @Override
    public void itemLoaded(Item item, boolean success, int code) {
        //This is only called when the Activity is launched from a link outside the app
        if(item.getType() == ItemType.COMMENT) {
            loadCommentParent(item);
        } else {
            mLaunchItem = item;
            setupFragments(SharedPrefsController.getInstance(this).getPageTypes(), mLaunchItem);
            setTitle(mLaunchItem);
        }
    }

    private void setTitle(Item item) {
        Log.i(TAG, "setTitle: " + item.toString() + ", " + Arrays.toString(item.getComments()));
        mTitle.setPaddingRelative(mTitle.getPaddingStart(), 16, mTitle.getPaddingEnd(), 16);
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
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
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
    public void itemsLoaded(ArrayList<Item> items, boolean success, int code) {
        //This should never be called, so we just ignore it
    }
}
