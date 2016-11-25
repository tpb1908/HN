package com.tpb.hn.item.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.CommentAdapter;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.network.loaders.CachedItemLoader;
import com.tpb.hn.network.loaders.ItemManager;
import com.tpb.hn.storage.permanent.ItemCache;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by theo on 18/10/16.
 */

public class Comments extends Fragment implements ItemLoader, FragmentPagerAdapter.FragmentCycleListener, ItemManager.ItemLoadListener, CommentAdapter.UserOpener {
    private static final String TAG = Comments.class.getSimpleName();
    private Tracker mTracker;

    private Unbinder unbinder;

    @BindView(R.id.comment_recycler) RecyclerView mRecycler;
    @BindView(R.id.comment_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.comment_no_comments) TextView mMessageView;

    private Item mRootItem; //The Item from the HN api
    private Item mCommentItem; //The Item from Algolia, with comments
    private CommentAdapter mAdapter;
    private CommentAdapter.UserOpener mOpener;

    private boolean itemReady = false;
    private boolean viewsReady = false;

    public static Comments newInstance() {
        return new Comments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_comments, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAdapter = new CommentAdapter(mRecycler, mSwiper, this);
        mSwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                loadItem(mRootItem);
            }
        });
        mSwiper.setRefreshing(true);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        if(mRootItem != null && !itemReady) Util.getItemManager(getContext(), this).loadItemForComments(mRootItem.getId());
        if(itemReady) bindItem();
        viewsReady = true;
        return view;
    }

    private void showMessage(boolean error) {
        mMessageView.setVisibility(View.VISIBLE);
        mSwiper.setVisibility(View.GONE);
        mMessageView.setText(error ? R.string.error_comment_loading : R.string.text_no_comments);
    }

    private void bindItem() {
        if(mRootItem.getDescendants() == 0) {
            showMessage(false);
        } else {
            if(mSwiper == null) {
                viewsReady = false;
                return;
            }
            mSwiper.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.GONE);
            mAdapter.loadItem(mCommentItem);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof CommentAdapter.UserOpener) {
            mOpener = (CommentAdapter.UserOpener) context;
        } else {
            throw new IllegalArgumentException("Context must implement " + CommentAdapter.UserOpener.class.getSimpleName());
        }
    }

    @Override
    public void openUser(Item item) {
        mOpener.openUser(item);
    }

    @Override
    public void loadItem(Item item) {
        mRootItem = item;
        if(item.getDescendants() == 0) {
            itemReady = true;
        } else if(getContext() != null){
            Util.getItemManager(getContext(), this).loadItemForComments(item.getId());
        }

    }

    @Override
    public void itemLoaded(Item item, boolean success, int code) {
        itemReady = true;
        if(success && item.getCommentJSON().equals("[]")) {
            if(viewsReady) showMessage(false);
        } else if(!success) {
            if(code == CachedItemLoader.MESSAGE_COMMENTS_OFFLINE) {
                mCommentItem = mRootItem;
            } else if(viewsReady) {
                showMessage(true);
            }
        } else {
            mCommentItem = item;
            ItemCache.getInstance(getContext()).insert(mRootItem, false);
            if(viewsReady) bindItem();
        }
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
