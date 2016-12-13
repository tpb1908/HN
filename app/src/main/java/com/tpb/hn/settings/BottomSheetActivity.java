package com.tpb.hn.settings;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tpb.hn.R;

import butterknife.ButterKnife;

/**
 * Created by theo on 13/12/16.
 */

public abstract class BottomSheetActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_bottom_sheet);

        findViewById(R.id.outside_touch_area).setOnClickListener((v) -> finish());
        BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)).
                setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch(newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                finish();
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED:
                                setStatusBarDim(false);
                                break;
                            default:
                                setStatusBarDim(true);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
        ((FrameLayout) findViewById(R.id.bottom_sheet)).addView(getView(this, (ViewGroup) findViewById(R.id.bottom_sheet)));
        ButterKnife.bind(this);
        onSheetCreate(savedInstanceState);
    }

    abstract void onSheetCreate(@Nullable Bundle savedInstanceState);

    abstract View getView(Context context, ViewGroup parent);

    private void setStatusBarDim(boolean dim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dim ? Color.TRANSPARENT :
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

}
