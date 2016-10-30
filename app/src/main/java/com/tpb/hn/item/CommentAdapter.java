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
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.content.DividerItemDecoration;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 29/10/16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> implements ItemLoader, HNLoader.HNItemLoadDone{
    private static final String TAG = CommentAdapter.class.getSimpleName();

    private Item mRootItem;
    private RecyclerView mRecycler;

    private ArrayList<Comment> mComments = new ArrayList<>();
    private ArrayList<Comment> mVisibleComments = new ArrayList<>(); //TODO- Make this an int array
    private HNLoader mLoader;

    /*
    Inserting a comment-
    if comment parent is root item {
        iterate through added items and root kid ids
        when we get to the id of the comment, add it
    } else {
        iterate through added items for the id of the mComments parent
        pos = index of the mComments parent
        iterate through the add items (past pos) and the ids of the parents kids
        when we get to the id of the comment, add it
    }
     */

    private boolean usingCards;

    public CommentAdapter(RecyclerView recycler) {
        mRecycler = recycler;
        final Context context = mRecycler.getContext();
        usingCards = SharedPrefsController.getInstance(context).getUseCards();
        if(!usingCards) {
            mRecycler.addItemDecoration(new DividerItemDecoration(context.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        mLoader = new HNLoader(this);
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        final int pos = holder.getAdapterPosition();
        if(pos < mComments.size() && mComments.get(pos) != null) {
            holder.mBody.setText(Html.fromHtml(mComments.get(pos).item.getText()));
        }
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
        //TODO- Display message for item with no mComments
        return mRootItem == null ? 0 : mRootItem.getKids() == null ? 0 : mRootItem.getKids().length;
    }

    @Override
    public void loadItem(Item item) {
        mRootItem = item;
        if(item.getKids() != null) mLoader.loadItemsIndividually(item.getKids(), false);
        notifyDataSetChanged();
    }

    @Override
    public void itemLoaded(Item item, boolean success) {
        if(success) {
            insert(item);
            if(item.getKids() != null) {
                mLoader.loadItemsIndividually(item.getKids(), false);
            }
        }
    }

    private void insert(Item item) {
        Log.i(TAG, "insert: Item " + item.getId() );
        final Comment c = new Comment(item);
        int pp = -2;
        int[] siblings = null;

        if(item.getParent() == mRootItem.getId()) {
            pp = -1;
            siblings = mRootItem.getKids();
            c.depth = 0;
            Log.i(TAG, "insert: Root comment " + item.getId());
        } else {
            for(int i = 0; i < mComments.size(); i++) {
                if(item.getParent() == mComments.get(i).item.getId()) {
                    pp = i;
                    siblings = mComments.get(i).item.getKids();
                    c.depth = mComments.get(i).depth + 1;
                }
            }
        }
        if(pp == -2) {
            Log.d(TAG, "insert: Didn't find parent");
        } else {
            final int index = Util.linearSearch(siblings, item.getId());
            if(index == 0) {
                mComments.add(Math.max(pp, 0), c);
                Log.i(TAG, "insert: First comment in siblings. Pos " + pp);
                notifyDataSetChanged();
                //notifyItemInserted(Math.max(pp, 0));
            } else {
                for(int i = 0; i < mComments.size(); i++) {
                    final Comment other = mComments.get(i);
                    if(other.depth < c.depth) {
                        mComments.add(Math.max(i - 1, 0), c);
                        Log.i(TAG, "insert: Found lower depth. Pos " + Math.max(i - 1, 0));
                        notifyDataSetChanged();
                        return;
                        //notifyItemInserted(Math.max(i - 1, 0));
                    } else if(other.item.getParent() == c.item.getParent()) {
                        int sibPos = Util.linearSearch(siblings, other.item.getId());
                        Log.i(TAG, "insert: Found sibling");
                        if(sibPos > index) {
                            mComments.add(Math.max(i - 1, 0), c);
                            Log.i(TAG, "insert: Found higher sibling. Pos " + Math.max(i - 1, 0));
                            notifyDataSetChanged();
                            return;
                            //notifyItemInserted(Math.max(i - 1, 0));
                        }
                    }

                    if(i == mComments.size() - 1) {
                        mComments.add(c);
                        Log.i(TAG, "insert: End of comments. Pos " + mComments.size());
                        notifyDataSetChanged();
                        return;
                        //notifyItemInserted(mComments.size());
                    }
                }
            }
        }
    }

    private void insertComment(Item item) {
        final Comment c = new Comment(item);
        if(mComments.size() == 0) {
            c.depth = 0;
            mComments.add(c);
            notifyDataSetChanged();
            return;
        }

        int pp; //Parent position
        int[] siblings;
        if(item.getParent() == mRootItem.getId()) {
            c.depth = 0;
            pp = -1;
            siblings = mRootItem.getKids();
        } else {
            final int parentId = item.getParent();
            for(pp = 0; pp < mComments.size(); pp++) {
                if(mComments.get(pp).item.getId() == parentId) {
                    c.depth = mComments.get(pp).depth + 1;
                    break;
                }
            }
            siblings = mComments.get(pp).item.getKids();
        }
        if(siblings.length == 1) {
            //Add to position after the parent
            mComments.add(pp + 1, c);
            notifyItemChanged(pp + 1);
        } else {
            final int ind = Util.linearSearch(siblings, c.item.getId());
            Log.i(TAG, "insertComment: " + item.getId() + " Position " + ind + " in " + Arrays.toString(siblings));
            for(int i = Math.max(pp, 0); i < mComments.size(); i++) {
                Log.i(TAG, "insertComment: Searching comments " + mComments.get(i).depth);
                if(mComments.get(i).depth <= c.depth) {
                    //We check if this comment comes after ours
                    //Or if the depth of the comment is lower than ours
                    //In which case we insert it
                    //TODO
                    //We also need to consider what happens when there
                    //are not greater comments
                    //If our comment goes at the end
                    int sibi = Util.linearSearch(siblings, mComments.get(i).item.getId());
                    Log.i(TAG, "insertComment: Found a relation with index " + sibi);
                    if(sibi > ind || mComments.get(i).depth < c.depth) {
                        Log.i(TAG, "insertComment: Inserting " + c.item.getId() + " to " + i + 1);
                        mComments.add(i + 1, c);
                        notifyItemChanged(i + 1);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {

    }

    private class Comment {
        Item item;
        int depth = 0;
        boolean visible = true;

        Comment(Item item) {
            this.item = item;
        }
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.comment_card)
        CardView mCard;

        @BindView(R.id.comment_info)
        TextView mInfo;

        @BindView(R.id.comment_body)
        TextView mBody;

        CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
