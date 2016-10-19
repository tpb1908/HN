package com.tpb.hn.story;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.jetwick.snacktory.JResult;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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
        try {
            new ReadabilityLoader(this).execute("http://www.bbc.co.uk/news/business-37649892");
        } catch(Exception e) {
            Log.e(TAG, "onStart: ", e);
        }
    }

    @Override
    public void loadDone(JResult result, boolean success) {
        if(success) {
            Log.i(TAG, "loadDone: " + result.toString());
            mTitle.setText(result.getTitle());
            if(result.getImages().get(0) != null) {
                //TODO- Option to load images
                Log.i(TAG, "loadDone: " + result.getImages().get(0).src);
            }
            mBody.setText(result.getText());
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
