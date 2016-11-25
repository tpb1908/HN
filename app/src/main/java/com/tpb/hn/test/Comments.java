package com.tpb.hn.test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpb.hn.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.view.View.GONE;

/**
 * Created by theo on 25/11/16.
 */

public class Comments extends ContentFragment implements Loader.CommentLoader, CommentAdapter.UserOpener, ItemLoader {
    private static final String TAG = Comments.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.comment_recycler) RecyclerView mRecycler;
    @BindView(R.id.comment_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.comment_no_comments) TextView mMessageView;

    private Item mRootItem;
    private Comment mRootComment;

    private CommentAdapter mAdapter;


    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_comments, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAdapter = new CommentAdapter(mRecycler, mSwiper, this);
        mSwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
            }
        });
        mSwiper.setRefreshing(true);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    void attach(Context context) {

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
        Loader.getInstance(getContext()).loadChildJSON(mRootItem.getId(), this);
    }

    private void showMessage(boolean error) {
        mMessageView.setVisibility(View.VISIBLE);
        mSwiper.setVisibility(GONE);
        mMessageView.setText(error ? R.string.error_comment_loading : R.string.text_no_comments);
    }

    @Override
    public void loadItem(Item item) {
        mRootItem = item;
        if(mContentReady) loadComments();
    }

    @Override
    public void commentsLoaded(Comment rootComment) {
        mRootComment = rootComment;
        mContentReady = true;
        if(mViewsReady) bindData();
    }

    @Override
    public void commentError(int id, int code) {

    }

    @Override
    public void openUser(Comment item) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
