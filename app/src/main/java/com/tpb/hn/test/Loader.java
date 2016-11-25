package com.tpb.hn.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.tpb.hn.Util;
import com.tpb.hn.network.APIPaths;
import com.tpb.hn.network.HNParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by theo on 25/11/16.
 */

public class Loader extends BroadcastReceiver {
    private static final String TAG = Loader.class.getSimpleName();

    private static Loader instance;
    private SharedPreferences prefs;
    private boolean online = false;

    public static int ERROR_NOT_IN_CACHE = 100;
    public static int ERROR_TIMEOUT = 200;
    public static int ERROR_PARSING = 300;
    public static int ERROR_NETWORK_CHANGE = 400;
    public static int ERROR_UNKNOWN = 0;


    private ArrayList<WeakReference<NetworkChangeListener>> mNetworkListeners = new ArrayList<>();

    public static Loader getInstance(Context context) {
        if(instance == null) {
            instance = new Loader(context);
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

    public void getIds(final String url, final idLoader loader) {
        if(online) {
            AndroidNetworking.get(url)
                    .setTag(url)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            final int[] ids = HNParser.extractIntArray(response);
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

    public interface NetworkChangeListener {

        void networkStateChange(boolean netAvailable);

    }

}
