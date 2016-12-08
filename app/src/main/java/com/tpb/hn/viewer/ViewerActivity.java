package com.tpb.hn.viewer;

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
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.feed.FeedActivity;
import com.tpb.hn.data.Item;
import com.tpb.hn.viewer.fragments.CommentAdapter;
import com.tpb.hn.viewer.views.LockableViewPager;
import com.tpb.hn.helpers.APIPaths;
import com.tpb.hn.helpers.AdBlocker;
import com.tpb.hn.network.Loader;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.user.UserActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by theo on 25/10/16.
 */

public class ViewerActivity extends AppCompatActivity implements Loader.ItemLoader, FragmentPagerAdapter.Fullscreen, CommentAdapter.UserOpener {
    private static final String TAG = ViewerActivity.class.getSimpleName();
    public static Item mLaunchItem;
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
    private Tracker mTracker;
    private Item mRootItem;
    private FragmentPagerAdapter mAdapter;
    private int originalFlags;
    private boolean mShouldShowFab = false;
    private int mOpenedCommentId;

    @OnClick(R.id.item_back_button)
    public void onClick() {
        onBackPressed();
    }

    @OnLongClick(R.id.item_back_button)
    boolean onLongClick() {
        FeedActivity.returning = true;
        finish();
        return true;
    }

    @OnClick(R.id.item_author)
    void onAuthorClick() {
        if(mRootItem != null) {
            mLaunchItem = mRootItem;
            openUser(mLaunchItem);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = ((Analytics) getApplication()).getDefaultTracker();

        final SharedPrefsController prefs = SharedPrefsController.getInstance(this);
        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_viewer);
        ButterKnife.bind(this);
        setSupportActionBar(mStoryToolbar);

        final Intent launchIntent = getIntent();
        if(Intent.ACTION_VIEW.equals(launchIntent.getAction())) {
            AdBlocker.init(this);
            final String data = launchIntent.getDataString();
            Loader.getInstance(this).loadItem(APIPaths.parseItemUrl(data), this);
        } else {
            if(UserActivity.mLaunchItem != null && UserActivity.mLaunchItem.isComment() && FeedActivity.mLaunchItem == null) {
                loadCommentParent(UserActivity.mLaunchItem);
                Toast.makeText(getApplicationContext(), R.string.text_traversing_comments, Toast.LENGTH_LONG).show();
            } else {
                if(FeedActivity.mLaunchItem != null) {
                    mLaunchItem = FeedActivity.mLaunchItem;
                    FeedActivity.mLaunchItem = null;
                } else {
                    mLaunchItem = UserActivity.mLaunchItem;
                }
                mRootItem = mLaunchItem;
                setupFragments(prefs.getPageTypes(), mLaunchItem);
                setTitle(mLaunchItem);
                if(launchIntent.getSerializableExtra("type") != null) {
                    final FragmentPagerAdapter.PageType type = (FragmentPagerAdapter.PageType) launchIntent.getSerializableExtra("type");
                    final int index = mAdapter.indexOf(type);
                    mStoryPager.post(() -> {
                        if(index != -1) mStoryPager.setCurrentItem(index);
                    });

                }
            }
        }

        originalFlags = getWindow().getDecorView().getSystemUiVisibility();
    }

    @Override
    public void openUser(Item item) {
        mLaunchItem = item;
        startActivity(new Intent(ViewerActivity.this, UserActivity.class),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        Pair.create(mBackButton, "button"),
                        Pair.create(mStoryAppbar, "appbar")).toBundle());
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
    public void onBackPressed() {
        if(mAdapter.onBackPressed()) {
            mStoryPager.setVisibility(View.INVISIBLE);
            mTitle.setVisibility(View.INVISIBLE);
            mBackButton.setVisibility(View.INVISIBLE);
            hideFab();
            Loader.getInstance(this).removeListeners();
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FeedActivity.returning) {
            finish();
        } else {
            super.onResume();
            mFab.postDelayed(() -> {
                mShouldShowFab = true;
                showFab();
            }, 300);
            mTracker.setScreenName(TAG);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            startActivity(new Intent(getApplicationContext(), FeedActivity.class));
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void setupFragments(FragmentPagerAdapter.PageType[] possiblePages, Item item) {
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), mStoryPager, possiblePages, item, mOpenedCommentId);
        mStoryPager.setAdapter(mAdapter);
        mStoryPager.setOffscreenPageLimit(mAdapter.getCount());
        mStoryTabs.setupWithViewPager(mStoryPager);
    }

    private void loadCommentParent(Item item) {
        mOpenedCommentId = item.getId();
        Loader.getInstance(this).loadItem(item.getParent(), this);
    }

    @Override
    public void itemLoaded(Item item) {
        //This is only called when the Activity is launched from a link outside the app
        if(item.isComment()) {
            loadCommentParent(item);
        } else {
            mRootItem = item;
            mLaunchItem = item;
            setupFragments(SharedPrefsController.getInstance(this).getPageTypes(), mLaunchItem);
            setTitle(mLaunchItem);
        }
    }

    @Override
    public void itemError(int id, int code) {
        if(Analytics.VERBOSE) Log.i(TAG, "itemError: " + id + " | " + code);
    }

    private void setTitle(Item item) {
        mTitle.setPaddingRelative(mTitle.getPaddingStart(), 16, mTitle.getPaddingEnd(), 16);
        mTitle.setText(item.getTitle());
        mUrl.setText(item.getFormattedURL());
        mStats.setText(item.getInfo());
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

}
