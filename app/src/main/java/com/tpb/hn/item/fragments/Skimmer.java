package com.tpb.hn.item.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andrewgiang.textspritzer.lib.SpritzerTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.network.ReadabilityLoader;
import com.tpb.hn.storage.SharedPrefsController;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 * Takes the data from Readability and displays it Spritz style
 */

public class Skimmer extends Fragment implements ItemLoader, ReadabilityLoader.ReadabilityLoadDone, ItemAdapter.FragmentCycle {
    private static final String TAG = Skimmer.class.getSimpleName();
    private Tracker mTracker;

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

    @BindView(R.id.skimmer_error_message)
    TextView mErrorTextView;

    private ItemAdapter.Fullscreen fullscreen;

    private String article;
    private boolean isArticleReady = false;


    public static Skimmer newInstance(ItemAdapter.Fullscreen fullscreen) {
        final Skimmer s = new Skimmer();
        s.fullscreen = fullscreen;
        return s;
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
                if(Build.VERSION.SDK_INT >= 24) {
                    mSkimmerProgress.setProgress(0, true);
                } else {
                    mSkimmerProgress.setProgress(0);
                }
                try { //TODO- See if there is a better way to do this
                    Thread.sleep(60000/mTextView.getSpritzer().getWpm());
                } catch(Exception ignored) {}
                mTextView.getSpritzer().pause();

            }
        });

        if(isArticleReady) {
            setupSkimmer();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("article") != null) {
                article = savedInstanceState.getString("article");
                setupSkimmer();
            }
        }
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        return inflated;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("article", article);
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        Log.i(TAG, "loadDone: Skimmer load " + isArticleReady);
        if(success) {
            try {
                String content = result.getString("content");
                if(content == null) content = " ";
                article = Html.fromHtml(content).
                        toString().
                        replace("\n", " ");
                isArticleReady = true;
                if(mTextView != null) setupSkimmer();
                Log.i(TAG, "loadDone: Done loading. Is view OK? " + (mTextView != null));
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
            }
        } else  {
            mErrorTextView.setVisibility(View.VISIBLE);
            mProgressSpinner.setVisibility(View.GONE);
            mTextView.setVisibility(View.INVISIBLE);
            mSkimmerProgress.setVisibility(View.INVISIBLE);
            if(code == ReadabilityLoader.ReadabilityLoadDone.ERROR_PDF) {
                mErrorTextView.setText(R.string.error_pdf_skimmer);
            } else {
                mErrorTextView.setText(R.string.error_loading_page);
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
        final SharedPrefsController prefs = SharedPrefsController.getInstance(getContext());
        new MaterialDialog.Builder(getContext())
                .title(R.string.title_wpm_dialog)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .autoDismiss(false)
                .input(String.format(getString(R.string.hint_wpm_input), prefs.getSkimmerWPM()),
                        null,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                boolean error = false;
                                try {
                                    final int wpm = Integer.parseInt(input.toString());
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
                                    dialog.getInputEditText().setError(getContext().getString(R.string.error_wpm_input));
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        })
                .canceledOnTouchOutside(true)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .cancelable(true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }

    @Override
    public void loadItem(Item item) {
        if(item.getType() == ItemType.STORY && item.getUrl() != null) {
            new ReadabilityLoader(this).loadArticle(item.getUrl(), true);
        } else {
            article = item.getText() == null ? "" : Html.fromHtml(item.getText()).toString();
            isArticleReady = true;
        }
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
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
