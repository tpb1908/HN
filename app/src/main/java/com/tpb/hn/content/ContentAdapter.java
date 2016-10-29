package com.tpb.hn.content;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
import com.tpb.hn.R;
import com.tpb.hn.Util;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 18/10/16.
 */

class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ItemHolder> implements HNLoader.HNItemLoadDone, HNLoader.HNItemIdLoadDone {
    private static final String TAG = ContentAdapter.class.getSimpleName();

    private Context mContext;
    private HNLoader mLoader = new HNLoader(this);
    private String contentState;
    private boolean usingCards = false;
    private boolean markReadWhenPassed = false;
    private int[] ids;
    private Item[] data = new Item[] {};
    private ContentOpener mOpener;
    private int mLastPosition = 0;
    private LinearLayoutManager mManager;

    ContentAdapter(ContentOpener opener, RecyclerView recycler, final LinearLayoutManager manager) {
        mContext = recycler.getContext();
        this.mOpener = opener;
        final SharedPrefsController prefs = SharedPrefsController.getInstance(recycler.getContext());
        usingCards = prefs.getUseCards();
        markReadWhenPassed = prefs.getMarkReadWhenPassed();
        mManager = manager;
        if(!usingCards) {
            recycler.addItemDecoration(new DividerItemDecoration(mContext.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
        }
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = Math.max(manager.findFirstVisibleItemPosition(), 0);
                    if(ids != null) {
                        int pos2;
                        if(pos > mLastPosition) {
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
            }
        });
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
        contentState = defaultPage;
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
        int pos = holder.getAdapterPosition();
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
        }
    }

    @Override
    public int getItemCount() {
        return data.length == 0 ? 100 : data.length;
    }

    @Override
    public void onViewRecycled(ItemHolder holder) {
        super.onViewRecycled(holder);
        if(holder.mTitle.getLineCount() > 1) {
            holder.mTitle.setText("");
            holder.itemView.requestLayout();
        }
        holder.mTitle.setTextColor(Color.parseColor("#767676"));
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
        //Id loading will only happen once each time the data is to be set
    }

    @Override
    public void itemLoaded(Item item, boolean success) {
        for(int i = 0; i < ids.length; i++) {
            if(item.getId() == ids[i]) {
                data[i] = item;
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {
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

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ContentAdapter.this.data != null &&
                            ContentAdapter.this.data.length >= getAdapterPosition() &&
                            ContentAdapter.this.data[getAdapterPosition()] != null) {
                        ContentAdapter.this.mOpener.openItem(ContentAdapter.this.data[getAdapterPosition()]);
                    }
                   else {
                        ContentAdapter.this.attemptLoadAgain(getAdapterPosition());
                    }
                }
            });
        }

    }

    public interface ContentOpener {

        void openItem(Item item);

        void openUser(Item item);

        void openPage(Item item, ItemAdapter.PageType type);

    }

}
