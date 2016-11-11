package com.tpb.hn.network.loaders;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.tpb.hn.network.APIPaths;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by theo on 19/10/16.
 */

public class TextLoader {
    private static final String TAG = TextLoader.class.getSimpleName();
    private static HashMap<String, JSONObject> cache = new HashMap<>();
    private static HashMap<String, ArrayList<TextLoadDone>> listenerCache = new HashMap<>();
    private TextLoadDone listener;

    public TextLoader(TextLoadDone listener) {
        this.listener = listener;
    }

    public void loadArticle(final String url, boolean forImmediateUse) {
        if(url.endsWith(".pdf")) {
            listener.loadDone("", false, TextLoadDone.ERROR_PDF);
            return;
        }
        if(cache.containsKey(url)) {
            Log.i(TAG, "loadArticle: Listener is " + listener);
            listener.loadDone(cache.get(url), true, TextLoadDone.NO_ERROR);
        } //We still pull data to see if it has changed

        if(listenerCache.containsKey(url)) {
            listenerCache.get(url).add(listener);
        } else {
            final ArrayList<TextLoadDone> listeners = new ArrayList<>();
            listeners.add(listener);
            listenerCache.put(url, listeners);
            final long start = System.nanoTime();
            AndroidNetworking.get(APIPaths.getMercuryParserPath(url))
                    .setTag("mercury")
                    .setOkHttpClient(APIPaths.MERCURY_CLIENT)
                    .setPriority(forImmediateUse ? Priority.IMMEDIATE : Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "onResponse: " + (System.nanoTime()-start)/1E9);
                                Log.i(TAG, "onResponse: updating " + listenerCache.get(url).size() + " listeners");
                                for(TextLoadDone rld : listenerCache.get(url)) {
                                    if(rld == null) {
                                        listenerCache.get(url).removeAll(Collections.singleton(null)); //Remove all null
                                    } else {
                                        rld.loadDone(response, response != null, TextLoadDone.NO_ERROR);
                                    }
                                }
                                listenerCache.remove(url);
                                if(response != null) {
                                    cache.put(url, response);
                                }
                                Log.i(TAG, "onResponse: " + response.toString());
                            } catch(Exception e) {
                                Log.e(TAG, "onResponse: ", e);
                                Log.i(TAG, "onResponse: " + e.getMessage());
                                for(TextLoadDone rld : listenerCache.get(url)) {
                                    rld.loadDone("", false, TextLoadDone.ERROR_PARSING);
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: ", anError );
                            Log.i(TAG, "onError: " + anError.getErrorBody() );
                            for(TextLoadDone rld : listenerCache.get(url)) {
                                rld.loadDone("", false, anError.getErrorCode());
                            }

                        }
                    });
        }

    }

    public interface TextLoadDone {
        int ERROR_PARSING = -100;
        int ERROR_PDF = -200;
        int NO_ERROR = 0;

        void loadDone(JSONObject result, boolean success, int code);

        void loadDone(String result, boolean success, int code);

    }

}
