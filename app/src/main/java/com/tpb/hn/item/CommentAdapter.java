package com.tpb.hn.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.content.DividerItemDecoration;
import com.tpb.hn.storage.SharedPrefsController;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 29/10/16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private static final String TAG = CommentAdapter.class.getSimpleName();

    private RecyclerView mRecycler;

    private boolean usingCards;

    public CommentAdapter(RecyclerView recycler) {
        mRecycler = recycler;
        final Context context = mRecycler.getContext();
        usingCards = SharedPrefsController.getInstance(context).getUseCards();
        if(!usingCards) {
            mRecycler.addItemDecoration(new DividerItemDecoration(context.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
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
        return 25;
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.comment_card)
        CardView mCard;

        CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
