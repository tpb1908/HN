package com.tpb.hn.network.loaders;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.HNParser;
import com.tpb.hn.storage.permanent.Cache;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by theo on 21/10/16.
 */

public class HNItemLoader {
    private static final String TAG = HNItemLoader.class.getSimpleName();

    private static Cache cache;

    private static SparseArray<ArrayList<HNItemLoadDone>> listenerCache = new SparseArray<>();
    private static int[] top = new int[500];
    private static int[] newstories = new int[500];
    private static int[] best = new int[200];
    private static int[] ask = new int[200];
    private static int[] show = new int[200];
    private static int[] jobs = new int[200];


    private HNItemLoadDone itemListener;

    public HNItemLoader(Context context, HNItemLoadDone itemListener) {
        cache = Cache.getInstance(context);
        this.itemListener = itemListener;
    }


    public void getIds(HNItemIdLoadDone listener, String page) {
        getItemIds(listener, page);
    }

    public void getTopIds(HNItemIdLoadDone listener) {
        getItemIds(listener, APIPaths.getTopPath());
    }

    public void getNewIds(HNItemIdLoadDone listener) {
        getItemIds(listener, APIPaths.getNewPath());
    }

    public void getBestIds(HNItemIdLoadDone listener) {
        getItemIds(listener, APIPaths.getBestPath());
    }

    public void getAskIds(HNItemIdLoadDone listener) {
        getItemIds(listener, APIPaths.getAskPath());
    }

    public void getShowIds(HNItemIdLoadDone listener) {
        getItemIds(listener, APIPaths.getShowPath());
    }

    public void getJobsIds(HNItemIdLoadDone listener) {
        getItemIds(listener, APIPaths.getJobPath());
    }

    private void getItemIds(final HNItemIdLoadDone IdListener, final String url) {
        Log.i(TAG, "getItemIds: Getting item Ids with  " + url);
        AndroidNetworking.get(url)
                .setTag(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final int[] items = HNParser.extractIntArray(response);
                        if(url.equals(APIPaths.getTopPath())) {
                            top = items;
                        } else if(url.equals(APIPaths.getNewPath())) {
                            newstories = items;
                        } else if(url.equals(APIPaths.getBestPath())) {
                            best = items;
                        } else if(url.equals(APIPaths.getAskPath())) {
                            ask = items;
                        } else if(url.equals(APIPaths.getShowPath())) {
                            show = items;
                        } else if(url.equals(APIPaths.getJobPath())) {
                            jobs = items;
                        }
                        IdListener.IdLoadDone(items);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void loadItemsIndividually(final int[] ids, boolean getFromCache) {
        loadItemsIndividually(ids, getFromCache, false);
    }

    public void loadItemsIndividually(final int[] ids, boolean getFromCache, final boolean inBackground) {
        if(inBackground) Log.i(TAG, "loadItemsIndividually: Background load started");
        for(int i : ids) {
            int cachePos = cache.position(i);
            if(cachePos >= 0 && getFromCache) {
                itemListener.itemLoaded(cache.getItems().get(cachePos), true, 200);
            } else if(!getFromCache) {
                AndroidNetworking.get(APIPaths.getItemPath(i))
                        .setTag(inBackground ? "background" : i)
                        .setPriority(inBackground ? Priority.LOW : Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    final Item item = HNParser.JSONToItem(response);
                                    cache.insert(item, inBackground);
                                    itemListener.itemLoaded(item, item != null, 200);
                                } catch(Exception e) {
                                    Log.e(TAG, "onResponse error: ", e);
                                    itemListener.itemLoaded(null, false, -100);
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                itemListener.itemLoaded(null, false, anError.getResponse() == null ? 0 : anError.getResponse().code());
                            }
                        });
            }
        }
    }


    public void cancelBackgroundLoading() {
        AndroidNetworking.cancel("background");
    }

    public void loadItem(final int id) {
        for(Item i : cache.getItems()) {
            if(i.getId() == id) {
                itemListener.itemLoaded(i, true, 200);
                break;
            }
        }
        if(listenerCache.get(id) == null) {
            listenerCache.put(id, new ArrayList<>(Collections.singletonList(itemListener)));
        } else {
            listenerCache.get(id).add(itemListener);
        }
        AndroidNetworking.get(APIPaths.getItemPath(id))
                .setTag(id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Item item = HNParser.JSONToItem(response);

                            for(HNItemLoadDone ild : listenerCache.get(id)) {
                                ild.itemLoaded(item, item != null, 200);
                            }
                            listenerCache.remove(id);

                            cache.update(item);
                        } catch(Exception e) {
                            loadItem(id);
                            Log.e(TAG, "onResponse error : ", e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        itemListener.itemLoaded(null, false, anError.getResponse().code());
                    }
                });
    }

    public void loadItemForComments(final int id) {
        AndroidNetworking.get(APIPaths.getAlgoliaItemPath(id))
                .setTag(id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Item item = HNParser.commentJSONToItem(response);

                            itemListener.itemLoaded(item, true, 200);

                            cache.update(item);
                        } catch(Exception e) {
                            loadItem(id);
                            Log.e(TAG, "onResponse error : ", e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        itemListener.itemLoaded(null, false, anError.getResponse().code());
                    }
                });
    }

    public interface HNItemLoadDone {

        void itemLoaded(Item item, boolean success, int code);

        void itemsLoaded(ArrayList<Item> items, boolean success, int code);

    }

    public interface HNItemIdLoadDone {

        void IdLoadDone(int[] ids);
    }

}
