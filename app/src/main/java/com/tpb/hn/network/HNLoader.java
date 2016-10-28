package com.tpb.hn.network;

import android.util.Log;
import android.util.SparseArray;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.ItemType;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by theo on 21/10/16.
 */

public class HNLoader {
    private static final String TAG = HNLoader.class.getSimpleName();

    private static ArrayList<Item> itemCache = new ArrayList<>();
    private SparseArray<ArrayList<HNItemLoadDone>> listenerCache = new SparseArray<>();

    private static HashMap<Item, ArrayList<Item>> kidCache = new HashMap<>();

    private static int[] top = new int[500];
    private static int[] newstories = new int[500];
    private static int[] best = new int[200];
    private static int[] ask = new int[200];
    private static int[] show = new int[200];
    private static int[] jobs = new int[200];


    private HNItemLoadDone itemListener;

    public HNLoader(HNItemLoadDone itemListener) {
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
        for(int i : ids) {
            int cachePos = checkCache(i);
            if(cachePos > -1 && getFromCache) {
                itemListener.itemLoaded(itemCache.get(cachePos), true);
            } else if(cachePos == -1){
                AndroidNetworking.get(APIPaths.getItemPath(i))
                        .setTag(i)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    final Item item = HNParser.JSONToItem(response);
                                    if(item != null) insertItemToCache(item);
                                    itemListener.itemLoaded(item, item != null);
                                } catch(Exception e) {
                                    Log.e(TAG, "onResponse error: ", e);
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                //TODO- Get code like this anError.getResponse().code();
//                                Log.e(TAG, "onError: ", anError);
//                                try {
//                                    Log.i(TAG, "onError: " + anError.getResponse().body().string());
//                                } catch(IOException ioe) {
//                                    Log.e(TAG, "onError: ", ioe);
//                                }
                            }
                        });
            }
        }
    }

    private int checkCache(int id) {
        final Item key = new Item();
        key.setId(id);
        int pos = Collections.binarySearch(itemCache, key);
        return Math.max(pos, -1);
    }

    private void updateCachedRootItem(Item item) {
        boolean set = false;
        for(int i = 0; i < itemCache.size(); i++) {
            if(itemCache.get(i).getId() == item.getId()) {
                itemCache.set(i, item);
                set = true;
                break;
            }
        }
        if(!set) itemCache.add(item);
    }

    private void insertItemToCache(Item item) {
//        boolean set = false;
//        for(int i = 0; i < itemCache.size(); i++) {
//            if(item.getId() > itemCache.get(i).getId()) {
//                itemCache.add(i, item);
//                set = true;
//                Log.i(TAG, "insertItemToCache: Inserting at position " + i);
//                break;
//            }
//        }
        //if(!set) itemCache.add(0, item);
//        final int pos = Collections.binarySearch(itemCache, item);
//        if(pos < 0) {
//            itemCache.add(0, item);
//        } else if(pos >= itemCache.size()) {
//            itemCache.add(item);
//        } else {
//            Log.i(TAG, "insertItemToCache: Inserting to position " + pos + " of " + itemCache.size());
//            itemCache.add(pos, item);
//        }
        if(item.getType() == ItemType.COMMENT) {
            final Item parent = new Item();
            parent.setId(item.getParent());
            ArrayList<Item> siblings = kidCache.get(parent);
            if(siblings == null) {
                siblings = new ArrayList<>();
                kidCache.put(parent, siblings);
            }
            siblings.add(item);
            Collections.sort(siblings);
        } else {
            itemCache.add(item);
            Collections.sort(itemCache);
        }
    }


    public void loadItem(final int id) {
        for(Item i : itemCache) {
            if(i.getId() == id) {
                itemListener.itemLoaded(i, true);
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
                                ild.itemLoaded(item, item != null);
                            }
                            listenerCache.remove(id);

                            updateCachedRootItem(item);
                        } catch(Exception e) {
                            loadItem(id);
                            Log.e(TAG, "onResponse error : ", e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //TODO- Get code like this anError.getResponse().code();
                        Log.e(TAG, "onError: ", anError );
                    }
                });
    }

    //TODO- Deal with failed requests
    private class MultiLoadListener implements HNItemLoadDone {
        private final HNItemLoadDone listener;
        private final ArrayList<Item> items;
        private final int count;

        MultiLoadListener(HNItemLoadDone listener, ArrayList<Item> items, int count) {
            this.listener = listener;
            this.items = items;
            this.count = count;
        }


        @Override
        public void itemLoaded(Item item, boolean success) {
            items.add(item);
            if(items.size() == count) {
                listener.itemsLoaded(items, true);
            }
        }

        @Override
        public void itemsLoaded(ArrayList<Item> items, boolean success) {

        }
    }


    //TODO- Send error codes
    public interface HNItemLoadDone {

        void itemLoaded(Item item, boolean success);

        void itemsLoaded(ArrayList<Item> items, boolean success);

    }

    public interface HNItemIdLoadDone {

        void IdLoadDone(int[] ids);
    }

}
