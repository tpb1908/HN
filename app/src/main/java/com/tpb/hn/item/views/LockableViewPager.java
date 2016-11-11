package com.tpb.hn.item.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by theo on 06/11/16.
 */

public class LockableViewPager extends android.support.v4.view.ViewPager {
    private boolean isSwipeEnabled = true;

    public LockableViewPager(Context context) {
        this(context, null);
    }

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSwipeEnabled && super.onInterceptTouchEvent(ev);
    }

    public void setSwipeEnabled(boolean enabled) {
        isSwipeEnabled = enabled;
    }

}
