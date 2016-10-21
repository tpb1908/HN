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
 * If Chrome custom tabs are supported, they will be used
 * Otherwise it will load a webview
 */

public class Browser extends Fragment implements StoryLoader, StoryAdapter.FragmentCycle {
    private static final String TAG = Browser.class.getSimpleName();

    private Unbinder unbinder;

    private boolean isUsingChromium = false;

    @BindView(R.id.browser_webview)
    WebView mWebView;

    @BindView(R.id.browser_loading_spinner)
    ProgressBar mLoadingSpinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_browser, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        return inflated;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.loadUrl("http://www.bbc.co.uk/news/science-environment-37707776");
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
        //Precache here
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
