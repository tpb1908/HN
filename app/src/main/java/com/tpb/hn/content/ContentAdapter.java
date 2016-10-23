package com.tpb.hn.content;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.story.StoryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 18/10/16.
 */

class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.Holder> implements HNLoader.HNItemLoadDone, HNLoader.HNItemIdLoadDone {
    private static final String TAG = ContentAdapter.class.getSimpleName();

    private HNLoader loader = new HNLoader(this);
    private String contentState;
    private boolean contentStateChanged = false;
    private int[] ids;
    private Item[] data = new Item[] {};
    private ContentOpener opener;

    ContentAdapter(ContentOpener opener, RecyclerView recycler, final LinearLayoutManager manager) {
        this.opener = opener;
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = manager.findFirstVisibleItemPosition();
                    if(ids != null) {
                        Log.i(TAG, "onScrollStateChanged: Loading items");
                        loader.loadItemsIndividually(Arrays.copyOfRange(ids, pos, pos + 15));
                    }
                }
            }
        });
    }

    void loadItems(String defaultPage) {
        Log.i(TAG, "loadItemsIndividually: Loading items for page " + defaultPage.toLowerCase());

        loader.getIds(this, defaultPage);

//        switch(defaultPage.toLowerCase()) {
//            case "top":
//                loader.getTop(this);
//                //loader.getTop(this, 20);
//                break;
//            case "best":
//                loader.getBest(this, 20);
//                break;
//            case "ask":
//                loader.getAsk(this, 20);
//                break;
//            case "new":
//                loader.getNew(this, 20);
//                break;
//            case "show":
//                loader.getShow(this, 20);
//                break;
//            case "jobs":
//                loader.getJobs(this, 20);
//                break;
//        }
        contentStateChanged = contentState != null; //On first run it will stay false
        contentState = defaultPage;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        int pos = holder.getAdapterPosition();
        if(data.length > pos && data[pos] != null) {
            holder.mTitle.setText(data[pos].getFormattedTitle());
            holder.mInfo.setText(data[pos].getFormattedInfo());
            holder.mAuthor.setText(data[pos].getFormattedBy());
            holder.mURL.setText(data[pos].getFormattedURL());
            holder.mNumber.setText(Integer.toString(pos + 1));
        }
    }

    @Override
    public int getItemCount() {
        return Math.max(data.length, 100);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
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

    @Override
    public void IdLoadDone(int[] ids) {
        Log.i(TAG, "IdLoadDone: ");
        if(this.ids == null) { //First time loading
            loader.loadItemsIndividually(Arrays.copyOfRange(ids, 0, 10));
        }
        this.ids = ids;
        //Id loading will only happen once each time the data is to be set
        data = new Item[ids.length + 1];
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

    class Holder extends RecyclerView.ViewHolder {
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

        Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ContentAdapter.this.data[getAdapterPosition()] != null) {
                        ContentAdapter.this.opener.openItem(ContentAdapter.this.data[getAdapterPosition()]);
                    }
                }
            });
        }

    }

    public interface ContentOpener {

        void openItem(Item item);

        void openUser(Item item);

        void openPage(Item item, StoryAdapter.PageType type);

    }

}
