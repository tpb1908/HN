package com.tpb.hn.item.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by theo on 24/11/16.
 */

public abstract class ContentFragment extends Fragment {

    private boolean mViewsReady = false;
    private boolean mContentReady = false;
    private boolean mContextReady = false;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = createView(inflater, container, savedInstanceState);
        mViewsReady = true;
        if(mContentReady) bindData();
        return view;
    }

    @Override
    public final void onAttach(Context context) {
        super.onAttach(context);
        attach(context);
        mContentReady = true;
    }

    abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    abstract void attach(Context context);

    abstract void bindData();
}
