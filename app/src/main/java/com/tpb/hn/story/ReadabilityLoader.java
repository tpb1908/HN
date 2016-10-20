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
 * http://stackoverflow.com/questions/6053602/what-arguments-are-passed-into-asynctaskarg1-arg2-arg3
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
        if(cache.containsKey(url)) {
            listener.loadDone(cache.get(url), true);
        }
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
                                    rld.loadDone(response, response != null);
                                }
                                listenerCache.remove(url);
                                if(response != null) {
                                    cache.put(url, response);
                                }
                            } catch(Exception e) {
                                Log.e(TAG, "onResponse: ", e);
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: ", anError );
                        }
                    });
        }

    }

    private void stripImageCopyright() {}

    public interface ReadabilityLoadDone {

        void loadDone(JSONObject result, boolean success);

    }

}
