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
import java.util.Arrays;
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

    private static int[] top = new int[500];
    private static int[] newstories = new int[500];
    private static int[] best = new int[200];
    private static int[] ask = new int[200];
    private static int[] show = new int[200];
    private static int[] jobs = new int[200];


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

    //<editor-fold desc="Getters">
    public void getTop(HNItemIdLoadDone IdLoadDone) {
        getItemIds(IdLoadDone ,APIPaths.getTopStoriesPath(), false, -1);
    }

    public void getTop(HNItemIdLoadDone IdLoadDone, int firstChunk) {
        getItemIds(IdLoadDone, APIPaths.getTopStoriesPath(), true, firstChunk);
    }

    public void getNew(HNItemIdLoadDone IdLoadDone) {
        getItemIds(IdLoadDone, APIPaths.getNewStoriesPath(), false, -1);
    }

    public void getNew(HNItemIdLoadDone IdLoadDone, int firstChunk) {
        getItemIds(IdLoadDone, APIPaths.getNewStoriesPath(), true, firstChunk);
    }

    public void getBest(HNItemIdLoadDone IdLoadDone) {
        getItemIds(IdLoadDone, APIPaths.getBestStoriesPath(), false, -1);
    }

    public void getBest(HNItemIdLoadDone IdLoadDone, int firstChunk) {
        getItemIds(IdLoadDone, APIPaths.getBestStoriesPath(), true, firstChunk);
    }

    public void getAsk(HNItemIdLoadDone IdLoadDone) {
        getItemIds(IdLoadDone, APIPaths.getAskStoriesPath(), false, -1);
    }

    public void getAsk(HNItemIdLoadDone IdLoadDone, int firstChunk) {
        getItemIds(IdLoadDone, APIPaths.getAskStoriesPath(), true, firstChunk);
    }


    public void getShow(HNItemIdLoadDone IdLoadDone) {
        getItemIds(IdLoadDone, APIPaths.getShowStoriesPath(), false, -1);
    }

    public void getShow(HNItemIdLoadDone IdLoadDone, int firstChunk) {
        getItemIds(IdLoadDone, APIPaths.getShowStoriesPath(), true, firstChunk);
    }

    public void getJobs(HNItemIdLoadDone IdLoadDone) {
        getItemIds(IdLoadDone, APIPaths.getJobStoriesPath(), false, -1);
    }

    public void getJobs(HNItemIdLoadDone IdLoadDone, int firstChunk) {
        getItemIds(IdLoadDone, APIPaths.getJobStoriesPath(), true, firstChunk);
    }

    private void getItemIds(HNItemIdLoadDone IdLoadDone, String url) {
        getItemIds(IdLoadDone, url, false, -1);
    }
    //</editor-fold>

    private void getItemIds(final HNItemIdLoadDone IdListener, final String url, final boolean loadItems, final int firstChunk) {
        if(firstChunk != -1) {
            loadItems(IdListener, url, firstChunk);
        } else {
            AndroidNetworking.get(url)
                    .setTag(url)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            final int[] items = HNParser.extractIntArray(response);
                            if(url.equals(APIPaths.getTopStoriesPath())) {
                                top = items;
                            } else if(url.equals(APIPaths.getNewStoriesPath())) {
                                newstories = items;
                            } else if(url.equals(APIPaths.getBestStoriesPath())) {
                                best = items;
                            } else if(url.equals(APIPaths.getAskStoriesPath())) {
                                ask = items;
                            } else if(url.equals(APIPaths.getShowStoriesPath())) {
                                show = items;
                            } else if(url.equals(APIPaths.getJobStoriesPath())) {
                                jobs = items;
                            }
                            Log.i(TAG, "onResponse: " + items);
                            IdListener.IdLoadDone(items);
                            if(loadItems) {
                                loadItems(items);
                            }
                        }

                        @Override
                        public void onError(ANError anError) {

                        }
                    });
        }
    }

    public void loadItems(final HNItemIdLoadDone idLoadDone, String url, final int firstChunk) {
        AndroidNetworking.get(url)
                .setTag(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final int[] items = HNParser.extractIntArray(response);
                        idLoadDone.IdLoadDone(items);
                        if(firstChunk < items.length) {
                            final int[] first = Arrays.copyOfRange(items, 0, Math.min(firstChunk, items.length));
                            listenerCache.put(Arrays.hashCode(first), new ArrayList<>(Collections.singletonList(itemListener)));
                            loadItems(first);
                            final int[] second = Arrays.copyOfRange(items, firstChunk, items.length);
                            listenerCache.put(Arrays.hashCode(second), new ArrayList<>(Collections.singletonList(itemListener)));
                            loadItems(second);
                        } else {
                            listenerCache.put(Arrays.hashCode(items), new ArrayList<>(Collections.singletonList(itemListener)));
                            loadItems(items);
                        }

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

    public void loadItem(final int id) {
        for(Item i : rootItems) {
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

    public interface HNItemIdLoadDone {

        void IdLoadDone(int[] ids);
    }

    public interface HNUserLoadDone {

        void userLoaded(User user);
    }



}
