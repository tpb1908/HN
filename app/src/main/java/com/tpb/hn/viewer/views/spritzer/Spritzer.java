package com.tpb.hn.viewer.views.spritzer;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.helpers.Util;
import com.tpb.hn.viewer.views.HintingSeekBar;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;


/**
 * Spritzer parses a String into a Queue
 * of words, and displays them one-by-one
 * onto a TextView at a given WPM.
 */
public class Spritzer {
    static final int CHARS_LEFT_OF_PIVOT = 3;
    private static final String TAG = "Spritzer";
    private static final boolean VERBOSE = false;
    private static final int MSG_PRINT_WORD = 1;
    private static final int MAX_WORD_LENGTH = 13;
    private final Handler mSpritzHandler;
    private final Object mPlayingSync = new Object();
    int mCurWordIdx;
    private String[] mWordArray;                  // A parsed list of words parsed from {@link #setText(String input)}
    private ArrayDeque<String> mWordQueue;        // The queue of words from mWordArray yet to be displayed
    private TextView mTarget;
    private int mWPM;
    private boolean mPlaying;
    private boolean mPlayingRequested;
    private boolean mSpritzThreadStarted;
    private boolean mJustJumped;
    private HintingSeekBar mSeekBar;
    private NestedScrollView mScrollView;
    private DelayStrategy mDelayStrategy;
    private OnCompletionListener mOnCompletionListener;

