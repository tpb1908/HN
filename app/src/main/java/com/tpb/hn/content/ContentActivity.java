package com.tpb.hn.content;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.SettingsActivity;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.Login;
import com.tpb.hn.storage.SharedPrefsController;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 * Main activity
 * Displays the content feed
 */

public class ContentActivity extends AppCompatActivity implements ContentAdapter.ContentOpener, Login.LoginListener {
    private static final String TAG = ContentActivity.class.getSimpleName();
    private Tracker mTracker;

    @BindView(R.id.content_toolbar) Toolbar mContentToolbar;
    @BindView(R.id.content_appbar) AppBarLayout mAppBar;
    @BindView(R.id.nav_spinner) Spinner mNavSpinner;
    @BindView(R.id.content_recycler) FastScrollRecyclerView mRecycler;
    @BindView(R.id.content_swiper) SwipeRefreshLayout mRefreshSwiper;
    @BindView(R.id.content_subtitle) TextView mSubtitle;

    private ContentAdapter mAdapter;

    public static Item mLaunchItem;

    private long lastUpdateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = ((Analytics) getApplication()).getDefaultTracker();
        final SharedPrefsController prefs = SharedPrefsController.getInstance(getApplicationContext());

        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);

        AdBlocker.init(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());

        mRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new ContentAdapter(getApplicationContext(), this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager(), mRefreshSwiper);
        mRecycler.setAdapter(mAdapter);

        mNavSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        ));
        mNavSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.loadItems(mNavSpinner.getSelectedItem().toString());
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Analytics.CATEGORY_NAVIGATION)
                        .setAction(Analytics.KEY_PAGE + mNavSpinner.getSelectedItem().toString())
                        .build());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mContentToolbar.setTitle("");
        setSupportActionBar(mContentToolbar);
        lastUpdateTime = System.currentTimeMillis();
        Log.i(TAG, "onCreate: Starting");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivityForResult(new Intent(ContentActivity.this, SettingsActivity.class), 1);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final boolean shouldRestart = data.getBooleanExtra("restart", false);
        Log.i(TAG, "onActivityResult: " + shouldRestart);
        if(shouldRestart) {
            recreate();
        }
    }

    @Override
    public void loginResponse(boolean success) {
        Log.i(TAG, "loginResponse: Was login successful ? " + success);
    }

    @Override
    public void openItem(Item item) {
        final Intent i = new Intent(ContentActivity.this, ItemViewActivity.class);
        i.putExtra("item", item);
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
        mAdapter.beginBackgroundLoading();
    }

    @Override
    public void openUser(Item item) {

    }

    @Override
    public void openItem(Item item, FragmentPagerAdapter.PageType type) {
        final Intent i = new Intent(ContentActivity.this, ItemViewActivity.class);
        mLaunchItem = item;
        //i.putExtra("item", item);
        i.putExtra("type", type);
        //i.putExtra("comments", item.getCommentJSON());
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
        mAdapter.beginBackgroundLoading();

    }

    private ActivityOptionsCompat getSharedTransition() {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                Pair.create((View) mNavSpinner, "button"),
                Pair.create((View) mAppBar, "appbar"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mAdapter.cancelBackgroundLoading();
    }

}
