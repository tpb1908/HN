package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.item.ScrollingAdBlockingWebView;
import com.tpb.hn.network.APIPaths;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 * Loads the full page of an article
 * Data can be cached by creating a new webview instance somewhere
 * else and loading the page
 *
 */

public class Browser extends Fragment implements ItemLoader, ItemAdapter.FragmentCycle {
    private static final String TAG = Browser.class.getSimpleName();

    private Unbinder unbinder;


    @BindView(R.id.browser_webview)
    ScrollingAdBlockingWebView mWebView;

    @BindView(R.id.browser_loading_spinner)
    ProgressBar mLoadingSpinner;

    private boolean isArticleReady = false;
    private String url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_browser, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        if(isArticleReady) loadURL();
        if(savedInstanceState != null) mWebView.restoreState(savedInstanceState);
        return inflated;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.bindProgressBar(mLoadingSpinner, true, true);

    }

    private void loadURL() {
        Log.i(TAG, "loadURL: WebView loading");
        if(url != null) {
            if(url.endsWith(".pdf")) {
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl(APIPaths.getPDFDisplayPath(url));
            } else {
                mWebView.getSettings().setJavaScriptEnabled(false);
                mWebView.loadUrl(url);
            }
        }
    }

    @Override
    public void loadItem(Item item) {
        this.url = item.getUrl();
        isArticleReady = true;
        if(mWebView != null) loadURL();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
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
