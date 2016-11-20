package com.tpb.hn.item;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
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
    private SwipeRefreshLayout mSwiper;
    private UserOpener mOpener;
    private int mScreenWidth;

    private ArrayList<Comment> mComments = new ArrayList<>();
    private ArrayList<Integer> mVisibleItems = new ArrayList<>();
    private boolean usingCards;
    private boolean expandComments;
    private boolean shouldAnimate;

    public CommentAdapter(RecyclerView recycler, SwipeRefreshLayout swiper, UserOpener opener) {
        mRecycler = recycler;
        mSwiper = swiper;
        mOpener = opener;
        final Context context = mRecycler.getContext();
        final SharedPrefsController prefs = SharedPrefsController.getInstance(context);
        usingCards = prefs.getUseCardsComments();
        expandComments = prefs.getExpandComments();
        shouldAnimate = prefs.getAnimateComments();
        if(!usingCards) {
            mRecycler.addItemDecoration(new DividerItemDecoration(context.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        mScreenWidth = ((WindowManager) mRecycler.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();

        ButterKnife.bind(this, recycler);
    }


    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        final int pos = holder.getAdapterPosition();

        final Comment comment = mComments.get(mVisibleItems.get(pos));
        if(comment.parsedText == null) {
            if(comment.item.getText() != null) {
                final Spanned text;
                if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    text = Html.fromHtml(comment.item.getText(), Html.FROM_HTML_MODE_COMPACT);
                } else {
                    text = Html.fromHtml(comment.item.getText());
                }
                comment.parsedText = text.toString().substring(0, text.toString().length() - 2);
            }
        }
        holder.mBody.setText(comment.parsedText);
        holder.mTitle.setText(comment.item.getBy());
        holder.mColorBar.setBackgroundColor(mCommentColors[comment.depth%mCommentColors.length]);
        holder.mPadding.getLayoutParams().width = Util.pxFromDp(comment.depth * 4);

        if(usingCards) {
            holder.mCard.setUseCompatPadding(true);
            holder.mCard.setCardElevation(Util.pxFromDp(4));
            holder.mCard.setRadius(Util.pxFromDp(3));
        }
        if(!comment.bound && shouldAnimate){
            setTranslateAnimation(holder.itemView, comment.depth);
            comment.bound = true;
        }
    }

    private void setTranslateAnimation(View view, int multiplier) {
        final TranslateAnimation animation = new TranslateAnimation(-mScreenWidth, 0, 0, 0);
        animation.setDuration(Math.max(300, Math.min(800, multiplier * 150)));
        view.startAnimation(animation);
    }

    private void switchItemVisibility(int pos) {
        final int cPos = mVisibleItems.get(pos);
        final int depth = mComments.get(cPos).depth;
        final boolean visibility = !mComments.get(cPos).childrenVisible;
        mComments.get(cPos).childrenVisible = visibility;
        int end = cPos + 1;
        for(; end < mComments.size(); end++) {
            Log.i(TAG, "switchItemVisibility: Comment " + mComments.get(end));
            if(mComments.get(end).depth > depth) {
                mComments.get(end).visible = visibility;
            } else break;
        }
        Log.i(TAG, "switchItemVisibility: cPos " + cPos + ", end " + end);
        if(cPos != end) {
            buildPositions();
            if(visibility) {
                notifyItemRangeInserted(cPos + 1, end - 1);
            } else {
                notifyItemRangeRemoved(cPos + 1, end - 1);
            }
        }
    }

    public void clear() {
        mComments.clear();
        mVisibleItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mVisibleItems.size();
    }
    @Override
    public void loadItem(Item item) {

        mRootItem = item;
        Log.i(TAG, "loadItem: Root item " + mRootItem.toString());
        final Handler uiHandler = new Handler(mRecycler.getContext().getMainLooper());
        if(mRootItem.getComments().length == 0) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    mRootItem.parseComments();
                    mComments = flatten(mRootItem.getComments(), 0);
                    mRootItem.setDescendants(mComments.size());
                    /*
                        FIXME
                        WTF postDelayed doesn't work
                     */
                    try {
                        Thread.sleep(800);
                    } catch(Exception e) {} //Ignored
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            buildPositions();
                            notifyDataSetChanged();
                            mSwiper.setRefreshing(false);
                        }
                    });

                }
            });
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(800);
                    } catch(Exception e) {} //Ignored
                    buildPositions();
                    notifyDataSetChanged();
                    mSwiper.setRefreshing(false);
                }
            });

        }

        //TODO- Sort the top comments by points or time

    }

    private ArrayList<Comment> flatten(Item[] items, int depth) {
        final ArrayList<Comment> list = new ArrayList<>();
        for(Item i : items) {
            if(i.getBy() != null) {
                final Comment c = new Comment(i, depth);
                if(!expandComments) {
                    c.visible = depth == 0;
                }
                list.add(c);
                i.parseComments();
                if(i.getComments().length > 0) {
                    list.addAll(flatten(i.getComments(), depth + 1));
                }
            }
        }
        return list;
    }

    private void buildPositions() {
        mVisibleItems.clear();
        for(int i = 0; i < mComments.size(); i++) {
            if(mComments.get(i).visible) mVisibleItems.add(i);
        }
    }

    private void openUser(int pos) {
        mOpener.openUser(mComments.get(mVisibleItems.get(pos)).item);
    }


    private class Comment {
        Item item;
        int depth = 0;
        boolean bound = false;
        boolean visible = true;
        boolean childrenVisible = true;
        String parsedText;

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
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CommentAdapter.this.switchItemVisibility(getAdapterPosition());
                    return false;
                }
            });
            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommentAdapter.this.openUser(getAdapterPosition());
                }
            });
        }

    }

    public interface UserOpener {

        void openUser(Item item);

    }

}
