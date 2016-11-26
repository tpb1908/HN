package com.tpb.hn.item.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.tpb.hn.Util;

/**
 * Created by theo on 26/11/16.
 */

public class HintingSeekBar extends SeekBar {
    private static final String TAG = HintingSeekBar.class.getSimpleName();
    private Paint mPaint = new Paint();
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
        final int thumb_x = (int) ( (getWidth() - getPaddingRight()) * getProgress() / (double) getMax());
        final int middle = (int) (this.getHeight() + mPaint.getTextSize()) / 2;
        canvas.drawText(mHint, thumb_x, middle, mPaint);
    }

    public void setPercentageProvider() {
        mProvider = new SeekHintProvider() {
            @Override
            public String getHint(int progress) {
                return Integer.toString( (int) (100 * progress/ (float) getMax()));
            }
        };
    }

    public void setProgressProvider() {
        mProvider = new SeekHintProvider() {
            @Override
            public String getHint(int progress) {
                return Integer.toString(progress);
            }
        };
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
        mPaint.setTextSize(Util.pxFromSp(sp));
    }

    public interface SeekHintProvider {

        String getHint(int progress);

    }

}
