package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.widget.ANImageView;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.item.ReadabilityLoader;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 */

public class Readability extends Fragment implements ItemLoader, ReadabilityLoader.ReadabilityLoadDone, ItemAdapter.FragmentCycle {
    private static final String TAG = Readability.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.readability_title)
    TextView mTitle;

    @BindView(R.id.readability_body)
    TextView mBody;

    @BindView(R.id.readability_image)
    ANImageView mImage;

    @BindView(R.id.readability_loading_spinner)
    ProgressBar mProgressSpinner;

    @BindView(R.id.readability_wrapper)
    LinearLayout mWrapper;

    @BindView(R.id.readability_error_message)
    TextView mErrorTextView;

    @BindView(R.id.readability_scroller)
    NestedScrollView mScroller;

    private String title;
    private String content;

    private boolean isArticleReady = false;

    public static Readability newInstance() {
        return new Readability();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_readability, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        if(isArticleReady) {
            setupTextView();
        } else if(savedInstanceState != null) {
            if(savedInstanceState.getString("title") != null) {
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
        if(content != null) mBody.setText(Html.fromHtml(content));

    }

    @Override
    public void loadDone(JSONObject result, boolean success, int code) {
        Log.i(TAG, "loadDone: " + success + ", " + code + ", " + result);
        if(success) {
            try {
                content = result.getString("content");
                title = result.getString("title");
                isArticleReady = true;
                if(mBody != null) setupTextView();
                final String imageURL = result.getString("lead_image_url");
                mImage.setImageUrl(imageURL);
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
                mProgressSpinner.setVisibility(View.GONE);
                mTitle.setText(R.string.error_loading_page);
            }
        } else {
            mProgressSpinner.setVisibility(View.GONE);
            mErrorTextView.setVisibility(View.VISIBLE);
            if(code == ReadabilityLoader.ReadabilityLoadDone.ERROR_PDF) {
                mErrorTextView.setText(R.string.error_pdf_readability);
            } else {
                mErrorTextView.setText(R.string.error_loading_page);
            }

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
    public void loadItem(Item item) {
        //TODO- Check if it has a url, not just type
        Log.i(TAG, "loadItem: Readability loading item " + item);
        if(item.getType() == ItemType.STORY) {
            new ReadabilityLoader(this).loadArticle(item.getUrl(), true);
        } else {
            title = item.getTitle();
            content = item.getText();
            isArticleReady = true;
        }

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }
}
