package com.tpb.hn.viewer.views.spritzer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theo on 12/11/16.
 * http://stackoverflow.com/questions/8612652/select-a-word-on-a-tap-in-textview-edittext
 */

public class ClickableTextView extends TextView {
    private final int currentPos;
    private OnSpanClickListener mListener;

    public ClickableTextView(Context context, int currentPos) {
        super(context);
        this.currentPos = currentPos;
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

    public void setListener(OnSpanClickListener listener) {
        mListener = listener;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        setMovementMethod(LinkMovementMethod.getInstance());
        super.setText(text, BufferType.SPANNABLE);
        final Spannable spans = (Spannable) getText();
        Integer[] indices = getIndices(getText().toString(), ' ');
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
                if(mListener != null) mListener.spanClicked(pos);
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
