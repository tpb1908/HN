package com.tpb.hn.story;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 */

public class Readability extends Fragment implements StoryLoader, ReadabilityLoader.ReadabilityLoadDone, StoryAdapter.FragmentCycle {
    private static final String TAG = Readability.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.readability_title)
    TextView mTitle;

    @BindView(R.id.readability_body)
    TextView mBody;

    @BindView(R.id.readability_image)
    ImageView mImage;

    @BindView(R.id.readability_loading_spinner)
    ProgressBar mProgressSpinner;

    @BindView(R.id.readability_wrapper)
    LinearLayout mWrapper;

    private String title;
    private String content;

    private boolean isArticleReady = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_readability, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        if(isArticleReady) {
            setupTextView();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("title") != null) {
                Log.i(TAG, "onCreateView: Getting from savedInstanceState");
                title = savedInstanceState.getString("title");
                content = savedInstanceState.getString("content");
                setupTextView();
            }
        }
        return inflated;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupTextView() {
        mProgressSpinner.setVisibility(View.GONE);
        mWrapper.setVisibility(View.VISIBLE);
        mTitle.setText(title);
        mBody.setText(Html.fromHtml(content));
    }

    @Override
    public void loadDone(JSONObject result, boolean success) {
        if(success) {
            try {
                content = result.getString("content");
                title = result.getString("title");
                isArticleReady = true;
                if(mBody != null) setupTextView();
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
                mProgressSpinner.setVisibility(View.INVISIBLE);
                mTitle.setText(R.string.error_loading_page);
            }
        } else {
            mTitle.setText(R.string.error_loading_page);
            //TODO- Option to retry
            //TODO- Interface for showing snackbar
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putSerializable("content", content);
    }

    @Override
    public void loadStory(Item item) {
        Log.i(TAG, "loadStory: Readability loading story");
        new ReadabilityLoader(this).loadArticle(item.getUrl());
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }
}
