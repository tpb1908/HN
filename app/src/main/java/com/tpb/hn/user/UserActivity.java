package com.tpb.hn.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tpb.hn.R;
import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.User;
import com.tpb.hn.feed.FeedActivity;
import com.tpb.hn.feed.FeedAdapter;
import com.tpb.hn.helpers.APIPaths;
import com.tpb.hn.helpers.AdBlocker;
import com.tpb.hn.network.UserLoader;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.viewer.FragmentPagerAdapter;
import com.tpb.hn.viewer.ViewerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by theo on 19/11/16.
 */

public class UserActivity extends AppCompatActivity implements UserLoader.HNUserLoadDone, FeedAdapter.FeedManager {
    private static final String TAG = UserActivity.class.getSimpleName();
    public static Item mLaunchItem;
    @BindView(R.id.user_content_recycler) RecyclerView mRecycler;
    @BindView(R.id.user_content_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.user_name) TextView mName;
    @BindView(R.id.user_account_info) TextView mInfo;
    @BindView(R.id.user_account_about) TextView mAbout;
    @BindView(R.id.user_back_button) ImageButton mBackButton;
    @BindView(R.id.user_appbar) AppBarLayout mAppBar;
    private User mUser;
    private boolean viewsReady = false;
    private boolean userReady = false;
    private boolean mVolumeNavigation;
    private FeedAdapter mAdapter;

    @OnClick(R.id.user_back_button)
    void onClick() {
        onBackPressed();
    }

    @OnLongClick(R.id.user_back_button)
    boolean onLongClick() {
        FeedActivity.returning = true;
        finish();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPrefsController prefs = SharedPrefsController.getInstance(getApplicationContext());

        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        mRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new FeedAdapter(getApplication(), this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager(), mSwiper);
        mRecycler.setAdapter(mAdapter);
        mVolumeNavigation = prefs.getVolumeNavigation();
        final Intent launchIntent = getIntent();
        if(Intent.ACTION_VIEW.equals(launchIntent.getAction())) {
            AdBlocker.init(this);
            final String data = launchIntent.getDataString();
            new UserLoader(this).loadUser(APIPaths.parseUserUrl(data));

        } else {
            final Item item;
            if(FeedActivity.mLaunchItem != null) {
                item = FeedActivity.mLaunchItem;
                FeedActivity.mLaunchItem = null;
            } else {
                item = ViewerActivity.mLaunchItem;
            }
            new UserLoader(this).loadUser(item.getBy());
        }

        viewsReady = true;
        if(userReady) bindData();
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

    private void bindData() {
        mName.setText(mUser.getId());
        mInfo.setText(mUser.getInfo());
        if(mUser.getAbout() == null) {
            mAbout.setVisibility(View.GONE);
        } else {
            mAbout.setVisibility(View.VISIBLE); //This will only be needed if we refresh and the user has created an about
            mAbout.setText(Html.fromHtml(mUser.getAbout()));
            mAbout.setMovementMethod(LinkMovementMethod.getInstance());
            if(mAbout.getLineCount() > mAbout.getMaxLines()) {
                mAbout.setOnClickListener(view -> showLongAboutPopup());
            }
        }
        mAdapter.idsLoaded(mUser.getSubmitted());
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            startActivity(new Intent(getApplicationContext(), FeedActivity.class));
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void showLongAboutPopup() {
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(mUser.getId());
        final View text = LayoutInflater.from(this).inflate(R.layout.dialog_long_about, (ViewGroup) findViewById(android.R.id.content));
        ((TextView) text.findViewById(R.id.dialog_text)).setText(Html.fromHtml(mUser.getAbout()));
        adb.setView(text);

        adb.show();
    }

    @Override
    public void userLoaded(User user) {
        mUser = user;
        userReady = true;
        if(viewsReady) bindData();
    }

    @Override
    public void openItem(Item item) {
        if(item.isDeleted()) {
            Toast.makeText(getApplicationContext(),
                    String.format(getString(R.string.error_opening_deleted),
                            item instanceof Comment ? "comment" : "item"),
                    Toast.LENGTH_LONG).show();
        } else {
            mLaunchItem = item;
            Log.i(TAG, "openItem: " + item.toString());
            final Intent i = new Intent(UserActivity.this, ViewerActivity.class);
            i.putExtra("launcher", TAG);
            startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    Pair.create(mBackButton, "button"),
                    Pair.create(mAbout, "details"),
                    Pair.create(mAppBar, "appbar")).toBundle());
            overridePendingTransition(R.anim.slide_up, R.anim.none);
        }
    }

    @Override
    public void openItem(Item item, FragmentPagerAdapter.PageType type) {
        openItem(item);
    }

    @Override
    public void openUser(Item item) {
        //Used on refresh
        new UserLoader(this).loadUser(mUser.getId());
    }

    @Override
    public void displayLastUpdate(long lastUpdate) {
        //Ignored
    }

    @Override
    public void refresh() {
        //This may be used if we implement search
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FeedActivity.returning) finish();

    }
}
