package com.tpb.hn.network.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.Util;
import com.tpb.hn.data.Comment;
import com.tpb.hn.data.Item;
import com.tpb.hn.network.APIPaths;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by theo on 25/11/16.
 */

public class Loader extends BroadcastReceiver {
    private static final String TAG = Loader.class.getSimpleName();

    private static Loader instance;
    private SharedPreferences prefs;
    private boolean online = false;

    private static HashMap<String, ArrayList<WeakReference<TextLoader>>> listeners = new HashMap<>();

    public static int ERROR_NOT_IN_CACHE = 100;
    public static int ERROR_TIMEOUT = 200;
    public static int ERROR_PARSING = 300;
    public static int ERROR_NETWORK_CHANGE = 400;
    public static int ERROR_UNKNOWN = 0;
    public static int ERROR_PDF = 500;


    private ArrayList<WeakReference<NetworkChangeListener>> mNetworkListeners = new ArrayList<>();

    public static Loader getInstance(Context context) {
        if(instance == null) {
            instance = new Loader(context);
            instance.online = Util.isNetworkAvailable(context);
        }
        return instance;
    }

    private Loader(Context context) {
        prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        online = netInfo != null;
        for(int i = 0; i < mNetworkListeners.size(); i++) {
            if(mNetworkListeners.get(i).get() == null) {
                mNetworkListeners.remove(i);
                i--;
            } else {
                mNetworkListeners.get(i).get().networkStateChange(online);
            }
        }
    }

    public void addNetworkListener(NetworkChangeListener listener) {
        mNetworkListeners.add(new WeakReference<>(listener));
    }

    public void removeNetworkListener(NetworkChangeListener listener) {
        mNetworkListeners.remove(new WeakReference<>(listener));
    }

    public void cancelBackgroundLoading() {
        AndroidNetworking.forceCancel("BG");
    }

