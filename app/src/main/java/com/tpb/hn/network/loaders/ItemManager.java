package com.tpb.hn.network.loaders;

import com.tpb.hn.data.Item;

import java.util.ArrayList;

/**
 * Created by theo on 21/11/16.
 */

public interface ItemManager {

    void getIds(HNItemLoader.HNItemIdLoadDone listener, String page);

    void getTopIds(HNItemLoader.HNItemIdLoadDone listener);
    void getNewIds(HNItemLoader.HNItemIdLoadDone listener);
    void getBestIds(HNItemLoader.HNItemIdLoadDone listener);
    void getAskIds(HNItemLoader.HNItemIdLoadDone listener);
    void getShowIds(HNItemLoader.HNItemIdLoadDone listener);
    void getJobsIds(HNItemLoader.HNItemIdLoadDone listener);

    void loadItemsIndividually(final int[] ids, boolean getFromCache);

    void loadItemsIndividually(final int[] ids, boolean getFromCache, final boolean inBackground);

    void cancelBackgroundLoading();

    void loadItem(final int id);

    void loadItemForComments(final int id);

    public interface HNItemLoadDone {

        void itemLoaded(Item item, boolean success, int code);

        void itemsLoaded(ArrayList<Item> items, boolean success, int code);

    }

    public interface HNItemIdLoadDone {

        void IdLoadDone(int[] ids);
    }
}