    public Spritzer(TextView target) {
        init();
        mTarget = target;
        mSpritzHandler = new SpritzHandler(this);
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    /**
     * Prepare to Spritz the given String input
     * <p/>
     * Call {@link #start()} to begin display
     *
     * @param input
     */
    public void setText(String input) {
        createWordArrayFromString(input);
        refillWordQueue();
    }

    private void createWordArrayFromString(String input) {
        mWordArray = input
                .replaceAll("/\\s+/g", " ")      // condense adjacent spaces
                .split(" ");                    // split on spaces
    }

    private void init() {

        mDelayStrategy = new DefaultDelayStrategy();
        mWordQueue = new ArrayDeque<>();
        mWPM = 500;
        mPlaying = false;
        mPlayingRequested = false;
        mSpritzThreadStarted = false;
        mCurWordIdx = 0;
        mJustJumped = false;
    }

    public int getMinutesRemainingInQueue() {
        if(mWordQueue.size() == 0) {
            return 0;
        }
        return mWordQueue.size() / mWPM;
    }

    public int getWpm() {
        return mWPM;
    }

    /**
     * Set the target Word Per Minute rate.
     * Effective immediately.
     *
     * @param wpm
     */
    public void setWpm(int wpm) {
        mWPM = wpm;
    }

    /**
     * Swap the target TextView. Call this if your
     * host Activity is Destroyed and Re-Created.
     * Effective immediately.
     *
     * @param target
     */
    public void swapTextView(TextView target) {
        mTarget = target;
        if(!mPlaying) {
            printLastWord();
        }

    }

    /**
     * Start displaying the String input
     * fed to {@link #setText(String)}
     */
    public void start() {
        if(mPlaying || mWordArray == null) {
            return;
        }
        if(mWordQueue.isEmpty()) {
            refillWordQueue();
        }

        mPlayingRequested = true;
        startTimerThread();
    }

    public void setPosition(int pos) {
        if(pos < mCurWordIdx) {
            refillWordQueue();
        }
        while(!mWordQueue.isEmpty() && mCurWordIdx < pos) {
            mWordQueue.remove();
            mCurWordIdx++;
        }
        step();
    }

    public boolean gotoWord(String word) {
        final int pos = Util.indexOf(mWordArray, word);
        if(pos != -1) {
            setPosition(pos);
            return true;
        }
        return false;
    }

    private int getInterWordDelay() {
        return 60000 / mWPM;
    }

    private void refillWordQueue() {
        updateProgress();
        mCurWordIdx = 0;
        mWordQueue.clear();
        mWordQueue.addAll(Arrays.asList(mWordArray));
        if(mSeekBar != null) mSeekBar.setMax(mWordQueue.size());
    }

    private void updateProgress() {
        if(!mWordQueue.isEmpty() && !mJustJumped) {
            final float pcDif = Math.abs((mCurWordIdx - mSeekBar.getProgress()) / (float) mWordQueue.size());
            if(pcDif > 0.01f) { //We don't want to be up
                if(mSeekBar != null && !mJustJumped) mSeekBar.setProgress(mCurWordIdx);
                if(mScrollView != null)
                    mScrollView.smoothScrollBy(0, (int) (mScrollView.getChildAt(0).getHeight() * pcDif));
            }
        }
    }

    private void step() {
        try {
            processNextWord();
        } catch(InterruptedException ie) {
            Log.e(TAG, "step: ", ie);
        }
    }

    /**
     * Read the current head of mWordQueue and
     * submit the appropriate Messages to mSpritzHandler.
     * <p/>
     * Split long words y submitting the first segment of a word
     * and placing the second at the head of mWordQueue for processing
     * during the next cycle.
     * <p/>
     * Must be called on a background thread, as this method uses
     * {@link Thread#sleep(long)} to time pauses in display.
     *
     * @throws InterruptedException
     */
    private void processNextWord() throws InterruptedException {
        if(!mWordQueue.isEmpty()) {
            String word = mWordQueue.remove();
            mCurWordIdx += 1;
            // Split long words, at hyphen if present
            word = splitLongWord(word);

            mSpritzHandler.sendMessage(mSpritzHandler.obtainMessage(MSG_PRINT_WORD, word));
            mJustJumped = false;
            final int delayMultiplier = mDelayStrategy.delayMultiplier(word);
            //Do not allow multiplier that is less than 1
            final int wordDelay = getInterWordDelay() * (mDelayStrategy != null ? delayMultiplier < 1 ? 1 : delayMultiplier : 1);
            Thread.sleep(wordDelay);

        }
        updateProgress();
    }

    /**
     * Split the given String if appropriate and
     * add the tail of the split to the head of
     * {@link #mWordQueue}
     *
     * @param word
     * @return
     */
    private String splitLongWord(String word) {
        if(word.length() > MAX_WORD_LENGTH) {
            int splitIndex = findSplitIndex(word);
            String firstSegment;
            if(VERBOSE) {
                Log.i(TAG, "Splitting long word " + word + " into " + word.substring(0, splitIndex) + " and " + word.substring(splitIndex));
            }
            firstSegment = word.substring(0, splitIndex);
            // A word split is always indicated with a hyphen unless ending in a period
            if(!firstSegment.contains("-") && !firstSegment.endsWith(".")) {
                firstSegment = firstSegment + "-";
            }
            mCurWordIdx--; //have to account for the added word in the queue
            mWordQueue.addFirst(word.substring(splitIndex));
            word = firstSegment;

        }
        return word;
    }

    /**
     * Determine the split index on a given String
     * e.g If it exceeds MAX_WORD_LENGTH or contains a hyphen
     *
     * @param thisWord
     * @return the index on which to split the given String
     */
    private int findSplitIndex(String thisWord) {
        int splitIndex;
        // Split long words, at hyphen or dot if present.
        if(thisWord.contains("-")) {
            splitIndex = thisWord.indexOf("-") + 1;
        } else if(thisWord.contains(".")) {
            splitIndex = thisWord.indexOf(".") + 1;
        } else if(thisWord.length() > MAX_WORD_LENGTH * 2) {
            // if the word is floccinaucinihilipilifcation, for example.
            splitIndex = MAX_WORD_LENGTH - 1;
            // 12 characters plus a "-" == 13.
        } else {
            // otherwise we want to split near the middle.
            splitIndex = Math.round(thisWord.length() / 2F);
        }
        // in case we found a split character that was > MAX_WORD_LENGTH characters in.
        if(splitIndex > MAX_WORD_LENGTH) {
            // If we split the word at a splitting char like "-" or ".", we added one to the splitIndex
            // in order to ensure the splitting char appears at the head of the split. Not accounting
            // for this in the recursive call will cause a StackOverflowException
            return findSplitIndex(thisWord.substring(0,
                    wordContainsSplittingCharacter(thisWord) ? splitIndex - 1 : splitIndex));
        }
        if(VERBOSE) {
            Log.i(TAG, "Splitting long word " + thisWord + " into " + thisWord.substring(0, splitIndex) +
                    " and " + thisWord.substring(splitIndex));
        }
        return splitIndex;
    }

    private boolean wordContainsSplittingCharacter(String word) {
        return (word.contains(".") || word.contains("-"));
    }

    private void printLastWord() {
        if(mWordArray != null) {
            printWord(mWordArray[mWordArray.length - 1]);
        }
    }

    /**
     * Applies the given String to this Spritzer's TextView,
     * padding the beginning if necessary to align the pivot character.
     * Styles the pivot character.
     *
     * @param word
     */
    private void printWord(String word) {
        int startSpan;
        int endSpan;
        word = word.trim().replace("\n", "");
        if(VERBOSE) Log.i(TAG + word.length(), word);
        if(word.length() == 1) {
            StringBuilder builder = new StringBuilder();
            for(int x = 0; x < CHARS_LEFT_OF_PIVOT; x++) {
                builder.append(" ");
            }
            builder.append(word);
            word = builder.toString();
            startSpan = CHARS_LEFT_OF_PIVOT;
            endSpan = startSpan + 1;
        } else if(word.length() <= CHARS_LEFT_OF_PIVOT * 2) {
            StringBuilder builder = new StringBuilder();
            int halfPoint = word.length() / 2;
            int beginPad = CHARS_LEFT_OF_PIVOT - halfPoint;
            for(int x = 0; x <= beginPad; x++) {
                builder.append(" ");
            }
            builder.append(word);
            word = builder.toString();
            startSpan = halfPoint + beginPad;
            endSpan = startSpan + 1;
            if(VERBOSE) Log.i(TAG + word.length(), "pivot: " + word.substring(startSpan, endSpan));
        } else {
            startSpan = CHARS_LEFT_OF_PIVOT;
            endSpan = startSpan + 1;
        }

        final Spannable spanRange = new SpannableString(word);
        final TextAppearanceSpan tas = new TextAppearanceSpan(mTarget.getContext(), R.style.PivotLetter);
        spanRange.setSpan(tas, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTarget.setText(spanRange);
    }

    public void pause() {
        mPlayingRequested = false;
    }

    public boolean isPlaying() {
        return mPlaying;
    }

    /**
     * Begin the background timer thread
     */
    private void startTimerThread() {
        synchronized(mPlayingSync) {
            if(!mSpritzThreadStarted) {
                new Thread(() -> {
                    if(VERBOSE) {
                        Log.i(TAG, "Starting spritzThread with queue length " + mWordQueue.size());
                    }
                    mPlaying = true;
                    mSpritzThreadStarted = true;
                    while(mPlayingRequested) {
                        try {
                            processNextWord();
                            if(mWordQueue.isEmpty()) {
                                if(VERBOSE) {
                                    Log.i(TAG, "Queue is empty after processNextWord. Pausing");
                                }
                                mTarget.post(() -> {
                                    if(mOnCompletionListener != null) {
                                        mOnCompletionListener.onComplete();
                                    }
                                });
                                mPlayingRequested = false;

                            }
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                    if(VERBOSE)
                        Log.i(TAG, "Stopping spritzThread");
                    mPlaying = false;
                    mSpritzThreadStarted = false;

                }).start();
            }
        }
    }

    public String[] getWordArray() {
        return mWordArray;
    }

    public ArrayDeque<String> getWordQueue() {
        return mWordQueue;
    }

    public void attachSeekBar(HintingSeekBar bar) {
        if(bar != null) {
            mSeekBar = bar;
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int pos, boolean fromUser) {
                    if(fromUser) {
                        if((float) Math.abs(mCurWordIdx - pos) > 0.01f * mWordArray.length) {
                            mScrollView.smoothScrollBy(0,
                                    (int) (mScrollView.getChildAt(0).getHeight()
                                            * Math.abs((mCurWordIdx - pos) / (float) mWordQueue.size())));

                            mCurWordIdx = pos;
                            mJustJumped = true;
                            step();
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    pause();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    public void attachScrollView(NestedScrollView scrollView) {
        mScrollView = scrollView;
    }

    public void setDelayStrategy(DelayStrategy strategy) {
        mDelayStrategy = strategy;

    }


    public interface OnCompletionListener {

        void onComplete();

    }

    /**
     * A Handler intended for creation on the Main thread.
     * Messages are intended to be passed from a background
     * timing thread. This Handler communicates timing
     * thread events to the Main thread for UI update.
     */
    static class SpritzHandler extends Handler {
        private final WeakReference<Spritzer> mWeakSpritzer;

        public SpritzHandler(Spritzer muxer) {
            mWeakSpritzer = new WeakReference<>(muxer);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            Spritzer spritzer = mWeakSpritzer.get();
            if(spritzer == null) {
                return;
            }

            switch(what) {
                case MSG_PRINT_WORD:
                    spritzer.printWord((String) obj);
                    break;
                default:
                    throw new RuntimeException("Unexpected msg what=" + what);
            }
        }

    }


}