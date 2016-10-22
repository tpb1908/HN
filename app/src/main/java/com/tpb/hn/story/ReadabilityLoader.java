package com.tpb.hn.story;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.tpb.hn.network.APIPaths;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by theo on 19/10/16.
 */

public class ReadabilityLoader {
    private static final String TAG = ReadabilityLoader.class.getSimpleName();
    private static HashMap<String, JSONObject> cache = new HashMap<>();
    private static HashMap<String, ArrayList<ReadabilityLoadDone>> listenerCache = new HashMap<>();
    private ReadabilityLoadDone listener;

    public ReadabilityLoader(ReadabilityLoadDone listener) {
        this.listener = listener;
    }

    void loadArticle(final String url) {
        if(url.endsWith(".pdf")) {
            listener.loadDone(null, false, ReadabilityLoadDone.ERROR_PDF);
            return;
        }
        if(cache.containsKey(url)) {
            listener.loadDone(cache.get(url), true, ReadabilityLoadDone.NO_ERROR);
        } //We still pull data to see if it has changed

        if(listenerCache.containsKey(url)) {
            listenerCache.get(url).add(listener);
        } else {
            final ArrayList<ReadabilityLoadDone> listeners = new ArrayList<>();
            listeners.add(listener);
            listenerCache.put(url, listeners);
            final long start = System.nanoTime();
            AndroidNetworking.get(APIPaths.getReadabilityParserPath(url))
                    .setTag("readability")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "onResponse: " + (System.nanoTime()-start)/1E9);
                                Log.i(TAG, "onResponse: updating " + listenerCache.get(url).size() + " listeners");

                                for(ReadabilityLoadDone rld : listenerCache.get(url)) {
                                    rld.loadDone(response, response != null, ReadabilityLoadDone.NO_ERROR);
                                }
                                listenerCache.remove(url);
                                if(response != null) {
                                    cache.put(url, response);
                                }
                            } catch(Exception e) {
                                Log.e(TAG, "onResponse: ", e);
                                Log.i(TAG, "onResponse: " + e.getMessage());
                                for(ReadabilityLoadDone rld : listenerCache.get(url)) {
                                    rld.loadDone(null, false, ReadabilityLoadDone.ERROR_PARSING);
                                }
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: ", anError );
                            Log.i(TAG, "onError: " + anError.getErrorBody());
                            for(ReadabilityLoadDone rld : listenerCache.get(url)) {
                                rld.loadDone(null, false, anError.getErrorCode());
                            }

                        }
                    });
        }

    }

    private void stripImageCopyright() {}

    public interface ReadabilityLoadDone {
        int ERROR_PARSING = -100;
        int ERROR_PDF = -200;
        int NO_ERROR = 0;

        void loadDone(JSONObject result, boolean success, int code);

    }

}
