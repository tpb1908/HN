package com.tpb.hn.content;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.data.Formatter;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.network.loaders.HNItemLoader;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 18/10/16.
 */

public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        HNItemLoader.HNItemLoadDone,
        HNItemLoader.HNItemIdLoadDone,
        FastScrollRecyclerView.SectionedAdapter {
    private static final String TAG = ContentAdapter.class.getSimpleName();

    private Context mContext;
    private HNItemLoader mLoader;
    private String mCurrentPage;
    private boolean mIsContent;
    private boolean mIsUsingCards = false;
    private boolean mShouldMarkRead = false;
    private int[] mIds;
    private int[] mOldIds;
    private long mLastUpdateTime;
    private Item[] mData = new Item[] {};
    private ContentManager mManager;
    private int mLastPosition = 0;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwiper;

    //TODO- Clean this up
    public ContentAdapter(Context context,
                   ContentManager manager,
                   RecyclerView recycler,
                   final LinearLayoutManager layoutManager,
                   final SwipeRefreshLayout swiper) {
        mIsContent = manager instanceof ContentActivity;
        Log.i(TAG, "ContentAdapter: " + mIsContent);
        mContext = context;
        mManager = manager;
        mSwiper = swiper;
        mLayoutManager = layoutManager;
        mLoader = new HNItemLoader(mContext, this);
        mLastUpdateTime = new Date().getTime() / 1000;
        final SharedPrefsController prefs = SharedPrefsController.getInstance(recycler.getContext());
        mIsUsingCards = prefs.getUseCards();
        mShouldMarkRead = prefs.getMarkReadWhenPassed();
        mLayoutManager = layoutManager;
        if(!mIsUsingCards) {
            recycler.addItemDecoration(new DividerItemDecoration(mContext.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mIsContent) {
                    loadItems(mCurrentPage);
                } else {
                    mIds = new int[0];
                    notifyDataSetChanged();
                    mManager.openUser(null);
                }
            }
        });

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadItemsOnScroll(false);
                }
            }

        });
        if(recycler instanceof FastScrollRecyclerView) {
            ((FastScrollRecyclerView)recycler).setStateChangeListener(new OnFastScrollStateChangeListener() {
                @Override
                public void onFastScrollStart() {

                }

                @Override
                public void onFastScrollStop() {
                    loadItemsOnScroll(true);
                }
            });
        }

    }

    private void loadItemsOnScroll(boolean fastScroll) {
        int pos = Math.max(mLayoutManager.findFirstVisibleItemPosition(), 0);
        if(pos > mLastPosition && mShouldMarkRead) {
            for(int i = mLastPosition; i < pos; i++) {
                if(mData[i] != null) mData[i].setViewed(true);
            }
        }
        if(mIds != null) {
            int pos2;
            /*
            If we are scrolling down the list, we load the items below
            the first visible.
            Also if we have just fastscrolled to a position, as there
            is no inertia and we have stopped at this point
             */
            if(pos > mLastPosition || fastScroll) {
                pos2 = Math.min(pos + 15, mIds.length);
            } else {
                pos2 = Math.max(pos - 15, 0);
            }
            if(pos2 < pos) {
                final int t = pos;
                pos = pos2;
                pos2 = t;
            }
            final ArrayList<Integer> toLoad = new ArrayList<>();
            for(int i = pos; i < pos2 && pos2 < mData.length; i++) {
                if(mData[i] == null) toLoad.add(mIds[i]);
            }
            mLoader.loadItemsIndividually(Util.convertIntegers(toLoad), false);
        }
        mLastPosition = pos;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return Integer.toString(position);
    }

    void loadItems(String defaultPage) {
        Log.i(TAG, "loadItemsIndividually: Loading items for page " + defaultPage.toLowerCase());
        this.mOldIds = new int[0];
        this.mIds = new int[0];
        this.mData = new Item[0];
        AndroidNetworking.forceCancelAll();
        notifyDataSetChanged();
        switch(defaultPage.toLowerCase()) {
            case "top":
                mLoader.getTopIds(this);
                break;
            case "best":
                mLoader.getBestIds(this);
                break;
            case "ask":
                mLoader.getAskIds(this);
                break;
            case "new":
                mLoader.getNewIds(this);
                break;
            case "show":
                mLoader.getShowIds(this);
                break;
            case "job":
                mLoader.getJobsIds(this);
                break;
        }
        mCurrentPage = defaultPage;
        mLastUpdateTime = new Date().getTime() / 1000;
        mManager.displayLastUpdate(mLastUpdateTime);
    }

    void beginBackgroundLoading() {
        final int[] notLoaded = new int[mIds.length];
        int count = 0;
        for(Item i : mData) {
            if(i != null) {
                final int pos = Arrays.binarySearch(mIds, i.getId());
                if(pos < 0 || pos > mData.length) notLoaded[count++] = i.getId();
            }
        }
        mLoader.loadItemsIndividually(Arrays.copyOfRange(notLoaded, 0, count), false, true);
    }

    void cancelBackgroundLoading() {
        mLoader.cancelBackgroundLoading();
    }

    private void attemptLoadAgain(int pos) {
        if(pos < mIds.length) {
            mLoader.loadItem(mIds[pos]);
            Toast.makeText(mContext, R.string.text_attempting_item_load, Toast.LENGTH_SHORT).show();
        }
    }

    void getLastUpdate() {
        mManager.displayLastUpdate(mLastUpdateTime);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_item, parent, false));
        } else if(viewType == 1) {
            return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_user_comment, parent, false));
        } else {
            return new EmptyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_empty, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final int pos = viewHolder.getAdapterPosition();
        if(viewHolder instanceof ItemHolder) {
            final ItemHolder holder = (ItemHolder) viewHolder;
            if(mData.length > pos && mData[pos] != null) {
                final Item item = mData[pos];
                holder.mTitle.setText(item.getFormattedTitle());
                holder.mInfo.setText(item.getFormattedInfo());
                if(mIsContent) {
                    holder.mAuthor.setVisibility(View.VISIBLE);
                    holder.mAuthor.setText(item.getFormattedBy());
                    holder.mNumber.setText(String.format(Locale.getDefault(), "%d", pos + 1));
                } else {
                    holder.mAuthor.setVisibility(View.GONE);
                    holder.mNumber.setVisibility(View.GONE);
                }

                if(item.getUrl() == null)  {
                    holder.mURL.setVisibility(View.GONE);
                } else {
                    holder.mURL.setVisibility(View.VISIBLE);
                    holder.mURL.setText(item.getFormattedURL());
                }
                if(item.isViewed()) {
                    holder.mTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Medium_Inverse);
                } else {
                    holder.mTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Title);
                }
                if(item.isNew()) {
                    holder.mNumber.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Large);
                } else {
                    holder.mNumber.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Medium);
                }
            }
            if(mIsUsingCards) {
                holder.mCard.setUseCompatPadding(true);
                holder.mCard.setCardElevation(Util.pxFromDp(4));
                holder.mCard.setRadius(Util.pxFromDp(3));
                holder.mCard.setPadding(0, Util.pxFromDp(8), 0, Util.pxFromDp(8));
            }
        } else if(viewHolder instanceof CommentHolder) {
            final CommentHolder commentHolder = (CommentHolder) viewHolder;
            if(mData.length > pos && mData[pos] != null) {
                commentHolder.mTime.setText(Formatter.appendAgo(Formatter.timeAgo(mData[pos].getTime())));
                if(mData[pos].isDeleted()) {
                    commentHolder.mBody.setText(R.string.text_deleted_comment);
                } else if(mData[pos].getText() != null) {
                    final Spanned text;
                    if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        text = Html.fromHtml(mData[pos].getText(), Html.FROM_HTML_MODE_COMPACT);
                    } else {
                        text = Html.fromHtml(mData[pos].getText());
                    }
                    commentHolder.mBody.setText(text.toString().substring(0, text.toString().length() - 2));
                }
            }
            if(mIsUsingCards) {
                commentHolder.mCard.setUseCompatPadding(true);
                commentHolder.mCard.setCardElevation(Util.pxFromDp(4));
                commentHolder.mCard.setRadius(Util.pxFromDp(3));
                commentHolder.mCard.setPadding(0, Util.pxFromDp(8), 0, Util.pxFromDp(8));
            }
        }
    }

    @Override
    public int getItemCount() {
        //TODO- Get an average value for each of the pages being loaded
        return mData.length == 0 ? 500 : mData.length;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < mData.length && mData[position] != null) {
            return mData[position].getType() == ItemType.COMMENT ?  mIsContent ? 0 : 1 : 0;
        }
        return mIsContent ? 0 : -1;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        if(viewHolder instanceof ItemHolder) {
            final ItemHolder holder = (ItemHolder) viewHolder;
            if(holder.mTitle.getLineCount() > 1) {
                holder.mTitle.setText("");
                holder.itemView.requestLayout();
            }
            holder.mTitle.setText(R.string.text_title_empty);
            holder.mInfo.setText(R.string.text_info_empty);
            holder.mAuthor.setText("");
            holder.mURL.setText("");
            holder.mNumber.setText("");
            holder.mTitle.requestLayout();
        }

    }

    @Override
    public void IdLoadDone(int[] ids) {
        Log.i(TAG, "IdLoadDone: ");
        this.mOldIds = mIds;
        this.mIds = ids;
        int currentPos = Math.max(mLayoutManager.findFirstVisibleItemPosition(), 0);
        if(currentPos > ids.length) {
            mLayoutManager.scrollToPosition(ids.length);
        }
        mData = new Item[ids.length + 1];
        mLoader.loadItemsIndividually(Arrays.copyOfRange(ids, currentPos, Math.min(currentPos + 10, ids.length)), false);
        notifyDataSetChanged();
        mSwiper.setRefreshing(false);
        //Id loading will only happen once each time the mData is to be set
    }

    @Override
    public void itemLoaded(Item item, boolean success, int code) {
        if(success) {
            for(int i = 0; i < mIds.length; i++) {
                if(item.getId() == mIds[i]) {
                    mData[i] = item;
                    if(mOldIds != null) {
                        final int possiblePos = Arrays.binarySearch(mOldIds, item.getId());
                        item.setNew(possiblePos > 0 && possiblePos < mIds.length);
                    }

                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success, int code) {
        Collections.sort(items);
        final Item key = new Item();
        for(int i = 0; i < mIds.length; i++) {
            key.setId(mIds[i]);
            int index = Collections.binarySearch(items, key);
            if(index >= 0) {
                mData[i] = items.get(index);
                notifyItemChanged(index);
            }
        }
    }

    private void openItem(int pos, FragmentPagerAdapter.PageType type) {
        if(mData != null && pos < mData.length && mData[pos] != null) {
            Log.i(TAG, "openItem: " + mData[pos].getComments().length);
            mData[pos].setViewed(true);
            notifyItemChanged(pos);
            mManager.openItem(mData[pos], type);
        } else {
            attemptLoadAgain(pos);
        }
    }

    private void openUser(int pos) {
        if(mData != null && pos < mData.length && mData[pos] != null) {
            mManager.openUser(mData[pos]);
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_card) CardView mCard;
        @BindView(R.id.item_title) TextView mTitle;
        @BindView(R.id.item_stats) TextView mInfo;
        @BindView(R.id.item_author) TextView mAuthor;
        @BindView(R.id.item_url) TextView mURL;
        @BindView(R.id.item_number) TextView mNumber;

        @OnClick(R.id.item_url)
        void urlClick() {
            ContentAdapter.this.openItem(getAdapterPosition(), FragmentPagerAdapter.PageType.BROWSER);
        }

        @OnClick(R.id.item_author)
        void authorClick() {
            ContentAdapter.this.openUser(getAdapterPosition());
        }

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentAdapter.this.openItem(getAdapterPosition(), null);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ContentAdapter.this.attemptLoadAgain(getAdapterPosition());
                    return true;
                }
            });
        }

    }

    class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.comment_card) CardView mCard;
        @BindView(R.id.comment_time) TextView mTime;
        @BindView(R.id.comment_body) TextView mBody;

        CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentAdapter.this.openItem(getAdapterPosition(), null);
                }
            });
        }

    }

    class EmptyHolder extends RecyclerView.ViewHolder {

        EmptyHolder(@NonNull View itemView) {
            super(itemView);
        }

    }

    public interface ContentManager {

        void openItem(Item item);

        void openItem(Item item, FragmentPagerAdapter.PageType type);

        void openUser(Item item);

        void displayLastUpdate(long lastUpdate);

    }

}
