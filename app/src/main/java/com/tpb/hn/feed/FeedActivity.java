package com.tpb.hn.feed;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.helpers.AdBlocker;
import com.tpb.hn.helpers.Formatter;
import com.tpb.hn.helpers.Util;
import com.tpb.hn.network.Login;
import com.tpb.hn.settings.FeedSettingsActivity;
import com.tpb.hn.settings.Preferences;
import com.tpb.hn.settings.SettingsActivity;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.user.UserActivity;
import com.tpb.hn.viewer.FragmentPagerAdapter;
import com.tpb.hn.viewer.ViewerActivity;
import com.tpb.hn.viewer.views.FloatingFAB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 17/10/16.
 * Main activity
 * Displays the content feed
 */

public class FeedActivity extends AppCompatActivity implements FeedAdapter.FeedManager, Login.LoginListener {
    private static final String TAG = FeedActivity.class.getSimpleName();
    private static final String TOP_ACTION = "com.tpb.hn.feed.feedactivity.top";
    private static final String BEST_ACTION = "com.tpb.hn.feed.feedactivity.best";
    private static final String NEW_ACTION = "com.tpb.hn.feed.feedactivity.new";
    private static final String SEARCH_ACTION = "com.tpb.hn.feed.feedactivity.search";

    public static Item mLaunchItem;
    public static boolean returning = false;
    @BindView(R.id.content_toolbar) Toolbar mContentToolbar;
    @BindView(R.id.content_appbar) AppBarLayout mAppBar;
    @BindView(R.id.nav_spinner) Spinner mNavSpinner;
    @BindView(R.id.content_recycler) FastScrollRecyclerView mRecycler;
    @BindView(R.id.content_swiper) SwipeRefreshLayout mRefreshSwiper;
    @BindView(R.id.content_subtitle) TextView mSubtitle;
    @BindView(R.id.floating_fab) FloatingFAB mFFAB;

    @BindView(R.id.content_search_filters) Toolbar mSearchFilterHolder;
    @BindView(R.id.content_toolbar_switcher) ViewSwitcher mSwitcher;
    @BindView(R.id.content_search_edittext) EditText mSearch;
    @BindView(R.id.button_search) ImageButton mSearchButton;
    @BindView(R.id.button_close_search) ImageButton mCloseSearchButton;
    @BindView(R.id.spinner_filter_type) Spinner mTypeSpinner;
    @BindView(R.id.spinner_filter_date) Spinner mDateSpinner;
    @BindView(R.id.spinner_filter_sort) Spinner mSortSpinner;
    private Tracker mTracker;
    private FeedAdapter mAdapter;
    private boolean mVolumeNavigation;
    private int mThemePostponeTime = Integer.MAX_VALUE;
    private boolean mIsSearching = false;
    private boolean mHasSearched = false;
    private boolean mIsKeyboardOpen = false;

    private long mFilterDateStart = new Date().getTime();
    private long mFilterDateEnd = 0;
    private ItemType mFilterType = ItemType.ALL;
    private boolean mFilterSort;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((Analytics) getApplication()).getDefaultTracker();

