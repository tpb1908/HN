package com.tpb.hn.item.fragments;

import android.content.Context;
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
import android.widget.SeekBar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.item.ItemViewActivity;
import com.tpb.hn.item.views.spritzer.SpritzerTextView;
import com.tpb.hn.network.loaders.TextLoader;
import com.tpb.hn.storage.SharedPrefsController;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 * Takes the data from Readability and displays it Spritz style
 */

public class Skimmer extends Fragment implements ItemLoader, TextLoader.TextLoadDone, FragmentPagerAdapter.FragmentCycleListener {
    private static final String TAG = Skimmer.class.getSimpleName();
    private Tracker mTracker;

    private Unbinder unbinder;

    private ItemViewActivity mParent;

    @BindView(R.id.spritzer_text_view)
    SpritzerTextView mTextView;

    @BindView(R.id.skimmer_spritzer_progress)
    SeekBar mSkimmerProgress;


    private String article;
    private boolean isArticleReady = false;

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

    @OnClick(R.id.spritzer_text_view)
    void onSpritzerClick() {
        mTextView.showTextDialog();
    }

    @OnLongClick(R.id.spritzer_text_view)
    boolean onSpritzerLongClick() {
        mTextView.showWPMDialog();
        return true;
    }

    public static Skimmer newInstance() {
        return new Skimmer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_skimmer, container, false);

        unbinder = ButterKnife.bind(this, inflated);

        mTextView.attachSeekBar(mSkimmerProgress);

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ItemViewActivity) {
            mParent = (ItemViewActivity) context;
        } else {
            throw new IllegalArgumentException("Activity must be instance of " + ItemViewActivity.class.getSimpleName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("article", article);
    }

    @Override
    public void loadItem(Item item) {
        if(item.getType() == ItemType.STORY && item.getUrl() != null) {
            new TextLoader(this).loadArticle(item.getUrl(), true);
        } else {
            article = item.getText() == null ? "" : Html.fromHtml(item.getText()).toString();
            isArticleReady = true;
        }
    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        if(success) {
            try {
                bindData((String) result.get("content"));
            } catch(Exception e) {
                Log.e(TAG, "bindData: ", e);
            }
        } else  {
            mTextView.setVisibility(View.INVISIBLE);
            mSkimmerProgress.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void loadDone(String result, boolean success, int code) {
        if(success) {
            bindData(result);
        } else {
            //TODO- Error handling
            mTextView.setVisibility(View.INVISIBLE);
            mSkimmerProgress.setVisibility(View.INVISIBLE);

        }
    }

    private void bindData(String content) {
        if(content== null) content = " ";
        article = Html.fromHtml(content).
                toString().
                replace("\n", " ");
        isArticleReady = true;
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
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mParent.hideFab();
    }

    @Override
    public boolean onBackPressed() {
        //The Skimmer never needs to stop the back button
        return true;
    }
}
