package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.CommentAdapter;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.network.loaders.HNItemLoader;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 */

public class Comments extends Fragment implements ItemLoader, FragmentPagerAdapter.FragmentCycleListener, HNItemLoader.HNItemLoadDone {
    private static final String TAG = Comments.class.getSimpleName();
    private Tracker mTracker;

    private Unbinder unbinder;

    @BindView(R.id.comment_recycler)
    RecyclerView mRecycler;


    private Item mRootItem;
    private CommentAdapter mAdapter;

    public static Comments newInstance() {
        return new Comments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_comments, container, false);
        unbinder = ButterKnife.bind(this, view);

        mAdapter = new CommentAdapter(mRecycler);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        return view;
    }

    @Override
    public void loadItem(Item item) {
        new HNItemLoader(getContext(), this).loadItemForComments(item.getId());
    }

    @Override
    public void itemLoaded(Item item, boolean success, int code) {
        mAdapter.loadItem(item);
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success, int code) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