        new Preferences(this, key -> Log.i(TAG, "preferenceChanged: " + getString(key)), R.string.pref_feed_floating_fab, R.string.pref_feed_scroll_bar);
        new Handler().postDelayed(() -> Preferences.setFeedFloatingFABEnabled(FeedActivity.this, true), 1000);


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
                    } else if(dy < 0) {
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) setupAppShortcuts();

        setupSpinners();

        mRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new FeedAdapter(getApplicationContext(), this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager(), mRefreshSwiper);
        mRecycler.setAdapter(mAdapter);

        mFFAB.setListener(mAdapter);
        mFFAB.setVisibility(prefs.getShowFloatingFAB() ? View.VISIBLE : View.INVISIBLE);
        if(prefs.getShowFloatingFAB()) {
            mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                boolean scrollingDown;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if(scrollingDown != dy > 0) {
                        scrollingDown = dy > 0;
                        mFFAB.setState(scrollingDown ? FloatingFAB.FloatingFABState.DOWN : FloatingFAB.FloatingFABState.UP);
                    }
                    super.onScrolled(recyclerView, dx, dy);
                }

            });
        }

        mVolumeNavigation = prefs.getVolumeNavigation();

        mContentToolbar.setTitle("");
        setSupportActionBar(mContentToolbar);

        final Intent launchIntent = getIntent();
        if(launchIntent != null) {
            switch(launchIntent.getAction()) {
                case TOP_ACTION:
                    mNavSpinner.setSelection(0);
                    mAdapter.loadItems(Section.TOP);
                    break;
                case NEW_ACTION:
                    mNavSpinner.setSelection(1);
                    break;
                case BEST_ACTION:
                    mNavSpinner.setSelection(2);
                    break;
                case SEARCH_ACTION:
                    new Handler().postDelayed(() -> mSearchButton.callOnClick(), 300);
            }
        }

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

    @OnClick(R.id.button_close_search)
    void closeClick() {
        mIsSearching = false;
        mSearch.clearFocus();
        if(mIsKeyboardOpen) {
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
                    mSwitcher.setInAnimation(FeedActivity.this, R.anim.expand_horizontal);
                    mSwitcher.setOutAnimation(FeedActivity.this, android.R.anim.fade_out);
                }
            });
        } else {
            mSwitcher.showNext();
            mSearchFilterHolder.setVisibility(View.GONE);
            mSwitcher.setInAnimation(FeedActivity.this, R.anim.expand_horizontal);
            mSwitcher.setOutAnimation(FeedActivity.this, android.R.anim.fade_out);
        }
    }

    @OnClick(R.id.button_search)
    void searchClick() {
        if(!mIsSearching) {
            mIsSearching = true;
            mSwitcher.showNext();
            mSearchFilterHolder.setVisibility(View.VISIBLE);
            mSearch.requestFocus();
            Util.showKeyboard(this, mSearch);
            mIsKeyboardOpen = true;
            mSwitcher.setInAnimation(this, android.R.anim.fade_in);
            mSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        } else if(!mSearch.getText().toString().isEmpty()) {
            mAdapter.search(mSearch.getText().toString(), mFilterType, mFilterDateStart, mFilterDateEnd, mFilterSort);
            mHasSearched = true;
            //Perform search
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private void setupAppShortcuts() {
        final ShortcutManager manager = getSystemService(ShortcutManager.class);
        if(manager.getDynamicShortcuts().size() == 0) {
            final List<ShortcutInfo> shortcuts = new ArrayList<>();
            shortcuts.add(new ShortcutInfo.Builder(this, "top")
                    .setShortLabel(getString(R.string.shortcut_top))
                    .setIntent(new Intent(TOP_ACTION))
                    .build());
            shortcuts.add(new ShortcutInfo.Builder(this, "best")
                    .setShortLabel(getString(R.string.shortcut_best))
                    .setIntent(new Intent(BEST_ACTION))
                    .build());
            shortcuts.add(new ShortcutInfo.Builder(this, "new")
                    .setShortLabel(getString(R.string.shortcut_new))
                    .setIntent(new Intent(NEW_ACTION))
                    .build());
            shortcuts.add(new ShortcutInfo.Builder(this, "search")
                    .setShortLabel(getString(R.string.shortcut_search))
                    .setIntent(new Intent(SEARCH_ACTION))
                    .build());
            manager.setDynamicShortcuts(shortcuts);
        }

    }

    private void setupSpinners() {
        Log.i(TAG, "setupSpinners: Setting up");
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
                mHasSearched = false;
                mAdapter.loadItems(Util.getSection(FeedActivity.this, mNavSpinner.getSelectedItem().toString()));
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
                R.layout.listitem_simple_spinner_dropdown,
                android.R.id.text1,
                getResources().getStringArray(R.array.date_filter_spinner_items)
        );
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateSpinner.setAdapter(dateAdapter);
        mDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final String[] items = getResources().getStringArray(R.array.date_filter_spinner_items);
                if(view == null) return;
                final String selected = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                final Date date = new Date();
                Log.i(TAG, "onItemSelected: " + date.getTime());
                if(selected.equals(items[0])) { //All time
                    mFilterDateEnd = -1;
                    mFilterDateStart = -1;
                } else if(selected.equals(items[1])) { //Year
                    mFilterDateEnd = date.getTime() / 1000;
                    mFilterDateStart = mFilterDateEnd - (12 * 28 * 24 * 3600);
                } else if(selected.equals(items[2])) { //6 months
                    mFilterDateEnd = date.getTime() / 1000;
                    mFilterDateStart = mFilterDateEnd - (6 * 28 * 24 * 3600);
                } else if(selected.equals(items[3])) {
                    mFilterDateEnd = date.getTime() / 1000;
                    mFilterDateStart = mFilterDateEnd - (3 * 28 * 24 * 3600);
                } else if(selected.equals(items[4])) { //Month
                    mFilterDateEnd = date.getTime() / 1000;
                    mFilterDateStart = mFilterDateEnd - (28 * 24 * 3600);
                } else if(selected.equals(items[5])) { //Week
                    mFilterDateEnd = date.getTime() / 1000;
                    mFilterDateStart = mFilterDateEnd - (7 * 24 * 3600);
                } else if(selected.equals(items[6])) {
                    mFilterDateEnd = date.getTime() / 1000;
                    mFilterDateStart = mFilterDateEnd - (24 * 3600);
                } else {
                    showDateRangeDialog();
                }
                if(mHasSearched) mSearchButton.callOnClick();
                Log.i(TAG, "onItemSelected: " + mFilterDateStart + ", " + mFilterDateEnd);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        final ArrayAdapter<CharSequence> typeAdapter = new ArrayAdapter<>(
                this,
                R.layout.listitem_simple_spinner_dropdown,
                android.R.id.text1,
                getResources().getStringArray(R.array.type_filter_spinner_items)
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(typeAdapter);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final String[] items = getResources().getStringArray(R.array.type_filter_spinner_items);
                if(view == null) return;
                final String selected = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                if(selected.equals(items[0])) {
                    mFilterType = ItemType.ALL;
                } else if(selected.equals(items[1])) {
                    mFilterType = ItemType.STORY;
                } else if(selected.equals(items[2])) {
                    mFilterType = ItemType.COMMENT;
                } else if(selected.equals(items[3])) {
                    mFilterType = ItemType.POLL;
                } else if(selected.equals(items[4])) {
                    mFilterType = ItemType.SHOW;
                } else if(selected.equals(items[5])) {
                    mFilterType = ItemType.ASK;
                } else {
                    mFilterType = ItemType.SAVED;
                }
                if(mHasSearched) mSearchButton.callOnClick();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final ArrayAdapter<CharSequence> sortAdapter = new ArrayAdapter<>(
                this,
                R.layout.listitem_simple_spinner_dropdown,
                android.R.id.text1,
                getResources().getStringArray(R.array.sort_filter_spinner_items)
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortSpinner.setAdapter(sortAdapter);
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final String[] items = getResources().getStringArray(R.array.sort_filter_spinner_items);
                if(view == null) return;
                final String selected = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                mFilterSort = !selected.equals(items[0]);
                if(mHasSearched) mSearchButton.callOnClick();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void showDateRangeDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.text_title_date_range)
                .customView(R.layout.dialog_date_range, false)
                .positiveText(android.R.string.ok)
                .onPositive((dlg, which) -> {
                    final DatePicker start = (DatePicker) dlg.getCustomView().findViewById(R.id.date_start);
                    final DatePicker end = (DatePicker) dlg.getCustomView().findViewById(R.id.date_end);
                    final Calendar calendar = Calendar.getInstance();
                    calendar.set(start.getYear(), start.getMonth(), start.getDayOfMonth());
                    final long date1 = calendar.getTime().getTime() / 1000;
                    calendar.set(end.getYear(), end.getMonth(), end.getDayOfMonth());
                    final long date2 = calendar.getTime().getTime() / 1000;
                    if(date2 > date1) {
                        Toast.makeText(FeedActivity.this, R.string.text_switching_dates, Toast.LENGTH_SHORT).show();
                    }
                    mFilterDateStart = Math.min(date1, date2);
                    mFilterDateEnd = Math.max(date1, date2);
                    Log.i(TAG, "showDateRangeDialog: Start " + mFilterDateStart + ", " + mFilterDateEnd);

                })
                .build();
        dialog.show();
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
                    new MaterialDialog.Builder(this)
                            .title(R.string.title_change_theme)
                            .positiveText(android.R.string.ok)
                            .negativeText(R.string.action_later)
                            .onPositive((dialog, which) -> {
                                prefs.setUseDarkTheme(dark);
                                FeedActivity.this.recreate();
                            })
                            .onNegative((dialog, which) -> mThemePostponeTime = time)
                            .show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            startActivityForResult(new Intent(FeedActivity.this, SettingsActivity.class),
                    1,
                    getSharedTransition().toBundle());
        } else {
            startActivity(new Intent(FeedActivity.this, FeedSettingsActivity.class));
        }
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
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        returning = false;
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mAdapter.getLastUpdate();
        mAdapter.getPrefs(this);
        mVolumeNavigation = SharedPrefsController.getInstance(this).getVolumeNavigation();
        new Handler().postDelayed(() -> {
            if(SharedPrefsController.getInstance(this).getShowFloatingFAB()) {
                mFFAB.setAlpha(1.0f);
                mFFAB.show();
            } else {
                mFFAB.hide();
            }
        }, 600);


        checkThemeChange(true);
    }

    @Override
    public void loginResponse(boolean success) {
        if(Analytics.VERBOSE) Log.i(TAG, "loginResponse: Was login successful ? " + success);
    }

    @Override
    public void openItem(Item item) {
        final Intent i = new Intent(FeedActivity.this, ViewerActivity.class);
        mLaunchItem = item;
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
        mAdapter.beginBackgroundLoading();
    }

    @Override
    public void openItem(Item item, FragmentPagerAdapter.PageType type) {
        final Intent i = new Intent(FeedActivity.this, ViewerActivity.class);
        Log.i(TAG, "openItem: isComment " + item.isComment());
        mLaunchItem = item;
        i.putExtra("launcher", TAG);
        i.putExtra("type", type);
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
        mAdapter.beginBackgroundLoading();

    }

    @Override
    public void openUser(Item item) {
        mLaunchItem = item;
        final Intent i = new Intent(FeedActivity.this, UserActivity.class);
        i.putExtra("launcher", TAG);
        startActivity(i, getSharedTransition().toBundle());
        overridePendingTransition(R.anim.slide_up, R.anim.none);
    }

    @Override
    public void displayLastUpdate(long lastUpdate) {
        mSubtitle.setText(String.format(this.getString(R.string.text_last_updated), Formatter.shortTimeAgo(lastUpdate)));
    }

    @Override
    public void refresh() {
        mSearchButton.callOnClick();
    }

    private ActivityOptionsCompat getSharedTransition() {
        return SharedPrefsController.getInstance(this).getShowFloatingFAB() ?
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        Pair.create(mIsSearching ? mCloseSearchButton : mNavSpinner, "button"),
                        Pair.create(mAppBar, "appbar"),
                        Pair.create(mFFAB, "fab")
                ) :
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        Pair.create(mIsSearching ? mCloseSearchButton : mNavSpinner, "button"),
                        Pair.create(mAppBar, "appbar")
                );

    }

    public enum Section {
        TOP, BEST, NEW, ASK, SHOW, JOB, SAVED, SEARCH
    }

}
