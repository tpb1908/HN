package com.tpb.hn.viewer.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tpb.hn.R;

/**
 * Created by theo on 10/12/16.
 */

public class FloatingFAB extends FloatingActionButton {
    private static final String TAG = FloatingFAB.class.getSimpleName();

    private float mInitialX, mInitialY, mLastDifY;
    private boolean mIsDragging = false;
    private float mAcceleration = 1f;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
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

    private Runnable drag = new Runnable() {
        @Override
        public void run() {
            listener.fabDrag(mLastDifY * mAcceleration);
            if(getY() >= ((View) getParent()).getHeight() - getHeight()) {
                mAcceleration  = Math.max(mAcceleration + 0.1f, 2f);
            } else {
                mAcceleration = 1f;
            }
            mUiHandler.postDelayed(this, 167);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = ev.getRawX();
                mInitialY = ev.getRawY();
                if(listener != null) listener.fabDown();
                return true;
            case MotionEvent.ACTION_MOVE:
                moveToPosition(ev);
                final float dify = (ev.getRawY() - mInitialY)/((View) getParent()).getHeight();
                if(dify > 0 && mLastDifY < 0) {
                    setImageResource(R.drawable.ic_arrow_downward);
                    mAcceleration = 1f;
                } else if(dify < 0 && mLastDifY > 0) {
                    setImageResource(R.drawable.ic_arrow_upward);
                    mAcceleration = 1f;
                }
                if(Math.abs(dify-mLastDifY) > 0.1f || mLastDifY == 0) {
                    mIsDragging = true;
                    mLastDifY = dify;
                    if(listener != null) {
                        mUiHandler.post(drag);
                    } else {
                        mIsDragging = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mUiHandler.removeCallbacks(drag);
                mAcceleration = 1f;
                if(Math.abs((ev.getRawY() - mInitialY))/((View) getParent()).getHeight() < 0.05f && listener != null) listener.fabUp();
                mIsDragging = false;
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

        void fabDown();

        void fabUp();

        void fabDrag(float velocitypc);

    }

}
