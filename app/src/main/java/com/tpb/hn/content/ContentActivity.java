package com.tpb.hn.content;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.data.Formatter;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.Login;
import com.tpb.hn.settings.SettingsActivity;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.user.UserViewActivity;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 * Main activity
 * Displays the content feed
 */

public class ContentActivity extends AppCompatActivity implements ContentAdapter.ContentManager, Login.LoginListener {
    private static final String TAG = ContentActivity.class.getSimpleName();
    public static Item mLaunchItem;
    @BindView(R.id.content_toolbar) Toolbar mContentToolbar;
    @BindView(R.id.content_appbar) AppBarLayout mAppBar;
    @BindView(R.id.nav_spinner) Spinner mNavSpinner;
    @BindView(R.id.content_recycler) FastScrollRecyclerView mRecycler;
    @BindView(R.id.content_swiper) SwipeRefreshLayout mRefreshSwiper;
    @BindView(R.id.content_subtitle) TextView mSubtitle;

    @BindView(R.id.content_search_filters) Toolbar mSearchFilterHolder;
    @BindView(R.id.content_toolbar_switcher) ViewSwitcher mSwitcher;
    @BindView(R.id.content_search_edittext) EditText mSearch;
    @BindView(R.id.button_search) ImageButton mSearchButton;
    @BindView(R.id.button_close_search) ImageButton mCloseSearchButton;
    @BindView(R.id.spinner_filter_type) Spinner mTypeSpinner;
    @BindView(R.id.spinner_filter_date) Spinner mDateSpinner;
    @BindView(R.id.spinner_filter_sort) Spinner mSortSpinner;
    private Tracker mTracker;
    private ContentAdapter mAdapter;
    private boolean mVolumeNavigation;
    private int mThemePostponeTime = Integer.MAX_VALUE;
    private boolean mIsSearching = false;
    private boolean mIsKeyboardOpen = false;

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
        if(prefs.getBottomToolbar()) {
            setContentView(R.layout.activity_content_bottom);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            ButterKnife.bind(this);
            mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                boolean onscreen = true;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(dy > 10) {
                        if(onscreen && !mIsSearching) {
                            mAppBar.animate().translationY(mAppBar.getHeight());
                            onscreen = false;
                        }
                    } else {
                        if(!onscreen) {
                            mAppBar.animate().translationY(0);
                            onscreen = true;
                        }
                    }
                }
            });
        } else {
            setContentView(R.layout.activity_content_top);
            ButterKnife.bind(this);
        }

        AdBlocker.init(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());

        mRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new ContentAdapter(getApplicationContext(), this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager(), mRefreshSwiper);
        mRecycler.setAdapter(mAdapter);
        mVolumeNavigation = prefs.getVolumeNavigation();

        mSearchButton.setOnClickListener((view) -> {
            if(!mIsSearching) {
                mIsSearching = true;
                mSwitcher.showNext();
                mSearchFilterHolder.setVisibility(View.VISIBLE);
                mSearch.requestFocus();
                Util.showKeyboard(this, mSearch);
                mIsKeyboardOpen = true;
                mSwitcher.setInAnimation(this, android.R.anim.fade_in);
                mSwitcher.setOutAnimation(this, android.R.anim.fade_out);
            } else {
                //Perform search
            }
        });

        mCloseSearchButton.setOnClickListener((view) -> {
            mIsSearching = false;
            mSearch.clearFocus();
            if(mIsKeyboardOpen) {
                //TODO Find out how to deal with keyboard closed by back press (As it doesn't trigger the key listeners)
                Util.hideKeyboard(this, mSearch, new ResultReceiver(new Handler(Looper.getMainLooper())) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        mIsKeyboardOpen = false;
                        try {
                            Thread.sleep(20);
                        } catch(InterruptedException ignored) {
                        } //~ 1 frame stops animation judder
                        mSwitcher.showNext();
                        mSearchFilterHolder.setVisibility(View.GONE);
                        mSwitcher.setInAnimation(ContentActivity.this, R.anim.expand_horizontal);
                        mSwitcher.setOutAnimation(ContentActivity.this, android.R.anim.fade_out);
                    }
                });
            } else {
                mSwitcher.showNext();
                mSearchFilterHolder.setVisibility(View.GONE);
                mSwitcher.setInAnimation(ContentActivity.this, R.anim.expand_horizontal);
                mSwitcher.setOutAnimation(ContentActivity.this, android.R.anim.fade_out);
            }
        });


        setupSpinners();

        mContentToolbar.setTitle("");
        setSupportActionBar(mContentToolbar);


        final Handler timeAgoHandler = new Handler();
        timeAgoHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mSubtitle != null) {
                    mAdapter.getLastUpdate();

                    checkThemeChange(true);

                    timeAgoHandler.postDelayed(this, 1000 * 60);
                }
            }
        }, 1000 * 60);
        checkThemeChange(false);

        //final String[] values = new String[] {"Test", "Test1", "Test2", "Test3", "Test4", "Test5", "Test6", "Test7", "Test8", "Test9", "Test10", "Test11", "Test12", "Test13", "Test14", "Test15", "Test16"};
        //DraggableListDialog.newInstance(values).show(getSupportFragmentManager(), "Test");
    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(mVolumeNavigation) {
            switch(event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if(event.getAction() == KeyEvent.ACTION_DOWN) {
                        mAdapter.scrollUp();
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if(event.getAction() == KeyEvent.ACTION_DOWN) {
                        mAdapter.scrollDown();
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void setupSpinners() {
        final ArrayAdapter<CharSequence> navAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        );
        navAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNavSpinner.setAdapter(navAdapter);

        mNavSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.loadItems(Util.getSection(ContentActivity.this, mNavSpinner.getSelectedItem().toString()));
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Analytics.CATEGORY_NAVIGATION)
                        .setAction(Analytics.KEY_PAGE + mNavSpinner.getSelectedItem().toString())
                        .build());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final ArrayAdapter<CharSequence> dateAdapter = new ArrayAdapter<>(
                this,
                R.layout.simple_spinner_dropdown_item,
                android.R.id.text1,
                getResources().getStringArray(R.array.date_filter_spinner_items)
        );
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateSpinner.setAdapter(dateAdapter);

        final ArrayAdapter<CharSequence> typeAdapter = new ArrayAdapter<>(
                this,
                R.layout.simple_spinner_dropdown_item,
                android.R.id.text1,
                getResources().getStringArray(R.array.type_filter_spinner_items)
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(typeAdapter);

        final ArrayAdapter<CharSequence> sortAdapter = new ArrayAdapter<>(
                this,
                R.layout.simple_spinner_dropdown_item,
                android.R.id.text1,
                getResources().getStringArray(R.array.sort_filter_spinner_items)
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortSpinner.setAdapter(sortAdapter);

    }

    private void checkThemeChange(boolean showDialog) {
        final SharedPrefsController prefs = SharedPrefsController.getInstance(getApplicationContext());
        if(prefs.getAutoDark()) {
            final Calendar c = Calendar.getInstance();
            final int time = Formatter.hmToInt(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

            final android.util.Pair<Integer, Integer> times = prefs.getDarkTimeRange();

        /*
            time greater than start and last time less than start
            time greater than end and last time less than end
         */
            final boolean dark = (time >= times.first && time < times.second) //Standard s t e
                    || (time < times.first && times.second < times.first && time < times.second); // s mid t e
            if(Analytics.VERBOSE) Log.i(TAG, "checkThemeChange: " + dark);
            if(dark != prefs.getUseDarkTheme()) {
                if(!showDialog) {
                    prefs.setUseDarkTheme(dark);
                    recreate();
                } else if(time - mThemePostponeTime > 5) { //Wait 5 minutes to remind
                    //FIXME- Dialogs stacking up
                    new MaterialDialog.Builder(this)
                            .title(R.string.title_change_theme)
                            .positiveText(android.R.string.ok)
                            .negativeText(R.string.action_later)
                            .onPositive((dialog, which) -> {
                                prefs.setUseDarkTheme(dark);
                                ContentActivity.this.recreate();
                            })
                            .onNegative((dialog, which) -> mThemePostponeTime = time)
                            .show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivityForResult(new Intent(ContentActivity.this, SettingsActivity.class),
                1,
                getSharedTransition().toBundle());
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            final boolean shouldRestart = data.getBooleanExtra("restart", false);
            if(Analytics.VERBOSE) Log.i(TAG, "onActivityResult: " + shouldRestart);
            if(shouldRestart) {
                recreate();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mIsSearching) {
            mCloseSearchButton.callOnClick();
        } else {
            //TODO Option for prompting on back pressed
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mAdapter.getLastUpdate();
        mAdapter.getPrefs(this);
        mVolumeNavigation = SharedPrefsController.getInstance(this).getVolumeNavigation();
        checkThemeChange(true);
    }

    @Override
    public void loginResponse(boolean success) {
        if(Analytics.VERBOSE) Log.i(TAG, "loginResponse: Was login successful ? " + success);
    }

    @Override
    public void openItem(Item item) {
        final Intent i = new Intent(ContentActivity.this, ItemViewActivity.class);
        mLaunchItem = item;
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
        mAdapter.beginBackgroundLoading();
    }

    @Override
    public void openItem(Item item, FragmentPagerAdapter.PageType type) {
        final Intent i = new Intent(ContentActivity.this, ItemViewActivity.class);
        mLaunchItem = item;
        i.putExtra("type", type);
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
        mAdapter.beginBackgroundLoading();

    }

    @Override
    public void openUser(Item item) {
        mLaunchItem = item;
        final Intent i = new Intent(ContentActivity.this, UserViewActivity.class);
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
    }

    @Override
    public void displayLastUpdate(long lastUpdate) {
        mSubtitle.setText(String.format(this.getString(R.string.text_last_updated), Formatter.shortTimeAgo(lastUpdate)));
    }

    private ActivityOptionsCompat getSharedTransition() {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                Pair.create(mIsSearching ? mCloseSearchButton : mNavSpinner, "button"),
                Pair.create(mAppBar, "appbar")
        );
    }

    public enum Section {
        TOP, BEST, NEW, ASK, SHOW, JOB, SAVED, SEARCH
    }

}
