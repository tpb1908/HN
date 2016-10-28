package com.tpb.hn.item.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.tpb.hn.data.Item;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemLoader;
import com.tpb.hn.network.HNLoader;

import java.util.ArrayList;

/**
 * Created by theo on 18/10/16.
 */

public class Comments extends Fragment implements ItemLoader, ItemAdapter.FragmentCycle, HNLoader.HNItemLoadDone {
    private static final String TAG = Comments.class.getSimpleName();

    private int[] kids;
    private ArrayList<Item> comments;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: Comment fragment created");
    }

    @Override
    public void itemLoaded(Item item, boolean success) {
        comments.add(item);
        Log.i(TAG, "itemLoaded: " + comments.toString());
    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {

    }

    @Override
    public void loadItem(Item item) {
        Log.i(TAG, "itemLoaded: Comments " + item);
        if(item.getKids() != null) {
            kids = item.getKids();
            comments = new ArrayList<>(kids.length + 1);
            comments.ensureCapacity(kids.length + 1);
            new HNLoader(this).loadItemsIndividually(kids, false);
        }

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }
}
