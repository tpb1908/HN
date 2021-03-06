package com.tpb.hn.viewer.views.spritzer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.widget.NestedScrollView;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tpb.hn.R;
import com.tpb.hn.settings.SharedPrefsController;
import com.tpb.hn.viewer.views.HintingSeekBar;

/**
 * Created by andrewgiang on 3/3/14.
 */
public class SpritzerTextView extends TextView implements View.OnClickListener {

    private static final String TAG = SpritzerTextView.class.getName();
    private static final int PAINT_WIDTH_DP = 4;          // thickness of spritz guide bars in dp
    private Spritzer mSpritzer;
    // For optimal drawing should be an even number
    private Paint mPaintGuides;
    private float mPaintWidthPx;
    private String mTestString;
    private boolean mDefaultClickListener = false;
    private int mAdditionalPadding;
    private OnClickControlListener mClickControlListener;

    public SpritzerTextView(Context context) {
        super(context);
        init();
    }

    public SpritzerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public SpritzerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Register a callback for when the view has been clicked
     * <p/>
     * Note: it is mandatory to use the clickControls
     *
     * @param listener
     */
    public void setOnClickControlListener(OnClickControlListener listener) {
        mClickControlListener = listener;
    }

    private void init(AttributeSet attrs) {
        setAdditionalPadding(attrs);
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SpritzerTextView, 0, 0);
        try {
            mDefaultClickListener = a.getBoolean(R.styleable.SpritzerTextView_clickControls, false);
        } finally {
            a.recycle();
        }
        init();

    }

    @SuppressWarnings("ResourceType")
    private void setAdditionalPadding(AttributeSet attrs) {
        //check padding attributes
        int[] attributes = new int[] {android.R.attr.padding, android.R.attr.paddingTop,
                android.R.attr.paddingBottom};

        final TypedArray paddingArray = getContext().obtainStyledAttributes(attrs, attributes);
        try {
            final int padding = paddingArray.getDimensionPixelOffset(0, 0);
            final int paddingTop = paddingArray.getDimensionPixelOffset(1, 0);
            final int paddingBottom = paddingArray.getDimensionPixelOffset(2, 0);
            mAdditionalPadding = Math.max(padding, Math.max(paddingTop, paddingBottom));
            Log.w(TAG, "Additional Padding " + mAdditionalPadding);
        } finally {
            paddingArray.recycle();
        }
    }

    private void init() {
        int pivotPadding = getPivotPadding();
        setPadding(getPaddingLeft(), pivotPadding, getPaddingRight(), pivotPadding);
        mPaintWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PAINT_WIDTH_DP, getResources().getDisplayMetrics());
        mSpritzer = new Spritzer(this);
        mPaintGuides = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGuides.setColor(getCurrentTextColor());
        mPaintGuides.setStrokeWidth(mPaintWidthPx);
        mPaintGuides.setAlpha(128);
        if(mDefaultClickListener) {
            this.setOnClickListener(this);
        }

    }

    public void showWPMDialog() {
        final SharedPrefsController prefs = SharedPrefsController.getInstance(getContext());
        new MaterialDialog.Builder(getContext())
                .title(R.string.title_wpm_dialog)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .autoDismiss(false)
                .input(String.format(getContext().getString(R.string.hint_wpm_input), mSpritzer.getWpm()),
                        null,
                        (dialog, input) -> {
                            boolean error = false;
                            try {
                                final int wpm = Integer.parseInt(input.toString());
                                if(wpm > 2000) {
                                    error = true;
                                } else {
                                    prefs.setSkimmerWPM(wpm);
                                    mSpritzer.setWpm(wpm);
                                }
                            } catch(Exception e) {
                                error = true;
                            }

                            if(error) {
                                dialog.getInputEditText().setError(getContext().getString(R.string.error_wpm_input));
                            } else {
                                dialog.dismiss();
                            }
                        })
                .canceledOnTouchOutside(true)
                .onNegative((dialog, which) -> dialog.dismiss())
                .cancelable(true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }

    public void showTextDialog() {
        final ClickableTextView text = new ClickableTextView(getContext(), mSpritzer.mCurWordIdx - 1);
        text.setText(mSpritzer.getWordArray());
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.title_text_dialog)
                .customView(text, true)
                .negativeText(android.R.string.cancel)
                .show();

        text.setListener(pos -> {
            mSpritzer.setPosition(pos);
            dialog.dismiss();
        });
    }

    public void setPosition(int pos) {
        mSpritzer.setPosition(pos);
    }

    private int getPivotPadding() {
        return getPivotIndicatorLength() * 2 + mAdditionalPadding;
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        int pivotPadding = getPivotPadding();
        setPadding(getPaddingLeft(), pivotPadding, getPaddingRight(), pivotPadding);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Measurements for top & bottom guide line
        int beginTopX = 0;
        int endTopX = getMeasuredWidth();
        int topY = 0;

        int beginBottomX = 0;
        int endBottomX = getMeasuredWidth();
        int bottomY = getMeasuredHeight();
        // Paint the top guide and bottom guide bars
        canvas.drawLine(beginTopX, topY, endTopX, topY, mPaintGuides);
        canvas.drawLine(beginBottomX, bottomY, endBottomX, bottomY, mPaintGuides);

        // Measurements for pivot indicator
        float centerX = calculatePivotXOffset() + getPaddingLeft();
        final int pivotIndicatorLength = getPivotIndicatorLength();

        // Paint the pivot indicator
        canvas.drawLine(centerX, topY + (mPaintWidthPx / 2), centerX, topY + (mPaintWidthPx / 2) + pivotIndicatorLength, mPaintGuides); //line through center of circle
        canvas.drawLine(centerX, bottomY - (mPaintWidthPx / 2), centerX, bottomY - (mPaintWidthPx / 2) - pivotIndicatorLength, mPaintGuides);
    }

    private int getPivotIndicatorLength() {

        return getPaint().getFontMetricsInt().bottom;
    }

    private float calculatePivotXOffset() {
        // Craft a test String of precise length
        // to reach pivot character
        if(mTestString == null) {
            // Spritzer requires monospace font so character is irrelevant
            mTestString = "a";
        }
        // Measure the rendered distance of CHARS_LEFT_OF_PIVOT chars
        // plus half the pivot character
        return (getPaint().measureText(mTestString, 0, 1) * (Spritzer.CHARS_LEFT_OF_PIVOT + .50f));
    }

    /**
     * Pass input text to spritzer object
     *
     * @param input
     */
    public void setSpritzText(String input) {
        mSpritzer.setText(input);
    }

    /**
     * If true, this view will automatically pause or play spritz text upon view clicks
     * <p/>
     * If false, the callback OnClickControls are not invoked and
     *
     * @param useDefaultClickControls
     */
    public void setUseClickControls(boolean useDefaultClickControls) {
        mDefaultClickListener = useDefaultClickControls;
    }

    /**
     * Will play the spritz text that was set in setSpritzText
     */
    public void play() {
        mSpritzer.start();
    }

    public void pause() {
        mSpritzer.pause();
    }

    public int getWpm() {
        return mSpritzer.getWpm();
    }

    /**
     * This determines the words per minute the sprizter will read at
     *
     * @param wpm the number of words per minute
     */
    public void setWpm(int wpm) {
        mSpritzer.setWpm(wpm);
    }

    public void attachSeekBar(HintingSeekBar bar) {
        mSpritzer.attachSeekBar(bar);
    }

    public void attachScrollView(NestedScrollView scrollView) {
        mSpritzer.attachScrollView(scrollView);
    }

    public void setOnCompletionListener(Spritzer.OnCompletionListener listener) {
        mSpritzer.setOnCompletionListener(listener);
    }

    /**
     * @param strategy @see {@link DefaultDelayStrategy></com.tpb.hn.item.views.spritzer.DefaultDelayStrategy>}
     */
    public void setDelayStrategy(DelayStrategy strategy) {
        mSpritzer.setDelayStrategy(strategy);
    }

    public Spritzer getSpritzer() {
        return mSpritzer;
    }

    /**
     * Set a custom spritzer
     *
     * @param spritzer
     */
    public void setSpritzer(Spritzer spritzer) {
        mSpritzer = spritzer;
        mSpritzer.swapTextView(this);
    }

    @Override
    public void onClick(View v) {
        if(mSpritzer.isPlaying()) {
            if(mClickControlListener != null) {
                mClickControlListener.onPause();
            }
            pause();
        } else {
            if(mClickControlListener != null) {
                mClickControlListener.onPlay();
            }
            play();
        }

    }

    public int getCurrentWordIndex() {
        return mSpritzer.mCurWordIdx;
    }

    public int getMinutesRemainingInQueue() {
        return mSpritzer.getMinutesRemainingInQueue();
    }

    /**
     * Interface definition for a callback to be invoked when the
     * clickControls are enabled and the view is clicked
     */
    public interface OnClickControlListener {
        /**
         * Called when the spritzer pauses upon click
         */
        void onPause();

        /**
         * Called when the spritzer plays upon clicked
         */
        void onPlay();
    }

}