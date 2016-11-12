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
    private String currentPage;
    private boolean usingCards = false;
    private boolean markReadWhenPassed = false;
    private int[] ids;
    private Item[] data = new Item[] {};
    private ContentOpener mOpener;
    private int mLastPosition = 0;
    private LinearLayoutManager mManager;
    private SwipeRefreshLayout mSwiper;

    ContentAdapter(ContentOpener opener, FastScrollRecyclerView recycler, final LinearLayoutManager manager, final SwipeRefreshLayout swiper) {
        mContext = recycler.getContext();
        mOpener = opener;
        mSwiper = swiper;
        mManager = manager;
        mLoader = new HNItemLoader(mContext, this);
        final SharedPrefsController prefs = SharedPrefsController.getInstance(recycler.getContext());
        usingCards = prefs.getUseCards();
        markReadWhenPassed = prefs.getMarkReadWhenPassed();
        mManager = manager;
        if(!usingCards) {
            recycler.addItemDecoration(new DividerItemDecoration(mContext.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems(currentPage);
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
        if(ids != null) {
            int pos2;
            /*
            If we are scrolling down the list, we load the items below
            the first visible.
            Also if we have just fastscrolled to a position, as there
            is no inertia and we have stopped at this point
             */
            if(pos > mLastPosition || fastScroll) {
                pos2 = Math.min(pos + 15, ids.length);
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
                if(data[i] == null) toLoad.add(ids[i]);
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
        this.ids = null;
        this.data = new Item[0];
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
        currentPage = defaultPage;
    }

    void beginBackgroundLoading() {
        mLoader.loadItemsIndividually(ids, false, true);
    }

    void cancelBackgroundLoading() {
        mLoader.cancelBackgroundLoading();
    }

    private void attemptLoadAgain(int pos) {
        if(pos < ids.length) {
            mLoader.loadItem(ids[pos]);
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
        if(data.length > pos && data[pos] != null) {
            holder.mTitle.setText(data[pos].getFormattedTitle());
            holder.mInfo.setText(data[pos].getFormattedInfo());
            holder.mAuthor.setText(data[pos].getFormattedBy());
            holder.mURL.setText(data[pos].getFormattedURL());
            holder.mNumber.setText(String.format(Locale.getDefault(), "%d", pos + 1));
            if(data[pos].isViewed()) {
                holder.mTitle.setTextColor(holder.mTitle.getResources().getColor(android.R.color.secondary_text_dark));
            }
        }
        if(usingCards) {
            holder.mCard.setUseCompatPadding(true);
            holder.mCard.setCardElevation(Util.pxFromDp(4));
            holder.mCard.setRadius(Util.pxFromDp(3));
            holder.mCard.setPadding(0, Util.pxFromDp(8), 0, Util.pxFromDp(8));
        }
    }

    @Override
    public int getItemCount() {
        //TODO- Get an average value for each of the pages being loaded
        return data.length == 0 ? 500 : data.length;
    }

    @Override
    public void onViewRecycled(ItemHolder holder) {
        super.onViewRecycled(holder);
        if(holder.mTitle.getLineCount() > 1) {
            holder.mTitle.setText("");
            holder.itemView.requestLayout();
        }
        holder.mTitle.setTextColor(mContext.getResources().getColor(R.color.colorSecondaryText));
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
        this.ids = ids;
        int currentPos = Math.max(mManager.findFirstVisibleItemPosition(), 0);
        if(currentPos > ids.length) {
            mManager.scrollToPosition(ids.length);
        }
        data = new Item[ids.length + 1];
        mLoader.loadItemsIndividually(Arrays.copyOfRange(ids, currentPos, Math.min(currentPos + 10, ids.length)), false);
        notifyDataSetChanged();
        mSwiper.setRefreshing(false);
        //Id loading will only happen once each time the data is to be set
    }

    @Override
    public void itemLoaded(Item item, boolean success, int code) {
        for(int i = 0; i < ids.length; i++) {
            if(item.getId() == ids[i]) {
                data[i] = item;
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success, int code) {
        Collections.sort(items);
        final Item key = new Item();
        for(int i = 0 ; i < ids.length; i++) {
            key.setId(ids[i]);
            int index = Collections.binarySearch(items, key);
            if(index >= 0) {
                data[i] = items.get(index);
                notifyItemChanged(index);
            }
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_card)
        CardView mCard;

        @BindView(R.id.item_title)
        TextView mTitle;

        @BindView(R.id.item_stats)
        TextView mInfo;

        @BindView(R.id.item_author)
        TextView mAuthor;

        @BindView(R.id.item_url)
        TextView mURL;

        @BindView(R.id.item_number)
        TextView mNumber;

        @OnClick(R.id.item_card)
        void cardClick() {
            dispatchClick(null);
        }

        @OnClick(R.id.item_stats)
        void statsClick() {
            dispatchClick(FragmentPagerAdapter.PageType.COMMENTS);
        }

        @OnClick(R.id.item_url)
        void urlClick() {
            dispatchClick(FragmentPagerAdapter.PageType.BROWSER);
        }

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void dispatchClick(FragmentPagerAdapter.PageType type) {
            if(ContentAdapter.this.data != null &&
                    ContentAdapter.this.data.length > getAdapterPosition() &&
                    ContentAdapter.this.data[getAdapterPosition()] != null) {
                ContentAdapter.this.mOpener.openItem(ContentAdapter.this.data[getAdapterPosition()], type);
            }
            else {
                ContentAdapter.this.attemptLoadAgain(getAdapterPosition());
            }
        }

    }

    public interface ContentOpener {

        void openItem(Item item);

        void openItem(Item item, FragmentPagerAdapter.PageType type);

        void openUser(Item item);

    }

}
