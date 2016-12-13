package com.tpb.hn.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tpb.hn.R;

/**
 * Created by theo on 13/12/16.
 */

public class FeedSettingsActivity extends BottomSheetActivity {

    @Override
    void onSheetCreate(@Nullable Bundle savedInstanceState) {
    }

    @Override
    View getView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.shard_preferences_feed, parent, false);
    }

    public void onToggleClick(@NonNull View view) {

    }
}
