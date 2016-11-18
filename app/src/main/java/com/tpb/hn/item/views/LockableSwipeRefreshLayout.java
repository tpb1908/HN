package com.tpb.hn.item.views;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by theo on 18/11/16.
 */

public class LockableSwipeRefreshLayout extends SwipeRefreshLayout {

    private boolean mIsScrollingEnabled = true;
    private WebView boundView;

    public LockableSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableSwipeRefreshLayout(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsScrollingEnabled && super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mIsScrollingEnabled && super.onTouchEvent(ev);
    }

    public void bindView(WebView view) {
        boundView = view;
    }

    public boolean isScrollingEnabled() {
        return mIsScrollingEnabled;
    }

    public void setScrollingEnabled(boolean mIsScrollingEnabled) {
        this.mIsScrollingEnabled = mIsScrollingEnabled;
    }

    @Override
    public boolean canChildScrollUp() {
        if(boundView != null) {
            return boundView.getScrollY() > 0;
        } else {
            return super.canChildScrollUp();
        }
    }

}
