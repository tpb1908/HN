package com.tpb.hn.content;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.tpb.hn.data.Item;
import com.tpb.hn.item.FragmentPagerAdapter;
import com.tpb.hn.network.loaders.HNItemLoader;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 18/10/16.
 */

class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ItemHolder> implements HNItemLoader.HNItemLoadDone, HNItemLoader.HNItemIdLoadDone, FastScrollRecyclerView.SectionedAdapter {
    private static final String TAG = ContentAdapter.class.getSimpleName();

    private Context mContext;
    private HNItemLoader mLoader;
    private String mCurrentPage;
    private boolean mIsUsingCards = false;
    private boolean mShouldMarkRead = false;
    private int[] mIds;
    private Item[] mData = new Item[] {};
    private ContentOpener mOpener;
    private int mLastPosition = 0;
    private LinearLayoutManager mManager;
    private SwipeRefreshLayout mSwiper;

    //TODO- Clean this up
    ContentAdapter(Context context, ContentOpener opener, FastScrollRecyclerView recycler, final LinearLayoutManager manager, final SwipeRefreshLayout swiper) {
        mContext = context;
        mOpener = opener;
        mSwiper = swiper;
        mManager = manager;
        mLoader = new HNItemLoader(mContext, this);

        final SharedPrefsController prefs = SharedPrefsController.getInstance(recycler.getContext());
        mIsUsingCards = prefs.getUseCards();
        mShouldMarkRead = prefs.getMarkReadWhenPassed();
        mManager = manager;
        if(!mIsUsingCards) {
            recycler.addItemDecoration(new DividerItemDecoration(mContext.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems(mCurrentPage);
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
        recycler.setStateChangeListener(new OnFastScrollStateChangeListener() {
            @Override
            public void onFastScrollStart() {

            }

            @Override
            public void onFastScrollStop() {
                loadItemsOnScroll(true);
            }
        });
    }

    private void loadItemsOnScroll(boolean fastScroll) {
        int pos = Math.max(mManager.findFirstVisibleItemPosition(), 0);
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
            for(int i = pos; i < pos2; i++) {
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
    }

    void beginBackgroundLoading() {
        mLoader.loadItemsIndividually(mIds, false, true);
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

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        final int pos = holder.getAdapterPosition();
        if(mData.length > pos && mData[pos] != null) {
            holder.mTitle.setText(mData[pos].getFormattedTitle());
            holder.mInfo.setText(mData[pos].getFormattedInfo());
            holder.mAuthor.setText(mData[pos].getFormattedBy());
            holder.mURL.setText(mData[pos].getFormattedURL());
            holder.mNumber.setText(String.format(Locale.getDefault(), "%d", pos + 1));
            if(mData[pos].isViewed()) {
                holder.mTitle.setTextColor(holder.mTitle.getResources().getColor(android.R.color.secondary_text_dark));
            }
        }
        if(mIsUsingCards) {
            holder.mCard.setUseCompatPadding(true);
            holder.mCard.setCardElevation(Util.pxFromDp(4));
            holder.mCard.setRadius(Util.pxFromDp(3));
            holder.mCard.setPadding(0, Util.pxFromDp(8), 0, Util.pxFromDp(8));
        }
    }

    @Override
    public int getItemCount() {
        //TODO- Get an average value for each of the pages being loaded
        return mData.length == 0 ? 500 : mData.length;
    }

    @Override
    public void onViewRecycled(ItemHolder holder) {
        super.onViewRecycled(holder);
        if(holder.mTitle.getLineCount() > 1) {
            holder.mTitle.setText("");
            holder.itemView.requestLayout();
        }
       // holder.mTitle.setTextColor(mContext.getResources().getColor(R.color.colorSecondaryText));
        holder.mTitle.setText(R.string.text_title_empty);
        holder.mInfo.setText(R.string.text_info_empty);
        holder.mAuthor.setText("");
        holder.mURL.setText("");
        holder.mNumber.setText("");
        holder.mTitle.requestLayout();
    }

    @Override
    public void IdLoadDone(int[] ids) {
        Log.i(TAG, "IdLoadDone: ");
        this.mIds = ids;
        int currentPos = Math.max(mManager.findFirstVisibleItemPosition(), 0);
        if(currentPos > ids.length) {
            mManager.scrollToPosition(ids.length);
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
                    Log.i(TAG, "itemLoaded: " + item.getComments().length);
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
            mOpener.openItem(mData[pos], type);
        } else {
            attemptLoadAgain(pos);
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

        ItemHolder(@NonNull View itemView) {
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

    interface ContentOpener {

        void openItem(Item item);

        void openItem(Item item, FragmentPagerAdapter.PageType type);

        void openUser(Item item);

    }

}
