package com.tpb.hn.story;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 * Loads the full page of an article
 * Data can be cached by creating a new webview instance somewhere
 * else and loading the page
 */

public class Browser extends Fragment implements StoryLoader, StoryAdapter.FragmentCycle {
    private static final String TAG = Browser.class.getSimpleName();

    private Unbinder unbinder;


    @BindView(R.id.browser_webview)
    WebView mWebView;

    @BindView(R.id.browser_loading_spinner)
    ProgressBar mLoadingSpinner;

    private boolean isArticleReady = false;
    private String url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_browser, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        if(isArticleReady) mWebView.loadUrl(url);
        return inflated;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadingSpinner.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void loadStory(Item item) {
        this.url = item.getUrl();
        isArticleReady = true;
        if(mWebView != null) mWebView.loadUrl(url);
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

    }
}
