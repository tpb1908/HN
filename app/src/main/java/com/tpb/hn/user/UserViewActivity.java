package com.tpb.hn.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tpb.hn.R;
import com.tpb.hn.content.ContentActivity;
import com.tpb.hn.content.ContentAdapter;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.User;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.loaders.HNUserLoader;
import com.tpb.hn.storage.SharedPrefsController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.type;

/**
 * Created by theo on 19/11/16.
 */

public class UserViewActivity extends AppCompatActivity implements HNUserLoader.HNUserLoadDone, ContentAdapter.ContentManager {
    private static final String TAG = UserViewActivity.class.getSimpleName();

    @BindView(R.id.user_content_recycler) RecyclerView mRecycler;
    @BindView(R.id.user_content_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.user_name) TextView mName;
    @BindView(R.id.user_account_info) TextView mInfo;
    @BindView(R.id.user_account_about) TextView mAbout;
    @BindView(R.id.user_back_button) ImageButton mBackButton;
    @BindView(R.id.user_appbar) AppBarLayout mAppBar;

    @OnClick(R.id.user_back_button)
    void onClick() {
        onBackPressed();
    }

    private User mUser;
    private boolean viewsReady = false;
    private boolean userReady = false;

    public static Item mLaunchItem;

    private ContentAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPrefsController prefs = SharedPrefsController.getInstance(getApplicationContext());

        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_user_view);
        ButterKnife.bind(this);
        mRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new ContentAdapter(getApplication(), this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager(), mSwiper);
        mRecycler.setAdapter(mAdapter);
        final Intent launchIntent = getIntent();
        if(Intent.ACTION_VIEW.equals(launchIntent.getAction())) {
            AdBlocker.init(this);
            final String data = launchIntent.getDataString();
            new HNUserLoader(this).loadUser(APIPaths.parseUserUrl(data));

        } else {
            final Item item;
            if(ContentActivity.mLaunchItem != null) {
                item = ContentActivity.mLaunchItem;
                ContentActivity.mLaunchItem = null;
            } else {
                item = ItemViewActivity.mLaunchItem;
            }
            new HNUserLoader(this).loadUser(item.getBy());
        }

        viewsReady = true;
        if(userReady) bindData();
    }

    private void bindData() {
        mName.setText(mUser.getId());
        mInfo.setText(mUser.getInfo());
        if(mUser.getAbout() == null) {
            mAbout.setVisibility(View.GONE);
        } else {
            mAbout.setVisibility(View.VISIBLE); //This will only be needed if we refresh and the user has created an about
            mAbout.setText(Html.fromHtml(mUser.getAbout()));
            //TODO- Find out how to deal with links that aren't <a></a>
            mAbout.setMovementMethod(LinkMovementMethod.getInstance());
        }
        mAdapter.IdLoadDone(mUser.getSubmitted());
    }

    @Override
    public void userLoaded(User user) {
        mUser = user;
        userReady = true;
        if(viewsReady) bindData();
        Log.i(TAG, "userLoaded: " + user.toString());
    }

    @Override
    public void openItem(Item item) {
        if(item.isDeleted()) {
            Toast.makeText(getApplicationContext(), R.string.error_opening_deleted, Toast.LENGTH_LONG).show();
        } else {
            mLaunchItem = item;
            Log.i(TAG, "openItem: " + item.toString());
            final Intent i = new Intent(UserViewActivity.this, ItemViewActivity.class);
            startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    Pair.create((View) mBackButton, "button"),
                    Pair.create((View) mAppBar, "appbar")).toBundle());
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
        new HNUserLoader(this).loadUser(mUser.getId());
    }

    @Override
    public void displayLastUpdate(long lastUpdate) {
        //Ignored
    }
}
