package com.tpb.hn.viewer.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by theo on 10/12/16.
 */

public class FloatingFAB extends FloatingActionButton {
    private static final String TAG = FloatingFAB.class.getSimpleName();

    private float mInitialX, mInitialY;
    private FloatingFABListener listener;

    public FloatingFAB(Context context) {
        super(context);
    }

    public FloatingFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingFAB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setListener(FloatingFABListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = ev.getRawX();
                mInitialY = ev.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                moveToPosition(ev);
                break;
            case MotionEvent.ACTION_UP:

                return true;
        }
        return super.onTouchEvent(ev);
    }

    private void moveToPosition(MotionEvent movement) {
        final int[] location = new int[2];
        getLocationOnScreen(location);
        final int offsetX = (location[0] - (int) getX());
        final int offsetY = (location[1] - (int) getY());
        int newX = (int) movement.getRawX() - offsetX - getWidth() / 2;
        int newY = (int) movement.getRawY() - offsetY - getHeight() / 2;
        if(newX < 0) newX = 0;
        if(newX > ((View) getParent()).getWidth() - getWidth()) newX = ((View) getParent()).getWidth() - getWidth();
        if(newY < 0) newY = 0;
        if(newY > ((View) getParent()).getHeight() - getHeight()) newY = ((View) getParent()).getHeight() - getHeight();
        setX(newX);
        setY(newY);

    }

    public interface FloatingFABListener {

    }

}
