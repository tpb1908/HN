package com.tpb.hn.story;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

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

public class Skimmer extends Fragment implements StoryLoader, ReadabilityLoader.ReadabilityLoadDone, StoryAdapter.FragmentCycle {
    private static final String TAG = Skimmer.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.skimmer_root_layout)
    RelativeLayout mRoot;

    @BindView(R.id.spritzer_text_view)
    SpritzerTextView mTextView;

    @BindView(R.id.skimmer_spritzer_progress)
    ProgressBar mSkimmerProgress;

    @BindView(R.id.skimmer_loading_spinner)
    ProgressBar mProgressSpinner;

    @BindView(R.id.skimmer_restart_button)
    Button mRestartButton;


    private String article;
    private boolean isViewReady = false;
    private boolean isArticleReady = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_skimmer, container, false);
        unbinder = ButterKnife.bind(this, inflated);
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

        mTextView.attachProgressBar(mSkimmerProgress);

        mRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextView.setSpritzText(article);
                mTextView.getSpritzer().start();
                try { //TODO- See if there is a better way to do this
                    Thread.sleep(60000/mTextView.getSpritzer().getWpm());
                } catch(Exception ignored) {}
                if(Build.VERSION.SDK_INT >= 24) {
                    mSkimmerProgress.setProgress(0, true);
                } else {
                    mSkimmerProgress.setProgress(0);
                }
                mTextView.getSpritzer().pause();

            }
        });

        isViewReady = true;
        if(isArticleReady) {
            setupSkimmer();
        }

        return inflated;
    }

    @Override
    public void loadDone(JSONObject result, boolean success) {
        if(success) {
            try {
                article = Html.fromHtml(result.getString("content")).
                        toString().
                        replace("\n", " ");
                isArticleReady = true;
                if(isViewReady) setupSkimmer();
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
            }
        }
    }

    private void setupSkimmer() {
        mProgressSpinner.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
        mSkimmerProgress.setVisibility(View.VISIBLE);
        mTextView.setWpm(SharedPrefsController.getInstance(getContext()).getSkimmerWPM());
        mTextView.setSpritzText(article);
        mTextView.pause();
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
        new ReadabilityLoader(this).loadArticle(item.getUrl());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
//        if(contentToLoad && viewLoaded) {
//            setupSkimmer();
//            contentToLoad = false;
//        }
    }
}
