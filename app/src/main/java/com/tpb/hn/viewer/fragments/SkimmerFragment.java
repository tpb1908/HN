package com.tpb.hn.viewer.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.Loader;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.viewer.FragmentPagerAdapter;
import com.tpb.hn.viewer.ViewerActivity;
import com.tpb.hn.viewer.views.HintingSeekBar;
import com.tpb.hn.viewer.views.spritzer.ClickableTextView;
import com.tpb.hn.viewer.views.spritzer.SpritzerTextView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

/**
 * Created by theo on 25/11/16.
 */

public class SkimmerFragment extends LoadingFragment implements Loader.ItemLoader, Loader.TextLoader, FragmentPagerAdapter.FragmentCycleListener {
    private static final String TAG = SkimmerFragment.class.getSimpleName();
    @BindView(R.id.skimmer_text_view) SpritzerTextView mTextView;
    @BindView(R.id.skimmer_text_body) ClickableTextView mTextBody;
    @BindView(R.id.skimmer_progress) HintingSeekBar mSkimmerProgress;
    @BindView(R.id.skimmer_error_textview) TextView mErrorView;
    @BindView(R.id.skimmer_body_scrollview) NestedScrollView mBodyScrollview;
    @BindView(R.id.spritzer_swiper) SwipeRefreshLayout mSwiper;
    private Tracker mTracker;
    private Unbinder unbinder;

    private ViewerActivity mParent;
    private Item mItem;
    private boolean mIsWaitingForAttach = false;
    private String mArticle;

    private boolean mBodyEnabled;

    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_skimmer, container, false);
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        unbinder = ButterKnife.bind(this, inflated);
        final SharedPrefsController prefs = SharedPrefsController.getInstance(getContext());
        if(prefs.showSeekBarHint()) {
            mSkimmerProgress.setPercentageProvider();
            mSkimmerProgress.setTextSize(16);
            mSkimmerProgress.setTextColor(getResources().getColor(R.color.colorPrimaryTextInverse));
        }
        mTextView.attachSeekBar(mSkimmerProgress);
        mBodyEnabled = prefs.getSkimmerBody();
        if(mBodyEnabled) {
            mTextBody.setVisibility(View.VISIBLE);
            mTextView.attachScrollView(mBodyScrollview);
            mTextBody.setListener((pos) -> {
                mTextView.setPosition(pos);
                mTextBody.highlightWord(pos + 1);
            });
        }

        mSwiper.setOnRefreshListener(() -> itemLoaded(mItem));

        if(mContentReady) {
            setupSkimmer();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("mArticle") != null) {
                mArticle = savedInstanceState.getString("mArticle");
                setupSkimmer();
            }
        } else {
            mSwiper.setRefreshing(true);
        }
        return inflated;
    }

    @Override
    void attach(Context context) {
        if(context instanceof ViewerActivity) {
            mParent = (ViewerActivity) context;
        } else {
            throw new IllegalArgumentException("Activity must be instance of " + ViewerActivity.class.getSimpleName());
        }
        if(mIsWaitingForAttach) {
            Loader.getInstance(getContext()).loadArticle(mItem, true, this);
        }

    }

    @Override
    void bindData() {
        mSwiper.setRefreshing(false);
        mArticle = Html.fromHtml(mArticle).toString();
        if(mBodyEnabled) mTextBody.setText(mArticle);
        setupSkimmer();
    }

    @OnLongClick(R.id.skimmer_touch_area)
    boolean onAreaLongClick() {
        mTextView.play();
        return false;
    }

    @OnTouch(R.id.skimmer_touch_area)
    boolean onAreaTouch(MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

            mTextView.pause();
        }
        return false;
    }

    @OnLongClick(R.id.skimmer_text_body)
    boolean onBodyLongClick() {
        mTextBody.setClickEnabled(false);
        mTextView.play();
        return false;
    }

    @OnTouch(R.id.skimmer_text_body)
    boolean onBodyTouch(MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mTextView.pause();
            mTextBody.highlightWord(mTextView.getCurrentWordIndex());
            mBodyScrollview.smoothScrollTo(0,
                    mTextBody.getLayout().getLineTop(
                            mTextBody.getLayout().getLineForOffset(
                                    mTextBody.getHighlightedPosition())));
            mTextBody.postDelayed(() -> mTextBody.setClickEnabled(true), 20);
        }
        return false;
    }

    @OnClick(R.id.skimmer_text_view)
    void onSpritzerClick() {
        if(mBodyEnabled) {
            mTextView.showTextDialog();
        } else {
            mTextView.showWPMDialog();
        }
    }

    @OnLongClick(R.id.skimmer_text_view)
    boolean onSpritzerLongClick() {
        if(mBodyEnabled) mTextView.showWPMDialog();
        return true;
    }

    private void displayErrorMessage() {
        mTextView.setVisibility(View.INVISIBLE);
        mSkimmerProgress.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.VISIBLE);
        mErrorView.setText(R.string.error_parsing_readable_text);
    }

    private void setupSkimmer() {
        if(Analytics.VERBOSE) Log.i(TAG, "setupSkimmer: " + mViewsReady + " | " + mTextView);
        mTextView.setVisibility(View.VISIBLE);
        mSkimmerProgress.setVisibility(View.VISIBLE);
        mTextView.setWpm(SharedPrefsController.getInstance(getContext()).getSkimmerWPM());
        mTextView.setSpritzText(mArticle);
        mTextView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPauseFragment() {
        mTextView.pause();
    }

    @Override
    public void onResumeFragment() {
        mParent.setUpFab(R.drawable.ic_refresh,
                view -> {
                    mTextView.setSpritzText(mArticle);
                    mTextView.getSpritzer().start();
                    if(Build.VERSION.SDK_INT >= 24) {
                        mSkimmerProgress.setProgress(0, true);
                    } else {
                        mSkimmerProgress.setProgress(0);
                    }
                    mBodyScrollview.scrollTo(0, 0);
                /*
                In order to skip one word, we have to wait
                for one minute / words per minute
                 */
                    mTextView.postDelayed(() -> mTextView.getSpritzer().pause(), 60000 / mTextView.getSpritzer().getWpm());
                });
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void itemLoaded(Item item) {
        mItem = item;
        if(item.getUrl() != null) {
            if(mViewsReady) mSwiper.setRefreshing(true);
            if(mContextReady) {
                Loader.getInstance(getContext()).loadArticle(item, true, this);
            } else {
                mIsWaitingForAttach = true;
            }
        } else {
            mArticle = item.getText() == null ? "" : Html.fromHtml(item.getText()).toString();
            mContentReady = true;
            if(mViewsReady) bindData();
        }
    }

    @Override
    public void itemError(int id, int code) {

    }

    @Override
    public void textLoaded(JSONObject result) {
        try {
            mArticle = result.getString("content");
            mContentReady = true;
            if(mViewsReady) bindData();
        } catch(JSONException jse) {
            Log.e(TAG, "textLoaded: ", jse);
            displayErrorMessage();
        }
    }

    @Override
    public void textError(String url, int code) {

    }
}
