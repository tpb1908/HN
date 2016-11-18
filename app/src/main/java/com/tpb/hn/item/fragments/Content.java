package com.tpb.hn.item.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Formatter;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.item.views.CachingAdBlockingWebView;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.loaders.TextLoader;

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

public class Content extends Fragment implements ItemLoader,
        TextLoader.TextLoadDone,
        FragmentPagerAdapter.FragmentCycleListener,
        CachingAdBlockingWebView.LinkHandler {
    private static final String TAG = Content.class.getSimpleName();
    private Tracker mTracker;

    private Unbinder unbinder;

    private ItemViewActivity mParent;

    @BindColor(R.color.md_grey_50) int lightBG;
    @BindColor(R.color.md_grey_bg) int darkBG;
    @BindColor(R.color.colorPrimaryText) int lightText;
    @BindColor(R.color.colorPrimaryTextInverse) int darkText;

    @BindView(R.id.fullscreen) LinearLayout mFullscreen;
    @BindView(R.id.webview_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.webview_scroller) NestedScrollView mScrollView;
    @BindView(R.id.webview) CachingAdBlockingWebView mWebView;
    @BindView(R.id.content_fragment_toolbar) android.widget.Toolbar mToolbar;
    @BindView(R.id.content_progressbar) ProgressBar mProgressBar;
    @BindView(R.id.content_toolbar_switcher) ViewSwitcher mSwitcher;
    @BindView(R.id.content_find_edittext) EditText mFindEditText;

    private boolean mIsFindShown = false;
    private boolean mIsSearchComplete = false;
    private boolean mIsShowingPDF = false;

    private FragmentPagerAdapter.PageType mType;

    private boolean mIsFullscreen = false;
    private boolean mIsContentReady;

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

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_content, container, false);
        unbinder = ButterKnife.bind(this, inflated);

        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();

        mWebView.bindProgressBar(mProgressBar, true, true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setLinkHandler(this);

        mParent.showFab();

        if(mIsContentReady) {
            bindData();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("url") != null) {
                url = savedInstanceState.getString("url");
                readablePage = savedInstanceState.getString("readablePage");
                bindData();
            }
        }
        mSwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
        mWebView.setLoadDoneListener(new CachingAdBlockingWebView.LoadListener() {
            @Override
            public void loadDone() {
                mSwiper.setRefreshing(false);
            }
        });
        return inflated;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ItemViewActivity) {
            mParent = (ItemViewActivity) context;
        } else {
            throw new IllegalArgumentException("Activity must be instance of " + ItemViewActivity.class.getSimpleName());
        }
    }

    //<editor-fold desc="Data loading and binding">
    private void bindData() {
        if(mIsContentReady && mWebView != null) {
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
                    If we do when the JSON is returned, and the JSON is returned from Cache
                    it will be returned before the Fragment has been attached and getContext()
                    will return null
                     */
                    readablePage = TextLoader.setTextColor(getContext(), readablePage, darkBG, darkText);
                    mWebView.loadData(readablePage, "text/html", "utf-8");
                }
            }
        }
    }

    @Override
    public void loadItem(Item item) {
        Log.d(TAG, "loadItem: Loading item " + mType);
        if(mType == FragmentPagerAdapter.PageType.BROWSER || mType == FragmentPagerAdapter.PageType.AMP_READER) {
            url = item.getUrl();
            mIsShowingPDF = url.toLowerCase().endsWith(".pdf");
            mIsContentReady = true;
            bindData();
        }  else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
            //Text reader deals with Item text, or readability
            if(item.getUrl() == null) {
                readablePage = Formatter.wrapInDiv(item.getText());
                mIsContentReady = true;
                bindData();
            } else {
                url = item.getUrl();
                new TextLoader(this).loadArticle(url, true);
            }
        }
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        if(success) {
            try {
                readablePage = result.get("content").toString();
                mIsContentReady = true;
                bindData();
            } catch(JSONException jse) {
                success = false;
            }
        }
        if(!success) {
            Log.d(TAG, "loadDone: Error");
            readablePage = Formatter.wrapInDiv(
                    Formatter.formatHTTPError(getContext(),
                    Formatter.capitaliseFirst(FragmentPagerAdapter.PageType.toReadableString(mType) + ":"),
                    code));
            mIsContentReady = true;
            bindData();
        }
    }

    //</editor-fold>

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

                @Override
                public void afterTextChanged(Editable editable) {

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

    private void toggleFullscreen(boolean fullscreen) {
        mIsFullscreen = fullscreen;
        if(mIsFullscreen) {
            mWebView.setZoomEnabled(true);
            mToolbar.setVisibility(View.VISIBLE);
            mParent.hideFab();
            mParent.openFullScreen();
            mScrollView.removeView(mWebView);
            mSwiper.setVisibility(View.GONE);
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
            mFullscreen.setVisibility(View.GONE);
            if(mIsShowingPDF) {
                mWebView.setVisibility(View.GONE);
                setupPDFButtons();
            }
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

    @Override
    public Pair<Boolean, String> handleLink(String url) {
        switch(mType) {
            case AMP_READER:
                return Pair.create(true, APIPaths.getMercuryAmpPath(url));
            case TEXT_READER:
                new TextLoader(this).loadArticle(url, true);
                Toast.makeText(getContext(), R.string.text_redirecting_reader, Toast.LENGTH_LONG).show();
                return Pair.create(false, null);
            default:
                return Pair.create(true, url);
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
        mWebView.setVisibility(View.GONE);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;

        final Button browserButton = new Button(getContext());
        browserButton.setLayoutParams(params);
        browserButton.setText(R.string.text_open_with_docs);

        final Button downloadButton = new Button(getContext());
        downloadButton.setLayoutParams(params);
        downloadButton.setText(R.string.text_download_pdf);
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
        mTracker.setScreenName(TAG + "_" + mType);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mParent.setUpFab(mIsFullscreen ? R.drawable.ic_chevron_down : R.drawable.ic_zoom_out_arrows,
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsSearchComplete) {
                    mWebView.findNext(true);
                } else {
                    toggleFullscreen(!mIsFullscreen);
                }
            }
        });

        if(mIsShowingPDF) {
            ButterKnife.findById(mToolbar, R.id.button_find_in_page).setVisibility(View.INVISIBLE);
            mParent.hideFab();
        } else {
            mParent.showFab();
        }
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
}
