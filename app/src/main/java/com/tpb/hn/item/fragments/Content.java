package com.tpb.hn.item.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.tpb.hn.item.views.LockableNestedScrollView;
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

public class Content extends Fragment implements ItemLoader, TextLoader.TextLoadDone, FragmentPagerAdapter.FragmentCycleListener {
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
    LinearLayout mFullscreen;

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
    private boolean mIsShowingPDF = false;

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
            mFindEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_SEARCH) {
                        findInPage();
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

    private void toggleFullscreen(boolean fullscreen) {

        mIsFullscreen = fullscreen;
        if(fullscreen) {
            mWebView.getSettings().setSupportZoom(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mParent.hideFab();
            mParent.openFullScreen();
            mToolbar.setVisibility(View.VISIBLE);
            mWebContainer.removeView(mWebView);
            if(mIsShowingPDF) {
                mFullscreen.removeAllViews();
                mWebView.setVisibility(View.VISIBLE);
            }
            mWebView.scrollTo(mScrollView.getScrollX(), mScrollView.getScrollY());
            mFullscreen.addView(mWebView);
            mScrollView.setScrollingEnabled(false);
            final ViewGroup.LayoutParams params = mWebView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.setLayoutParams(params);
        } else {
            mWebView.getSettings().setSupportZoom(false);
            mParent.showFab();
            mToolbar.setVisibility(View.GONE);
            mFullscreen.removeView(mWebView);
            if(mIsShowingPDF) {
                mWebView.setVisibility(View.GONE);
                setupPDFButtons();
            }
            mWebContainer.addView(mWebView);
            mScrollView.setScrollingEnabled(true);
            mWebContainer.post(new Runnable() {
                @Override
                public void run() {
                    mParent.closeFullScreen();
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
        if(mType == FragmentPagerAdapter.PageType.BROWSER || mType == FragmentPagerAdapter.PageType.AMP_READER) {
            url = item.getUrl();
            mIsShowingPDF = url.toLowerCase().endsWith(".pdf");
            mIsContentReady = true;
            bindData();
        }  else if(mType == FragmentPagerAdapter.PageType.TEXT_READER) {
            //Text reader deals with Item text, or readability
            if(item.getUrl() == null) {
                readablePage = item.getText();
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
                //TODO- Error message
            }
        } else {
            readablePage = Formatter.formatHTTPError(getContext(),
                    Formatter.capitaliseFirst(FragmentPagerAdapter.PageType.toReadableString(mType)),
                    code);
            mIsContentReady = true;
        }
    }

    @Override
    public void loadDone(String result, boolean success, int code) {
        if(success) {

            mIsContentReady = true;

            bindData();
        } else {
            readablePage = Formatter.formatHTTPError(getContext(),
                    Formatter.capitaliseFirst(FragmentPagerAdapter.PageType.toReadableString(mType)),
                    code);
            mIsContentReady = true;
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
                    mParent.showFab();
                }
            }
        });
        mWebView.findAllAsync(query);
    }

    private void setupPDFButtons() {
        mParent.hideFab();
        mWebView.setVisibility(View.GONE);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity= Gravity.CENTER_HORIZONTAL;

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
        mParent.setUpFab(mIsFullscreen ? R.drawable.ic_chevron_down : R.drawable.ic_zoom_out_arrows, mFullScreenToggler);
        if(mIsShowingPDF) {
            //TODO- Hide the find in page button as it can't be used in Docs PDF?
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
