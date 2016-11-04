package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.item.ScrollingAdBlockingWebView;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.ReadabilityLoader;

import org.json.JSONObject;

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

    @BindView(R.id.readability_webview)
    ScrollingAdBlockingWebView mWebView;

    @BindView(R.id.readability_loading_spinner)
    ProgressBar mProgressSpinner;

    @BindView(R.id.readability_error_message)
    TextView mErrorTextView;

    private boolean ampView = true;

    private ItemAdapter.Fullscreen fullscreen;

    private String url;

    private boolean isArticleReady = false;

    public static Readability newInstance(ItemAdapter.Fullscreen fullscreen) {
        final Readability r = new Readability();
        r.fullscreen = fullscreen;
        return r;
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
                setupViews();
            }
        }
        mWebView.bindProgressBar(mProgressSpinner, true, true);
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
        mErrorTextView.setVisibility(View.GONE);
        mWebView.loadUrl(url);
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        if(success) {
            try {
                isArticleReady = true;
                if(mWebView != null) setupViews();
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
                mProgressSpinner.setVisibility(View.GONE);

            }
        } else {
            mProgressSpinner.setVisibility(View.GONE);
            mErrorTextView.setVisibility(View.VISIBLE);
            if(code == ReadabilityLoader.ReadabilityLoadDone.ERROR_PDF) {
                mErrorTextView.setText(R.string.error_pdf_readability);
            } else {
                mErrorTextView.setText(R.string.error_loading_page);
            }

            //TODO- Option to retry
            //TODO- Interface for showing snackbar
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }

    @Override
    public void loadItem(Item item) {
        //TODO- Check if it has a url, not just type
        if(item.getType() == ItemType.STORY && item.getUrl() != null) {
            if(ampView) {
                isArticleReady = true;
                url = APIPaths.getMercuryAmpPath(item.getUrl());
                if(mWebView != null) setupViews();
            } else {
                new ReadabilityLoader(this).loadArticle(item.getUrl(), true);
            }
        } else {

            //isArticleReady = true;
        }

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }
}