    public void getIds(String key, final idLoader loader) {
        final String url;
        switch(key.toLowerCase()) {
            case "top":
                url = APIPaths.getTopPath();
                break;
            case "best":
                url = APIPaths.getBestPath();
                break;
            case "ask":
                url = APIPaths.getAskPath();
                break;
            case "new":
                url = APIPaths.getNewPath();
                break;
            case "show":
                url = APIPaths.getShowPath();
                break;
            case "job":
                url = APIPaths.getJobPath();
                break;
            default:
                url = "";
        }
        Log.d(TAG, "getIds: " + url);
        if(online) {
            AndroidNetworking.get(url)
                    .setTag(url)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            final int[] ids = Parser.extractIntArray(response);
                            Log.i(TAG, "onResponse: ids " + Arrays.toString(ids));
                            loader.idsLoaded(ids);
                            Util.putIntArrayInPrefs(prefs.edit(), url, ids);
                        }

                        @Override
                        public void onError(ANError anError) {
                            if(!online) {
                                loader.idError(ERROR_NETWORK_CHANGE);
                            } else {
                                loader.idError(ERROR_UNKNOWN);
                            }
                        }
                    });
        } else {
            final int[] ids = Util.getIntArrayFromPrefs(prefs, url);
            if(ids.length > 0) {
                loader.idsLoaded(ids);
            } else {
                loader.idError(ERROR_NOT_IN_CACHE);
            }
        }
    }

    public void loadItem(final int id, final ItemLoader loader) {
        if(online) {
            networkLoadItem(id, loader);
        } else {
            cacheLoadItem(id, loader);
        }
    }

    private void networkLoadItem(final int id, final ItemLoader loader) {
        AndroidNetworking.get(APIPaths.getItemPath(id))
                .setTag(id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Item item = Parser.parseItem(response);
                            loader.itemLoaded(item);
                        } catch(JSONException e) {
                            loader.itemError(id, ERROR_PARSING);
                        } catch(Exception e) {
                            loader.itemError(id, ERROR_UNKNOWN);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loader.itemError(id, ERROR_NETWORK_CHANGE);
                    }
                });

    }

    private void cacheLoadItem(final int id, ItemLoader loader) {

    }

    public void loadItems(final int[] ids, final boolean background, ItemLoader loader) {
        if(online) {
            networkLoadItems(ids, background, loader);
        } else if(!background) {
            cacheLoadItems(ids, loader);
        }
    }

    private void networkLoadItems(final int[] ids, final boolean background, final ItemLoader loader) {
        for(int i : ids) {
            final int id = i;
            AndroidNetworking.get(APIPaths.getItemPath(id))
                    .setTag(background ? "BG" : id)
                    .setPriority(background ? Priority.MEDIUM : Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                final Item item = Parser.parseItem(response);
                                loader.itemLoaded(item);
                            } catch(JSONException e) {
                                loader.itemError(id, ERROR_PARSING);
                            } catch(Exception e) {
                                loader.itemError(id, ERROR_UNKNOWN);
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            loader.itemError(id, ERROR_NETWORK_CHANGE);
                        }
                    });
        }
    }

    private void cacheLoadItems(final int[] ids, ItemLoader loader) {}

    public void loadChildJSON(final int id, CommentLoader loader) {
        if(online) {
            networkLoadChildJSON(id, loader);
        } else {
            cacheLoadChildJSON(id);
        }
    }

    private void networkLoadChildJSON(final int id, final CommentLoader loader) {
        AndroidNetworking.get(APIPaths.getAlgoliaItemPath(id))
                .setTag(id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Comment comment = Parser.parseComment(response);
                            loader.commentsLoaded(comment);
                        } catch(JSONException e) {
                            loader.commentError(id, ERROR_PARSING);
                        } catch(Exception e) {
                            loader.commentError(id, ERROR_UNKNOWN);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loader.commentError(id, ERROR_NETWORK_CHANGE);
                    }
                });
    }

    public void loadArticle(final String url, boolean forImmediateUse, TextLoader loader) {
        if(online) {
            networkLoadArticle(url, forImmediateUse, loader);
        } else {
            cacheLoadArticle(url, loader);
        }
    }

    private void networkLoadArticle(final String url, boolean forImmediateUse, TextLoader loader) {
        if(url.endsWith(".pdf")) {
            loader.textError(url, ERROR_PDF);
        } else {
            if(listeners.get(url) == null) listeners.put(url, new ArrayList<WeakReference<TextLoader>>());
            listeners.get(url).add(new WeakReference<>(loader));
            AndroidNetworking.get(APIPaths.getMercuryParserPath(url))
                    .setTag(url)
                    .setOkHttpClient(APIPaths.MERCURY_CLIENT)
                    .setPriority(forImmediateUse ? Priority.IMMEDIATE : Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.has("content") && !response.get("content").equals("<div></div>")) {
                                    for(WeakReference<TextLoader> loader : listeners.get(url)) {
                                        loader.get().textLoaded(response);
                                    }
                                }
                                listeners.remove(url);
                            } catch(JSONException jse) {
                                for(WeakReference<TextLoader> loader : listeners.get(url)) {
                                    loader.get().textError(url, ERROR_PARSING);
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            for(WeakReference<TextLoader> loader : listeners.get(url)) {
                                loader.get().textError(url, ERROR_NETWORK_CHANGE);
                            }
                        }
                    });
        }

    }

    private void cacheLoadArticle(final String url, TextLoader loader) {

    }

    private void cacheLoadChildJSON(final int id) {}

    public interface idLoader {

        void idsLoaded(int[] ids);

        void idError(int code);

    }

    public interface ItemLoader {

        void itemLoaded(Item item);

        void itemError(int id, int code);

    }

    public interface CommentLoader {

        void commentsLoaded(Comment rootComment);

        void commentError(int id, int code);

    }

    public interface TextLoader {

        void textLoaded(JSONObject result);

        void textError(String url, int code);
    }

    public interface NetworkChangeListener {

        void networkStateChange(boolean netAvailable);

    }

}
