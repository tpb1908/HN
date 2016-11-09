package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.ReadabilityLoader;
import com.tpb.hn.storage.SharedPrefsController;

import org.json.JSONObject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 */

public class Readability extends Fragment implements ItemLoader, ReadabilityLoader.ReadabilityLoadDone, ItemAdapter.FragmentCycle {
    private static final String TAG = Readability.class.getSimpleName();
    private Tracker mTracker;

    private Unbinder unbinder;

    private boolean fullscreen = false;

    @BindColor(R.color.md_grey_50)
    int lightBG;

    @BindColor(R.color.md_grey_bg)
    int darkBG;

    @BindColor(R.color.colorPrimaryText)
    int lightText;

    @BindColor(R.color.colorPrimaryTextInverse)
    int darkText;

    @BindView(R.id.test)
    TextView test;

    @BindView(R.id.fullscreen)
    FrameLayout mFullscreen;

    @BindView(R.id.webview_scroller)
    NestedScrollView mScrollView;

    @BindView(R.id.webview)
    WebView mWebView;

    @BindView(R.id.webview_container)
    FrameLayout mWebContainer;

//    @BindView(R.id.readability_webview)
//    ScrollingAdBlockingWebView mWebView;
//
//    @BindView(R.id.readability_loading_spinner)
//    ProgressBar mProgressSpinner;
//
//    @BindView(R.id.readability_error_message)
//    TextView mErrorTextView;

    private boolean ampView = true;

    private String url;
    private String boilerpipePage;

    private boolean isArticleReady = false;

    public static Readability newInstance() {
        return new Readability();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_readability, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        if(isArticleReady) {
            setupViews();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("url") != null) {
                url = savedInstanceState.getString("url");
                boilerpipePage = savedInstanceState.getString("boilerpipePage");
                setupViews();
            }
        }
        //mWebView.bindProgressBar(mProgressSpinner, true, true);
        mWebView.setNestedScrollingEnabled(false);
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        return inflated;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupViews() {
        //mProgressSpinner.setVisibility(View.GONE);
        //mErrorTextView.setVisibility(View.GONE);
        if(ampView) {
            mWebView.loadUrl(APIPaths.getMercuryAmpPath(url));
        } else {
            mWebView.loadData(boilerpipePage, "text/html", "utf-8");
        }
    }

    private void makeFullScreen() {
        mWebContainer.removeView(mWebView);
        mFullscreen.addView(mWebView);
        final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mWebView.setLayoutParams(params);
        fullscreen = true;
        Log.d(TAG, "makeFullScreen: scroller " + mScrollView.getMeasuredHeight() + ", container " + mWebContainer.getMeasuredHeight() + ", fullscreen " + mFullscreen.getMeasuredHeight() + ", test  "  + test.getMeasuredHeight());
        mFullscreen.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "makeFullScreen: scroller " + mScrollView.getMeasuredHeight() + ", container " + mWebContainer.getMeasuredHeight() + ", fullscreen " + mFullscreen.getMeasuredHeight() + ", test " + test.getMeasuredHeight() );
            }
        });
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        if(success) {
            try {
                isArticleReady = true;
                if(mWebView != null) setupViews();
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
                //mProgressSpinner.setVisibility(View.GONE);
            }
        } else {
            //mProgressSpinner.setVisibility(View.GONE);
            //mErrorTextView.setVisibility(View.VISIBLE);
            if(code == ReadabilityLoader.ReadabilityLoadDone.ERROR_PDF) {
              //  mErrorTextView.setText(R.string.error_pdf_readability);
            } else {
             //   mErrorTextView.setText(R.string.error_loading_page);
            }

            //TODO- Option to retry
            //TODO- Interface for showing snackbar
        }

    }

    @Override
    public void loadDone(String result, boolean success, int code) {
        Log.i(TAG, "loadDone: Boilerpipe load done " + success);
        if(success) {
            isArticleReady = true;
            final boolean darkTheme = SharedPrefsController.getInstance(getContext()).getUseDarkTheme();
            boilerpipePage = ReadabilityLoader.setHTMLStyling(result,
                    darkTheme ? darkBG : lightBG,
                    darkTheme ? darkText : lightText);
            Log.i(TAG, "loadDone: " + boilerpipePage);
            if(mWebView != null) setupViews();
        } else {
           // mProgressSpinner.setVisibility(View.GONE);
           // mErrorTextView.setVisibility(View.VISIBLE);
            if(code == ReadabilityLoader.ReadabilityLoadDone.ERROR_PDF) {
              //  mErrorTextView.setText(R.string.error_pdf_readability);
            } else {
               // mErrorTextView.setText(R.string.error_loading_page);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
        outState.putString("boilerpipe", boilerpipePage);
    }

    @Override
    public void loadItem(Item item) {
        //TODO- Check if it has a url, not just type
        if(item.getType() == ItemType.STORY && item.getUrl() != null) {
            new ReadabilityLoader(this).boilerPipe(item.getUrl(), true);
            if(ampView) {
                isArticleReady = true;
                url = APIPaths.getMercuryAmpPath(item.getUrl());
                if(mWebView != null) setupViews();
            } else {
                new ReadabilityLoader(this).boilerPipe(item.getUrl(), true);
            }
        } else {

        }

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
//        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        if(!fullscreen) makeFullScreen();

    }
}
