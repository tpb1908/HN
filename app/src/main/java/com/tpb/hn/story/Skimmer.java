package com.tpb.hn.story;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.andrewgiang.textspritzer.lib.SpritzerTextView;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 * Takes the data from Readability and displays it Spritz style
 */

public class Skimmer extends Fragment implements StoryLoader, ReadabilityLoader.ReadabilityLoadDone {
    private static final String TAG = Skimmer.class.getCanonicalName();

    private Unbinder unbinder;

    @BindView(R.id.skimmer_root_layout)
    FrameLayout mRoot;

    @BindView(R.id.spritzer_text_view)
    SpritzerTextView mTextView;

    @BindView(R.id.skimmer_loading_spinner)
    ProgressBar mProgressSpinner;

    private int wpm = 250;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_skimmer, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        return inflated;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRoot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mTextView.play();
                return false;
            }
        });

        mRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mTextView.pause();
                }
                return false;
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWPMDialog();
            }
        });

        new ReadabilityLoader(this).loadArticle("http://www.bbc.co.uk/news/science-environment-37707776");
    }

    @Override
    public void loadDone(JSONObject result, boolean success) {
        try {
            final String content = Html.fromHtml(result.getString("content")).
                    toString().
                    replace("\n", " ");
            mProgressSpinner.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setSpritzText(content);
            mTextView.pause();
        } catch(Exception e) {
            mProgressSpinner.setVisibility(View.INVISIBLE);
            Log.e(TAG, "loadDone: ", e);
        }
    }

    private void showWPMDialog() {
        final LayoutInflater li = LayoutInflater.from(getContext());
        final View input = li.inflate(R.layout.dialog_get_wpm, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(input);

        final EditText wpmInput = (EditText) input.findViewById(R.id.input_wpm);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    final int wpm = Integer.parseInt(wpmInput.getText().toString());
                    if(wpm > 1500) throw new Exception();
                    Skimmer.this.wpm = wpm;
                    mTextView.setWpm(wpm);
                } catch(Exception e) {
                    wpmInput.setError(getContext().getString(R.string.error_wpm_input));
                }
            }
        })
                .setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void loadStory(Item item) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
