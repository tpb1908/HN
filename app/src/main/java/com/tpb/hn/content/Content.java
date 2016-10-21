package com.tpb.hn.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.story.StoryAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity implements HNLoader.HNItemLoadDone {
    private static final String TAG = Content.class.getSimpleName();

    private PanelController mPanelController;
    private StoryAdapter mStoryAdapter;

    @BindView(R.id.content_toolbar)
    Toolbar mContentToolbar;

    @BindView(R.id.story_toolbar)
    Toolbar mStoryToolbar;

    @BindView(R.id.story_pager)
    ViewPager mStoryPager;

    @BindView(R.id.story_appbar)
    AppBarLayout mStoryAppbar;

    @BindView(R.id.story_panel)
    RelativeLayout mStoryPanel;

    @BindView(R.id.nav_spinner)
    Spinner mNavSpinner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        setSupportActionBar(mContentToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //TODO- Async do checks and start loading content
        mPanelController = new PanelController(
                (SlidingUpPanelLayout) ButterKnife.findById(this, R.id.sliding_layout));


        mStoryAdapter = new StoryAdapter(getSupportFragmentManager(),
                new StoryAdapter.PageType[] {StoryAdapter.PageType.BROWSER, StoryAdapter.PageType.COMMENTS, StoryAdapter.PageType.READABILITY, StoryAdapter.PageType.SKIMMER});

        mStoryPager.setAdapter(mStoryAdapter);
        mStoryPager.setOffscreenPageLimit(mStoryAdapter.getCount());

        ((TabLayout) ButterKnife.findById(this, R.id.story_tabs)).setupWithViewPager(mStoryPager);

        mNavSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        ));

        Log.i(TAG, "onCreate: ");

        new HNLoader(this).getTopItem();
//        AndroidNetworking.get(APIPaths.getItemPath(12759697))
//                .setTag("item")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsJSONObject(new JSONObjectRequestListener() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.i(TAG, "onResponse: Parsed " + HNParser.JSONToItem(response).toString());
//                        } catch(JSONException je) {
//                            Log.e(TAG, "onResponse: ", je);
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Log.e(TAG, "onError: ", anError );
//                    }
//                });
//        AndroidNetworking.get(APIPaths.getUserPath("owenwil"))
//                .setTag("user")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsJSONObject(new JSONObjectRequestListener() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.i(TAG, "onResponse: Parsed " + HNParser.JSONToUser(response).toString());
//                        } catch(JSONException je) {
//                            Log.e(TAG, "onResponse: ", je);
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Log.e(TAG, "onError: ", anError );
//                    }
//                });
    }

    @Override
    public void itemLoaded(Item item, boolean success) {
        mStoryAdapter.loadStory(item);
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        mPanelController.onResume();
    }

    @Override
    public void onBackPressed() {
        //TODO- Allow the fragments to override back press
        if(mPanelController.isExpanded()) {
            mPanelController.collapse();
        } else {
            super.onBackPressed();
        }
    }

}
