package com.tpb.hn.network.loaders;

import com.tpb.hn.data.Item;

import java.util.ArrayList;

/**
 * Created by theo on 21/11/16.
 */

public interface ItemManager {

    void getIds(ItemIdLoadListener listener, String page);

    void getTopIds(ItemIdLoadListener listener);
    void getNewIds(ItemIdLoadListener listener);
    void getBestIds(ItemIdLoadListener listener);
    void getAskIds(ItemIdLoadListener listener);
    void getShowIds(ItemIdLoadListener listener);
    void getJobsIds(ItemIdLoadListener listener);

    void loadItemsIndividually(final int[] ids, boolean getFromCache);

    void loadItemsIndividually(final int[] ids, boolean getFromCache, final boolean inBackground);

    void cancelBackgroundLoading();

    void loadItem(final int id);

    void loadItemForComments(final int id);

    public interface ItemLoadListener {

        void itemLoaded(Item item, boolean success, int code);

        void itemsLoaded(ArrayList<Item> items, boolean success, int code);

    }

    public interface ItemIdLoadListener {

        void IdLoadDone(int[] ids);

        void IdLoadError(int errorCode);
    }
}
