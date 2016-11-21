package com.tpb.hn.network.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.tpb.hn.data.Item;
import com.tpb.hn.storage.permanent.DB;


/**
 * Created by theo on 21/11/16.
 */

public class CachedItemLoader implements ItemManager {
    private static final String TAG = CachedItemLoader.class.getSimpleName();

    private static DB db;
    private static SharedPreferences prefs;

    private static final String KEY_SHARED_PREFS = "CACHE";

    private static final String KEY_TOP_IDS = "TOP";
    private static final String KEY_NEW_IDS = "NEW";
    private static final String KEY_BEST_IDS = "BEST";
    private static final String KEY_ASK_IDS = "ASK";
    private static final String KEY_SHOW_IDS = "SHOW";
    private static final String KEY_JOBS_IDS = "JOBS";

    private ItemLoadListener mListener;

    public CachedItemLoader(Context context, ItemLoadListener listener) {
        db = DB.getDB(context);
        prefs = context.getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        mListener = listener;
    }

    @Override
    public void getIds(ItemIdLoadListener listener, String page) {

    }

    @Override
    public void getTopIds(ItemIdLoadListener listener) {

    }

    @Override
    public void getNewIds(ItemIdLoadListener listener) {

    }

    @Override
    public void getBestIds(ItemIdLoadListener listener) {

    }

    @Override
    public void getAskIds(ItemIdLoadListener listener) {

    }

    @Override
    public void getShowIds(ItemIdLoadListener listener) {

    }

    @Override
    public void getJobsIds(ItemIdLoadListener listener) {

    }

    @Override
    public void loadItemsIndividually(int[] ids, boolean getFromCache) {
        loadItemsIndividually(ids, false, false);
    }

    @Override
    public void loadItemsIndividually(final int[] ids, boolean getFromCache, boolean inBackground) {
        //TODO- Load task which runs callback after each load from DB
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for(int id : ids) {
                    db.loadItem(new DB.DBLoadCallback() {
                        @Override
                        public void loadComplete(boolean success, Item item) {
                            mListener.itemLoaded(item, success, 0);
                        }
                    }, id);
                }
            }
        });
    }

    @Override
    public void cancelBackgroundLoading() {

    }

    @Override
    public void loadItem(int id) {

    }

    @Override
    public void loadItemForComments(int id) {

    }
}
