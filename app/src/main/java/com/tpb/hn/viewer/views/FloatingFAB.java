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
    private float mAcceleration = 1f;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    private FloatingFABListener mListener;
    private FloatingFABState mState = FloatingFABState.DOWN;
    private boolean mLongPress = false;

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
        this.mListener = listener;
    }

    private Runnable drag = new Runnable() {
        @Override
        public void run() {
            mListener.fabDrag(mLastDifY * mAcceleration);
            if(getY() >= ((View) getParent()).getHeight() - getHeight() || getY() <= getHeight()/2) {
                mAcceleration  = Math.max(mAcceleration + 0.1f, 2f);
            } else {
                mAcceleration = 1f;
            }
            mUiHandler.postDelayed(this, 167);
        }
    };

    private Runnable longPress = new Runnable() {
        @Override
        public void run() {
            mLongPress = true;
            mListener.fabLongPressDown(mState);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mListener == null) return super.onTouchEvent(ev);
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = ev.getRawX();
                mInitialY = ev.getRawY();
                mUiHandler.postDelayed(longPress, 300);
                mListener.fabDown(mState);
                return true;
            case MotionEvent.ACTION_MOVE:
                moveToPosition(ev);
                final float dy = (ev.getRawY() - mInitialY)/((View) getParent()).getHeight();
                if(dy > 0 && mLastDifY < 0) {
                    setImageResource(R.drawable.ic_arrow_downward);
                    mState = FloatingFABState.DOWN;
                    mAcceleration = 1f;
                } else if(dy < 0 && mLastDifY > 0) {
                    setImageResource(R.drawable.ic_arrow_upward);
                    mState = FloatingFABState.UP;
                    mAcceleration = 1f;
                }
                if(Math.abs(dy-mLastDifY) > 0.05f || mLastDifY == 0) {
                    mLastDifY = dy;
                    mListener.fabLongPressUp(mState);
                    mUiHandler.removeCallbacks(longPress);
                    mUiHandler.post(drag);
                }
                break;
            case MotionEvent.ACTION_UP:
                mUiHandler.removeCallbacks(longPress);
                mUiHandler.removeCallbacks(drag);
                if(mLongPress) {
                    mListener.fabLongPressUp(mState);
                    mLongPress = false;
                } else if(Math.abs((ev.getRawY() - mInitialY))/((View) getParent()).getHeight() < 0.05f) {
                    mListener.fabUp(mState);
                }
                mAcceleration = 1f;

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

        void fabDown(FloatingFABState state);

        void fabUp(FloatingFABState state);

        void fabDrag(float velocitypc);

        void fabLongPressDown(FloatingFABState state);

        void fabLongPressUp(FloatingFABState state);

    }

    public enum FloatingFABState {

        UP, DOWN

    }

}
