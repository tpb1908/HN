package com.tpb.hn.story;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;

import de.jetwick.snacktory.JResult;

/**
 * Created by theo on 18/10/16.
 */

public class Readability extends Fragment implements StoryLoader, ReadabilityLoader.ReadabilityLoadDone {
    private static final String TAG = Readability.class.getCanonicalName();

    //@BindView(R.id.readability_title)
    TextView mTitle;

    //@BindView(R.id.readability_body)
    TextView mBody;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflated = inflater.inflate(R.layout.fragment_readability, container, false);
        //ButterKnife.bind(inflated);
        mTitle = (TextView) inflated.findViewById(R.id.readability_title);
        mBody = (TextView) inflated.findViewById(R.id.readability_body);
        return inflated;
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
    public void loadDone(JResult result) {
        Log.i(TAG, "loadDone: " + result.toString());
        mTitle.setText(result.getTitle());
        mBody.setText(result.getText());
    }

    @Override
    public void loadStory(Item item) {

    }
}
