package com.tpb.hn.item.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tpb.hn.network.AdBlocker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by theo on 09/11/16.
 */

public class AdBlockingWebView extends WebView {
    private static final String TAG = AdBlockingWebView.class.getSimpleName();

    private ProgressBar mBoundProgressBar;
    private LinkHandler mHandler;
    private LoadListener mLoadListener;
    private boolean shouldBlockAds = true;
    private boolean shouldOverrideHeaders;
    //TODO- Allow setting max cache size, and clearing cache
    //http://stackoverflow.com/questions/14670638/webview-load-website-when-online-load-local-file-when-offline

    public AdBlockingWebView(Context context) {
        this(context, null);
    }

    public AdBlockingWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public AdBlockingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getSettings().setSupportZoom(true);
        this.setWebViewClient(new WebViewClient() {
            private final Map<String, Boolean> loadedUrls = new HashMap<>();

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(mHandler != null) {
                    final Pair<Boolean, String> val = mHandler.handleLink(url);
                    if(val.first) {
                        loadUrl(val.second);
                    }
                } else {
                    loadUrl(url);
                }
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(mHandler != null) {
                    final Pair<Boolean, String> val = mHandler.handleLink(request.getUrl().toString());
                    if(val.first) {
                        loadUrl(val.second);
                    }
                } else {
                    loadUrl(request.getUrl().toString());
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(mBoundProgressBar != null) mBoundProgressBar.setVisibility(VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(mLoadListener != null) mLoadListener.loadDone();
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if(shouldBlockAds) {
                    boolean ad;
                    if(!loadedUrls.containsKey(request.getUrl().toString())) {
                        ad = AdBlocker.isAd(request.getUrl().toString());
                        loadedUrls.put(request.getUrl().toString(), ad);
                    } else {
                        ad = loadedUrls.get(request.getUrl().toString());
                    }
                    return ad ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, request);
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    public void setHorizontalScrollingEnabled(boolean enabled) {
        if(enabled) {
            enableHorizontalScrolling();
        } else {
            disableHorizontalScrolling();
        }
    }

    public void setCacheEnabled() {
        getSettings().setAppCachePath(getContext().getApplicationContext().getCacheDir().getAbsolutePath());
        getSettings().setAllowFileAccess(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    public void setLoadFromCache() {
        getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    public void setLoadFromNetwork() {
        getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    public void enableHorizontalScrolling() {
        setOnTouchListener(null);
    }

    private void disableHorizontalScrolling() {
        setOnTouchListener((view, motionEvent) -> motionEvent.getAction() == MotionEvent.ACTION_MOVE);
    }

    public void setShouldBlockAds(boolean shouldBlock) {
        shouldBlockAds = shouldBlock;
    }


    public void bindProgressBar(final ProgressBar progressBar, final boolean animate, final boolean hideWhenDone) {
        mBoundProgressBar = progressBar;
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(Build.VERSION.SDK_INT >= 24) {
                    progressBar.setProgress(newProgress, animate);
                } else {
                    progressBar.setProgress(newProgress);
                }
                if(hideWhenDone && newProgress == 100) {
                    progressBar.setVisibility(GONE);
                }

            }
        });
    }

    public void setZoomEnabled(boolean canZoom) {
        getSettings().setSupportZoom(canZoom);
        getSettings().setBuiltInZoomControls(canZoom);
        getSettings().setDisplayZoomControls(false);
    }

    public void setLinkHandler(LinkHandler handler) {
        mHandler = handler;
    }

    public void setLoadDoneListener(LoadListener listener) {
        mLoadListener = listener;
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
        this.setVisibility(VISIBLE);
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        this.setVisibility(VISIBLE);
    }

    public interface LinkHandler {

        Pair<Boolean, String> handleLink(String url);

    }

    public interface LoadListener {

        void loadDone();

    }


}
