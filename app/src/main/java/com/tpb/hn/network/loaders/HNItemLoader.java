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
import com.tpb.hn.storage.permanent.ItemCache;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by theo on 21/10/16.
 */

public class HNItemLoader implements ItemManager {
    private static final String TAG = HNItemLoader.class.getSimpleName();

    private static ItemCache cache;

    private static SparseArray<ArrayList<ItemLoadListener>> listenerCache = new SparseArray<>();

    private ItemLoadListener itemListener;

    public static int ERROR_NETWORK_ITEM_LOADING = 50;

    public HNItemLoader(Context context, ItemLoadListener itemListener) {
        cache = ItemCache.getInstance(context);
        this.itemListener = itemListener;
    }

    @Override
    public void getIds(ItemIdLoadListener listener, String page) {
        getItemIds(listener, page);
    }

    @Override
    public void getTopIds(ItemIdLoadListener listener) {
        getItemIds(listener, APIPaths.getTopPath());
    }

    @Override
    public void getNewIds(ItemIdLoadListener listener) {
        getItemIds(listener, APIPaths.getNewPath());
    }

    @Override
    public void getBestIds(ItemIdLoadListener listener) {
        getItemIds(listener, APIPaths.getBestPath());
    }

    @Override
    public void getAskIds(ItemIdLoadListener listener) {
        getItemIds(listener, APIPaths.getAskPath());
    }

    @Override
    public void getShowIds(ItemIdLoadListener listener) {
        getItemIds(listener, APIPaths.getShowPath());
    }

    @Override
    public void getJobsIds(ItemIdLoadListener listener) {
        getItemIds(listener, APIPaths.getJobPath());
    }

    private void getItemIds(final ItemIdLoadListener IdListener, final String url) {
        Log.i(TAG, "getItemIds: Getting item Ids with  " + url);
        AndroidNetworking.get(url)
                .setTag(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final int[] items = HNParser.extractIntArray(response);
                        IdListener.IdLoadDone(items);
                    }

                    @Override
                    public void onError(ANError anError) {
                        IdListener.IdLoadError(ERROR_NETWORK_ITEM_LOADING);
                    }
                });
    }

    @Override
    public void loadItemsIndividually(final int[] ids, boolean getFromCache) {
        loadItemsIndividually(ids, getFromCache, false);
    }

    @Override
    public void loadItemsIndividually(final int[] ids, boolean getFromCache, final boolean inBackground) {
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
                                    item.setOffline(false);
                                    cache.insert(item, inBackground);
                                    itemListener.itemLoaded(item, true, 200);
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

    @Override
    public void cancelBackgroundLoading() {
        AndroidNetworking.cancel("background");
    }

    @Override
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
                            item.setOffline(false);
                            for(ItemLoadListener ild : listenerCache.get(id)) {
                                ild.itemLoaded(item, true, 200);
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

    @Override
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


}
