package com.tpb.hn.content;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.HNLoader;

import java.util.ArrayList;
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

    void loadItems(String defaultPage) {
        Log.i(TAG, "loadItems: Loading items for page " + defaultPage.toLowerCase());
        switch(defaultPage.toLowerCase()) {
            case "top":
                loader.getTop(this, 20);
                break;
            case "best":
                loader.getBest(this, 20);
                break;
            case "ask":
                loader.getAsk(this, 20);
                break;
            case "new":
                loader.getNew(this, 20);
                break;
            case "show":
                loader.getShow(this, 20);
                break;
            case "jobs":
                loader.getJobs(this, 20);
                break;
        }
        contentStateChanged = contentState != null; //On first run it will stay false
        contentState = defaultPage;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if(data.length > position && data[position] != null) {
            holder.mTitle.setText(data[position].getTitle());
            holder.mInfo.setText(data[position].getFormattedInfo());
            holder.mAuthor.setText(data[position].getBy());
            holder.mURL.setText(data[position].getFormattedURL());
        }
    }

    @Override
    public int getItemCount() {
        return Math.max(data.length, 100);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_title)
        TextView mTitle;

        @BindView(R.id.item_stats)
        TextView mInfo;

        @BindView(R.id.item_author)
        TextView mAuthor;

        @BindView(R.id.item_url)
        TextView mURL;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public void IdLoadDone(int[] ids) {
        //TODO Check for chunk loading
        Log.i(TAG, "IdLoadDone: ");
        this.ids = ids;
        //Id loading will only happen once each time the data is to be set
        data = new Item[ids.length + 1];
    }

    @Override
    public void itemLoaded(Item item, boolean success) {

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

}
