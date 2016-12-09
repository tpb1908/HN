package com.tpb.hn.viewer.views.spritzer;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theo on 12/11/16.
 * http://stackoverflow.com/questions/8612652/select-a-word-on-a-tap-in-textview-edittext
 */

public class ClickableTextView extends TextView {
    private static final String TAG = ClickableTextView.class.getSimpleName();

    private final int currentPos;
    private OnSpanClickListener mListener;
    private Integer[] indices = new Integer[0];
    private int oldSelectedPosition = 0;
    private int lastSelectedStart = 0;
    private int lastSelectedEnd = 0;
    private boolean isClickEnabled = true;

    public ClickableTextView(Context context) {
        super(context);
        currentPos = 0;
    }

    public ClickableTextView(Context context, int currentPos) {
        super(context);
        this.currentPos = currentPos;
    }

    public ClickableTextView(Context context, @NonNull AttributeSet attributeSet) {
        super(context, attributeSet);
        this.currentPos = 0;
    }

    public ClickableTextView(Context context, @NonNull AttributeSet attributeSet, int currentPos) {
        super(context, attributeSet);
        this.currentPos = currentPos;
    }

    private static Integer[] getIndices(String s, char c) {
        int pos = s.indexOf(c, 0);
        List<Integer> indices = new ArrayList<>();
        while(pos != -1) {
            indices.add(pos);
            pos = s.indexOf(c, pos + 1);
        }
        return indices.toArray(new Integer[0]);
    }

    public boolean isClickEnabled() {
        return isClickEnabled;
    }

    public void setClickEnabled(boolean clickEnabled) {
        isClickEnabled = clickEnabled;
    }

    public void setListener(OnSpanClickListener listener) {
        mListener = listener;
    }

    @Override
    public void setText(final CharSequence text, BufferType type) {
        //setMovementMethod(LinkMovementMethod.getInstance());
        super.setText(text, BufferType.SPANNABLE);
        new Handler().post(() -> {
            final Spannable spans = (Spannable) getText();

            indices = getIndices(getText().toString(), ' ');
            int start = 0;
            int end = 0;
            // to cater last/only word loop will run equal to the length of indices.length
            for(int i = 0; i <= indices.length; i++) {
                final ClickableSpan clickSpan = getClickableSpan(i, i == currentPos);
                // to cater last/only word
                end = (i < indices.length ? indices[i] : spans.length());
                spans.setSpan(clickSpan, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = end + 1;
            }
        });
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        Log.i(TAG, "onSelectionChanged: ");
        if(getText() != null && selStart != getText().length() && selEnd != getText().length()) {
            onSelectionChanged(getText().length(), getText().length());
            return;
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    public void highlightWord(int pos) {
        final Spannable spans = (Spannable) getText();
        int start = 0;
        int end = 0;
        int i;
        // to cater last/only word loop will run equal to the length of indices.length
        for(i = 0; i < pos && i <= indices.length; i++) {
            // to cater last/only word
            start = end + 1;
            end = (i < indices.length ? indices[i] : spans.length());
        }
        final ClickableSpan clearSpan = getClickableSpan(oldSelectedPosition, false);
        spans.setSpan(clearSpan, lastSelectedStart, lastSelectedEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        oldSelectedPosition = pos;
        lastSelectedStart = start;
        lastSelectedEnd = end;
        final ClickableSpan clickSpan = getClickableSpan(i, true);
        spans.setSpan(clickSpan, start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public int getHighlightedPosition() {
        return lastSelectedStart;
    }

    public void setText(String[] text) {
        final StringBuilder builder = new StringBuilder();
        for(String s : text) {
            builder.append(s);
            builder.append(' ');
        }
        setText(builder.toString());
    }

    private CleanClickableSpan getClickableSpan(final int pos, boolean underline) {
        return new CleanClickableSpan(pos, underline) {
            @Override
            public void onClick(View widget) {
                if(mListener != null && isClickEnabled) mListener.spanClicked(pos);
            }
        };
    }

    public interface OnSpanClickListener {

        void spanClicked(int pos);

    }

    private abstract class CleanClickableSpan extends ClickableSpan {
        final int pos;
        final boolean underline;

        public CleanClickableSpan(int pos, boolean underline) {
            this.pos = pos;
            this.underline = underline;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(underline);
        }
    }

}
