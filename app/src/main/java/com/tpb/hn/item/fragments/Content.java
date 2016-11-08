package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.network.ReadabilityLoader;

import org.json.JSONObject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 06/11/16.
 */

public class Content extends Fragment implements ItemLoader, ReadabilityLoader.ReadabilityLoadDone, ItemAdapter.FragmentCycle {
    private static final String TAG = Content.class.getSimpleName();
    private Tracker mTracker;

    private Unbinder unbinder;

    @BindColor(R.color.md_grey_50)
    int lightBG;

    @BindColor(R.color.md_grey_bg)
    int darkBG;

    @BindColor(R.color.colorPrimaryText)
    int lightText;

    @BindColor(R.color.colorPrimaryTextInverse)
    int darkText;

    @BindView(R.id.fullscreen)
    FrameLayout mFullscreen;

    @BindView(R.id.webview_scroller)
    NestedScrollView mScrollView;

    @BindView(R.id.webview)
    WebView mWebView;

    @BindView(R.id.webview_container)
    FrameLayout mWebContainer;

    private ItemAdapter.PageType mType;

    private boolean mIsFullscreen = false;
    private boolean mIsArticleReady;


    private String url;
    private String readablePage;

    public static Content newInstance(ItemAdapter.PageType type) {
        if(type == ItemAdapter.PageType.COMMENTS || type == ItemAdapter.PageType.SKIMMER) {
            throw new IllegalArgumentException("Page type must be browser, amp page, or readability");
        }
        final Content content = new Content();
        content.mType = type;

        return content;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_content, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        if(mIsArticleReady) {
            bindData();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("url") != null) {
                url = savedInstanceState.getString("url");
                readablePage = savedInstanceState.getString("readablePage");
                bindData();
            }
        }
        //mWebView.bindProgressBar(mProgressSpinner, true, true);
        mWebView.setNestedScrollingEnabled(false);
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        return inflated;
    }

    private void bindData() {

    }

    private void toggleFullscreen(boolean fullscreen) {
        mIsFullscreen = fullscreen;
        if(fullscreen) {
            mWebContainer.removeView(mWebView);
            mFullscreen.addView(mWebView);
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.setLayoutParams(params);
        } else {
            mFullscreen.removeView(mWebView);
            mWebContainer.addView(mWebContainer);
            final ViewGroup.LayoutParams params = mWebContainer.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mWebContainer.setLayoutParams(params);
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
        outState.putString("readablePage", readablePage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void loadItem(Item item) {
        if(mType == ItemAdapter.PageType.BROWSER) {
            //Load url
        } else if(mType == ItemAdapter.PageType.AMP_READER) {
            //Load amp url
        } else if(mType == ItemAdapter.PageType.TEXT_READER) {
            /*
            Check if there is a URL, if so load it
            Otherwise load the items text
             */
        }
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        //If a JSONObject is returned we have loaded the boilerpipe page
    }

    @Override
    public void loadDone(String result, boolean success, int code) {
        //We have loaded the boilerpipe page
    }

}
