package com.tpb.hn.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tpb.hn.R;

/**
 * Created by theo on 13/12/16.
 */

public class FeedSettingsActivity extends BottomSheetActivity {

    @Override
    void onSheetCreate(@Nullable Bundle savedInstanceState) {
        Toast.makeText(this, "FeedSettings", Toast.LENGTH_LONG).show();
    }

    @Override
    View getView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.shard_preferences_feed, parent, false);
    }
}
