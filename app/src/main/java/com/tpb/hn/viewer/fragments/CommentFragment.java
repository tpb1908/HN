package com.tpb.hn.viewer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.Loader;
import com.tpb.hn.viewer.FragmentPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.view.View.GONE;

/**
 * Created by theo on 25/11/16.
 */

public class CommentFragment extends LoadingFragment implements Loader.CommentLoader,
        CommentAdapter.UserOpener,
        Loader.ItemLoader,
        FragmentPagerAdapter.FragmentCycleListener,
        FragmentPagerAdapter.KeyEventHandler {
    private static final String TAG = CommentFragment.class.getSimpleName();
    @BindView(R.id.comment_recycler) RecyclerView mRecycler;
    @BindView(R.id.comment_swiper) SwipeRefreshLayout mSwiper;
    @BindView(R.id.comment_no_comments) TextView mMessageView;
    private Tracker mTracker;
    private Unbinder unbinder;
    private Item mRootItem;
    private int mCommentId;
    private Comment mRootComment;
    private CommentAdapter.UserOpener mOpener;

    private CommentAdapter mAdapter;

    private boolean mIsWaitingForContext = false;

    public static CommentFragment getInstance(int commentId) {
        final CommentFragment comments = new CommentFragment();
        comments.mCommentId = commentId;
        return comments;
    }

    @Override
    View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_comments, container, false);
        mTracker = ((Analytics) getActivity().getApplication()).getDefaultTracker();
        unbinder = ButterKnife.bind(this, view);
        mAdapter = new CommentAdapter(mRecycler, mSwiper, this, mCommentId);
        mSwiper.setOnRefreshListener(() -> {
            mAdapter.clear();
            loadComments();
        });
        mSwiper.setRefreshing(true);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    void attach(Context context) {
        if(mIsWaitingForContext)
            Loader.getInstance(getContext()).loadChildren(mRootItem.getId(), this);
        if(context instanceof CommentAdapter.UserOpener) {
            mOpener = (CommentAdapter.UserOpener) context;
        } else {
            throw new IllegalArgumentException("Context must implement " + CommentAdapter.UserOpener.class.getSimpleName());

        }
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
        mSwiper.setRefreshing(false);
        mSwiper.setVisibility(GONE);
        mMessageView.setText(error ? R.string.error_comment_loading : R.string.text_no_comments);
    }

    @Override
    public boolean onKeyEvent(int keyCode) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mAdapter.scrollDown();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mAdapter.scrollUp();
            return true;
        }
        return false;
    }

    @Override
    public void itemLoaded(Item item) {
        if(Analytics.VERBOSE) Log.i(TAG, "itemLoaded: " + item);
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
        if(Analytics.VERBOSE) Log.i(TAG, "commentsLoaded: CommentFragment loaded");
        mRootComment = rootComment;
        mContentReady = true;
        if(mViewsReady) bindData();
    }

    @Override
    public void commentError(int id, int code) {
        if(Analytics.VERBOSE) Log.i(TAG, "commentError: " + id + " | " + code);
    }

    @Override
    public void openUser(Item item) {
        mOpener.openUser(item);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
