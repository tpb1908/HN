package com.tpb.hn.item.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.tpb.hn.network.ReadabilityLoader;

import org.json.JSONObject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by theo on 06/11/16.
 */

public class Content extends Fragment implements ItemLoader, ReadabilityLoader.ReadabilityLoadDone, FragmentPagerAdapter.FragmentCycle {
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
        } else {
            //Close the toolbar
        }
    }

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
        mParent.setUpFab(R.drawable.ic_zoom_out_arrows, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFullscreen(!mIsFullscreen);
            }
        });

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
                mWebView.loadUrl(url);
               // mWebView.loadUrl(APIPaths.getMercuryAmpPath(url));
            } else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
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
                    /*
                    If there are matches
                    Set the FAB button to
                    mWebView.findNext()

                     */

                }
            }
        });
        mWebView.findAllAsync(query);
    }

    private void toggleFullscreen(boolean fullscreen) {
        mIsFullscreen = fullscreen;
        if(fullscreen) {
            mWebContainer.removeView(mWebView);
            mFullscreen.addView(mWebView);
            mScrollView.setScrollingEnabled(false);
            mToolbar.setVisibility(View.VISIBLE);
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.setLayoutParams(params);
            mParent.openFullScreen();
        } else {
            mToolbar.setVisibility(View.GONE);
            mScrollView.setScrollingEnabled(true);
            mFullscreen.removeView(mWebView);
            mWebContainer.addView(mWebView);
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mWebView.setLayoutParams(params);
            mParent.closeFullScreen();
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
                mIsArticleReady = true;
                new ReadabilityLoader(this).boilerPipe(url, true);
            }
        }
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        /*
        This will be unused until I can get a key from Mercury
         */
    }

    @Override
    public void loadDone(String result, boolean success, int code) {
        //TODO- Error checking
        readablePage = result;
        mIsArticleReady = true;
        bindData();
    }

}
