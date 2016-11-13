package com.tpb.hn.item.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
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
    private OnSpanClickListener mListener;

    public ClickableTextView(Context context) {
        super(context);
    }

    public ClickableTextView(Context context, @NonNull AttributeSet attributeSet) {
        super(context, attributeSet);
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
        for (int i = 0; i <= indices.length; i++) {
            ClickableSpan clickSpan = getClickableSpan();
            // to cater last/only word
            end = (i < indices.length ? indices[i] : spans.length());
            spans.setSpan(clickSpan, start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
    }

    private ClickableSpan getClickableSpan(){
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                final TextView tv = (TextView) widget;
                final String s = tv
                        .getText()
                        .subSequence(tv.getSelectionStart(),
                                tv.getSelectionEnd()).toString();
                Log.d("tapped on:", s);
                if(mListener != null) mListener.spanClicked(s);
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
    }

    public static Integer[] getIndices(String s, char c) {
        int pos = s.indexOf(c, 0);
        List<Integer> indices = new ArrayList<Integer>();
        while (pos != -1) {
            indices.add(pos);
            pos = s.indexOf(c, pos + 1);
        }
        return indices.toArray(new Integer[0]);
    }

    public interface OnSpanClickListener {

        void spanClicked(String span);

    }

}