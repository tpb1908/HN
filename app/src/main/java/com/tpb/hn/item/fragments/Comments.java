package com.tpb.hn.item.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.network.loaders.Loader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.view.View.GONE;

/**
 * Created by theo on 25/11/16.
 */

public class Comments extends ContentFragment implements Loader.CommentLoader,
        CommentAdapter.UserOpener,
        Loader.ItemLoader,
        FragmentPagerAdapter.FragmentCycleListener {
    private static final String TAG = Comments.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.comment_recycler) RecyclerView mRecycler;
    @BindView(R.id.comment_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.comment_no_comments) TextView mMessageView;

    private Item mRootItem;
    private Comment mRootComment;

    private CommentAdapter mAdapter;

    private boolean mIsWaitingForContext = false;

    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_comments, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAdapter = new CommentAdapter(mRecycler, mSwiper, this);
        mSwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                loadComments();
            }
        });
        mSwiper.setRefreshing(true);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    void attach(Context context) {
        if(mIsWaitingForContext) Loader.getInstance(getContext()).loadChildren(mRootItem.getId(), this);
    }

    @Override
    void bindData() {
        if(mRootItem.getDescendants() == 0) {
            showMessage(false);
        } else {
            mSwiper.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(GONE);
            mAdapter.loadComment(mRootComment);
        }
    }

    private void loadComments() {
        Loader.getInstance(getContext()).loadChildren(mRootItem.getId(), this);
    }

    private void showMessage(boolean error) {
        mMessageView.setVisibility(View.VISIBLE);
        mSwiper.setVisibility(GONE);
        mMessageView.setText(error ? R.string.error_comment_loading : R.string.text_no_comments);
    }

    @Override
    public void itemLoaded(Item item) {
        Log.i(TAG, "itemLoaded: " + item);
        mRootItem = item;
        if(mContextReady) {
            loadComments();
        } else {
            mIsWaitingForContext = true;
        }
    }

    @Override
    public void itemError(int id, int code) {

    }

    @Override
    public void commentsLoaded(Comment rootComment) {
        Log.i(TAG, "commentsLoaded: Comments loaded");
        mRootComment = rootComment;
        mContentReady = true;
        if(mViewsReady) bindData();
    }

    @Override
    public void commentError(int id, int code) {
        Log.i(TAG, "commentError: " + id + " | " + code);
    }

    @Override
    public void openUser(Comment item) {

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
