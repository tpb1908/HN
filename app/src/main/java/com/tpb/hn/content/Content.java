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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.HNParser;
import com.tpb.hn.story.StoryAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity {
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
        mStoryPager.setOffscreenPageLimit(Integer.MAX_VALUE);
        mStoryPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int pos = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((StoryAdapter.FragmentCycle) mStoryAdapter.getItem(position)).onResumeFragment();
                ((StoryAdapter.FragmentCycle) mStoryAdapter.getItem(pos)).onPauseFragment();
                pos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ((TabLayout) ButterKnife.findById(this, R.id.story_tabs)).setupWithViewPager(mStoryPager);




        mNavSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        ));

        final Item test = new Item();
        test.setUrl("http://www.bbc.co.uk/news/uk-37725327");
        mStoryAdapter.loadStory(test);
        Log.i(TAG, "onCreate: " + APIPaths.getMaxItemPath());
        AndroidNetworking.get(APIPaths.getItemPath(12759697))
                .setTag("item")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, "onResponse: Parsed " + HNParser.JSONToItem(response).toString());
                        } catch(JSONException je) {
                            Log.e(TAG, "onResponse: ", je);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: ", anError );
                    }
                });
        AndroidNetworking.get(APIPaths.getUserPath("owenwil"))
                .setTag("user")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, "onResponse: Parsed " + HNParser.JSONToUser(response).toString());
                        } catch(JSONException je) {
                            Log.e(TAG, "onResponse: ", je);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: ", anError );
                    }
                });
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
