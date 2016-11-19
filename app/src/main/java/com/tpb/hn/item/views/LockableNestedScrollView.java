package com.tpb.hn.item.views;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by theo on 09/11/16.
 */

public class LockableNestedScrollView extends NestedScrollView {

    private boolean mIsScrollingEnabled = true;

    public LockableNestedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockableNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableNestedScrollView(Context context) {
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



    public boolean isScrollingEnabled() {
        return mIsScrollingEnabled;
    }

    public void setScrollingEnabled(boolean mIsScrollingEnabled) {
        this.mIsScrollingEnabled = mIsScrollingEnabled;
    }
}
