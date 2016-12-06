package com.tpb.hn.item.fragments;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.content.DividerItemDecoration;
import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Formatter;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.loaders.Parser;
import com.tpb.hn.settings.SharedPrefsController;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 25/11/16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private static final String TAG = CommentAdapter.class.getSimpleName();
    private final RecyclerView mRecycler;
    private final SwipeRefreshLayout mSwiper;
    private final UserOpener mOpener;
    private final int mScreenWidth;
    private final ArrayList<Integer> mVisibleItems = new ArrayList<>();
    private final boolean usingCards;
    private final int mCommentId;
    @BindArray(R.array.comment_colors) int[] mCommentColors;
    private Comment mRootComment;
    private ArrayList<CommentWrapper> mComments = new ArrayList<>();
    private boolean expandComments;

    public CommentAdapter(RecyclerView recycler, SwipeRefreshLayout swiper, UserOpener opener, int commentId) {
        mRecycler = recycler;
        mSwiper = swiper;
        mOpener = opener;
        mCommentId = commentId;
        final Context context = mRecycler.getContext();
        final SharedPrefsController prefs = SharedPrefsController.getInstance(context);
        usingCards = prefs.getUseCardsComments();
        expandComments = prefs.getExpandComments();
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

        final CommentWrapper comment = mComments.get(mVisibleItems.get(pos));
        if(comment.parsedText == null) {
            if(comment.comment.getText() != null) {
                comment.parsedText = Util.parseHTMLCommentText(comment.comment.getText());
            }
        }
        holder.mBody.setText(comment.parsedText);
        holder.mTitle.setText(
                String.format(holder.itemView.getContext().getString(R.string.text_comment_title_date),
                        comment.comment.getFormattedBy(), Formatter.timeAgo(comment.comment.getTime())));
        holder.mColorBar.setBackgroundColor(mCommentColors[comment.depth % mCommentColors.length]);
        holder.mPadding.getLayoutParams().width = Util.pxFromDp(comment.depth * 4);

        if(usingCards) {
            holder.mCard.setUseCompatPadding(true);
            holder.mCard.setCardElevation(Util.pxFromDp(4));
            holder.mCard.setRadius(Util.pxFromDp(3));
        }
    }

    @Override
    public int getItemCount() {
        return mVisibleItems.size();
    }

    @Override
    public void onViewRecycled(CommentHolder holder) {
        super.onViewRecycled(holder);
        holder.itemView.clearAnimation();

    }

    private void switchItemVisibility(int pos) {
        final int cPos = mVisibleItems.get(pos);
        final int depth = mComments.get(cPos).depth;
        final boolean visibility = !mComments.get(cPos).childrenVisible;
        mComments.get(cPos).childrenVisible = visibility;
        int end = cPos + 1;
        for(; end < mComments.size(); end++) {
            if(mComments.get(end).depth > depth) {
                mComments.get(end).visible = visibility;
                if(Analytics.VERBOSE) Log.i(TAG, "switchItemVisibility: CommentWrapper " + mComments.get(end));
            } else {
                if(Analytics.VERBOSE) Log.i(TAG, "switchItemVisibility: Breaking " + mComments.get(end));
                end--;
                break;
            }
        }
        if(Analytics.VERBOSE) Log.i(TAG, "switchItemVisibility: cPos " + cPos + ", end " + end);
        if(cPos < end) {
            buildPositions();
            if(visibility) {
                if(end - cPos == 1) {
                    notifyItemInserted(end);
                } else {
                    notifyItemRangeInserted(cPos + 1, end);
                }
            } else {
                if(end - cPos == 1) {
                    notifyItemRemoved(end);
                } else {
                    notifyItemRangeRemoved(cPos + 1, end);
                }
            }
        }
    }

    public void clear() {
        mComments.clear();
        mVisibleItems.clear();
        notifyDataSetChanged();
    }

    public void loadComment(Comment comment) {

        mRootComment = comment;

        if(Analytics.VERBOSE) Log.i(TAG, "loadItem: Root comment " + mRootComment.toString());
        final Handler uiHandler = new Handler(mRecycler.getContext().getMainLooper());
        if(!mRootComment.getChildren().equals("")) {
            if(Analytics.VERBOSE) Log.i(TAG, "loadComment: Beginning flattening");
            new Handler().post(() -> {
                try {
                    mComments = flatten(parseCommentString(mRootComment.getChildren()), 0);
                } catch(JSONException jse) {
                    Log.e(TAG, "run: ", jse);
                }
                mRootComment.setDescendants(mComments.size());

                uiHandler.postDelayed(() -> {
                    buildPositions();
                    notifyDataSetChanged();
                    mSwiper.setRefreshing(false);
                    if(mCommentId != 0) scrollToComment();

                }, 300);
            });

        } else {
            uiHandler.postDelayed(() -> {
                buildPositions();
                notifyDataSetChanged();
                mSwiper.setRefreshing(false);
            }, 500);

        }
    }

    private void scrollToComment() {
        for(int i : mVisibleItems) {
            if(mComments.get(i).comment.getId() == mCommentId) {
                expandComments = true;
                //TODO Add a setting for this behaviour
                //TODO- Add the code when flattening the comments to expand only the chosen id branch
                /*
                Animating insertions while scrolling this fast just doesn't work
                 */
                mRecycler.scrollToPosition(i);
                break;
            }
        }
    }

    private ArrayList<CommentWrapper> flatten(Comment[] comments, int depth) {
        final ArrayList<CommentWrapper> list = new ArrayList<>();
        for(Comment com : comments) {
            if(com.getBy() != null) {
                final CommentWrapper c = new CommentWrapper(com, depth);
                if(!expandComments) {
                    c.visible = depth == 0;
                    c.childrenVisible = depth != 0;
                }
                list.add(c);
                try {
                    final Comment[] children = parseCommentString(com.getChildren());
                    if(children.length > 0) {
                        list.addAll(flatten(children, depth + 1));
                    }
                } catch(JSONException jse) {
                } //Ignored
            }
        }
        return list;
    }

    private Comment[] parseCommentString(String json) throws JSONException {
        final JSONArray childJSON = new JSONArray(json);
        final Comment[] children = new Comment[childJSON.length()];
        for(int i = 0; i < childJSON.length(); i++) {
            try {
                children[i] = Parser.parseComment(childJSON.getJSONObject(i));
            } catch(JSONException jse) {
                children[i] = Parser.getDeadComment(childJSON.getJSONObject(i));
            }
        }
        return children;
    }

    private void buildPositions() {
        mVisibleItems.clear();
        for(int i = 0; i < mComments.size(); i++) {
            if(mComments.get(i).visible) mVisibleItems.add(i);
        }
    }

    private void openUser(int pos) {
        mOpener.openUser(mComments.get(mVisibleItems.get(pos)).comment);
    }


    public interface UserOpener {

        void openUser(Item item);

    }

    private class CommentWrapper {
        final Comment comment;
        int depth = 0;
        boolean visible = true;
        boolean childrenVisible = true;
        CharSequence parsedText;

        CommentWrapper(Comment comment) {
            this.comment = comment;
        }

        CommentWrapper(Comment comment, int depth) {
            this.comment = comment;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return "{" + comment.getId() + ", " +
                    comment.getBy() + ", " +
                    depth + "}";
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
            final TranslateAnimation animation = new TranslateAnimation(-mScreenWidth, 0, 0, 0);
            animation.setDuration(300);
            itemView.startAnimation(animation);
            ButterKnife.bind(this, itemView);
            itemView.setOnLongClickListener(view -> {
                CommentAdapter.this.switchItemVisibility(getAdapterPosition());
                return false;
            });
            mTitle.setOnClickListener(view -> CommentAdapter.this.openUser(getAdapterPosition()));
        }

    }
}
