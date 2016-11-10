package com.tpb.hn.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tpb.hn.network.AdBlocker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by theo on 09/11/16.
 */

public class CachingAdBlockingWebView extends WebView {
    private static final String TAG = CachingAdBlockingWebView.class.getSimpleName();

    private ProgressBar mBoundProgressBar;

    public CachingAdBlockingWebView(Context context) {
        this(context, null);
    }

    public CachingAdBlockingWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public CachingAdBlockingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getSettings().setSupportZoom(true);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setWebViewClient(new WebViewClient() {
            private Map<String, Boolean> loadedUrls = new HashMap<>();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                boolean ad;
                if(!loadedUrls.containsKey(request.getUrl().toString())) {
                    ad = AdBlocker.isAd(request.getUrl().toString());
                    loadedUrls.put(request.getUrl().toString(), ad);
                } else {
                    ad = loadedUrls.get(request.getUrl().toString());
                }
                return ad ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(mBoundProgressBar != null) mBoundProgressBar.setVisibility(VISIBLE);
            }
        });
    }

    public void bindProgressBar(final ProgressBar progressBar, final boolean animate, final boolean hideWhenDone) {
        mBoundProgressBar = progressBar;
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(Build.VERSION.SDK_INT >= 24 ) {
                    progressBar.setProgress(newProgress,  animate);
                } else {
                    progressBar.setProgress(newProgress);
                }
                if(hideWhenDone && newProgress == 100) {
                    progressBar.setVisibility(GONE);
                    CachingAdBlockingWebView.this.setBackgroundColor(Color.WHITE);
                }

            }
        });
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        this.setVisibility(VISIBLE);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
        this.setVisibility(VISIBLE);
    }
    
    
}
