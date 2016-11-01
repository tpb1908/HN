package com.tpb.hn.content;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.User;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemViewer;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.network.HNUserLoader;
import com.tpb.hn.network.Login;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity implements HNLoader.HNItemLoadDone, ContentAdapter.ContentOpener, Login.LoginListener, HNUserLoader.HNUserLoadDone {
    private static final String TAG = Content.class.getSimpleName();
    private Tracker mTracker;

    @BindView(R.id.content_toolbar)
    Toolbar mContentToolbar;

    @BindView(R.id.nav_spinner)
    Spinner mNavSpinner;

    @BindView(R.id.content_recycler)
    FastScrollRecyclerView mRecycler;

    @BindView(R.id.content_swiper)
    SwipeRefreshLayout mSwiper;

    private ContentAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((Analytics) getApplication()).getDefaultTracker();
        final SharedPrefsController prefs = SharedPrefsController.getInstance(this);
        prefs.setUseDarkTheme(true);
        prefs.setUseCards(true);
        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        AdBlocker.init(this);
        AndroidNetworking.initialize(getApplicationContext());

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ContentAdapter(this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager(), mSwiper);
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

    }

    @Override
    public void userLoaded(User user) {

    }

    @Override
    public void response(boolean success) {
        Log.i(TAG, "response: Was login successful ? " + success);
    }

    @Override
    public void itemLoaded(Item item, boolean success) {

    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {
    }

    @Override
    public void openItem(Item item) {
        final Intent i = new Intent(Content.this, ItemViewer.class);
        i.putExtra("item", item);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up, R.anim.none);
    }

    @Override
    public void openUser(Item item) {

    }

    @Override
    public void openPage(Item item, ItemAdapter.PageType type) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        //TODO- Allow the fragments to override back press
        super.onBackPressed();
    }

}
