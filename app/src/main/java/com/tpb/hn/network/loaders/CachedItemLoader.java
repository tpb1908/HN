package com.tpb.hn.network.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tpb.hn.Util;
import com.tpb.hn.data.Item;
import com.tpb.hn.storage.permanent.DB;


/**
 * Created by theo on 21/11/16.
 * TODO Implement ItemManager in DB
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

    public static int ERROR_IDS_NOT_CACHED = 100;
    public static int MESSAGE_COMMENTS_OFFLINE = -100;


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
        if(prefs.contains(KEY_TOP_IDS)) {
            listener.IdLoadDone(Util.getIntArrayFromPrefs(prefs, KEY_TOP_IDS));
        } else {
            listener.IdLoadError(ERROR_IDS_NOT_CACHED);
        }
    }

    @Override
    public void getNewIds(ItemIdLoadListener listener) {
        if(prefs.contains(KEY_NEW_IDS)) {
            listener.IdLoadDone(Util.getIntArrayFromPrefs(prefs, KEY_NEW_IDS));
        } else {
            listener.IdLoadError(ERROR_IDS_NOT_CACHED);
        }
    }

    @Override
    public void getBestIds(ItemIdLoadListener listener) {
        if(prefs.contains(KEY_BEST_IDS)) {
            listener.IdLoadDone(Util.getIntArrayFromPrefs(prefs, KEY_BEST_IDS));
        } else {
            listener.IdLoadError(ERROR_IDS_NOT_CACHED);
        }
    }

    @Override
    public void getAskIds(ItemIdLoadListener listener) {
        if(prefs.contains(KEY_ASK_IDS)) {
            listener.IdLoadDone(Util.getIntArrayFromPrefs(prefs, KEY_ASK_IDS));
        } else {
            listener.IdLoadError(ERROR_IDS_NOT_CACHED);
        }
    }

    @Override
    public void getShowIds(ItemIdLoadListener listener) {
        if(prefs.contains(KEY_SHOW_IDS)) {
            listener.IdLoadDone(Util.getIntArrayFromPrefs(prefs, KEY_SHOW_IDS));
        } else {
            listener.IdLoadError(ERROR_IDS_NOT_CACHED);
        }
    }

    @Override
    public void getJobsIds(ItemIdLoadListener listener) {
        if(prefs.contains(KEY_JOBS_IDS)) {
            listener.IdLoadDone(Util.getIntArrayFromPrefs(prefs, KEY_JOBS_IDS));
        } else {
            listener.IdLoadError(ERROR_IDS_NOT_CACHED);
        }
    }

    @Override
    public void loadItemsIndividually(int[] ids, boolean getFromCache) {
        loadItemsIndividually(ids, false, false);
    }

    public static void writeItemIds(Context context, int[] ids, String key) {
        Log.i(TAG, "writeItemIds: " + ids.length);
        if(prefs == null) prefs = context.getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        Util.putIntArrayInPrefs(editor, key, ids);
        editor.commit();
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
                        public void loadComplete(final boolean success, final Item item) {
                           // item.setOffline(true);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mListener.itemLoaded(item, success, 0);
                                }
                            });

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
        mListener.itemLoaded(null, false, MESSAGE_COMMENTS_OFFLINE);
    }
}
