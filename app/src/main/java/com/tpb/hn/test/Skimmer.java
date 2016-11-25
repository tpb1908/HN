package com.tpb.hn.test;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.item.views.spritzer.SpritzerTextView;
import com.tpb.hn.storage.SharedPrefsController;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

/**
 * Created by theo on 25/11/16.
 */

public class Skimmer extends ContentFragment implements ItemLoader, Loader.TextLoader, FragmentPagerAdapter.FragmentCycleListener {
    private static final String TAG = Skimmer.class.getSimpleName();

    private Unbinder unbinder;

    private ItemViewActivity mParent;

    @BindView(R.id.skimmer_text_view) SpritzerTextView mTextView;
    @BindView(R.id.skimmer_progress) SeekBar mSkimmerProgress;
    @BindView(R.id.skimmer_restart_button) Button mRestartButton;
    @BindView(R.id.skimmer_error_textview) TextView mErrorView;

    private String article;

    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_skimmer, container, false);

        unbinder = ButterKnife.bind(this, inflated);

        mTextView.attachSeekBar(mSkimmerProgress);

        if(mContentReady) {
            setupSkimmer();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("article") != null) {
                article = savedInstanceState.getString("article");
                setupSkimmer();
            }
        }
        return inflated;
    }

    @OnLongClick(R.id.skimmer_touch_area)
    boolean onLongClick() {
        mTextView.play();
        return false;
    }

    @OnTouch(R.id.skimmer_touch_area)
    boolean onTouch(MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mTextView.pause();
        }
        return false;
    }

    @OnClick(R.id.skimmer_restart_button)
    void onRestartClick() {
        mTextView.setSpritzText(article);
        mTextView.getSpritzer().start();
        if(Build.VERSION.SDK_INT >= 24) {
            mSkimmerProgress.setProgress(0, true);
        } else {
            mSkimmerProgress.setProgress(0);
        }
                /*
                In order to skip one word, we have to wait
                for one minute / words per minute
                 */
        mTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTextView.getSpritzer().pause();
            }
        }, 60000 / mTextView.getSpritzer().getWpm());
    }

    @OnClick(R.id.skimmer_text_view)
    void onSpritzerClick() {
        mTextView.showTextDialog();
    }

    @OnLongClick(R.id.skimmer_text_view)
    boolean onSpritzerLongClick() {
        mTextView.showWPMDialog();
        return true;
    }


    private void displayErrorMessage() {
        mTextView.setVisibility(View.INVISIBLE);
        mSkimmerProgress.setVisibility(View.INVISIBLE);
        mRestartButton.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.VISIBLE);
        mErrorView.setText(R.string.error_parsing_readable_text);
    }

    private void bindData(String content) {
        if(content == null) content = " ";
        article = Html.fromHtml(content).
                toString().
                replace("\n", " ");
        mContentReady = true;
        if(mTextView != null) setupSkimmer();
    }

    private void setupSkimmer() {
        mTextView.setVisibility(View.VISIBLE);
        mSkimmerProgress.setVisibility(View.VISIBLE);
        mTextView.setWpm(SharedPrefsController.getInstance(getContext()).getSkimmerWPM());
        mTextView.setSpritzText(article);
        mTextView.pause();
    }

    @Override
    void attach(Context context) {

    }

    @Override
    void bindData() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPauseFragment() {
        mTextView.pause();
    }

    @Override
    public void onResumeFragment() {

    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void loadItem(Item item) {

    }

    @Override
    public void textLoaded(JSONObject result) {

    }

    @Override
    public void textError(String url, int code) {

    }
}