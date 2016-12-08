package com.tpb.hn.viewer.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ScaleDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.tpb.hn.helpers.Util;

/**
 * Created by theo on 26/11/16.
 */

public class HintingSeekBar extends SeekBar {
    private static final String TAG = HintingSeekBar.class.getSimpleName();
    private final Paint mPaint = new Paint();
    private String mHint = "";
    private SeekHintProvider mProvider;

    public HintingSeekBar(Context context) {
        super(context);
    }

    public HintingSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HintingSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mProvider != null) mHint = mProvider.getHint(getProgress());
        final float thumb_x = getThumb().getBounds().left + getThumb().getBounds().width() / 2f;
        final float middle = (this.getHeight() + mPaint.getTextSize()) / 2;
        canvas.drawText(mHint, thumb_x, middle, mPaint);
    }

    public void setPercentageProvider() {
        mProvider = progress -> Integer.toString((int) (100 * progress / (float) getMax()));
    }

    public void setProgressProvider() {
        mProvider = Integer::toString;
    }

    public void setTextColor(int color) {
        mPaint.setColor(color);
    }

    public void setHint(String hint) {
        mHint = hint;
    }

    public void setHintProvider(SeekHintProvider provider) {
        mProvider = provider;
    }

    public void removeHintProvider() {
        mProvider = null;
    }

    public void setTextSize(int sp) {
        final int px = Util.pxFromSp(sp);
        mPaint.setTextSize(px);
        //https://developer.android.com/reference/android/graphics/drawable/ScaleDrawable.html
        setThumb(new ScaleDrawable(getThumb(), 0x11, px / 14f, px / 14f));
    }

    public interface SeekHintProvider {

        String getHint(int progress);

    }

}
