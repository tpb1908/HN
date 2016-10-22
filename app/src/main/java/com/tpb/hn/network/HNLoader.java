package com.tpb.hn.network;

import android.util.Log;
import android.util.SparseArray;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by theo on 21/10/16.
 */

public class HNLoader {
    private static final String TAG = HNLoader.class.getSimpleName();

    private static ArrayList<Item> rootItems = new ArrayList<>();
    private SparseArray<ArrayList<HNItemLoadDone>> listenerCache = new SparseArray<>();
    private static HashMap<Item, ArrayList<Item>> kids = new HashMap<>();
    private HNItemLoadDone itemListener;
    private HNUserLoadDone userListener;

    public HNLoader(HNItemLoadDone itemListener) {
        this.itemListener = itemListener;
    }

    public HNLoader(HNUserLoadDone userListener) {
        this.userListener = userListener;
    }

    public HNLoader(HNItemLoadDone itemListener, HNUserLoadDone userListener) {
        this.itemListener = itemListener;
        this.userListener = userListener;
    }

    public void loadItem(final int id) {
        for(Item i : rootItems) {
            if(i.getId() == id) {
                itemListener.itemLoaded(i, true);
                break;
            }
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
                            Log.e(TAG, "onResponse: ", e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //TODO- Get code like this anError.getResponse().code();
                        Log.e(TAG, "onError: ", anError );
                    }
                });
    }

    public void getTopItem() {
        AndroidNetworking.get(APIPaths.getBestStoriesPath())
                .setTag("MAXITEM")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final int[] items = HNParser.extractIntArray(response);
                        listenerCache.put(items[0], new ArrayList<>(Collections.singletonList(itemListener)));
                        loadItem(items[0]);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void loadItems(String url) {
        AndroidNetworking.get(url)
                .setTag(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final int[] items = HNParser.extractIntArray(response);
                        listenerCache.put(items[0], new ArrayList<>(Collections.singletonList(itemListener)));
                        loadItems(items);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void loadItems(final int[] ids) {
        final ArrayList<Item> items = new ArrayList<>(ids.length);
        final MultiLoadListener mll = new MultiLoadListener(itemListener, items, ids.length);
        for(int i : ids) {
            AndroidNetworking.get(APIPaths.getItemPath(i))
                    .setTag(i)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                final Item item = HNParser.JSONToItem(response);
                                mll.itemLoaded(item, item != null);
                            } catch(Exception e) {

                                Log.e(TAG, "onResponse: ", e);
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            //TODO- Get code like this anError.getResponse().code();
                            Log.e(TAG, "onError: ", anError );
                        }
                    });
        }
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

    private void updateCachedRootItem(Item item) {
        boolean set = false;
        for(int i = 0; i < rootItems.size(); i++) {
            if(rootItems.get(i).getId() == item.getId()) {
                rootItems.set(i, item);
                set = true;
                break;
            }
        }
        if(!set) rootItems.add(item);
    }

    //TODO- Send error codes
    public interface HNItemLoadDone {

        void itemLoaded(Item item, boolean success);

        void itemsLoaded(ArrayList<Item> items, boolean success);

    }

    public interface HNUserLoadDone {

        void userLoaded(User user);
    }



}
