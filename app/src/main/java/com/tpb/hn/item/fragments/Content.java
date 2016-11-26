package com.tpb.hn.item.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.data.Formatter;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.item.views.AdBlockingWebView;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.storage.SharedPrefsController;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemLoader;
import com.tpb.hn.network.loaders.Loader;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by theo on 25/11/16.
 */

public class Content extends ContentFragment implements ItemLoader,
        Loader.TextLoader,
        FragmentPagerAdapter.FragmentCycleListener,
        AdBlockingWebView.LinkHandler {
    private static final String TAG = Content.class.getSimpleName();

    private Unbinder unbinder;

    private ItemViewActivity mParent;

    @BindColor(R.color.md_grey_50) int lightBG;
    @BindColor(R.color.md_grey_bg) int darkBG;
    @BindColor(R.color.colorPrimaryText) int lightText;
    @BindColor(R.color.colorPrimaryTextInverse) int darkText;

    @BindView(R.id.fullscreen) LinearLayout mFullscreen;
    @BindView(R.id.webview_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.webview_scroller) NestedScrollView mScrollView;
    @BindView(R.id.webview) AdBlockingWebView mWebView;
    @BindView(R.id.content_fragment_toolbar) android.widget.Toolbar mToolbar;
    @BindView(R.id.content_progressbar) ProgressBar mProgressBar;
    @BindView(R.id.content_toolbar_switcher) ViewSwitcher mSwitcher;
    @BindView(R.id.content_find_edittext) EditText mFindEditText;

    private boolean mIsFindShown = false;
    private boolean mIsSearchComplete = false;
    private boolean mIsShowingPDF = false;
    private boolean mLazyLoadCanStart;
    private boolean mDisableHorizontalScrolling;

    private FragmentPagerAdapter.PageType mType;

    private boolean mIsFullscreen = false;

    private String url;
    private String mReadablePage;

    private Item mItem;

    public Content(FragmentPagerAdapter.PageType type) {
        mType = type;
    }

    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_content, container, false);
        unbinder = ButterKnife.bind(this, inflated);

        final SharedPrefsController prefs = SharedPrefsController.getInstance(getContext());
        mDisableHorizontalScrolling = prefs.getDisableHorizontalScrolling();
        mLazyLoadCanStart = !prefs.getLazyLoad();
        mWebView.setHorizontalScrollingEnabled(!prefs.getDisableHorizontalScrolling());
        mWebView.bindProgressBar(mProgressBar, true, true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setLinkHandler(this);
        mWebView.setShouldBlockAds(prefs.getBlockAds());
        mWebView.setCacheEnabled(true);
        if(Util.isNetworkAvailable(getContext())) {
            mWebView.setLoadFromNetwork();
        } else {
            mWebView.setLoadFromCache();
        }
        mParent.showFab();

        if(mContentReady) {
            bindData();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("url") != null) {
                url = savedInstanceState.getString("url");
                mReadablePage = savedInstanceState.getString("mReadablePage");
                bindData();
            }
        }
        mSwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
        mWebView.setLoadDoneListener(new AdBlockingWebView.LoadListener() {
            @Override
            public void loadDone() {
                if(mSwiper != null) mSwiper.setRefreshing(false);
            }
        });
        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY > oldScrollY) {
                    mParent.hideFab();
                } else {
                    mParent.showFab();
                }
            }
        });

        return inflated;
    }

    @Override
    void attach(Context context) {

    }

    @Override
    public void loadItem(Item item) {
        mItem = item;
        if(mType == FragmentPagerAdapter.PageType.BROWSER || mType == FragmentPagerAdapter.PageType.AMP_READER) {
            url = item.getUrl();
            mIsShowingPDF = url.toLowerCase().endsWith(".pdf");
            mContentReady = true;
            if(mViewsReady) bindData();
        } else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
            //Text reader deals with Item text, or readability
            if(item.getUrl() == null) {
                mReadablePage = Formatter.wrapInDiv(item.getText());
                mContentReady = true;
                if(mViewsReady) bindData();
            } else {
                url = item.getUrl();
                Loader.getInstance(getContext()).loadArticle(url, true, this);
            }
        }
    }

    @Override
    void bindData() {
        if(mIsShowingPDF) {
            setupPDFButtons();
        } else {
            if(mType == FragmentPagerAdapter.PageType.BROWSER) {
                mWebView.loadUrl(url);
            } else if(mType == FragmentPagerAdapter.PageType.AMP_READER) {
                mWebView.loadUrl(APIPaths.getMercuryAmpPath(url));
            } else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
                    /*
                    We have to do the theming on bind
                    If we do when the JSON is returned, and the JSON is returned from ItemCache
                    it will be returned before the Fragment has been attached and getContext()
                    will return null
                     */
                final boolean darkTheme = SharedPrefsController.getInstance(getContext()).getUseDarkTheme();
                mReadablePage = Formatter.setTextColor(getContext(), mReadablePage,
                        darkTheme ? darkBG : lightBG,
                        darkTheme ? darkText : lightText);
                mWebView.loadData(mReadablePage, "text/html", "utf-8");
            }
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
            toggleFullscreen(false);
        }
    }

    @OnClick(R.id.button_content_back)
    void webViewBack() {
        if(mWebView.canGoBack()) mWebView.goBack();
    }

    @OnClick(R.id.button_content_forward)
    void webViewForward() {
        if(mWebView.canGoForward()) mWebView.goForward();
    }

    @OnClick(R.id.button_content_refresh)
    void refresh() {
        mWebView.reload();
    }

    @OnClick(R.id.button_find_in_page)
    void onFindInPagePressed() {
        if(mIsFindShown) {
            findInPage(true);
        } else {
            mSwitcher.showNext();
            mFindEditText.requestFocus();
            mFindEditText.addTextChangedListener(new TextWatcher() {
                long lastUpdate = System.currentTimeMillis();

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    mFindEditText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(System.currentTimeMillis() - lastUpdate >= 300) {
                                findInPage(false);
                                lastUpdate = System.currentTimeMillis();
                            }
                        }
                    }, 300);
                }
            });
            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            mIsFindShown = true;
            mFindEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_SEARCH) {
                        findInPage(true);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void toggleFullscreen(boolean fullscreen) {
        mIsFullscreen = fullscreen;
        if(mIsFullscreen) {
            mWebView.setZoomEnabled(true);
            mToolbar.setVisibility(View.VISIBLE);
            mParent.hideFab();
            mParent.openFullScreen();
            mScrollView.removeView(mWebView);
            mSwiper.setVisibility(View.GONE);
            mWebView.enableHorizontalScrolling();
            if(mIsShowingPDF) {
                mFullscreen.removeAllViews();
                mWebView.setVisibility(View.VISIBLE);
            }
            mWebView.scrollTo(mScrollView.getScrollX(), mScrollView.getScrollY());
            mFullscreen.setVisibility(View.VISIBLE);
            mFullscreen.addView(mWebView);
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.setLayoutParams(params);

        } else {
            mWebView.setZoomEnabled(false);
            mParent.showFab();
            mToolbar.setVisibility(View.GONE);
            mWebView.setDrawingCacheEnabled(true);
            mFullscreen.removeView(mWebView);
            mWebView.setHorizontalScrollingEnabled(!mDisableHorizontalScrolling);
            if(mIsShowingPDF) {
                mWebView.setVisibility(View.GONE);
                setupPDFButtons();
                mParent.closeFullScreen();
            } else {
                mFullscreen.setVisibility(View.GONE);
                mSwiper.setVisibility(View.VISIBLE);
                mScrollView.addView(mWebView);
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mParent.closeFullScreen();
                        mScrollView.scrollTo(mWebView.getScrollX(), mWebView.getScrollY());
                    }
                });

                final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mWebView.setLayoutParams(params);
                mWebView.clearMatches();
                mParent.setFabDrawable(R.drawable.ic_zoom_out_arrows);
                mIsSearchComplete = false;
            }

        }
    }

    private void findInPage(final boolean closeWhenDone) {
        final String query = mFindEditText.getText().toString();
        mWebView.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                if(isDoneCounting) {
                    mParent.setFabDrawable(R.drawable.ic_chevron_down);
                    mParent.showFab();
                    if(closeWhenDone) mIsSearchComplete = true;
                }
            }
        });
        mWebView.findAllAsync(query);
    }

    private void setupPDFButtons() {
        mParent.hideFab();
        mSwiper.setVisibility(View.GONE);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;

        final Button browserButton = new Button(getContext());
        browserButton.setLayoutParams(params);
        browserButton.setText(R.string.text_open_with_docs);

        final Button downloadButton = new Button(getContext());
        downloadButton.setLayoutParams(params);
        downloadButton.setText(R.string.text_download_pdf);
        mFullscreen.setVisibility(View.VISIBLE);
        mFullscreen.addView(browserButton);
        mFullscreen.addView(downloadButton);

        browserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.loadUrl(APIPaths.getPDFDisplayPath(url));
                toggleFullscreen(true);
            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }
        });
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }

    @Override
    public boolean onBackPressed() {
        if(mIsFullscreen) {
            if(mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                toggleFullscreen(false);
            }
            return false;
        }
        return true;
    }

    @Override
    public Pair<Boolean, String> handleLink(String url) {
        switch(mType) {
            case AMP_READER:
                return Pair.create(true, APIPaths.getMercuryAmpPath(url));
            case TEXT_READER:
                Loader.getInstance(getContext()).loadArticle(url, true, this);
                Toast.makeText(getContext(), R.string.text_redirecting_reader, Toast.LENGTH_LONG).show();
                return Pair.create(false, null);
            default:
                return Pair.create(true, url);
        }
    }

    @Override
    public void textLoaded(JSONObject result) {
        try {
            mReadablePage = result.getString("content");

            mContentReady = true;
            if(mViewsReady) bindData();
        } catch(JSONException jse) {
        }
    }

    @Override
    public void textError(String url, int code) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
