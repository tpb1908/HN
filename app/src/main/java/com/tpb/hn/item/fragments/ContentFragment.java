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

    boolean mViewsReady = false;
    boolean mContentReady = false;
    boolean mContextReady = false;

    @Override
    public final void onAttach(Context context) {
        super.onAttach(context);
        attach(context);
        mContextReady = true;
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = createView(inflater, container, savedInstanceState);
        mViewsReady = true;
        if(mContentReady) bindData();
        return view;
    }

    abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    abstract void attach(Context context);

    abstract void bindData();

}
