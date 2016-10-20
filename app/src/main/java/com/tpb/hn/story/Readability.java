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

public class Readability extends Fragment implements StoryLoader, ReadabilityLoader.ReadabilityLoadDone {
    private static final String TAG = Readability.class.getCanonicalName();

    private Unbinder unbinder;

    @BindView(R.id.readability_title)
    TextView mTitle;

    @BindView(R.id.readability_body)
    TextView mBody;

    @BindView(R.id.readability_image)
    ImageView mImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_readability, container, false);
        unbinder = ButterKnife.bind(this, inflated);
        return inflated;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        new ReadabilityLoader(this).loadArticle("http://www.bbc.co.uk/news/science-environment-37707776");
    }

    @Override
    public void loadDone(JSONObject result, boolean success) {
        if(success) {
            Log.i(TAG, "loadDone: " + result.toString());
            try {
                mTitle.setText(result.getString("title"));
                mBody.setText(Html.fromHtml(result.getString("content")));
            } catch(Exception e) {
                Log.e(TAG, "loadDone: ", e);
                mTitle.setText(R.string.error_loading_page);
            }
        } else {
            mTitle.setText(R.string.error_loading_page);
            //TODO- Option to retry
            //TODO- Interface for showing snackbar
        }

    }

    @Override
    public void loadStory(Item item) {
        //TODO- Show loading indicator
        mTitle.setText(null);
        mBody.setText(null);
    }
}
