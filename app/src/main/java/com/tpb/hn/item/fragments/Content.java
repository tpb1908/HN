package com.tpb.hn.item.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.CachingAdBlockingWebView;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.item.LockableNestedScrollView;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.ReadabilityLoader;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by theo on 06/11/16.
 */

public class Content extends Fragment implements ItemLoader, ReadabilityLoader.ReadabilityLoadDone, FragmentPagerAdapter.FragmentCycleListener {
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

    private ItemViewActivity mParent;

    @BindView(R.id.fullscreen)
    FrameLayout mFullscreen;

    @BindView(R.id.webview_scroller)
    LockableNestedScrollView mScrollView;

    @BindView(R.id.webview_container)
    FrameLayout mWebContainer;

    @BindView(R.id.webview)
    CachingAdBlockingWebView mWebView;

    @BindView(R.id.content_fragment_toolbar)
    android.widget.Toolbar mToolbar;

    @BindView(R.id.content_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.content_toolbar_switcher)
    ViewSwitcher mSwitcher;

    @BindView(R.id.content_find_edittext)
    EditText mFindEditText;

    private boolean mIsFindShown = false;
    private boolean mIsSearchComplete = false;

    @OnClick(R.id.button_find_in_page)
    void onFindInPagePressed() {
        if(mIsFindShown) {
            findInPage();
        } else {
            mSwitcher.showNext();
            mFindEditText.requestFocus();
            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            mIsFindShown = true;
        }
    }

    @OnClick(R.id.button_content_toolbar_close)
    void onClosePressed() {
        if(mIsFindShown) {
            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            mFindEditText.setText("");
            mSwitcher.showPrevious();
            mIsFindShown = false;
            mIsSearchComplete = false;
            mParent.setFabDrawable(R.drawable.ic_zoom_out_arrows);
        } else {
            toggleFullscreen(!mIsFullscreen);
        }
    }

    private View.OnClickListener mFullScreenToggler  = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mIsSearchComplete) {
                mWebView.findNext(true);
            } else {
                toggleFullscreen(!mIsFullscreen);
            }
        }
    };


    private FragmentPagerAdapter.PageType mType;

    private boolean mIsFullscreen = false;
    private boolean mIsArticleReady;

    private String url;
    private String readablePage;

    public static Content newInstance(FragmentPagerAdapter.PageType type) {
        if(type == FragmentPagerAdapter.PageType.COMMENTS || type == FragmentPagerAdapter.PageType.SKIMMER) {
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

        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();

        mWebView.bindProgressBar(mProgressBar, true, true);

        mParent.showFab();

        if(mIsArticleReady) {
            bindData();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("url") != null) {
                url = savedInstanceState.getString("url");
                readablePage = savedInstanceState.getString("readablePage");
                bindData();
            }
        }

        return inflated;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(!(context instanceof ItemViewActivity)) {
            throw new IllegalArgumentException("Activity must be instance of " + ItemViewActivity.class.getSimpleName());
        } else {
            mParent = (ItemViewActivity) context;
        }
    }

    private void bindData() {
        if(mIsArticleReady && mWebView != null) {
            if(mType == FragmentPagerAdapter.PageType.BROWSER) {
                mWebView.loadUrl(url);
            } else if(mType == FragmentPagerAdapter.PageType.AMP_READER) {
                mWebView.loadUrl(APIPaths.getMercuryAmpPath(url));
            } else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
                Log.i(TAG, "bindData: Text reader");
                mWebView.loadData(readablePage, "text/html", "utf-8");
            }
        }
    }

    private void findInPage() {
        final String query = mFindEditText.getText().toString();
        mWebView.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                if(isDoneCounting) {
                    mIsSearchComplete = true;
                    mParent.setFabDrawable(R.drawable.ic_chevron_down);
                }
            }
        });
        mWebView.findAllAsync(query);
    }

    private void toggleFullscreen(boolean fullscreen) {
        Log.i(TAG, "toggleFullscreen: " + fullscreen);
        mIsFullscreen = fullscreen;
        if(fullscreen) {
            mWebContainer.removeView(mWebView);
            //mWebView.scrollTo(mScrollView.getScrollX(), mScrollView.getScrollY());
            mFullscreen.addView(mWebView);
            mScrollView.setScrollingEnabled(false);
            mToolbar.setVisibility(View.VISIBLE);
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.setLayoutParams(params);
            mParent.openFullScreen();
        } else {
            mToolbar.setVisibility(View.GONE);
            mFullscreen.removeView(mWebView);
            mScrollView.setScrollingEnabled(true);
            mWebContainer.addView(mWebView);
//            mWebContainer.post(new Runnable() {
//                @Override
//                public void run() {
//                    mWebView.scrollTo(mScrollView.getScrollX(), mScrollView.getScrollY());
//                }
//            });
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mWebView.setLayoutParams(params);
            mWebView.clearMatches();
            mParent.closeFullScreen();
            mParent.setFabDrawable(R.drawable.ic_zoom_out_arrows);
            mIsSearchComplete = false;
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
        mParent.setUpFab(mIsFullscreen ? R.drawable.ic_chevron_down : R.drawable.ic_zoom_out_arrows, mFullScreenToggler);
    }

    @Override
    public void loadItem(Item item) {
        if(mType == FragmentPagerAdapter.PageType.BROWSER || mType == FragmentPagerAdapter.PageType.AMP_READER) {
            //Load url
            url = item.getUrl();
            mIsArticleReady = true;
            bindData();
        }  else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
            //Text reader deals with Item text, or BoilerPipe readability
            if(item.getUrl() == null) {
                readablePage = item.getText();
                mIsArticleReady = true;
                bindData();
            } else {
                url = item.getUrl();
               // new ReadabilityLoader(this).boilerPipe(url, true);
                new ReadabilityLoader(this).loadArticle(url, true);
            }
        }
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        try {
            readablePage = result.get("content").toString();
            mIsArticleReady = true;
            bindData();
        } catch(JSONException jse) {}
    }

    @Override
    public void loadDone(String result, boolean success, int code) {
        //TODO- Error checking
        readablePage = result;
        mIsArticleReady = true;
        bindData();
    }

}
