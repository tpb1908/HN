package com.tpb.hn.viewer.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.helpers.APIPaths;
import com.tpb.hn.helpers.Formatter;
import com.tpb.hn.helpers.Util;
import com.tpb.hn.network.Loader;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.viewer.FragmentPagerAdapter;
import com.tpb.hn.viewer.ViewerActivity;
import com.tpb.hn.viewer.views.AdBlockingWebView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.tpb.hn.viewer.FragmentPagerAdapter.PageType.AMP_READER;
import static com.tpb.hn.viewer.FragmentPagerAdapter.PageType.TEXT_READER;

/**
 * Created by theo on 25/11/16.
 */

public class ContentFragment extends LoadingFragment implements Loader.ItemLoader,
        Loader.TextLoader,
        FragmentPagerAdapter.FragmentCycleListener,
        AdBlockingWebView.LinkHandler {
    private static final String TAG = ContentFragment.class.getSimpleName();
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
    private Tracker mTracker;
    private Unbinder unbinder;
    private ViewerActivity mParent;
    private boolean mIsFindShown = false;
    private boolean mIsSearchComplete = false;
    private boolean mIsShowingPDF = false;
    private boolean mLazyLoadCanStart;
    private boolean mDisableHorizontalScrolling;
    private boolean mFullScreenContent;

    private FragmentPagerAdapter.PageType mType;
    private boolean mIsFullscreen = false;
    private boolean mShown = false;

    private String url;
    private String mReadablePage;

    public ContentFragment() {
    }

    public static ContentFragment newInstance(FragmentPagerAdapter.PageType type) {
        final ContentFragment content = new ContentFragment();
        content.mType = type;
        return content;
    }


    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_content, container, false);
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        unbinder = ButterKnife.bind(this, inflated);

        final SharedPrefsController prefs = SharedPrefsController.getInstance(getContext());
        mDisableHorizontalScrolling = prefs.getDisableHorizontalScrolling();
        mLazyLoadCanStart = !prefs.getLazyLoad();
        mFullScreenContent = prefs.getFullscreenContent();
        mWebView.setHorizontalScrollingEnabled(!prefs.getDisableHorizontalScrolling());
        mWebView.bindProgressBar(mProgressBar, true, true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setLinkHandler(this);
        mWebView.setShouldBlockAds(prefs.getBlockAds());
        mWebView.setCacheEnabled();
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
        mSwiper.setOnRefreshListener(() -> mWebView.reload());

        mWebView.setLoadDoneListener(() -> {
            if(mSwiper != null) mSwiper.setRefreshing(false);
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
        if(context instanceof ViewerActivity) {
            mParent = (ViewerActivity) context;
        } else {
            throw new IllegalArgumentException("Activity must be instance of " + ViewerActivity.class.getSimpleName());
        }
    }

    @Override
    void bindData() {
        if(Analytics.VERBOSE) Log.i(TAG, "bindData: Binding");
        if(mShown) return;
        mShown = true;
        if(mIsShowingPDF) {
            if(Analytics.VERBOSE) Log.i(TAG, "bindData: Setting up PDF buttons");
            setupPDFButtons();
        } else {
            if(mType == FragmentPagerAdapter.PageType.BROWSER) {
                mWebView.loadUrl(url);
            } else if(mType == AMP_READER) {
                mWebView.loadUrl(APIPaths.getMercuryAmpPath(url));
            } else if(mType == TEXT_READER) {
                if(Analytics.VERBOSE) Log.i(TAG, "bindData: Mercury");
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

    @Override
    public void itemLoaded(Item item) {
        if(mType == FragmentPagerAdapter.PageType.BROWSER || mType == AMP_READER) {
            url = item.getUrl();
            mIsShowingPDF = url.toLowerCase().endsWith(".pdf");
            mContentReady = true;
            if(mViewsReady) bindData();


        } else if(mType == TEXT_READER) {
            //Text reader deals with Item text, or readability
            if(item.getUrl() == null) {
                mReadablePage = Formatter.wrapInDiv(item.getText());
                mContentReady = true;
                if(mViewsReady) bindData();


            } else {
                url = item.getUrl();
                Loader.getInstance(getContext()).loadArticle(item, true, this);
            }
        }
    }

    @Override
    public void itemError(int id, int code) {

    }

    @OnClick(R.id.button_content_toolbar_close)
    void onClosePressed() {
        if(mIsFindShown) {
            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            mFindEditText.setText("");
            mSwitcher.showNext();
            mSwitcher.setInAnimation(getContext(), R.anim.expand_horizontal);
            mSwitcher.setOutAnimation(getContext(), android.R.anim.fade_out);
            mIsFindShown = false;
            mIsSearchComplete = false;
            mParent.hideFab();
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
            findInPage();
        } else {
            mSwitcher.showNext();
            mSwitcher.setInAnimation(getContext(), android.R.anim.fade_in);
            mSwitcher.setOutAnimation(getContext(), R.anim.shrink_horizontal);
            mFindEditText.requestFocus();
            mFindEditText.addTextChangedListener(new TextWatcher() {
                long lastUpdate = System.currentTimeMillis();

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    mFindEditText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(System.currentTimeMillis() - lastUpdate >= 300) {
                                findInPage();
                                lastUpdate = System.currentTimeMillis();
                            }
                        }
                    }, 300);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            Util.toggleKeyboard(getContext());
            mIsFindShown = true;
            mFindEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
                if(i == EditorInfo.IME_ACTION_SEARCH) {
                    findInPage();
                    return true;
                }
                return false;
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
            if(mFullScreenContent) mParent.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
                mScrollView.post(() -> {
                    mParent.closeFullScreen();
                    mScrollView.scrollTo(mWebView.getScrollX(), mWebView.getScrollY());
                });

                final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mWebView.setLayoutParams(params);
                mWebView.clearMatches();
                mParent.setFabDrawable(R.drawable.ic_zoom_out_arrows);
                mIsSearchComplete = false;
            }
            if(mFullScreenContent) mParent.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void findInPage() {
        final String query = mFindEditText.getText().toString();
        mWebView.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            if(isDoneCounting) {
                mParent.setFabDrawable(R.drawable.ic_chevron_down);
                mParent.showFab();
                mIsSearchComplete = true;
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

        browserButton.setOnClickListener(view -> {
            mWebView.loadUrl(APIPaths.getPDFDisplayPath(url));
            toggleFullscreen(true);
        });
        downloadButton.setOnClickListener(view -> {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        });
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        mLazyLoadCanStart = true;
        if(mViewsReady && mContentReady) bindData();

        mShown = true;
        mParent.setUpFab(mIsFullscreen ? R.drawable.ic_chevron_down : R.drawable.ic_zoom_out_arrows,
                view -> {
                    if(mIsSearchComplete) {
                        mWebView.findNext(true);
                    } else {
                        toggleFullscreen(!mIsFullscreen);
                    }
                });

        if(mIsShowingPDF) {
            ButterKnife.findById(mToolbar, R.id.button_find_in_page).setVisibility(View.INVISIBLE);
            mParent.hideFab();
        } else {
            mParent.showFab();
        }
        mTracker.setPage(TAG + "_" + mType);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        if(url.endsWith(".pdf")) {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
            return new Pair<>(false, url);
        } else {
            switch(mType) {
                case AMP_READER:
                    return Pair.create(true, APIPaths.getMercuryAmpPath(url));
                case TEXT_READER:
                    Loader.getInstance(getContext()).redirectThroughMercury(url, this);
                    mShown = false;
                    Toast.makeText(getContext(), R.string.text_redirecting_reader, Toast.LENGTH_LONG).show();
                    return Pair.create(false, null);
                default:
                    return Pair.create(true, url);
            }
        }
    }

    @Override
    public void textLoaded(JSONObject result) {
        Log.i(TAG, "textLoaded: Text loaded");
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
