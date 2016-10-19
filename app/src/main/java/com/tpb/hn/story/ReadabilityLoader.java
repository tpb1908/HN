package com.tpb.hn.story;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import de.jetwick.snacktory.SCache;

/**
 * Created by theo on 19/10/16.
 * http://stackoverflow.com/questions/6053602/what-arguments-are-passed-into-asynctaskarg1-arg2-arg3
 */

public class ReadabilityLoader extends AsyncTask<String, Void, JResult> {
    private static final String TAG = ReadabilityLoader.class.getCanonicalName();
    private static HtmlFetcher fetcher = new HtmlFetcher();
    private ReadabilityLoadDone listener;

    static {
        fetcher.setCache(new ReadabilityCache());
    }

    public ReadabilityLoader(ReadabilityLoadDone listener) {
        this.listener = listener;
    }

    @Override
    protected JResult doInBackground(String... params) {
        if(params.length == 0) throw new UnsupportedOperationException("Must pass URL to ReadabilityLoader");
        final long s = System.nanoTime();
        try {
            final JResult res = fetcher.fetchAndExtract(params[0], 10000, true);
            Log.i(TAG, "doInBackground: Time diff" + (System.nanoTime() - s)/1E9);
            return res;
        } catch(Exception e) {
            Log.e(TAG, "doInBackground: ", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(JResult jResult) {
        super.onPostExecute(jResult);
        listener.loadDone(jResult, jResult == null);
    }

    public interface ReadabilityLoadDone {

        void loadDone(JResult result, boolean error);

    }

    //TODO- Set this up with higher level caching
    private static class ReadabilityCache implements SCache {
        private static HashMap<String, JResult> map = new HashMap<>();

        @Override
        public JResult get(String s) {
            return map.get(s);
        }

        @Override
        public void put(String s, JResult jResult) {
            map.put(s, jResult);
        }

        @Override
        public int getSize() {
            return map.size();
        }

        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        public boolean containsValue(JResult value) {
            return map.containsValue(value);
        }

        public void putAll(Map<String, JResult> values) {
            map.putAll(values);
        }
    }

    static void clearChache() {
        fetcher.clearCacheCounter();
    }

    public static SCache getCache() {
        return fetcher.getCache();
    }

}
