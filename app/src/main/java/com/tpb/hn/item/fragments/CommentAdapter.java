package com.tpb.hn.item.fragments;

import android.content.Context;
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
import com.tpb.hn.data.*;
import com.tpb.hn.storage.SharedPrefsController;
import com.tpb.hn.data.Comment;
import com.tpb.hn.network.loaders.Parser;

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

    @BindArray(R.array.comment_colors) int[] mCommentColors;

    private Comment mRootComment;
    private RecyclerView mRecycler;
    private SwipeRefreshLayout mSwiper;
    private UserOpener mOpener;
    private int mScreenWidth;

    private ArrayList<CommentWrapper> mComments = new ArrayList<>();
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

        final CommentWrapper comment = mComments.get(mVisibleItems.get(pos));
        if(comment.parsedText == null) {
            if(comment.comment.getText() != null) {
                final Spanned text;
                if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    text = Html.fromHtml(comment.comment.getText(), Html.FROM_HTML_MODE_COMPACT);
                } else {
                    text = Html.fromHtml(comment.comment.getText());
                }
                comment.parsedText = text.toString().substring(0, text.toString().length() - 2);
            }
        }
        holder.mBody.setText(comment.parsedText);
        holder.mTitle.setText(
                String.format(holder.itemView.getContext().getString(R.string.text_comment_title_date_points),
                        comment.comment.getFormattedBy(), Formatter.timeAgo(comment.comment.getTime()),
                        comment.comment.getScore() + " points"));
        holder.mColorBar.setBackgroundColor(mCommentColors[comment.depth % mCommentColors.length]);
        holder.mPadding.getLayoutParams().width = Util.pxFromDp(comment.depth * 4);

        if(usingCards) {
            holder.mCard.setUseCompatPadding(true);
            holder.mCard.setCardElevation(Util.pxFromDp(4));
            holder.mCard.setRadius(Util.pxFromDp(3));
        }
        if(!comment.bound && shouldAnimate) {
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
            Log.i(TAG, "switchItemVisibility: CommentWrapper " + mComments.get(end));
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


    public void loadComment(Comment comment) {

        mRootComment = comment;

        Log.i(TAG, "loadItem: Root comment " + mRootComment.toString());
        final Handler uiHandler = new Handler(mRecycler.getContext().getMainLooper());
        if(!mRootComment.getChildren().equals("")) {
            Log.i(TAG, "loadComment: Beginning flattening");
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mComments = flatten(parseCommentString(mRootComment.getChildren()), 0);
                    } catch(JSONException jse) {
                        Log.e(TAG, "run: ", jse);
                    }
                    mRootComment.setDescendants(mComments.size());

                    uiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buildPositions();
                            notifyDataSetChanged();
                            mSwiper.setRefreshing(false);
                        }
                    }, 300);
                }
            });

        } else {
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buildPositions();
                    notifyDataSetChanged();
                    mSwiper.setRefreshing(false);
                }
            }, 500);

        }

        //TODO- Sort the top comments by points or time

    }

    private ArrayList<CommentWrapper> flatten(Comment[] comments, int depth) {
        final ArrayList<CommentWrapper> list = new ArrayList<>();
        for(Comment com : comments) {
            if(com.getBy() != null) {
                final CommentWrapper c = new CommentWrapper(com, depth);
                if(!expandComments) {
                    c.visible = depth == 0;
                }
                list.add(c);
                try {
                    final Comment[] children = parseCommentString(com.getChildren());
                    if(children.length > 0) {
                        list.addAll(flatten(children, depth + 1));
                    }
                } catch(JSONException jse) {} //Ignored
            }
        }
        return list;
    }

    private Comment[] parseCommentString(String json) throws JSONException {
        final JSONArray childJSON = new JSONArray(json);
        final Comment[] children = new Comment[childJSON.length()];
        for(int i = 0; i < childJSON.length(); i++) {
            children[i] = Parser.parseComment(childJSON.getJSONObject(i));
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


    private class CommentWrapper {
        Comment comment;
        int depth = 0;
        boolean bound = false;
        boolean visible = true;
        boolean childrenVisible = true;
        String parsedText;

        CommentWrapper(Comment comment) {
            this.comment = comment;
        }

        CommentWrapper(Comment comment, int depth) {
            this.comment = comment;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return "{" + comment.getId() + ", "  +
                    comment.getBy() +
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

        void openUser(Comment item);

    }
}
