package com.tpb.hn.story;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andrewgiang.textspritzer.lib.SpritzerTextView;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.storage.SharedPrefsController;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 * Takes the data from Readability and displays it Spritz style
 */

public class Skimmer extends Fragment implements StoryLoader, ReadabilityLoader.ReadabilityLoadDone {
    private static final String TAG = Skimmer.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.skimmer_root_layout)
    LinearLayout mRoot;

    @BindView(R.id.spritzer_text_view)
    SpritzerTextView mTextView;

    @BindView(R.id.skimmer_loading_spinner)
    ProgressBar mProgressSpinner;

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
            mProgressSpinner.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setWpm(SharedPrefsController.getInstance(getContext()).getSkimmerWPM());
            mTextView.setSpritzText(content);
            mTextView.pause();
        } catch(Exception e) {
            mProgressSpinner.setVisibility(View.GONE);
            Log.e(TAG, "loadDone: ", e);
        }
    }

    private void showWPMDialog() {

        final MaterialDialog md = new MaterialDialog.Builder(getContext())
                .title(R.string.title_wpm_dialog)
                .customView(R.layout.dialog_get_wpm, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
        final EditText wpmInput = (EditText) md.getCustomView().findViewById(R.id.input_wpm);
        final SharedPrefsController prefs = SharedPrefsController.getInstance(getContext());
        wpmInput.setHint(String.format(getString(R.string.hint_wpm_input), prefs.getSkimmerWPM()));

        md.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: Material dialog click");
                boolean error = false;
                try {
                    final int wpm = Integer.parseInt(wpmInput.getText().toString());
                    if(wpm > 2000) {
                        error = true;
                    } else {
                        mTextView.setWpm(wpm);
                        prefs.setSkimmerWPM(wpm);
                    }
                } catch(Exception e) {
                    error = true;
                }
                if(error) {
                    wpmInput.setError(getContext().getString(R.string.error_wpm_input));
                } else {
                    md.dismiss();
                }
            }
        });
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
