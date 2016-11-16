package com.tpb.hn.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.content.DividerItemDecoration;
import com.tpb.hn.data.Item;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 29/10/16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> implements ItemLoader {
    private static final String TAG = CommentAdapter.class.getSimpleName();

    @BindArray(R.array.comment_colors) int[] mCommentColors;

    private Item mRootItem;
    private RecyclerView mRecycler;

    private ArrayList<Comment> mComments = new ArrayList<>();
    private boolean usingCards;

    public CommentAdapter(RecyclerView recycler) {
        mRecycler = recycler;
        final Context context = mRecycler.getContext();
        usingCards = SharedPrefsController.getInstance(context).getUseCards();
        if(!usingCards) {
            mRecycler.addItemDecoration(new DividerItemDecoration(context.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        ButterKnife.bind(this, recycler);
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        final int pos = holder.getAdapterPosition();
        final Comment comment = mComments.get(pos);
        if(comment.item.getText() != null) {
            holder.mBody.setText(Html.fromHtml(comment.item.getText()));
        }
        holder.mTitle.setText(comment.item.getBy() + " " + comment.depth);
        holder.mColorBar.setBackgroundColor(mCommentColors[comment.depth%mCommentColors.length]);
        holder.mPadding.getLayoutParams().width = Util.pxFromDp(comment.depth * 4);

        if(usingCards) {
            holder.mCard.setUseCompatPadding(true);
            holder.mCard.setCardElevation(Util.pxFromDp(4));
            holder.mCard.setRadius(Util.pxFromDp(3));
        }
    }

    @Override
    public void onViewRecycled(CommentHolder holder) {

    }

    @Override
    public int getItemCount() {
        //TODO- Display message for item with no comments
        return mComments.size();
    }
    @Override
    public void loadItem(Item item) {
        Log.i(TAG, "loadItem: Adapter loading items");
        mRootItem = item;
        Log.i(TAG, "loadItem: Root item " + mRootItem.toString());
        mComments = flatten(mRootItem.getComments(), 0);
        notifyDataSetChanged();
        //TODO- Sort the top comments by points or time

    }

    private ArrayList<Comment> flatten(Item[] items, int depth) {
        final ArrayList<Comment> list = new ArrayList<>();
        for(Item i : items) {
            list.add(new Comment(i, depth));
            if(i.getComments().length > 0) {
                list.addAll(flatten(i.getComments(), depth + 1));
            }
        }
        return list;
    }



    private class Comment {
        Item item;
        int depth = 0;
        boolean visible = true;

        Comment(Item item) {
            this.item = item;
        }

        Comment(Item item, int depth) {
            this.item = item;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return  "{" +item.getId() + ", "
                   + item.getBy() + ", "
                    + (item.getKids() == null ? "" : Arrays.toString(item.getKids()) + ", ") +
                    + depth + "}";
        }
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.comment_card) CardView mCard;
        @BindView(R.id.comment_title) TextView mTitle;
        @BindView(R.id.comment_body) TextView mBody;
        @BindView(R.id.comment_color) FrameLayout mColorBar;
        @BindView(R.id.comment_padding) FrameLayout mPadding;
        CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
